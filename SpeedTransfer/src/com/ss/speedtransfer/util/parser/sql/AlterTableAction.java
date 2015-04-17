// {{CopyrightNotice}}

package com.ss.speedtransfer.util.parser.sql;

import java.util.ArrayList;

/**
 * The <code>AlterTableAction</code> class
 */
public final class AlterTableAction {

	/**
	 * Element parameters to do with the action.
	 */
	private ArrayList elements;

	/**
	 * The action to perform.
	 */
	private String action;

	/**
	 * Constructor.
	 */
	public AlterTableAction() {
		elements = new ArrayList();
	}

	/**
	 * Set the action to perform.
	 */
	public void setAction(String str) {
		this.action = str;
	}

	/**
	 * Adds a parameter to this action.
	 */
	public void addElement(Object ob) {
		elements.add(ob);
	}

	/**
	 * Returns the name of this action.
	 */
	public String getAction() {
		return action;
	}

	/**
	 * Returns the ArrayList that represents the parameters of this action.
	 */
	public ArrayList getElements() {
		return elements;
	}

	/**
	 * Returns element 'n'.
	 */
	public Object getElement(int n) {
		return elements.get(n);
	}

}