package com.ss.speedtransfer;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionConstants;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonMenuConstants;
import org.eclipse.ui.navigator.ICommonViewerSite;
import org.eclipse.ui.navigator.ICommonViewerWorkbenchSite;

import com.ss.speedtransfer.actions.DefaultNavigatorAction;
import com.ss.speedtransfer.util.LicenseManager;


public class NavigatorActionProvider extends CommonActionProvider {

	DefaultNavigatorAction runAction = null;

	public NavigatorActionProvider() {
		super();
	}

	public void init(ICommonActionExtensionSite aSite) {
		ICommonViewerSite viewSite = aSite.getViewSite();
		if (viewSite instanceof ICommonViewerWorkbenchSite) {
			ICommonViewerWorkbenchSite workbenchSite = (ICommonViewerWorkbenchSite) viewSite;
			ISelection selection = viewSite.getSelectionProvider().getSelection();

			runAction = new DefaultNavigatorAction(workbenchSite.getPage());
			viewSite.getSelectionProvider().addSelectionChangedListener(runAction);
			if (selection instanceof IStructuredSelection)
				runAction.selectionChanged((IStructuredSelection) selection);

		}
	}

	public void fillActionBars(IActionBars actionBars) {
		if (runAction.isEnabled())
			actionBars.setGlobalActionHandler(ICommonActionConstants.OPEN, runAction);
	}

	public void fillContextMenu(IMenuManager menu) {
		if (LicenseManager.isStudioVersion()) {
			if (runAction.isEnabled())
				menu.appendToGroup(ICommonMenuConstants.GROUP_OPEN, runAction);
		}

	}

}
