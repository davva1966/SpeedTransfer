// {{CopyrightNotice}}

package com.ss.speedtransfer.util.parser.sql;

/**
 * Represents a column selected to be in the output of a select statement. This includes being either an aggregate function, a column or "*" which is the entire set of columns.
 */
public final class SelectColumn {

	/**
	 * If the column represents a glob of columns (eg. 'Part.*' or '*') then this is set to the glob string and 'expression' is left blank.
	 */
	public String glob_name;

	/**
	 * The fully resolved name that this column is given in the resulting table.
	 */
	public Variable resolved_name;

	/**
	 * The alias of this column string.
	 */
	public String alias;

	/**
	 * The expression of this column. This is only NOT set when name == "*" indicating all the columns.
	 */
	public Expression expression;

	/**
	 * The name of this column used internally to reference it.
	 */
	public Variable internal_name;

	public String toString() {
		String str = "";
		if (glob_name != null)
			str += " GLOB_NAME = " + glob_name;
		if (resolved_name != null)
			str += " RESOLVED_NAME = " + resolved_name;
		if (alias != null)
			str += " ALIAS = " + alias;
		if (expression != null)
			str += " EXPRESSION = " + expression;
		if (internal_name != null)
			str += " INTERNAL_NAME = " + internal_name;
		return str;
	}

}
