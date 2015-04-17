// {{CopyrightNotice}}

package com.ss.speedtransfer.util.parser.sql;

/**
 * A JDBC independant type definition list. This allows the specification of all JDBC 1.0 and 2.0 types without requiring the JDBC 2.0 'java.sql.Types' interface.
 */
public interface SQLTypes {

	public final static int BIT = -7;

	public final static int TINYINT = -6;

	public final static int SMALLINT = 5;

	public final static int INTEGER = 4;

	public final static int BIGINT = -5;

	public final static int FLOAT = 6;

	public final static int REAL = 7;

	public final static int DOUBLE = 8;

	public final static int NUMERIC = 2;

	public final static int DECIMAL = 3;

	public final static int CHAR = 1;

	public final static int VARCHAR = 12;

	public final static int LONGVARCHAR = -1;

	public final static int DATE = 91;

	public final static int TIME = 92;

	public final static int TIMESTAMP = 93;

	public final static int BINARY = -2;

	public final static int VARBINARY = -3;

	public final static int LONGVARBINARY = -4;

	public final static int NULL = 0;

	public final static int OTHER = 1111;

	public final static int JAVA_OBJECT = 2000;

	public final static int DISTINCT = 2001;

	public final static int STRUCT = 2002;

	public final static int ARRAY = 2003;

	public final static int BLOB = 2004;

	public final static int CLOB = 2005;

	public final static int REF = 2006;

	public final static int BOOLEAN = 16;

}
