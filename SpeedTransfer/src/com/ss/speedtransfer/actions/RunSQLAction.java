package com.ss.speedtransfer.actions;

import java.util.List;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.handlers.RadioState;

import com.ss.speedtransfer.model.QueryDefinition;
import com.ss.speedtransfer.ui.ActionDropDownMenuCreator;
import com.ss.speedtransfer.util.SSUtil;
import com.ss.speedtransfer.util.UIHelper;


public class RunSQLAction extends Action {

	protected QueryDefinition queryDef;
	protected String type = null;
	protected boolean useLast = false;

	public RunSQLAction(List<IAction> exportActions, QueryDefinition queryDef) {
		super("Run SQL");
		this.queryDef = queryDef;
		setImageDescriptor(UIHelper.instance().getImageDescriptor("execute.gif"));

		ActionDropDownMenuCreator runMenuCreator = new ActionDropDownMenuCreator();
		for (IAction action : exportActions)
			runMenuCreator.addAction(action);

		setMenuCreator(runMenuCreator);
		useLast = true;

	}

	public RunSQLAction(QueryDefinition queryDef, String type) {
		super("", IAction.AS_RADIO_BUTTON);
		this.queryDef = queryDef;
		this.type = type;
		setImageDescriptor(UIHelper.instance().getImageDescriptor("execute.gif"));

	}

	public String getType() {
		return type;
	}

	public void run() {
		ICommandService commandService = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
		Command command = commandService.getCommand("com.ss.speedtransfer.runQueryCommand");

		if (type != null && useLast == false) {
			try {
				HandlerUtil.updateRadioState(command, type);
			} catch (ExecutionException e) {
				UIHelper.instance().showErrorMsg("Error", "Unexpected error occurred. Error: " + SSUtil.getMessage(e));
			}
		} else {
			type = (String) command.getState(RadioState.STATE_ID).getValue();
		}

		IAction action;
		if (type.equals("excel"))
			action = new RunSQLToExcelAction(queryDef);
		else
			action = new RunSQLToTableAction(queryDef);

		action.run();

	}
}
