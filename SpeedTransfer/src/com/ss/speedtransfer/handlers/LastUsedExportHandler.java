package com.ss.speedtransfer.handlers;

import org.eclipse.core.resources.IFile;

public class LastUsedExportHandler extends AbstractExportHandler {

	protected void export(IFile queryDefinitionFile) {

		if (lastUsedHandler == null)
			return;
		if (lastUsedHandler != this)
			lastUsedHandler.export(queryDefinitionFile);
	}

	protected boolean isProperHandler() {
		return false;

	}
}
