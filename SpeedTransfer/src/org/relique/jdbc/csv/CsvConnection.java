/*
 *  CsvJdbc - a JDBC driver for CSV files
 *  Copyright (C) 2001  Jonathan Ackerman
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.relique.jdbc.csv;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import java.util.concurrent.Executor;

import org.relique.io.CryptoFilter;

/**
 * This class implements the Connection interface for the CsvJdbc driver.
 * 
 * @author Jonathan Ackerman
 * @author Sander Brienen
 * @author Michael Maraya
 * @author Tomasz Skutnik
 * @author Christoph Langer
 * @version $Id: CsvConnection.java,v 1.36 2011/04/26 08:22:57 mfrasca Exp $
 */
public class CsvConnection implements Connection {

	/** Directory where the CSV files to use are located */
	private String path;

	/** File extension to use */
	private String extension = CsvDriver.DEFAULT_EXTENSION;

	/** Field separator to use */
	private char separator = CsvDriver.DEFAULT_SEPARATOR;

	/** Field quotechar to use */
	private char quotechar = CsvDriver.DEFAULT_QUOTECHAR;

	public boolean isRaiseUnsupportedOperationException() {
		return raiseUnsupportedOperationException;
	}

	public void setRaiseUnsupportedOperationException(boolean raiseUnsupportedOperationException) {
		this.raiseUnsupportedOperationException = raiseUnsupportedOperationException;
	}

	/** Field headerline to use */
	private String headerline = CsvDriver.DEFAULT_HEADERLINE;

	/** Should headers be suppressed */
	private boolean suppressHeaders = CsvDriver.DEFAULT_SUPPRESS;

	/** Should headers be trimmed */
	private boolean trimHeaders = CsvDriver.DEFAULT_TRIM_HEADERS;

	/** should files be grouped in one table - as if there was an index */
	private boolean indexedFiles = CsvDriver.DEFAULT_INDEXED_FILES;

	/** how to interpret string values before returning them to the caller */
	private String columnTypes = CsvDriver.DEFAULT_COLUMN_TYPES;

	/**
	 * whether ot not to raise a UnsupportedOperationException when calling a
	 * method irrelevant in this context (ex: autocommit whereas there is only
	 * readonly accesses)
	 */
	private boolean raiseUnsupportedOperationException;

	/** Collection of all created Statements */
	private Vector statements = new Vector();

	/** CharSet that should be used to read the files */
	private String charset = null;

	/** Stores whether this Connection is closed or not */
	private boolean closed;

	private String fileNamePattern;
	private String[] nameParts;
	private String timestampFormat;
	private String dateFormat;
	private String timeFormat;
	private String timeZoneName;
	private Character commentChar;

	private int skipLeadingLines = 0;

	private boolean ignoreUnparseableLines;

	private boolean fileTailPrepend;

	private CryptoFilter decryptingFilter;

	private boolean defectiveHeaders;

	private int skipLeadingDataLines;

	private int transposedLines;

	private int transposedFieldsToSkip;

	private boolean autoCommit;

	private String quoteStyle;

	/**
	 * Creates a new CsvConnection that takes the supplied path
	 * 
	 * @param path
	 *            directory where the CSV files are located
	 */
	protected CsvConnection(String path) {
		// validate argument(s)
		if (path == null || path.length() == 0) {
			throw new IllegalArgumentException("'path' argument may not be empty or null");
		}
		this.path = path;
	}

