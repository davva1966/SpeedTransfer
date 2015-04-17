// {{CopyrightNotice}}

package com.ss.speedtransfer.util.parser.sql;

import java.util.ArrayList;
import java.util.List;

/**
 * Search expression is a form of an Expression that is split up into component parts that can be easily formed into a search query.
 */
public final class SearchExpression {

	/**
	 * The originating expression.
	 */
	private Expression search_expression;

	/**
	 * Accept the passed visitor The visitor visit this node and all it's child elements The visitAfter is called if the visit method returns true
	 * 
	 * @param visitor
	 *            - the visitor
	 * @param data
	 *            - object that is passed along the visitor
	 */
	public void acceptVisitor(SQLVisitor visitor, Object data) {
		if (visitor.visit(this, data)) {

		}
		visitor.visitEnd(this, data);
	}

	/**
	 * Sets this search expression from the given expression.
	 */
	public void setFromExpression(Expression expression) {
		this.search_expression = expression;
	}

	/**
	 * Returns the search expression as an Expression object.
	 */
	public Expression getFromExpression() {
		return search_expression;
	}

	/**
	 * Concatinates a new expression to the end of this expression and uses the 'AND' operator to seperate the expressions. This is very useful for adding new logical conditions to the expression at
	 * runtime.
	 */
	void appendExpression(Expression expression) {
		if (search_expression == null) {
			search_expression = expression;
		} else {
			search_expression = new Expression(search_expression, Operator.get("and"), expression);
		}
	}

	/**
	 * Returns all the Elements from all expressions in this condition tree.
	 */
	List allElements() {
		if (search_expression != null) {
			return search_expression.allElements();
		} else {
			return new ArrayList();
		}
	}

	public Object clone() throws CloneNotSupportedException {
		SearchExpression v = (SearchExpression) super.clone();
		if (search_expression != null) {
			v.search_expression = (Expression) search_expression.clone();
		}
		return v;
	}

	public String toString() {
		if (search_expression != null) {
			return search_expression.toString();
		} else {
			return "NO SEARCH EXPRESSION";
		}
	}

}
