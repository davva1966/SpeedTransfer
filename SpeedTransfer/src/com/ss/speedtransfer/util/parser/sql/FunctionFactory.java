// {{CopyrightNotice}}

package com.ss.speedtransfer.util.parser.sql;

/**
 * The <code>FunctionFactory</code> class
 */
public abstract class FunctionFactory {

	private static final Expression GLOB_EXPRESSION;

	static {
		GLOB_EXPRESSION = new Expression();
		GLOB_EXPRESSION.addElement(TObject.stringVal("*"));
		GLOB_EXPRESSION.text().append("*");
	}

	/**
	 * Represents a function argument * for glob's such as 'count(*)'
	 */
	public static final Expression[] GLOB_LIST = new Expression[] { GLOB_EXPRESSION };

	/**
	 * Constructs the FunctionFactory.
	 */
	public FunctionFactory() {
	}
}