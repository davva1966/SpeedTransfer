// {{CopyrightNotice}}

package com.ss.speedtransfer.util.parser.sql;

import java.util.ArrayList;

/**
 * The <code>TableSelectExpression</code> class
 */
public final class TableSelectExpression {

	/**
	 * True if we only search for distinct elements.
	 */
	public boolean distinct = false;

	/**
	 * Number of rows to retrieve
	 */
	public int top = -1;

	/**
	 * The list of columns to select from. (SelectColumn)
	 */
	public ArrayList columns = new ArrayList();

	/**
	 * The from clause.
	 */
	public FromClause from_clause = new FromClause();

	/**
	 * The where clause.
	 */
	public SearchExpression where_clause = new SearchExpression();

	/**
	 * The list of columns to group by. (ByColumn)
	 */
	public ArrayList group_by = new ArrayList();

	/**
	 * The group max variable or null if no group max.
	 */
	public Variable group_max = null;

	/**
	 * The having clause.
	 */
	public SearchExpression having_clause = new SearchExpression();

	/**
	 * If there is a composite function this is set to the composite enumeration from CompositeTable.
	 */
	int composite_function = -1; // (None)

	/**
	 * If this is an ALL composite (no removal of duplicate rows) it is true.
	 */
	boolean is_composite_all;

	/**
	 * The composite table itself.
	 */
	TableSelectExpression next_composite;

	/**
	 * Constructor.
	 */
	public TableSelectExpression() {
	}

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
	 * Chains a new composite function to this expression. For example, if this expression is a UNION ALL with another expression it would be set through this method.
	 */
	public void chainComposite(TableSelectExpression expression, String composite, boolean is_all) {
		this.next_composite = expression;
		composite = composite.toLowerCase();
		if (composite.equals("union")) {
			composite_function = CompositeTable.UNION;
		} else if (composite.equals("intersect")) {
			composite_function = CompositeTable.INTERSECT;
		} else if (composite.equals("except")) {
			composite_function = CompositeTable.EXCEPT;
		} else {
			throw new Error("Don't understand composite function '" + composite + "'");
		}
		is_composite_all = is_all;
	}

	/**
	 * Get columns
	 * 
	 * @return
	 */
	public ArrayList getColumns() {
		return columns;
	}

	/**
	 * Check if distinct
	 * 
	 * @return
	 */
	public boolean isDistinct() {
		return distinct;
	}

	/**
	 * Get from clause
	 * 
	 * @return
	 */
	public FromClause getFrom_clause() {
		return from_clause;
	}

	/**
	 * Get group by
	 * 
	 * @return
	 */
	public ArrayList getGroup_by() {
		return group_by;
	}

	/**
	 * Get where clause
	 * 
	 * @return
	 */
	public SearchExpression getWhere_clause() {
		return where_clause;
	}

	/**
	 * @return Returns the next_composite.
	 */
	public TableSelectExpression getNext_composite() {
		return next_composite;
	}

	/**
	 * @return Returns the composite_function.
	 */
	public int getComposite_function() {
		return composite_function;
	}

	/**
	 * @return Returns the is_composite_all.
	 */
	public boolean isIs_composite_all() {
		return is_composite_all;
	}

}