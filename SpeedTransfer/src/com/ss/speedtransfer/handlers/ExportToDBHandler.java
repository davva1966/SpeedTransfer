package com.ss.speedtransfer.handlers;

import org.eclipse.core.resources.IFile;

import com.ss.speedtransfer.actions.ExportToDBAction;
import com.ss.speedtransfer.model.QueryDefinition;
import com.ss.speedtransfer.util.SSUtil;
import com.ss.speedtransfer.util.UIHelper;


public class ExportToDBHandler extends AbstractExportHandler {

	protected void export(IFile queryDefinitionFile) {
		try {
			ExportToDBAction action = new ExportToDBAction(QueryDefinition.getModelFor(queryDefinitionFile));
			action.run();
		} catch (Exception e) {
			UIHelper.instance().showErrorMsg("Error", SSUtil.getMessage(e));
		}

	}

}
