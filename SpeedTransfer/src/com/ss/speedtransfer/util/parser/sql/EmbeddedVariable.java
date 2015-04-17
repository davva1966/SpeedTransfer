//{{CopyrightNotice}}

package com.ss.speedtransfer.util.parser.sql;

/**
 * The <code>EmbeddedVariable</code> class
 */
public class EmbeddedVariable extends Variable {

	/**
	 * Initializes a newly created <code>EmbeddedVariable</code>
	 * 
	 * @param table_name
	 * @param column_name
	 */
	public EmbeddedVariable(TableName table_name, String column_name) {
		super(table_name, column_name);
	}

	/**
	 * Initializes a newly created <code>EmbeddedVariable</code>
	 * 
	 * @param column_name
	 */
	public EmbeddedVariable(String column_name) {
		super(column_name);
	}

	/**
	 * Initializes a newly created <code>EmbeddedVariable</code>
	 * 
	 * @param v
	 */
	public EmbeddedVariable(Variable v) {
		super(v);
	}

}
