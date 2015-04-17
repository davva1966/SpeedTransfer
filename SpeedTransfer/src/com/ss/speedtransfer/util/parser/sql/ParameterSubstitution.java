// {{CopyrightNotice}}

package com.ss.speedtransfer.util.parser.sql;

/**
 * The <code>ParameterSubstitution</code> class
 */
public class ParameterSubstitution implements java.io.Serializable {

	/**
	 * The numerical number of this parameter substitution.
	 */
	private int parameter_id;

	/**
	 * Creates the substitution.
	 */
	public ParameterSubstitution(int parameter_id) {
		this.parameter_id = parameter_id;
	}

	/**
	 * Returns the number of this parameter id.
	 */
	public int getID() {
		return parameter_id;
	}

	/**
	 * Equality test.
	 */
	public boolean equals(Object ob) {
		ParameterSubstitution sub = (ParameterSubstitution) ob;
		return this.parameter_id == sub.parameter_id;
	}

}