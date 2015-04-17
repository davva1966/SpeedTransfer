package com.ss.speedtransfer.actions;

import org.eclipse.jface.action.IAction;

import com.ss.speedtransfer.export.QueryToExcelExporter;
import com.ss.speedtransfer.model.QueryDefinition;
import com.ss.speedtransfer.util.ConnectionManager;
import com.ss.speedtransfer.util.SSUtil;
import com.ss.speedtransfer.util.UIHelper;


public class ExportToExcelAction extends AbstractExportAction {

	public ExportToExcelAction(QueryDefinition queryDef) {
		super("Export to Excel", IAction.AS_PUSH_BUTTON);
		this.queryDef = queryDef;
		setImageDescriptor(UIHelper.instance().getImageDescriptor("excel.gif"));

	}

	public void run() {
		try {

			if (SSUtil.validateSelectOnly(queryDef) == false)
				return;

			lastUsedAction = this;
			ConnectionManager.checkConnection(queryDef);
			QueryToExcelExporter exporter = new QueryToExcelExporter(queryDef);
			exporter.export();
		} catch (Exception e) {
			UIHelper.instance().showErrorMsg("Error", SSUtil.getMessage(e));
		}

	}

}
