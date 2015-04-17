package com.ss.speedtransfer.actions;

import org.eclipse.jface.action.IAction;

import com.ss.speedtransfer.export.QueryToCSVExporter;
import com.ss.speedtransfer.model.QueryDefinition;
import com.ss.speedtransfer.util.ConnectionManager;
import com.ss.speedtransfer.util.SSUtil;
import com.ss.speedtransfer.util.UIHelper;


public class ExportToCSVAction extends AbstractExportAction {

	public ExportToCSVAction(QueryDefinition queryDef) {
		super("Export to CSV", IAction.AS_PUSH_BUTTON);
		this.queryDef = queryDef;
		setImageDescriptor(UIHelper.instance().getImageDescriptor("csv.gif"));

	}

	public void run() {
		try {

			if (SSUtil.validateSelectOnly(queryDef) == false)
				return;

			lastUsedAction = this;
			ConnectionManager.checkConnection(queryDef);
			QueryToCSVExporter exporter = new QueryToCSVExporter(queryDef);
			exporter.export();
		} catch (Exception e) {
			UIHelper.instance().showErrorMsg("Error", SSUtil.getMessage(e));
		}
	}

}