	/**
	 * Creates a new CsvConnection that takes the supplied path and properties
	 * 
	 * @param path
	 *            directory where the CSV files are located
	 * @param info
	 *            set of properties containing custom options
	 * @throws SQLException
	 */
	protected CsvConnection(String path, Properties info) throws SQLException {
		this(path);
		// check for properties
		if (info != null) {
			// set the file extension to be used
			if (info.getProperty(CsvDriver.FILE_EXTENSION) != null) {
				extension = info.getProperty(CsvDriver.FILE_EXTENSION);
			}
			// set the separator character to be used
			if (info.getProperty(CsvDriver.SEPARATOR) != null) {
				separator = info.getProperty(CsvDriver.SEPARATOR).charAt(0);
			}
			// set the quotechar character to be used
			if (info.getProperty(CsvDriver.QUOTECHAR) != null) {
				quotechar = info.getProperty(CsvDriver.QUOTECHAR).charAt(0);
			}
			// set the headerline to be used
			if (info.getProperty(CsvDriver.HEADERLINE) != null) {
				headerline = info.getProperty(CsvDriver.HEADERLINE);
			}
			// set the header suppression flag
			if (info.getProperty(CsvDriver.SUPPRESS_HEADERS) != null) {
				suppressHeaders = Boolean.valueOf(info.getProperty(CsvDriver.SUPPRESS_HEADERS)).booleanValue();
			}
			// default charset
			if (info.getProperty(CsvDriver.CHARSET) != null) {
				charset = info.getProperty(CsvDriver.CHARSET);
			}
			// default column types
			if (info.getProperty(CsvDriver.COLUMN_TYPES) != null) {
				columnTypes = info.getProperty(CsvDriver.COLUMN_TYPES);
			}
			// are files indexed? ()
			if (info.getProperty(CsvDriver.INDEXED_FILES) != null) {
				indexedFiles = Boolean.valueOf(info.getProperty(CsvDriver.INDEXED_FILES)).booleanValue();
				fileNamePattern = info.getProperty("fileTailPattern");
				nameParts = info.getProperty("fileTailParts", "").split(",");
				setFileTailPrepend(Boolean.parseBoolean(info.getProperty(CsvDriver.FILE_TAIL_PREPEND, CsvDriver.DEFAULT_FILE_TAIL_PREPEND)));
			}
			// is the stream to be decrypted? ()
			// per default: no, it's unencrypted and will not be decrypted
			decryptingFilter = null;
			if (info.getProperty(CsvDriver.CRYPTO_FILTER_CLASS_NAME) != null) {
				String className = info.getProperty(CsvDriver.CRYPTO_FILTER_CLASS_NAME);
				try {
					Class encrypterClass = Class.forName(className);
					String[] parameterTypes = info.getProperty("cryptoFilterParameterTypes", "String").split(",");
					String[] parameterStrings = info.getProperty("cryptoFilterParameters", "").split(",");
					StringConverter converter = new StringConverter("", "", "");
					Class[] parameterClasses = new Class[parameterStrings.length];
					Object[] parameterValues = new Object[parameterStrings.length];
					for (int i = 0; i < parameterStrings.length; i++) {
						parameterClasses[i] = converter.forSQLName(parameterTypes[i]);
						parameterValues[i] = converter.convert(parameterTypes[i], parameterStrings[i]);
					}
					Constructor constructor = encrypterClass.getConstructor(parameterClasses);
					decryptingFilter = (CryptoFilter) constructor.newInstance(parameterValues);
					// ignore all possible exceptions: just leave the stream
					// undecrypted.
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				} catch (NoSuchMethodException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					throw new SQLException("could not find codec class " + className);
				}
				if (decryptingFilter == null) {
					throw new SQLException("could not initialize CryptoFilter");
				}
			}
			setTransposedLines(Integer.parseInt(info.getProperty(CsvDriver.TRANSPOSED_LINES, "0")));
			setTransposedFieldsToSkip(Integer.parseInt(info.getProperty(CsvDriver.TRANSPOSED_FIELDS_TO_SKIP, "0")));

			setTimestampFormat(info.getProperty(CsvDriver.TIMESTAMP_FORMAT, CsvDriver.DEFAULT_TIMESTAMP_FORMAT));
			setDateFormat(info.getProperty(CsvDriver.DATE_FORMAT, CsvDriver.DEFAULT_DATE_FORMAT));
			setTimeFormat(info.getProperty(CsvDriver.TIME_FORMAT, CsvDriver.DEFAULT_TIME_FORMAT));
			setTimeZoneName(info.getProperty(CsvDriver.TIME_ZONE_NAME, CsvDriver.DEFAULT_TIME_ZONE_NAME));
			setCommentChar(info.getProperty(CsvDriver.COMMENT_CHAR, CsvDriver.DEFAULT_COMMENT_CHAR));
			setDefectiveHeaders(info.getProperty(CsvDriver.DEFECTIVE_HEADERS, CsvDriver.DEFAULT_DEFECTIVE_HEADERS));
			setSkipLeadingDataLines(info.getProperty(CsvDriver.SKIP_LEADING_DATA_LINES, CsvDriver.DEFAULT_SKIP_LEADING_DATA_LINES));
			setSkipLeadingLines(info.getProperty(CsvDriver.SKIP_LEADING_LINES, CsvDriver.DEFAULT_SKIP_LEADING_LINES));
			setQuoteStyle(info.getProperty(CsvDriver.QUOTE_STYLE, CsvDriver.DEFAULT_QUOTE_STYLE));
			setIgnoreUnparseableLines(Boolean.parseBoolean(info.getProperty(CsvDriver.IGNORE_UNPARSEABLE_LINES, CsvDriver.DEFAULT_IGNORE_UNPARSEABLE_LINES)));

			setRaiseUnsupportedOperationException(Boolean.parseBoolean(info.getProperty(CsvDriver.RAISE_UNSUPPORTED_OPERATION_EXCEPTION, CsvDriver.DEFAULT_RAISE_UNSUPPORTED_OPERATION_EXCEPTION)));
		}
	}

	private void setQuoteStyle(String property) {
		// TODO Auto-generated method stub
		quoteStyle = property;
	}

	public String getQuoteStyle() {
		return quoteStyle;
	}

	private void setTimeZoneName(String property) {
		timeZoneName = property;
	}

