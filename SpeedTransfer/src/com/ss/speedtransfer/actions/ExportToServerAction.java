package com.ss.speedtransfer.actions;

import org.eclipse.jface.action.IAction;

import com.ss.speedtransfer.export.ServerExporter;
import com.ss.speedtransfer.model.QueryDefinition;
import com.ss.speedtransfer.util.SSUtil;
import com.ss.speedtransfer.util.UIHelper;


public class ExportToServerAction extends AbstractExportAction {
	
	protected Object[] itemsToExport;

	public ExportToServerAction(QueryDefinition queryDef) {
		super("Export to server", IAction.AS_PUSH_BUTTON);
		this.itemsToExport = new Object[1];
		this.itemsToExport[0] = queryDef;
		setImageDescriptor(UIHelper.instance().getImageDescriptor("run_on_server.gif"));

	}
	
	public ExportToServerAction(Object[] itemsToExport) {
		super("Export to server", IAction.AS_PUSH_BUTTON);
		this.itemsToExport = itemsToExport;
		setImageDescriptor(UIHelper.instance().getImageDescriptor("run_on_server.gif"));

	}

	public void run() {
		try {
			ServerExporter exporter = new ServerExporter(itemsToExport);
			exporter.export();
		} catch (Exception e) {
			UIHelper.instance().showErrorMsg("Error", SSUtil.getMessage(e));
		}
	}

}
