// {{CopyrightNotice}}

package com.ss.speedtransfer.util.parser.sql;

/**
 * A definition of a function including its name and parameters. A FunctionDef can easily be transformed into a Function object via a set of FunctionFactory instances.
 * <p>
 * NOTE: This object is NOT immutable or thread-safe. A FunctionDef should not be shared among different threads.
 */
public final class FunctionDef {

	/**
	 * The name of the function.
	 */
	private String name;

	/**
	 * The list of parameters for the function.
	 */
	private Expression[] params;

	/**
	 * Constructs the FunctionDef.
	 */
	public FunctionDef(String name, Expression[] params) {
		this.name = name;
		this.params = params;
	}

	/**
	 * The name of the function. For example, 'MIN' or 'CONCAT'.
	 */
	public String getName() {
		return name;
	}

	/**
	 * The list of parameters that are passed to the function. For example, a concat function may have 7 parameters ('There', ' ', 'are', ' ', 10, ' ', 'bottles.')
	 */
	public Expression[] getParameters() {
		return params;
	}

	/**
	 * Human understandable string, used for the column title.
	 */
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append(name);
		buf.append('(');
		for (int i = 0; i < params.length; ++i) {
			buf.append(params[i].text().toString());
			if (i < params.length - 1) {
				buf.append(',');
			}
		}
		buf.append(')');
		return new String(buf);
	}

}