	public String getTimeZoneName() {
		return timeZoneName;
	}

	/**
	 * Creates a <code>Statement</code> object for sending SQL statements to the
	 * database. SQL statements without parameters are normally executed using
	 * <code>Statement</code> objects. If the same SQL statement is executed
	 * many times, it may be more efficient to use a
	 * <code>PreparedStatement</code> object.
	 * <P>
	 * Result sets created using the returned <code>Statement</code> object will
	 * by default be type <code>TYPE_FORWARD_ONLY</code> and have a concurrency
	 * level of <code>CONCUR_READ_ONLY</code>.
	 * 
	 * @return a new default <code>Statement</code> object
	 * @exception SQLException
	 *                if a database access error occurs
	 */
	public Statement createStatement() throws SQLException {
		CsvStatement statement = new CsvStatement(this, java.sql.ResultSet.TYPE_SCROLL_INSENSITIVE);
		statements.add(statement);
		return statement;
	}

	/**
	 * Creates a <code>PreparedStatement</code> object for sending parameterized
	 * SQL statements to the database.
	 * <P>
	 * A SQL statement with or without IN parameters can be pre-compiled and
	 * stored in a <code>PreparedStatement</code> object. This object can then
	 * be used to efficiently execute this statement multiple times.
	 * 
	 * <P>
	 * <B>Note:</B> This method is optimized for handling parametric SQL
	 * statements that benefit from precompilation. If the driver supports
	 * precompilation, the method <code>prepareStatement</code> will send the
	 * statement to the database for precompilation. Some drivers may not
	 * support precompilation. In this case, the statement may not be sent to
	 * the database until the <code>PreparedStatement</code> object is executed.
	 * This has no direct effect on users; however, it does affect which methods
	 * throw certain <code>SQLException</code> objects.
	 * <P>
	 * Result sets created using the returned <code>PreparedStatement</code>
	 * object will by default be type <code>TYPE_FORWARD_ONLY</code> and have a
	 * concurrency level of <code>CONCUR_READ_ONLY</code>.
	 * 
	 * @param sql
	 *            an SQL statement that may contain one or more '?' IN parameter
	 *            placeholders
	 * @return a new default <code>PreparedStatement</code> object containing
	 *         the pre-compiled SQL statement
	 * @exception SQLException
	 *                if a database access error occurs
	 */
	public PreparedStatement prepareStatement(String sql) throws SQLException {
		return new CsvPreparedStatement(this, sql, java.sql.ResultSet.TYPE_SCROLL_INSENSITIVE);
	}

	/**
	 * Creates a <code>CallableStatement</code> object for calling database
	 * stored procedures. The <code>CallableStatement</code> object provides
	 * methods for setting up its IN and OUT parameters, and methods for
	 * executing the call to a stored procedure.
	 * 
	 * <P>
	 * <B>Note:</B> This method is optimized for handling stored procedure call
	 * statements. Some drivers may send the call statement to the database when
	 * the method <code>prepareCall</code> is done; others may wait until the
	 * <code>CallableStatement</code> object is executed. This has no direct
	 * effect on users; however, it does affect which method throws certain
	 * SQLExceptions.
	 * <P>
	 * Result sets created using the returned <code>CallableStatement</code>
	 * object will by default be type <code>TYPE_FORWARD_ONLY</code> and have a
	 * concurrency level of <code>CONCUR_READ_ONLY</code>.
	 * 
	 * @param sql
	 *            an SQL statement that may contain one or more '?' parameter
	 *            placeholders. Typically this statement is a JDBC function call
	 *            escape string.
	 * @return a new default <code>CallableStatement</code> object containing
	 *         the pre-compiled SQL statement
	 * @exception SQLException
	 *                if a database access error occurs
	 */
	public CallableStatement prepareCall(String sql) throws SQLException {
		throw new UnsupportedOperationException("Connection.prepareCall(String) unsupported");
	}

	/**
	 * Converts the given SQL statement into the system's native SQL grammar. A
	 * driver may convert the JDBC SQL grammar into its system's native SQL
	 * grammar prior to sending it. This method returns the native form of the
	 * statement that the driver would have sent.
	 * 
	 * @param sql
	 *            an SQL statement that may contain one or more '?' parameter
	 *            placeholders
	 * @return the native form of this statement
	 * @exception SQLException
	 *                if a database access error occurs
	 */
	public String nativeSQL(String sql) throws SQLException {
		throw new UnsupportedOperationException("Connection.nativeSQL(String) unsupported");
	}

