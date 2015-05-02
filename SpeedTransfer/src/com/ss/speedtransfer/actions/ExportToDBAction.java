package com.ss.speedtransfer.actions;

import org.eclipse.jface.action.IAction;

import com.ss.speedtransfer.export.QueryToDatabaseExporter;
import com.ss.speedtransfer.model.QueryDefinition;
import com.ss.speedtransfer.util.ConnectionManager;
import com.ss.speedtransfer.util.LicenseManager;
import com.ss.speedtransfer.util.SSUtil;
import com.ss.speedtransfer.util.UIHelper;


public class ExportToDBAction extends AbstractExportAction {

	public ExportToDBAction(QueryDefinition queryDef) {
		super("Export to Database", IAction.AS_PUSH_BUTTON);
		this.queryDef = queryDef;
		setImageDescriptor(UIHelper.instance().getImageDescriptor("export_to_db.gif"));

	}

	public void run() {
		try {

			if (LicenseManager.isSelectOnly()) {
				UIHelper.instance().showErrorMsg("Illegal export", "Export to database is not permitted. Query can not be executed.");
				return;
			}

			lastUsedAction = this;
			ConnectionManager.checkConnection(queryDef);
			QueryToDatabaseExporter exporter = new QueryToDatabaseExporter(queryDef);
			exporter.export();
		} catch (Exception e) {
			UIHelper.instance().showErrorMsg("Error", SSUtil.getMessage(e));
		}
	}

}
