package com.ss.speedtransfer.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.actions.RefreshAction;
import org.eclipse.ui.handlers.HandlerUtil;

public class RefreshNavigatorHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		RefreshAction refreshAction = new RefreshAction(HandlerUtil.getActiveWorkbenchWindow(event));
		refreshAction.run();

		return null;
	}
}
