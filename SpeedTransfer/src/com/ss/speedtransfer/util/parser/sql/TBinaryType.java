// {{CopyrightNotice}}

package com.ss.speedtransfer.util.parser.sql;

/**
 * The <code>TBinaryType</code> class
 */
public class TBinaryType extends TType {

	/**
	 * This constrained size of the binary block of data or -1 if there is no size limit.
	 */
	private int max_size;

	/**
	 * Constructs the type.
	 */
	public TBinaryType(int sql_type, int max_size) {
		super(sql_type);
		this.max_size = max_size;
	}

	/**
	 * Returns the maximum size of this binary type.
	 */
	public int getMaximumSize() {
		return max_size;
	}
}