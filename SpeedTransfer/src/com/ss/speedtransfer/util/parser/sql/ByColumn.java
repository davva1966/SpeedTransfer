// {{CopyrightNotice}}

package com.ss.speedtransfer.util.parser.sql;

/**
 * The <code>ByColumn</code> class
 */
public final class ByColumn {

	/**
	 * The name of the column in the 'by'.
	 */
	public Variable name;

	/**
	 * The expression that we are ordering by.
	 */
	public Expression exp;

	/**
	 * If 'order by' then true if sort is ascending (default).
	 */
	public boolean ascending = true;

	public String toString() {
		return "ByColumn(" + name + ", " + exp + ", " + ascending + ")";
	}

}