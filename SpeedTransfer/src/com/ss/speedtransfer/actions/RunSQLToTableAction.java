package com.ss.speedtransfer.actions;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.HandlerUtil;

import com.ss.speedtransfer.model.QueryDefinition;
import com.ss.speedtransfer.ui.StyledEdit;
import com.ss.speedtransfer.ui.view.QueryResultView;
import com.ss.speedtransfer.util.SSUtil;
import com.ss.speedtransfer.util.UIHelper;


public class RunSQLToTableAction extends AbstractRunSQLAction {

	public RunSQLToTableAction() {
		this(null);

	}

	public RunSQLToTableAction(QueryDefinition queryDef) {
		super(queryDef);
	}

	public RunSQLToTableAction(QueryDefinition queryDef, StyledEdit sqlEdit) {
		super(queryDef, sqlEdit);

	}

	public void run() {
		ICommandService commandService = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
		Command command = commandService.getCommand("com.ss.speedtransfer.runQueryCommand");

		try {
			HandlerUtil.updateRadioState(command, "table");
		} catch (ExecutionException e) {
			UIHelper.instance().showErrorMsg("Error", "Unexpected error occurred. Error: " + SSUtil.getMessage(e));
		}

		super.run();

	}

	protected void showResult(String secondaryID) throws Exception {
		IViewReference viewRef = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findViewReference(QueryResultView.ID, secondaryID);
		if (viewRef != null) {
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().hideView(viewRef);
		}
		QueryResultView viewPart = (QueryResultView) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(QueryResultView.ID, secondaryID, IWorkbenchPage.VIEW_ACTIVATE);

		viewPart.getViewer().setInput(queryDef);
	}

}
