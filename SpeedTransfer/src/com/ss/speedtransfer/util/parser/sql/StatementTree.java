// {{CopyrightNotice}}

package com.ss.speedtransfer.util.parser.sql;

import java.util.HashMap;
import java.util.Map;

/**
 * The <code>StatementTree</code> class
 */
public class StatementTree {

	/** The map with child statements */
	protected Map childMap = new HashMap();

	/** The type of the tree statement */
	protected String type = null;

	/**
	 * Initializes a newly created <code>StatementTree</code>
	 * 
	 * @param type
	 *            - the sql statement type
	 */
	public StatementTree(String type) {

		this.type = type;
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
	 * Put a new entry
	 * 
	 * @param name
	 *            - name of the entry
	 * @param obj
	 *            - the object
	 */
	public void putObject(String name, Object obj) {
		childMap.put(name, obj);
	}

	/**
	 * Put a new entry
	 * 
	 * @param name
	 *            - name of the entry
	 * @param obj
	 *            - the object
	 */
	public void putInt(String name, int obj) {
		childMap.put(name, new Integer(obj));
	}

	/**
	 * Put a new entry
	 * 
	 * @param name
	 *            - name of the entry
	 * @param obj
	 *            - the object
	 */
	public void putBoolean(String name, boolean obj) {
		childMap.put(name, new Boolean(obj));
	}

	/**
	 * Get object
	 * 
	 * @param name
	 * @return
	 */
	public Object getObject(String name) {
		return childMap.get(name);
	}

	/**
	 * Get TableSelectExpression
	 * 
	 * @return TableSelectExpression
	 */
	public TableSelectExpression getTableSelectExpression() {
		TableSelectExpression exp = (TableSelectExpression) getObject("table_expression");
		if (exp == null) {
			Object o = getObject("select_expression");
			if (o instanceof TableSelectExpression)
				exp = (TableSelectExpression) o;
		}
		return exp;
	}

	/**
	 * Get TableSelectExpression
	 * 
	 * @return TableSelectExpression
	 */
	public StatementTree getStatementChild() {
		return (StatementTree) getObject("select");
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}
}