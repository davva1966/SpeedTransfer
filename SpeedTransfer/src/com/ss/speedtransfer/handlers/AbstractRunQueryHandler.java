package com.ss.speedtransfer.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;

import com.ss.speedtransfer.model.QueryDefinition;
import com.ss.speedtransfer.ui.editor.querydef.QueryDefinitonEditor;
import com.ss.speedtransfer.ui.editor.querydef.SQLScratchPadEditor;
import com.ss.speedtransfer.util.SSUtil;
import com.ss.speedtransfer.util.UIHelper;


public abstract class AbstractRunQueryHandler extends AbstractHandler {

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
					runQuery((IFile) selectedItem);
					keepTrying = false;
				}
			}

			if (keepTrying && editor != null) {
				QueryDefinition queryDef = null;
				if (editor instanceof QueryDefinitonEditor)
					queryDef = ((QueryDefinitonEditor) editor).getQueryDefinition();
				if (editor instanceof SQLScratchPadEditor)
					queryDef = ((SQLScratchPadEditor) editor).getQueryDefinition();
				if (queryDef != null)
					runQuery(queryDef);
			}

		} catch (Exception e) {
			throw new ExecutionException(SSUtil.getMessage(e));
		}

		return null;
	}

	protected void runQuery(IFile queryDefinitionFile) throws Exception {
		try {
			runQuery(QueryDefinition.getModelFor(queryDefinitionFile));
		} catch (Exception e) {
			UIHelper.instance().showErrorMsg("Error", SSUtil.getMessage(e));
		}
	}

	protected abstract void runQuery(QueryDefinition queryDef) throws Exception;

}
