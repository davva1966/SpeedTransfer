package com.ss.speedtransfer.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import com.ss.speedtransfer.model.DBConnection;
import com.ss.speedtransfer.util.DefaultDBManager;
import com.ss.speedtransfer.util.SSUtil;

public class SetDefaultDBHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().getSelection();

		try {
			if (selection instanceof IStructuredSelection) {
				IStructuredSelection sel = (IStructuredSelection) selection;
				Object selectedItem = sel.getFirstElement();
				if (DBConnection.isDBConnectionFile(selectedItem)) {
					DefaultDBManager.instance().makeDefault((IFile) selectedItem);
				}
			}

		} catch (Exception e) {
			throw new ExecutionException(SSUtil.getMessage(e));
		}

		return null;
	}

}
