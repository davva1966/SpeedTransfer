/*
 *  CsvJdbc - a JDBC driver for CSV files
 *  Copyright (C) 2001  Jonathan Ackerman
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.relique.jdbc.csv;

import java.io.File;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * This class implements the Driver interface for the CsvJdbc driver.
 * 
 * @author Jonathan Ackerman
 * @author Sander Brienen
 * @author JD Evora
 * @author Tomasz Skutnik
 * @author Christoph Langer
 * @version $Id: CsvDriver.java,v 1.27 2011/10/18 14:37:05 simoc Exp $
 */

public class CsvDriver implements Driver {

	public static final String DEFAULT_EXTENSION = ".csv";
	public static final char DEFAULT_SEPARATOR = ',';
	public static final char DEFAULT_QUOTECHAR = '"';
	public static final String DEFAULT_HEADERLINE = null;
	public static final boolean DEFAULT_SUPPRESS = false;
	public static final String DEFAULT_RAISE_UNSUPPORTED_OPERATION_EXCEPTION = "true";
	public static final boolean DEFAULT_TRIM_HEADERS = true;
	public static final String DEFAULT_COLUMN_TYPES = "";
	public static final boolean DEFAULT_INDEXED_FILES = false;
	public static final String DEFAULT_TIMESTAMP_FORMAT = "YYYY-MM-DD HH:mm:ss";
	public static final String DEFAULT_DATE_FORMAT = "YYYY-MM-DD";
	public static final String DEFAULT_TIME_FORMAT = "HH:mm:ss";
	public static final String DEFAULT_COMMENT_CHAR = null;
	public static final String DEFAULT_SKIP_LEADING_LINES = null;
	public static final String DEFAULT_IGNORE_UNPARSEABLE_LINES = "False";
	public static final String DEFAULT_FILE_TAIL_PREPEND = "False";
	public static final String DEFAULT_DEFECTIVE_HEADERS = "False";
	public static final String DEFAULT_SKIP_LEADING_DATA_LINES = "0";

	public static final String FILE_EXTENSION = "fileExtension";
	public static final String SEPARATOR = "separator";
	public static final String QUOTECHAR = "quotechar";
	public static final String HEADERLINE = "headerline";
	public static final String SUPPRESS_HEADERS = "suppressHeaders";
	public static final String TRIM_HEADERS = "trimHeaders";
	public static final String COLUMN_TYPES = "columnTypes";
	public static final String INDEXED_FILES = "indexedFiles";
	public static final String TIMESTAMP_FORMAT = "timestampFormat";
	public static final String DATE_FORMAT = "dateFormat";
	public static final String TIME_FORMAT = "timeFormat";
	public static final String COMMENT_CHAR = "commentChar";
	public static final String SKIP_LEADING_LINES = "skipLeadingLines";
	public static final String IGNORE_UNPARSEABLE_LINES = "ignoreNonParseableLines";
	public static final String FILE_TAIL_PREPEND = "fileTailPrepend";
	public static final String DEFECTIVE_HEADERS = "defectiveHeaders";
	public static final String SKIP_LEADING_DATA_LINES = "skipLeadingDataLines";
	public static final String TRANSPOSED_LINES = "transposedLines";
	public static final String TRANSPOSED_FIELDS_TO_SKIP = "transposedFieldsToSkip";

	public static final String CHARSET = "charset";
	public static final String RAISE_UNSUPPORTED_OPERATION_EXCEPTION = "raiseUnsupportedOperationException";
	private final static String URL_PREFIX = "jdbc:relique:csv:";
	public static final String CRYPTO_FILTER_CLASS_NAME = "cryptoFilterClassName";

	public static final String TIME_ZONE_NAME = "timeZoneName";
	public static final String DEFAULT_TIME_ZONE_NAME = "UTC";
	// choosing Rome makes sure we change chronology from Julian to Gregorian on
	// 1582-10-04/15, as SQL does.
	public static final String QUOTE_STYLE = "quoteStyle";
	public static final String DEFAULT_QUOTE_STYLE = "SQL";

