package com.ss.speedtransfer.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

import com.ss.speedtransfer.model.QueryDefinition;


public abstract class AbstractExportAction extends Action {

	protected static AbstractExportAction lastUsedAction = null;
	protected QueryDefinition queryDef;

	public AbstractExportAction(String text) {
		super(text);
	}

	public AbstractExportAction() {
		super();
	}

	public AbstractExportAction(String text, ImageDescriptor image) {
		super(text, image);
	}

	public AbstractExportAction(String text, int style) {
		super(text, style);
	}

	public void setQueryDefinition(QueryDefinition queryDef) {
		this.queryDef = queryDef;
	}


}
