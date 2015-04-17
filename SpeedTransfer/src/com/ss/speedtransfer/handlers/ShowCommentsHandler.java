package com.ss.speedtransfer.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import com.ss.speedtransfer.ui.editor.querydef.SQLScratchPadEditor;
import com.ss.speedtransfer.ui.view.CommentsView;
import com.ss.speedtransfer.util.SSUtil;
import com.ss.speedtransfer.util.UIHelper;
import com.ss.speedtransfer.xml.editor.XMLFormEditor;
import com.ss.speedtransfer.xml.editor.XMLModel;


public class ShowCommentsHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().getSelection();
		IEditorPart editor = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().getActiveEditor();

		try {
			boolean keepTrying = true;
			if (selection instanceof IStructuredSelection) {
				IStructuredSelection sel = (IStructuredSelection) selection;
				Object selectedItem = sel.getFirstElement();
				if (selectedItem instanceof IFile) {
					showComments((IFile) selectedItem);
					keepTrying = false;
				}
			}

			if (keepTrying && editor != null) {
				XMLModel model = null;
				if (editor instanceof XMLFormEditor)
					model = ((XMLFormEditor) editor).getModel();
				else if (editor instanceof SQLScratchPadEditor)
					model = ((SQLScratchPadEditor) editor).getQueryDefinition();
				if (model != null)
					showComments(editor.getTitle(), model);
			}

		} catch (Exception e) {
			throw new ExecutionException(SSUtil.getMessage(e));
		}

		return null;
	}

	protected void showComments(IFile definitionFile) throws Exception {
		try {
			showComments(definitionFile.getName(), new XMLModel(definitionFile));
		} catch (Exception e) {
			UIHelper.instance().showErrorMsg("Error", SSUtil.getMessage(e));
		}
	}

	protected void showComments(String name, XMLModel model) throws Exception {
		try {
			CommentsView viewPart = (CommentsView) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(CommentsView.ID, null, IWorkbenchPage.VIEW_VISIBLE);
			viewPart.setName(name);
			viewPart.setModel(model);
		} catch (Exception e) {
			UIHelper.instance().showErrorMsg("Error", SSUtil.getMessage(e));
		}
	}

}