	/**
	 * Sets this connection's auto-commit mode to the given state. If a
	 * connection is in auto-commit mode, then all its SQL statements will be
	 * executed and committed as individual transactions. Otherwise, its SQL
	 * statements are grouped into transactions that are terminated by a call to
	 * either the method <code>commit</code> or the method <code>rollback</code>
	 * . By default, new connections are in auto-commit mode.
	 * <P>
	 * The commit occurs when the statement completes or the next execute
	 * occurs, whichever comes first. In the case of statements returning a
	 * <code>ResultSet</code> object, the statement completes when the last row
	 * of the <code>ResultSet</code> object has been retrieved or the
	 * <code>ResultSet</code> object has been closed. In advanced cases, a
	 * single statement may return multiple results as well as output parameter
	 * values. In these cases, the commit occurs when all results and output
	 * parameter values have been retrieved.
	 * <P>
	 * <B>NOTE:</B> If this method is called during a transaction, the
	 * transaction is committed.
	 * 
	 * @param autoCommit
	 *            <code>true</code> to enable auto-commit mode;
	 *            <code>false</code> to disable it
	 * @exception SQLException
	 *                if a database access error occurs
	 * @see #getAutoCommit
	 */
	public void setAutoCommit(boolean autoCommit) throws SQLException {
		if (raiseUnsupportedOperationException) {
			throw new UnsupportedOperationException("Connection.setAutoCommit(boolean) unsupported");
		} else {
			this.autoCommit = autoCommit;
		}
	}

	/**
	 * Retrieves the current auto-commit mode for this <code>Connection</code>
	 * object.
	 * 
	 * @return the current state of this <code>Connection</code> object's
	 *         auto-commit mode
	 * @exception SQLException
	 *                if a database access error occurs
	 * @see #setAutoCommit
	 */
	public boolean getAutoCommit() throws SQLException {
		if (raiseUnsupportedOperationException) {
			throw new UnsupportedOperationException("Connection.getAutoCommit() unsupported");
		} else {
			return this.autoCommit;
		}
	}

	/**
	 * Makes all changes made since the previous commit/rollback permanent and
	 * releases any database locks currently held by this
	 * <code>Connection</code> object. This method should be used only when
	 * auto-commit mode has been disabled.
	 * 
	 * @exception SQLException
	 *                if a database access error occurs or this
	 *                <code>Connection</code> object is in auto-commit mode
	 * @see #setAutoCommit
	 */
	public void commit() throws SQLException {
		if (raiseUnsupportedOperationException)
			throw new UnsupportedOperationException("Connection.commit() unsupported");
	}

	/**
	 * Undoes all changes made in the current transaction and releases any
	 * database locks currently held by this <code>Connection</code> object.
	 * This method should be used only when auto-commit mode has been disabled.
	 * 
	 * @exception SQLException
	 *                if a database access error occurs or this
	 *                <code>Connection</code> object is in auto-commit mode
	 * @see #setAutoCommit
	 */
	public void rollback() throws SQLException {
		if (raiseUnsupportedOperationException)
			throw new UnsupportedOperationException("Connection.rollback() unsupported");
	}

	/**
	 * Releases this <code>Connection</code> object's database and JDBC
	 * resources immediately instead of waiting for them to be automatically
	 * released.
	 * <P>
	 * Calling the method <code>close</code> on a <code>Connection</code> object
	 * that is already closed is a no-op.
	 * <P>
	 * <B>Note:</B> A <code>Connection</code> object is automatically closed
	 * when it is garbage collected. Certain fatal errors also close a
	 * <code>Connection</code> object.
	 * 
	 * @exception SQLException
	 *                if a database access error occurs
	 */
	public void close() throws SQLException {
		// close all created statements
		for (Enumeration i = statements.elements(); i.hasMoreElements();) {
			CsvStatement statement = (CsvStatement) i.nextElement();
			statement.close();
		}
		// set this Connection as closed
		closed = true;
	}

	/**
	 * Retrieves whether this <code>Connection</code> object has been closed. A
	 * connection is closed if the method <code>close</code> has been called on
	 * it or if certain fatal errors have occurred. This method is guaranteed to
	 * return <code>true</code> only when it is called after the method
	 * <code>Connection.close</code> has been called.
	 * <P>
	 * This method generally cannot be called to determine whether a connection
	 * to a database is valid or invalid. A typical client can determine that a
	 * connection is invalid by catching any exceptions that might be thrown
	 * when an operation is attempted.
	 * 
	 * @return <code>true</code> if this <code>Connection</code> object is
	 *         closed; <code>false</code> if it is still open
	 * @exception SQLException
	 *                if a database access error occurs
	 */
	public boolean isClosed() throws SQLException {
		return closed;
	}

	/**
	 * Retrieves a <code>DatabaseMetaData</code> object that contains metadata
	 * about the database to which this <code>Connection</code> object
	 * represents a connection. The metadata includes information about the
	 * database's tables, its supported SQL grammar, its stored procedures, the
	 * capabilities of this connection, and so on.
	 * 
	 * @return a <code>DatabaseMetaData</code> object for this
	 *         <code>Connection</code> object
	 * @exception SQLException
	 *                if a database access error occurs
	 */
	public DatabaseMetaData getMetaData() throws SQLException {
		return new CsvDatabaseMetaData(this);
	}

