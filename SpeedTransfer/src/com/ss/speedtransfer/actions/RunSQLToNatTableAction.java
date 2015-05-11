package com.ss.speedtransfer.actions;

import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import com.ss.speedtransfer.model.QueryDefinition;
import com.ss.speedtransfer.ui.StyledEdit;
import com.ss.speedtransfer.ui.view.QueryResultNatTableView;

public class RunSQLToNatTableAction extends AbstractRunSQLAction {

	public RunSQLToNatTableAction() {
		this(null);

	}

	public RunSQLToNatTableAction(QueryDefinition queryDef) {
		super(queryDef);
	}

	public RunSQLToNatTableAction(QueryDefinition queryDef, StyledEdit sqlEdit) {
		super(queryDef, sqlEdit);

	}
	
	protected void showResult(String secondaryID) throws Exception {
		IViewReference viewRef = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findViewReference(QueryResultNatTableView.ID, secondaryID);
		if (viewRef != null) {
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().hideView(viewRef);
		}
		QueryResultNatTableView viewPart = (QueryResultNatTableView) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(QueryResultNatTableView.ID, secondaryID, IWorkbenchPage.VIEW_ACTIVATE);
		viewPart.setQueryDefinition(queryDef);

	}

}