	/**
	 * Gets the propertyInfo attribute of the CsvDriver object
	 * 
	 * @param url
	 *            Description of Parameter
	 * @param info
	 *            Description of Parameter
	 * @return The propertyInfo value
	 * @exception SQLException
	 *                Description of Exception
	 * @since
	 */
	public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {

			List<DriverPropertyInfo> propList = new ArrayList<DriverPropertyInfo>();

			DriverPropertyInfo dpi = new DriverPropertyInfo("separator", null);
			dpi.description = "Used to specify a different column separator";
			dpi.required = false;
			propList.add(dpi);
			
			dpi = new DriverPropertyInfo("suppressHeaders", null);
			dpi.description = "Used to specify that the file does not contain column header information. if False, column headers are on first line";
			dpi.required = false;
			propList.add(dpi);
			
			dpi = new DriverPropertyInfo("headerline", null);
			dpi.description = "Useful in combination with above suppressHeaders, to specify a custom header line that matches the data. With suppressHeaders and without headerline, all columns are named sequentially COLUMNx";
			dpi.required = false;
			propList.add(dpi);
			
			dpi = new DriverPropertyInfo("fileExtension", null);
			dpi.description = "Used to specify a different file extension";
			dpi.required = false;
			propList.add(dpi);
			
			dpi = new DriverPropertyInfo("columnTypes", null);
			dpi.description = "You can use this property to provide the driver with a comma-separated string with names of SQL data types. when you retrieve a record field with getObject (as opposed to getString), the driver will parse the string and return you a correctly typed object. The order must match the column order in the file. If you provide less types than the number of columns, the last type is repeated for all trailing columns. Valid types are: INT, DECIMAL, CHAR, TIME, DATE and TIMESTAMP";
			dpi.required = false;
			propList.add(dpi);
			
			dpi = new DriverPropertyInfo("skipLeadingLines", null);
			dpi.description = "After opening a file, skip this many lines before starting interpreting its content";
			dpi.required = false;
			propList.add(dpi);
			
			dpi = new DriverPropertyInfo("skipLeadingDataLines", null);
			dpi.description = "After reading the header from a file, skip this many lines before starting interpreting lines as records";
			dpi.required = false;
			propList.add(dpi);
			
			dpi = new DriverPropertyInfo("defectiveHeaders", null);
			dpi.description = "In case a column name is an emtpy string, replace it with COLUMNx, where x is the ordinal identifying the column";
			dpi.required = false;
			propList.add(dpi);
			
			dpi = new DriverPropertyInfo("commentChar", null);
			dpi.description = "Lines before the header starting with this character are ignored. once the header has been read, all lines are considered as holding data";
			dpi.required = false;
			propList.add(dpi);
			
			dpi = new DriverPropertyInfo("quotechar", null);
			dpi.description = "How values are quoted. useful when values contain line breaks";
			dpi.required = false;
			propList.add(dpi);
			
			dpi = new DriverPropertyInfo("timestampFormat", null);
			dpi.description = "If you indicate a column to be of type Timestamp, the driver will try to parse each value string with these format strings to return a correctly valued typed object. Default: YYYYMMDD HHmm";
			dpi.required = false;
			propList.add(dpi);
			
			dpi = new DriverPropertyInfo("dateFormat", null);
			dpi.description = "If you indicate a column to be of type Date, the driver will try to parse each value string with these format strings to return a correctly valued typed object. Default: YYYYMMDD";
			dpi.required = false;
			propList.add(dpi);
			
			dpi = new DriverPropertyInfo("timeFormat", null);
			dpi.description = "If you indicate a column to be of type Time, the driver will try to parse each value string with these format strings to return a correctly valued typed object. Default: HHmm";
			dpi.required = false;
			propList.add(dpi);
			
			dpi = new DriverPropertyInfo("ignoreNonParseableLines", null);
			dpi.description = "When True, unparseable lines will not cause exceptions but will be silently ignored";
			dpi.required = false;
			propList.add(dpi);
			
			dpi = new DriverPropertyInfo("indexedFiles", null);
			dpi.description = "When True, all files with a filename matching the table name plus the regular expression given in property fileTailPattern are read as if they were a single file";
			dpi.required = false;
			propList.add(dpi);
			
			dpi = new DriverPropertyInfo("fileTailPattern", null);
			dpi.description = "Regular expression for matching filenames when property indexedFiles is True. If the regular expression contains groups (surrounded by parentheses) then the value of each group in matching filenames is added as an extra column to each line read from that file. For example, when querying table test, the regular expression -(\\d+)-(\\d+) will match files test-001-20081112.csv and test-002-20081113.csv. The column values 001 and 20081112 are added to each line read from the first file and 002 and 20081113 are added to each line read from the second file.";
			dpi.required = false;
			propList.add(dpi);
			
			dpi = new DriverPropertyInfo("fileNameParts", null);
			dpi.description = "Comma-separated list of column names for the additional columns generated by regular expression groups in the property fileTailPattern";
			dpi.required = false;
			propList.add(dpi);
			
			dpi = new DriverPropertyInfo("fileTailPrepend", null);
			dpi.description = "When True, columns generated by regular expression groups in the fileTailPattern property are prepended to the start of each line. When False, the generated columns are appended after the columns read for each line";
			dpi.required = false;
			propList.add(dpi);
			
			dpi = new DriverPropertyInfo("trimHeaders", null);
			dpi.description = "";
			dpi.required = false;
			propList.add(dpi);
			
			dpi = new DriverPropertyInfo("cryptoFilterClassName", null);
			dpi.description = "The full class name of a Java class that decrypts the file being read. The class must implement interface org.relique.io.CryptoFilter. The class org.relique.io.XORFilter included in CsvJdbc implements an XOR encryption filter.";
			dpi.required = false;
			propList.add(dpi);
			
			dpi = new DriverPropertyInfo("cryptoFilterParameterTypes", null);
			dpi.description = "Comma-separated list of data types to pass to the constructor of the decryption class set in property cryptoFilterClassName";
			dpi.required = false;
			propList.add(dpi);
			
			dpi = new DriverPropertyInfo("cryptoFilterParameters ", null);
			dpi.description = "Comma-separated list of values to pass to the constructor of the decryption class set in property cryptoFilterClassName";
			dpi.required = false;
			propList.add(dpi);

			return propList.toArray(new DriverPropertyInfo[0]);
	}

