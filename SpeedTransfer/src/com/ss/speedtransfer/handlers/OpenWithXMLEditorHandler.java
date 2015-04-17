package com.ss.speedtransfer.handlers;

import java.util.Iterator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.part.FileEditorInput;

import com.ss.speedtransfer.util.SSUtil;


@SuppressWarnings("restriction")
public class OpenWithXMLEditorHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().getSelection();
		if (selection instanceof IStructuredSelection) {

			IStructuredSelection sel = (IStructuredSelection) selection;
			Iterator<Object> iter = sel.iterator();
			while (iter.hasNext()) {
				Object selectedItem = iter.next();
				if (selectedItem instanceof IFile) {
					IWorkbenchPage page = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage();
					try {
						page.openEditor(new FileEditorInput((IFile) selectedItem), "org.eclipse.wst.xml.ui.internal.tabletree.XMLMultiPageEditorPart");
					} catch (Exception e) {
						throw new ExecutionException(SSUtil.getMessage(e));
					}
				}
			}

		}

		return null;
	}

}
