package com.ss.speedtransfer.actions;

import java.util.List;

import org.eclipse.jface.action.IAction;

import com.ss.speedtransfer.model.QueryDefinition;
import com.ss.speedtransfer.ui.ActionDropDownMenuCreator;
import com.ss.speedtransfer.util.UIHelper;


public class ExportSQLAction extends AbstractExportAction {

	public ExportSQLAction(List<IAction> exportActions, QueryDefinition queryDef) {
		super("Export SQL");
		this.queryDef = queryDef;
		setImageDescriptor(UIHelper.instance().getImageDescriptor("export.gif"));
		setToolTipText("Export data");

		ActionDropDownMenuCreator exportMenuCreator = new ActionDropDownMenuCreator();
		for (IAction action : exportActions)
			exportMenuCreator.addAction(action);

		setMenuCreator(exportMenuCreator);

	}

	public void run() {
		if (lastUsedAction != null) {
			lastUsedAction.setQueryDefinition(queryDef);
			lastUsedAction.run();
		}

	}

}