	/**
	 * Puts this connection in read-only mode as a hint to the driver to enable
	 * database optimizations.
	 * 
	 * <P>
	 * <B>Note:</B> This method cannot be called during a transaction.
	 * 
	 * @param readOnly
	 *            <code>true</code> enables read-only mode; <code>false</code>
	 *            disables it
	 * @exception SQLException
	 *                if a database access error occurs or this method is called
	 *                during a transaction
	 */
	public void setReadOnly(boolean readOnly) throws SQLException {
		if (raiseUnsupportedOperationException)
			throw new UnsupportedOperationException("Connection.setReadOnly(boolean) unsupported");
	}

	/**
	 * Retrieves whether this <code>Connection</code> object is in read-only
	 * mode.
	 * 
	 * @return <code>true</code> if this <code>Connection</code> object is
	 *         read-only; <code>false</code> otherwise
	 * @exception SQLException
	 *                if a database access error occurs
	 */
	public boolean isReadOnly() throws SQLException {
		return true;
	}

	/**
	 * Sets the given catalog name in order to select a subspace of this
	 * <code>Connection</code> object's database in which to work.
	 * <P>
	 * If the driver does not support catalogs, it will silently ignore this
	 * request.
	 * 
	 * @param catalog
	 *            the name of a catalog (subspace in this
	 *            <code>Connection</code> object's database) in which to work
	 * @exception SQLException
	 *                if a database access error occurs
	 * @see #getCatalog
	 */
	public void setCatalog(String catalog) throws SQLException {
		// silently ignore this request
	}

	/**
	 * Retrieves this <code>Connection</code> object's current catalog name.
	 * 
	 * @return the current catalog name or <code>null</code> if there is none
	 * @exception SQLException
	 *                if a database access error occurs
	 * @see #setCatalog
	 */
	public String getCatalog() throws SQLException {
		return null;
	}

	/**
	 * Attempts to change the transaction isolation level for this
	 * <code>Connection</code> object to the one given. The constants defined in
	 * the interface <code>Connection</code> are the possible transaction
	 * isolation levels.
	 * <P>
	 * <B>Note:</B> If this method is called during a transaction, the result is
	 * implementation-defined.
	 * 
	 * @param level
	 *            one of the following <code>Connection</code> constants:
	 *            <code>Connection.TRANSACTION_READ_UNCOMMITTED</code>,
	 *            <code>Connection.TRANSACTION_READ_COMMITTED</code>,
	 *            <code>Connection.TRANSACTION_REPEATABLE_READ</code>, or
	 *            <code>Connection.TRANSACTION_SERIALIZABLE</code>. (Note that
	 *            <code>Connection.TRANSACTION_NONE</code> cannot be used
	 *            because it specifies that transactions are not supported.)
	 * @exception SQLException
	 *                if a database access error occurs or the given parameter
	 *                is not one of the <code>Connection</code> constants
	 * @see DatabaseMetaData#supportsTransactionIsolationLevel
	 * @see #getTransactionIsolation
	 */
	public void setTransactionIsolation(int level) throws SQLException {
		throw new UnsupportedOperationException("Connection.setTransactionIsolation(int) unsupported");
	}

	/**
	 * Retrieves this <code>Connection</code> object's current transaction
	 * isolation level.
	 * 
	 * @return the current transaction isolation level, which will be one of the
	 *         following constants:
	 *         <code>Connection.TRANSACTION_READ_UNCOMMITTED</code>,
	 *         <code>Connection.TRANSACTION_READ_COMMITTED</code>,
	 *         <code>Connection.TRANSACTION_REPEATABLE_READ</code>,
	 *         <code>Connection.TRANSACTION_SERIALIZABLE</code>, or
	 *         <code>Connection.TRANSACTION_NONE</code>.
	 * @exception SQLException
	 *                if a database access error occurs
	 * @see #setTransactionIsolation
	 */
	public int getTransactionIsolation() throws SQLException {
		return Connection.TRANSACTION_NONE;
	}

	/**
	 * Retrieves the first warning reported by calls on this
	 * <code>Connection</code> object. If there is more than one warning,
	 * subsequent warnings will be chained to the first one and can be retrieved
	 * by calling the method <code>SQLWarning.getNextWarning</code> on the
	 * warning that was retrieved previously.
	 * <P>
	 * This method may not be called on a closed connection; doing so will cause
	 * an <code>SQLException</code> to be thrown.
	 * 
	 * <P>
	 * <B>Note:</B> Subsequent warnings will be chained to this SQLWarning.
	 * 
	 * @return the first <code>SQLWarning</code> object or <code>null</code> if
	 *         there are none
	 * @exception SQLException
	 *                if a database access error occurs or this method is called
	 *                on a closed connection
	 * @see SQLWarning
	 */
	public SQLWarning getWarnings() throws SQLException {
		if (raiseUnsupportedOperationException) {
			throw new UnsupportedOperationException("Connection.getWarnings() unsupported");
		} else {
			return null;
		}
	}

