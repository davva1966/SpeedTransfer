// {{CopyrightNotice}}

package com.ss.speedtransfer.util.parser.sql;

import java.util.ArrayList;
import java.util.Collection;

/**
 * The <code>FromClause</code> class
 */
public final class FromClause {

	static final long serialVersionUID = 565726601314503609L;

	/**
	 * The JoiningSet object that we have created to represent the joins in this FROM clause.
	 */
	private JoiningSet join_set = new JoiningSet();

	/**
	 * A list of all FromTableDef objects in this clause in order of when they were specified.
	 */
	private ArrayList def_list = new ArrayList();

	/**
	 * A list of all table names in this from clause.
	 */
	private ArrayList all_table_names = new ArrayList();

	/**
	 * An id used for making unique names for anonymous inner selects.
	 */
	private int table_key = 0;

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
	 * Creates a new unique key string.
	 */
	private String createNewKey() {
		++table_key;
		return Integer.toString(table_key);
	}

	private void addTableDef(String table_name, FromTableDef def) {
		if (table_name != null) {
			if (all_table_names.contains(table_name)) {
				throw new Error("Duplicate table name in FROM clause: " + table_name);
			}
			all_table_names.add(table_name);
		}
		// Create a new unique key for this table
		String key = createNewKey();
		def.setUniqueKey(key);
		// Add the table key to the join set
		join_set.addTable(new TableName(key));
		// Add to the alias def map
		def_list.add(def);
	}

	/**
	 * Adds a table name to this FROM clause. Note that the given name may be a dot deliminated ref such as (schema.table_name).
	 */
	public void addTable(String table_name) {
		addTableDef(table_name, new FromTableDef(table_name));
	}

	/**
	 * Adds a table name + alias to this FROM clause.
	 */
	public void addTable(String table_name, String table_alias) {
		addTableDef(table_alias, new FromTableDef(table_name, table_alias));
	}

	/**
	 * A generic form of a table declaration. If any parameters are 'null' it means the information is not available.
	 */
	public void addTableDeclaration(String table_name, TableSelectExpression select, String table_alias) {
		// This is an inner select in the FROM clause
		if (table_name == null && select != null) {
			if (table_alias == null) {
				addTableDef(null, new FromTableDef(select));
			} else {
				addTableDef(table_alias, new FromTableDef(select, table_alias));
			}
		}
		// This is a standard table reference in the FROM clause
		else if (table_name != null && select == null) {
			if (table_alias == null) {
				addTable(table_name);
			} else {
				addTable(table_name, table_alias);
			}
		}
		// Error
		else {
			throw new Error("Unvalid declaration parameters.");
		}

	}

	/**
	 * Adds a Join to the from clause. 'type' must be a join type as defined in JoiningSet.
	 */
	public void addJoin(int type) {
		// System.out.println("Add Join: " + type);
		join_set.addJoin(type);
	}

	/**
	 * Hack, add a joining type to the previous entry from the end. This is an artifact of how joins are parsed.
	 */
	public void addPreviousJoin(int type, Expression on_expression) {
		join_set.addPreviousJoin(type, on_expression);
	}

	/**
	 * Adds a Join to the from clause. 'type' must be a join type as defined in JoiningSet, and expression represents the ON condition.
	 */
	public void addJoin(int type, Expression on_expression) {
		join_set.addJoin(type, on_expression);
	}

	/**
	 * Returns the JoiningSet object for the FROM clause.
	 */
	public JoiningSet getJoinSet() {
		return join_set;
	}

	/**
	 * Returns the type of join after table 'n' in the set of tables in the from clause. Returns, JoiningSet.INNER_JOIN, JoiningSet.FULL_OUTER_JOIN, etc.
	 */
	public int getJoinType(int n) {
		return getJoinSet().getJoinType(n);
	}

	/**
	 * Returns the ON Expression for the type of join after table 'n' in the set.
	 */
	public Expression getOnExpression(int n) {
		return getJoinSet().getOnExpression(n);
	}

	/**
	 * Returns a Set of FromTableDef objects that represent all the tables that are in this from clause.
	 */
	public Collection allTables() {
		return def_list;
	}

}
