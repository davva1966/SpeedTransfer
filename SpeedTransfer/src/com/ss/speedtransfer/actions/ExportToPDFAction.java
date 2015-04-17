package com.ss.speedtransfer.actions;

import org.eclipse.jface.action.IAction;

import com.ss.speedtransfer.export.QueryToPDFExporter;
import com.ss.speedtransfer.model.QueryDefinition;
import com.ss.speedtransfer.util.ConnectionManager;
import com.ss.speedtransfer.util.SSUtil;
import com.ss.speedtransfer.util.UIHelper;


public class ExportToPDFAction extends AbstractExportAction {

	public ExportToPDFAction(QueryDefinition queryDef) {
		super("Export to PDF", IAction.AS_PUSH_BUTTON);
		this.queryDef = queryDef;
		setImageDescriptor(UIHelper.instance().getImageDescriptor("pdf.gif"));

	}

	public void run() {
		try {

			if (SSUtil.validateSelectOnly(queryDef) == false)
				return;

			lastUsedAction = this;
			ConnectionManager.checkConnection(queryDef);
			QueryToPDFExporter exporter = new QueryToPDFExporter(queryDef);
			exporter.export();
		} catch (Exception e) {
			UIHelper.instance().showErrorMsg("Error", SSUtil.getMessage(e));
		}

	}

}
