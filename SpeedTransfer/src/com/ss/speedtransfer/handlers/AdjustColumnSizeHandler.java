package com.ss.speedtransfer.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

import com.ss.speedtransfer.ui.view.QueryResultView;
import com.ss.speedtransfer.ui.view.QueryResultView;

public class AdjustColumnSizeHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchPart part = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().getActivePart();
		if (part instanceof QueryResultView)
			((QueryResultView) part).autoSizeColumns();
		if (part instanceof QueryResultView)
			((QueryResultView) part).autoSizeColumns();

		return null;
	}

}
