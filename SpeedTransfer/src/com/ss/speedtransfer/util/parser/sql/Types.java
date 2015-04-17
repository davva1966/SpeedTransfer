// {{CopyrightNotice}}

package com.ss.speedtransfer.util.parser.sql;

/**
 * The <code>Types</code> class
 */
public interface Types {

	public static final int DB_UNKNOWN = -1;

	public static final int DB_STRING = 1;

	public static final int DB_NUMERIC = 2;

	public static final int DB_TIME = 3;

	public static final int DB_BINARY = 4; // @deprecated - use BLOB

	public static final int DB_BOOLEAN = 5;

	public static final int DB_BLOB = 6;

	public static final int DB_OBJECT = 7;

	// This is an extended numeric type that handles neg and positive infinity
	// and NaN.
	public static final int DB_NUMERIC_EXTENDED = 8;

}