// {{CopyrightNotice}}

package com.ss.speedtransfer.util.parser.sql;

/**
 * The <code>TBooleanType</code> class
 */
public final class TBooleanType extends TType {

	/**
	 * Constructs the type.
	 */
	public TBooleanType(int sql_type) {
		super(sql_type);
	}

	public boolean comparableTypes(TType type) {
		return (type instanceof TBooleanType || type instanceof TNumericType);
	}

	public int compareObs(Object ob1, Object ob2) {

		if (ob2 instanceof BigNumber) {
			BigNumber n2 = (BigNumber) ob2;
			BigNumber n1 = ob1.equals(Boolean.FALSE) ? BigNumber.BIG_NUMBER_ZERO : BigNumber.BIG_NUMBER_ONE;
			return n1.compareTo(n2);
		}

		if (ob1 == ob2 || ob1.equals(ob2)) {
			return 0;
		} else if (ob1.equals(Boolean.TRUE)) {
			return 1;
		} else {
			return -1;
		}
	}

	public int calculateApproximateMemoryUse(Object ob) {
		return 5;
	}

	public Class javaClass() {
		return Boolean.class;
	}

}