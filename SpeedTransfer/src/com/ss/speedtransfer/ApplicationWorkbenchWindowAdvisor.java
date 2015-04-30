package com.ss.speedtransfer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.activities.IWorkbenchActivitySupport;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.eclipse.ui.internal.WorkbenchWindow;

import com.ss.speedtransfer.ui.editor.querydef.QueryDefinitonEditor;
import com.ss.speedtransfer.ui.editor.querydef.SQLScratchPadEditor;
import com.ss.speedtransfer.util.LicenseManager;
import com.ss.speedtransfer.util.PartListener;
import com.ss.speedtransfer.util.UIHelper;

public class ApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {

	public ApplicationWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
		super(configurer);
	}

	@Override
	public ActionBarAdvisor createActionBarAdvisor(IActionBarConfigurer configurer) {
		return new ApplicationActionBarAdvisor(configurer);
	}

	@Override
	public void preWindowOpen() {
		IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
		// configurer.setInitialSize(new Point(400, 300));
		configurer.setShowCoolBar(true);
		configurer.setShowStatusLine(true);
		configurer.setShowMenuBar(false);
		configurer.setShowProgressIndicator(true);
		PlatformUI.getPreferenceStore().setValue(IWorkbenchPreferenceConstants.SHOW_TRADITIONAL_STYLE_TABS, false);

		if (LicenseManager.isStudioVersion())
			configurer.setTitle("SpeedTransfer Developer Studio");
		else
			configurer.setTitle("SpeedTransfer Browser");

		if (LicenseManager.isTrial())
			configurer.setTitle(configurer.getTitle() + ", Trial version " + LicenseManager.getDaysRemaining() + " days remaining.");
		else
			configurer.setTitle(configurer.getTitle() + ", License: " + LicenseManager.getLicense());

	}

	public void postWindowOpen() {
		IWorkbenchActivitySupport activitySupport = PlatformUI.getWorkbench().getActivitySupport();
		Set<String> enabledActivities = new HashSet<String>();
		activitySupport.setEnabledActivityIds(enabledActivities);

		PartListener.instance().start();

		if (LicenseManager.isBrowserVersion())
			closeOpenQueryEditors();

		PartListener.instance().maybeShowStartView(UIHelper.instance().getActivePage());

		// Completely hide the workbench menu
		WorkbenchWindow workbenchWin = (WorkbenchWindow) PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		MenuManager menuManager = workbenchWin.getMenuManager();
		IContributionItem[] items = menuManager.getItems();
		for (IContributionItem item : items) {
			item.setVisible(false);
		}

	}

	public void postWindowClose() {
		try {
			// save the full workspace before quit
			ResourcesPlugin.getWorkspace().save(true, null);
		} catch (final CoreException e) {
		}
	}

	protected void closeOpenQueryEditors() {
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IEditorReference[] editorRefs = page.getEditorReferences();
		List<IEditorReference> editorsToClose = new ArrayList<IEditorReference>();

		for (IEditorReference editorRef : editorRefs) {
			if (editorRef.getId().equalsIgnoreCase(QueryDefinitonEditor.ID) || editorRef.getId().equalsIgnoreCase(SQLScratchPadEditor.ID)) {
				editorsToClose.add(editorRef);
			}
		}

		if (editorsToClose.size() > 0)
			page.closeEditors(editorsToClose.toArray(new IEditorReference[0]), false);

	}

}
