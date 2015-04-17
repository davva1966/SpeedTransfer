package com.ss.speedtransfer.xml.editor;

public interface XMLModelListener {

	public final String ATTR_CHANGED = "__attr_changed";
	public final String STRUCTURE_CHANGED = "__struct_changed";
	public final String REPLACED = "__replaced";
	

	void modelChanged(Object[] objects, String type, String property);
}