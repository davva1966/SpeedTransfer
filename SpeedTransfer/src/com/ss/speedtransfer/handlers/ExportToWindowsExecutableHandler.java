package com.ss.speedtransfer.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import com.ss.speedtransfer.export.WindowsExecutableExporter;


public class ExportToWindowsExecutableHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().getSelection();
		if (selection instanceof IStructuredSelection) {

			IStructuredSelection sel = (IStructuredSelection) selection;
			Object selectedItem = sel.getFirstElement();
			if (selectedItem instanceof IFile) {
				WindowsExecutableExporter exporter = new WindowsExecutableExporter();
				exporter.export((IFile) selectedItem);
			}

		}

		return null;
	}

}
