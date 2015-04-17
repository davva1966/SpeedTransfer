package com.ss.speedtransfer.model;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

import com.ss.speedtransfer.util.UIHelper;


public class SQLScratchPadInput implements IEditorInput {

	protected String dbConnectionFile = "";

	public String getDBConnectionFile() {
		return dbConnectionFile;
	}

	public void setDBConnectionFile(String dbConnectionFile) {
		this.dbConnectionFile = dbConnectionFile;
	}

	@Override
	public Object getAdapter(Class adapter) {
		return null;
	}

	@Override
	public String getToolTipText() {
		return "SQL Scratch Pad";
	}

	@Override
	public IPersistableElement getPersistable() {
		return null;
	}

	@Override
	public String getName() {
		return "SQL Scratch Pad";
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return UIHelper.instance().getImageDescriptor("sql_editor.gif");
	}

	@Override
	public boolean exists() {
		return false;
	}

}
