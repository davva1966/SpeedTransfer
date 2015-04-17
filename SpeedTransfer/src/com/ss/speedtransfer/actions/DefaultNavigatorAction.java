package com.ss.speedtransfer.actions;

import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.actions.OpenFileAction;

import com.ss.speedtransfer.model.QueryDefinition;
import com.ss.speedtransfer.util.SSUtil;
import com.ss.speedtransfer.util.LicenseManager;
import com.ss.speedtransfer.util.UIHelper;


public class DefaultNavigatorAction extends OpenFileAction {

	public DefaultNavigatorAction(IWorkbenchPage page) {
		this(page, null);
	}

	public DefaultNavigatorAction(IWorkbenchPage page, IEditorDescriptor descriptor) {
		super(page, descriptor);

	}

	public void run() {
		Iterator itr = getSelectedResources().iterator();
		while (itr.hasNext()) {
			IResource resource = (IResource) itr.next();
			if (resource instanceof IFile) {
				if (LicenseManager.isBrowserVersion() && QueryDefinition.isQueryDefinitionFile(resource))
					runQuery((IFile) resource);
				else
					super.run();
			}
		}
	}

	protected void runQuery(IFile file) {
		try {
			RunSQLToTableAction action = new RunSQLToTableAction(new QueryDefinition(file));
			action.run();
		} catch (Exception e) {
			UIHelper.instance().showErrorMsg("Error", SSUtil.getMessage(e));
		}
	}

}
