package com.ss.speedtransfer.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

import com.ss.speedtransfer.ui.view.QueryResultNatTableView;

public class ToggleShowSummaryRowHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchPart part = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().getActivePart();
		if (part instanceof QueryResultNatTableView)
			((QueryResultNatTableView) part).toggleSummaryRow();

		return null;
	}

}