	/**
	 * Clears all warnings reported for this <code>Connection</code> object.
	 * After a call to this method, the method <code>getWarnings</code> returns
	 * <code>null</code> until a new warning is reported for this
	 * <code>Connection</code> object.
	 * 
	 * @exception SQLException
	 *                if a database access error occurs
	 */
	public void clearWarnings() throws SQLException {
		if (raiseUnsupportedOperationException)
			throw new UnsupportedOperationException("Connection.getWarnings() unsupported");
	}

	// --------------------------JDBC 2.0-----------------------------

	/**
	 * Creates a <code>Statement</code> object that will generate
	 * <code>ResultSet</code> objects with the given type and concurrency. This
	 * method is the same as the <code>createStatement</code> method above, but
	 * it allows the default result set type and concurrency to be overridden.
	 * Now also supports <code>ResultSet.TYPE_SCROLL_SENSITIVE</code>
	 * 
	 * @param resultSetType
	 *            a result set type; one of
	 *            <code>ResultSet.TYPE_FORWARD_ONLY</code>,
	 *            <code>ResultSet.TYPE_SCROLL_INSENSITIVE</code>, or
	 *            <code>ResultSet.TYPE_SCROLL_SENSITIVE</code>
	 * @param resultSetConcurrency
	 *            a concurrency type; one of
	 *            <code>ResultSet.CONCUR_READ_ONLY</code> or
	 *            <code>ResultSet.CONCUR_UPDATABLE</code>
	 * @return a new <code>Statement</code> object that will generate
	 *         <code>ResultSet</code> objects with the given type and
	 *         concurrency
	 * @exception SQLException
	 *                if a database access error occurs or the given parameters
	 *                are not <code>ResultSet</code> constants indicating type
	 *                and concurrency
	 */
	public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
		CsvStatement statement = new CsvStatement(this, resultSetType);
		statements.add(statement);
		return statement;
	}

	/**
	 * Creates a <code>PreparedStatement</code> object that will generate
	 * <code>ResultSet</code> objects with the given type and concurrency. This
	 * method is the same as the <code>prepareStatement</code> method above, but
	 * it allows the default result set type and concurrency to be overridden.
	 * 
	 * @param sql
	 *            a <code>String</code> object that is the SQL statement to be
	 *            sent to the database; may contain one or more ? IN parameters
	 * @param resultSetType
	 *            a result set type; one of
	 *            <code>ResultSet.TYPE_FORWARD_ONLY</code>,
	 *            <code>ResultSet.TYPE_SCROLL_INSENSITIVE</code>, or
	 *            <code>ResultSet.TYPE_SCROLL_SENSITIVE</code>
	 * @param resultSetConcurrency
	 *            a concurrency type; one of
	 *            <code>ResultSet.CONCUR_READ_ONLY</code> or
	 *            <code>ResultSet.CONCUR_UPDATABLE</code>
	 * @return a new PreparedStatement object containing the pre-compiled SQL
	 *         statement that will produce <code>ResultSet</code> objects with
	 *         the given type and concurrency
	 * @exception SQLException
	 *                if a database access error occurs or the given parameters
	 *                are not <code>ResultSet</code> constants indicating type
	 *                and concurrency
	 */
	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
		throw new UnsupportedOperationException("Connection.prepareStatement(String \"" + sql + "\", int " + resultSetType + ", int " + resultSetConcurrency + ") unsupported");
	}

	/**
	 * Creates a <code>CallableStatement</code> object that will generate
	 * <code>ResultSet</code> objects with the given type and concurrency. This
	 * method is the same as the <code>prepareCall</code> method above, but it
	 * allows the default result set type and concurrency to be overridden.
	 * 
	 * @param sql
	 *            a <code>String</code> object that is the SQL statement to be
	 *            sent to the database; may contain on or more ? parameters
	 * @param resultSetType
	 *            a result set type; one of
	 *            <code>ResultSet.TYPE_FORWARD_ONLY</code>,
	 *            <code>ResultSet.TYPE_SCROLL_INSENSITIVE</code>, or
	 *            <code>ResultSet.TYPE_SCROLL_SENSITIVE</code>
	 * @param resultSetConcurrency
	 *            a concurrency type; one of
	 *            <code>ResultSet.CONCUR_READ_ONLY</code> or
	 *            <code>ResultSet.CONCUR_UPDATABLE</code>
	 * @return a new <code>CallableStatement</code> object containing the
	 *         pre-compiled SQL statement that will produce
	 *         <code>ResultSet</code> objects with the given type and
	 *         concurrency
	 * @exception SQLException
	 *                if a database access error occurs or the given parameters
	 *                are not <code>ResultSet</code> constants indicating type
	 *                and concurrency
	 */
	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
		throw new UnsupportedOperationException("Connection.prepareCall(String, int, int) unsupported");
	}

	/**
	 * Retrieves the <code>Map</code> object associated with this
	 * <code>Connection</code> object. Unless the application has added an
	 * entry, the type map returned will be empty.
	 * 
	 * @return the <code>java.util.Map</code> object associated with this
	 *         <code>Connection</code> object
	 * @exception SQLException
	 *                if a database access error occurs
	 * @see #setTypeMap
	 */
	public Map getTypeMap() throws SQLException {
		throw new UnsupportedOperationException("Connection.getTypeMap() unsupported");
	}

	// --------------------------JDBC 3.0-----------------------------
	/**
	 * Changes the holdability of <code>ResultSet</code> objects created using
	 * this <code>Connection</code> object to the given holdability.
	 * 
	 * @param holdability
	 *            a <code>ResultSet</code> holdability constant; one of
	 *            <code>ResultSet.HOLD_CURSORS_OVER_COMMIT</code> or
	 *            <code>ResultSet.CLOSE_CURSORS_AT_COMMIT</code>
	 * @throws SQLException
	 *             if a database access occurs, the given parameter is not a
	 *             <code>ResultSet</code> constant indicating holdability, or
	 *             the given holdability is not supported
	 * @since 1.4
	 * @see #getHoldability
	 * @see java.sql.ResultSet
	 */
	public void setHoldability(int holdability) throws SQLException {
		throw new UnsupportedOperationException("Connection.setHoldability(int) unsupported");
	}

	/**
	 * Retrieves the current holdability of ResultSet objects created using this
	 * Connection object.
	 * 
	 * @return the holdability, one of
	 *         <code>ResultSet.HOLD_CURSORS_OVER_COMMIT</code> or
	 *         <code>ResultSet.CLOSE_CURSORS_AT_COMMIT</code>
	 * @throws SQLException
	 *             if a database access occurs
	 * @since 1.4
	 * @see #setHoldability
	 * @see java.sql.ResultSet
	 */
	public int getHoldability() throws SQLException {
		throw new UnsupportedOperationException("Connection.getHoldability() unsupported");
	}

	/*
	 * Removed since this only builds under JDK 1.4 public Savepoint
	 * setSavepoint() throws SQLException { throw new
	 * UnsupportedOperationException("Connection.setSavepoint() unsupported"); }
	 * 
	 * public Savepoint setSavepoint(String name) throws SQLException { throw
	 * new
	 * UnsupportedOperationException("Connection.setSavepoint(String) unsupported"
	 * ); }
	 * 
	 * public void rollback(Savepoint savepoint) throws SQLException { throw new
	 * UnsupportedOperationException
	 * ("Connection.rollback(Savepoint) unsupported"); }
	 * 
	 * public void releaseSavepoint(Savepoint savepoint) throws SQLException {
	 * throw new UnsupportedOperationException(
	 * "Connection.releaseSavepoint(Savepoint) unsupported"); }
	 */

	public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
		throw new UnsupportedOperationException("Connection.createStatement(int,int,int) unsupported");
	}

	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
		throw new UnsupportedOperationException("Connection.prepareStatement(String,int,int,int) unsupported");
	}

	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
		throw new UnsupportedOperationException("Connection.prepareCall(String,int,int,int) unsupported");
	}

	public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
		throw new UnsupportedOperationException("Connection.prepareStatement(String,int) unsupported");
	}

	public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
		throw new UnsupportedOperationException("Connection.prepareStatement(String,int[]) unsupported");
	}

	public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
		throw new UnsupportedOperationException("Connection.prepareStatement(String,String[]) unsupported");
	}

	public void releaseSavepoint(Savepoint savePoint) throws SQLException {
		throw new UnsupportedOperationException("Connection.releaseSavepoint(Savepoint) unsupported");
	}

	public void rollback(Savepoint savePoint) throws SQLException {
		throw new UnsupportedOperationException("Connection.rollback(Savepoint) unsupported");
	}

	public Savepoint setSavepoint() throws SQLException {
		throw new UnsupportedOperationException("Connection.setSavepoint() unsupported");
	}

	public Savepoint setSavepoint(String str) throws SQLException {
		throw new UnsupportedOperationException("Connection.setSavepoint(String) unsupported");
	}

	// ---------------------------------------------------------------------
	// Properties
	// ---------------------------------------------------------------------

	/**
	 * Accessor method for the path property
	 * 
	 * @return current value for the path property
	 */
	protected String getPath() {
		return path;
	}

	/**
	 * Accessor method for the extension property
	 * 
	 * @return current value for the extension property
	 */
	protected String getExtension() {
		return extension;
	}

	/**
	 * Accessor method for the separator property
	 * 
	 * @return current value for the separator property
	 */
	protected char getSeparator() {
		return separator;
	}

	/**
	 * Accessor method for the headerline property
	 * 
	 * @return current value for the headerline property
	 */
	public String getHeaderline() {
		return headerline;
	}

	/**
	 * Accessor method for the quotechar property
	 * 
	 * @return current value for the quotechar property
	 */
	public char getQuotechar() {
		return quotechar;
	}

	/**
	 * Accessor method for the suppressHeaders property
	 * 
	 * @return current value for the suppressHeaders property
	 */
	protected boolean isSuppressHeaders() {
		return suppressHeaders;
	}

	/**
	 * accessor method for defectiveHeaders property
	 * 
	 * @return
	 */
	protected boolean isDefectiveHeaders() {
		return defectiveHeaders;
	}

	/**
	 * accessor method for defectiveHeaders property
	 * 
	 * @return
	 */
	protected int getSkipLeadingDataLines() {
		return skipLeadingDataLines;
	}

	/**
	 * Accessor method for the charset property
	 * 
	 * @return current value for the suppressHeaders property
	 */
	protected String getCharset() {
		return charset;
	}

	/**
	 * Accessor method for the trimHeaders property
	 * 
	 * @return current value for the trimHeaders property
	 */
	public boolean getTrimHeaders() {
		return trimHeaders;
	}

	public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public Blob createBlob() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public Clob createClob() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public Properties getClientInfo() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getClientInfo(String name) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isValid(int timeout) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	private void setDefectiveHeaders(String property) {
		this.defectiveHeaders = Boolean.parseBoolean(property);
	}

	private void setSkipLeadingDataLines(String property) {
		this.skipLeadingDataLines = Integer.parseInt(property);
	}

	public void setColumnTypes(String columnTypes) {
		this.columnTypes = columnTypes;
	}

	public String getColumnTypes() {
		return columnTypes;
	}

	public void setIndexedFiles(boolean indexedFiles) {
		this.indexedFiles = indexedFiles;
	}

	public boolean isIndexedFiles() {
		return indexedFiles;
	}

	public String getFileNamePattern() {
		return fileNamePattern;
	}

	public String[] getNameParts() {
		return nameParts;
	}

	public void setTimestampFormat(String timestampFormat) {
		this.timestampFormat = timestampFormat;
	}

	public String getTimestampFormat() {
		return timestampFormat;
	}

	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}

	public String getDateFormat() {
		return dateFormat;
	}

	public void setTimeFormat(String timeFormat) {
		this.timeFormat = timeFormat;
	}

	public String getTimeFormat() {
		return timeFormat;
	}

	public void setCommentChar(String value) {
		if (value == null) {
			commentChar = null;
		} else if (value.equals("")) {
			commentChar = null;
		} else {
			commentChar = new Character(value.charAt(0));
		}
	}

	public char getCommentChar() {
		if (commentChar == null)
			return 0;
		return commentChar.charValue();
	}

	private void setSkipLeadingLines(String property) {
		try {
			skipLeadingLines = Integer.parseInt(property);
		} catch (NumberFormatException e) {
			skipLeadingLines = 0;
		}
	}

	/**
	 * @return the skipLeadingLines
	 */
	public int getSkipLeadingLines() {
		return skipLeadingLines;
	}

	/**
	 * @param skipLeadingLines
	 *            the skipLeadingLines to set
	 */
	public void setSkipLeadingLines(int skipLeadingLines) {
		this.skipLeadingLines = skipLeadingLines;
	}

	public boolean isIgnoreUnparseableLines() {
		return ignoreUnparseableLines;
	}

	/**
	 * @param ignoreUnparseableLines
	 *            the ignoreUnparseableLines to set
	 */
	public void setIgnoreUnparseableLines(boolean ignoreUnparseableLines) {
		this.ignoreUnparseableLines = ignoreUnparseableLines;
	}

	public void setFileTailPrepend(boolean fileTailPrepend) {
		this.fileTailPrepend = fileTailPrepend;
	}

	public boolean isFileTailPrepend() {
		return fileTailPrepend;
	}

	public CryptoFilter getDecryptingCodec() {
		return this.decryptingFilter;
	}

	public NClob createNClob() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public SQLXML createSQLXML() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public void setClientInfo(Properties arg0) throws SQLClientInfoException {
		// TODO Auto-generated method stub

	}

	public void setClientInfo(String arg0, String arg1) throws SQLClientInfoException {
		// TODO Auto-generated method stub

	}

	public int getTransposedLines() {
		return transposedLines;
	}

	private void setTransposedLines(int i) {
		transposedLines = i;
	}

	public int getTransposedFieldsToSkip() {
		return transposedFieldsToSkip;
	}

	public void setTransposedFieldsToSkip(int i) {
		transposedFieldsToSkip = i;
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setSchema(String schema) throws SQLException {
		// TODO Auto-generated method stub

	}

	@Override
	public String getSchema() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void abort(Executor executor) throws SQLException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
		// TODO Auto-generated method stub

	}

	@Override
	public int getNetworkTimeout() throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

}
