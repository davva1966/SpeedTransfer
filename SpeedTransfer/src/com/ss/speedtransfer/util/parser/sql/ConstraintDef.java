// {{CopyrightNotice}}

package com.ss.speedtransfer.util.parser.sql;

import java.util.ArrayList;

import com.ss.speedtransfer.util.SSUtil;


/**
 * 
 * The <code>ConstraintDef</code> class
 */
public final class ConstraintDef {

	/**
	 * A PRIMARY_KEY constraint. With this constraint, the 'column_list' list contains the names of the columns in this table that are defined as the primary key. There may only be one primary key
	 * constraint per table.
	 */
	public static final int PRIMARY_KEY = 1;

	/**
	 * A UNIQUE constraint. With this constraint, the 'column_list' list contains the names of the columns in this table that must be unique.
	 */
	public static final int UNIQUE = 2;

	/**
	 * A FOREIGN_KEY constraint. With this constraint, the 'table_name' string contains the name of the table that this is a foreign key for, the 'column_list' list contains the list of foreign key
	 * columns, and 'column_list2' optionally contains the referenced columns.
	 */
	public static final int FOREIGN_KEY = 3;

	/**
	 * A CHECK constraint. With this constraint, the 'expression' object contains the expression that must evaluate to true when adding a column to the table.
	 */
	public static final int CHECK = 4;

	// The type of constraint (from types in DataTableConstraintDef)
	int type;

	// The name of the constraint or null if the constraint has no name (in
	// which case it must be given an auto generated unique name at some point).
	String name;

	// The Check Expression
	Expression check_expression;
	// The serializable plain check expression as originally parsed
	Expression original_check_expression;

	// The first column list
	ArrayList column_list;

	// The second column list
	ArrayList column_list2;

	// The name of the table if referenced.
	String reference_table_name;

	// The foreign key update rule
	String update_rule;

	// The foreign key delete rule
	String delete_rule;

	public ConstraintDef() {
	}

	/**
	 * Sets the name of the constraint.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Sets object up for a primary key constraint.
	 */
	public void setPrimaryKey(ArrayList list) {
		type = PRIMARY_KEY;
		column_list = list;
	}

	/**
	 * Sets object up for a unique constraint.
	 */
	public void setUnique(ArrayList list) {
		type = UNIQUE;
		column_list = list;
	}

	/**
	 * Sets object up for a check constraint.
	 */
	public void setCheck(Expression exp) {
		type = CHECK;
		check_expression = exp;
		try {
			original_check_expression = (Expression) exp.clone();
		} catch (CloneNotSupportedException e) {
			throw new Error(SSUtil.getMessage(e));
		}
	}

	/**
	 * Sets object up for foreign key reference.
	 */
	public void setForeignKey(String ref_table, ArrayList col_list, ArrayList ref_col_list, String delete_rule, String update_rule) {
		type = FOREIGN_KEY;
		reference_table_name = ref_table;
		column_list = col_list;
		column_list2 = ref_col_list;
		this.delete_rule = delete_rule;
		this.update_rule = update_rule;

		// System.out.println("ConstraintDef setting rules: " + delete_rule + ", " + update_rule);
	}

	/**
	 * Returns the first column list as a string array.
	 */
	public String[] getColumnList() {
		return (String[]) column_list.toArray(new String[column_list.size()]);
	}

	/**
	 * Returns the first column list as a string array.
	 */
	public String[] getColumnList2() {
		return (String[]) column_list2.toArray(new String[column_list2.size()]);
	}

	/**
	 * Returns the delete rule if this is a foreign key reference.
	 */
	public String getDeleteRule() {
		return delete_rule;
	}

	/**
	 * Returns the update rule if this is a foreign key reference.
	 */
	public String getUpdateRule() {
		return update_rule;
	}

	/**
	 * Sets that this constraint is initially deferred.
	 */
	public void setInitiallyDeferred() {

	}

	/**
	 * Sets that this constraint is not deferrable.
	 */
	public void setNotDeferrable() {

	}

}
