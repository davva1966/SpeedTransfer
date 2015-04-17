package com.ss.speedtransfer.handlers;

import org.eclipse.core.resources.IFile;

import com.ss.speedtransfer.actions.ExportToExcelAction;
import com.ss.speedtransfer.model.QueryDefinition;
import com.ss.speedtransfer.util.SSUtil;
import com.ss.speedtransfer.util.UIHelper;


public class ExportToExcelHandler extends AbstractExportHandler {

	protected void export(IFile queryDefinitionFile) {
		try {
			ExportToExcelAction action = new ExportToExcelAction(QueryDefinition.getModelFor(queryDefinitionFile));
			action.run();
		} catch (Exception e) {
			UIHelper.instance().showErrorMsg("Error", SSUtil.getMessage(e));
		}

	}

}
