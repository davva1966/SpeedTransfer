// {{CopyrightNotice}}

package com.ss.speedtransfer.util.parser.sql;

import java.util.Date;

/**
 * The <code>TDateType</code> class
 */
public class TDateType extends TType {

	static final long serialVersionUID = 1494137367081481985L;

	/**
	 * Constructs the type.
	 */
	public TDateType(int sql_type) {
		super(sql_type);
	}

	public boolean comparableTypes(TType type) {
		return (type instanceof TDateType);
	}

	public int compareObs(Object ob1, Object ob2) {
		return ((Date) ob1).compareTo((Date) ob2);
	}

	public int calculateApproximateMemoryUse(Object ob) {
		return 4 + 8;
	}

	public Class javaClass() {
		return Date.class;
	}

}