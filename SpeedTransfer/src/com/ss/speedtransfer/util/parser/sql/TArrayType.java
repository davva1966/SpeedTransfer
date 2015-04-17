// {{CopyrightNotice}}

package com.ss.speedtransfer.util.parser.sql;

/**
 * The <code>TArrayType</code> class
 */
public class TArrayType extends TType {

	/**
	 * Constructs the type.
	 */
	public TArrayType() {
		// There is no SQL type for a query plan node so we make one up here
		super(SQLTypes.ARRAY);
	}

	public boolean comparableTypes(TType type) {
		throw new Error("Query Plan types should not be compared.");
	}

	public int compareObs(Object ob1, Object ob2) {
		throw new Error("Query Plan types should not be compared.");
	}

	public int calculateApproximateMemoryUse(Object ob) {
		return 5000;
	}

	public Class javaClass() {
		return Expression[].class;
	}

}