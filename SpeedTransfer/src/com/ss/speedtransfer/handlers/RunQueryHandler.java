package com.ss.speedtransfer.handlers;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.handlers.RadioState;

import com.ss.speedtransfer.actions.RunSQLToExcelAction;
import com.ss.speedtransfer.actions.RunSQLToTableAction;
import com.ss.speedtransfer.model.QueryDefinition;


public class RunQueryHandler extends AbstractRunQueryHandler {

	protected String type = null;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ICommandService commandService = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
		Command command = commandService.getCommand("com.ss.speedtransfer.runQueryCommand");

		type = event.getParameter(RadioState.PARAMETER_ID);
		if (type != null)
			HandlerUtil.updateRadioState(command, type);
		else
			type = (String) event.getCommand().getState(RadioState.STATE_ID).getValue();

		super.execute(event);

		return null;
	}

	protected void runQuery(QueryDefinition queryDef) throws Exception {

		IAction action = new RunSQLToTableAction(queryDef);
		if (type.equals("excel"))
			action = new RunSQLToExcelAction(queryDef);
		else
			action = new RunSQLToTableAction(queryDef);

		action.run();

	}
}
