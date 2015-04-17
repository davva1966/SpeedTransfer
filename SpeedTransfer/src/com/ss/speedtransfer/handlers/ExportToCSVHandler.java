package com.ss.speedtransfer.handlers;

import org.eclipse.core.resources.IFile;

import com.ss.speedtransfer.actions.ExportToCSVAction;
import com.ss.speedtransfer.model.QueryDefinition;
import com.ss.speedtransfer.util.SSUtil;
import com.ss.speedtransfer.util.UIHelper;


public class ExportToCSVHandler extends AbstractExportHandler {

	protected void export(IFile queryDefinitionFile) {
		try {
			ExportToCSVAction action = new ExportToCSVAction(QueryDefinition.getModelFor(queryDefinitionFile));
			action.run();
		} catch (Exception e) {
			UIHelper.instance().showErrorMsg("Error", SSUtil.getMessage(e));
		}

	}

}
