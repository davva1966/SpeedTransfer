// {{CopyrightNotice}}

package com.ss.speedtransfer.util.parser.sql;

/**
 * The <code>Assignment</code> class
 */
public final class Assignment {

	/**
	 * The Variable that is the lhs of the assignment.
	 */
	private Variable variable;

	/**
	 * Set expression that is the rhs of the assignment.
	 */
	private Expression expression;

	/**
	 * Constructs the assignment.
	 */
	public Assignment(Variable variable, Expression expression) {
		this.variable = variable;
		this.expression = expression;
	}

	/**
	 * Returns the variable for this assignment.
	 */
	public Variable getVariable() {
		return variable;
	}

	/**
	 * Returns the Expression for this assignment.
	 */
	public Expression getExpression() {
		return expression;
	}

}