	/**
	 * Gets the majorVersion attribute of the CsvDriver object
	 * 
	 * @return The majorVersion value
	 * @since
	 */
	public int getMajorVersion() {
		return 1;
	}

	/**
	 * Gets the minorVersion attribute of the CsvDriver object
	 * 
	 * @return The minorVersion value
	 * @since
	 */
	public int getMinorVersion() {
		return 0;
	}

	/**
	 * Description of the Method
	 * 
	 * @param url
	 *            Description of Parameter
	 * @param info
	 *            Description of Parameter
	 * @return Description of the Returned Value
	 * @exception SQLException
	 *                Description of Exception
	 * @since
	 */
	public Connection connect(String url, Properties info) throws SQLException {
		DriverManager.println("CsvJdbc - CsvDriver:connect() - url=" + url);
		// check for correct url
		if (!url.startsWith(URL_PREFIX)) {
			return null;
		}
		// get filepath from url
		String filePath = url.substring(URL_PREFIX.length());
		if (!filePath.endsWith(File.separator)) {
			filePath += File.separator;
		}

		DriverManager.println("CsvJdbc - CsvDriver:connect() - filePath=" + filePath);

		// check if filepath is a correct path.
		File checkPath = new File(filePath);
		if (!checkPath.exists()) {
			throw new SQLException("Specified path '" + filePath + "' not found !");
		}
		if (!checkPath.isDirectory()) {
			throw new SQLException("Specified path '" + filePath + "' is  not a directory !");
		}

		return new CsvConnection(filePath, info);
	}

	/**
	 * Description of the Method
	 * 
	 * @param url
	 *            Description of Parameter
	 * @return Description of the Returned Value
	 * @exception SQLException
	 *                Description of Exception
	 * @since
	 */
	public boolean acceptsURL(String url) throws SQLException {
		DriverManager.println("CsvJdbc - CsvDriver:accept() - url=" + url);
		return url.startsWith(URL_PREFIX);
	}

	/**
	 * Description of the Method
	 * 
	 * @return Description of the Returned Value
	 * @since
	 */
	public boolean jdbcCompliant() {
		return false;
	}

	// This static block inits the driver when the class is loaded by the JVM.
	static {
		try {
			java.sql.DriverManager.registerDriver(new CsvDriver());
		} catch (SQLException e) {
			throw new RuntimeException("FATAL ERROR: Could not initialise CSV driver ! Message was: " + e.getMessage());
		}
	}

	@Override
	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		// TODO Auto-generated method stub
		return null;
	}
}
