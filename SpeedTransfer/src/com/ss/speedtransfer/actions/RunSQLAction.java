package com.ss.speedtransfer.actions;

import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import com.ss.speedtransfer.model.QueryDefinition;
import com.ss.speedtransfer.ui.StyledEdit;
import com.ss.speedtransfer.ui.view.QueryResultView;

public class RunSQLAction extends AbstractRunSQLAction {

	public RunSQLAction() {
		this(null);

	}

	public RunSQLAction(QueryDefinition queryDef) {
		super(queryDef);
	}

	public RunSQLAction(QueryDefinition queryDef, StyledEdit sqlEdit) {
		super(queryDef, sqlEdit);

	}
	
	protected void showResult(String secondaryID) throws Exception {
		IViewReference viewRef = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findViewReference(QueryResultView.ID, secondaryID);
		if (viewRef != null) {
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().hideView(viewRef);
		}
		QueryResultView viewPart = (QueryResultView) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(QueryResultView.ID, secondaryID, IWorkbenchPage.VIEW_ACTIVATE);
		viewPart.setQueryDefinition(queryDef);

	}

}
