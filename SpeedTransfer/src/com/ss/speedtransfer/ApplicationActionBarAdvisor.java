package com.ss.speedtransfer;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IExecutionListener;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.StatusLineContributionItem;
import org.eclipse.jface.action.ToolBarContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.ide.IDE;

import com.ss.speedtransfer.actions.SwitchWorkspaceAction;
import com.ss.speedtransfer.handlers.ToggleLazyLoadHandler;
import com.ss.speedtransfer.model.SQLScratchPadInput;
import com.ss.speedtransfer.ui.editor.querydef.SQLScratchPadEditor;
import com.ss.speedtransfer.util.DefaultDBManager;
import com.ss.speedtransfer.util.LicenseManager;
import com.ss.speedtransfer.util.SettingsManager;
import com.ss.speedtransfer.util.UIHelper;


public class ApplicationActionBarAdvisor extends ActionBarAdvisor {

	Action toggleLazyLoad;

	public ApplicationActionBarAdvisor(IActionBarConfigurer configurer) {
		super(configurer);
	}

	@Override
	protected void makeActions(IWorkbenchWindow window) {

		IWorkbenchAction deleteAction = ActionFactory.DELETE.create(window);
		IWorkbenchAction cutAction = ActionFactory.CUT.create(window);
		IWorkbenchAction copyAction = ActionFactory.COPY.create(window);
		IWorkbenchAction pasteAction = ActionFactory.PASTE.create(window);
		IWorkbenchAction saveAction = ActionFactory.SAVE.create(window);
		IWorkbenchAction saveAsAction = ActionFactory.SAVE_AS.create(window);
		IWorkbenchAction undoAction = ActionFactory.UNDO.create(window);
		IWorkbenchAction redoAction = ActionFactory.REDO.create(window);
		IWorkbenchAction printAction = ActionFactory.PRINT.create(window);

		register(deleteAction);
		register(cutAction);
		register(copyAction);
		register(pasteAction);
		register(saveAction);
		register(saveAsAction);
		register(undoAction);
		register(redoAction);
		register(printAction);

	}

	@Override
	protected void fillCoolBar(ICoolBarManager coolBar) {
		addToolbar(getActionBarConfigurer().getWindowConfigurer().getWindow(), getActionBarConfigurer());
	}

	@Override
	protected void fillMenuBar(IMenuManager menuBar) {
	}

	protected void fillStatusLine(IStatusLineManager statusLine) {
		if (LicenseManager.isStudioVersion()) {
			StatusLineContributionItem statusItem = new StatusLineContributionItem(DefaultDBManager.STATUS_LINE_ID, StatusLineContributionItem.CALC_TRUE_WIDTH);
			String defaultDB = SettingsManager.get(DefaultDBManager.DEFAULT_DB_SETTING);
			if (defaultDB != null && defaultDB.trim().length() > 0)
				statusItem.setText("Default Connection: " + defaultDB);
			else
				statusItem.setText("No Default Connection");
			statusLine.add(statusItem);
		}

	}

	private void addToolbar(IWorkbenchWindow window, IActionBarConfigurer configurer) {
		ICoolBarManager cbManager = configurer.getCoolBarManager();
		cbManager.add(new GroupMarker(IWorkbenchActionConstants.GROUP_APP));

		IToolBarManager appToolBar = new ToolBarManager(cbManager.getStyle());
		appToolBar.add(new Separator(IWorkbenchActionConstants.NEW_GROUP));

		appToolBar.add(new Separator());

		IAction action = new SwitchWorkspaceAction(null);
		action.setImageDescriptor(UIHelper.instance().getImageDescriptor("synced.gif"));
		action.setToolTipText("Restart the application using a different workspace");
		appToolBar.add(action);
		appToolBar.add(new Separator());

		action = ActionFactory.SAVE.create(window);
		appToolBar.add(action);
		action = ActionFactory.SAVE_AS.create(window);
		appToolBar.add(action);
		action = ActionFactory.SAVE_ALL.create(window);
		appToolBar.add(action);
		appToolBar.add(new Separator());
		action = ActionFactory.RESET_PERSPECTIVE.create(window);
		action.setImageDescriptor(UIHelper.instance().getImageDescriptor("reset_perspective.gif"));
		appToolBar.add(action);

		ICommandService service = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
		final Command command = service.getCommand("com.ss.speedtransfer.toggleLazyLoadCommand");

		appToolBar.add(new Separator());
		toggleLazyLoad = new Action("Toggle Lazy Load", IAction.AS_CHECK_BOX) {
			public void run() {
				try {
					Map<String, String> parms = new HashMap<String, String>();
					command.executeWithChecks(new ExecutionEvent(command, parms, null, null));
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		};

		toggleLazyLoad.setChecked(ToggleLazyLoadHandler.isLazyLoading());
		toggleLazyLoad.setImageDescriptor(UIHelper.instance().getImageDescriptor("lazyload.gif"));
		service.addExecutionListener(new IExecutionListener() {
			public void preExecute(String commandId, ExecutionEvent event) {
			}

			public void postExecuteSuccess(String commandId, Object returnValue) {
				if (commandId.equals("com.ss.speedtransfer.toggleLazyLoadCommand"))
					toggleLazyLoad.setChecked(ToggleLazyLoadHandler.isLazyLoading());
			}

			public void postExecuteFailure(String commandId, ExecutionException exception) {
			}

			public void notHandled(String commandId, NotHandledException exception) {
			}
		});

		appToolBar.add(toggleLazyLoad);

		appToolBar.add(new Separator());
		Action openScratchPad = new Action("Open SQL Scratch Pad", IAction.AS_PUSH_BUTTON) {
			public void run() {
				openSQLScratchPad();

			}
		};
		openScratchPad.setImageDescriptor(UIHelper.instance().getImageDescriptor("sql_editor.gif"));
		openScratchPad.setEnabled(false);

		appToolBar.add(openScratchPad);

		if (LicenseManager.isStudioVersion())
			openScratchPad.setEnabled(true);

		cbManager.add(new ToolBarContributionItem(appToolBar, IWorkbenchActionConstants.TOOLBAR_FILE));

	}

	private void openSQLScratchPad() {
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		SQLScratchPadInput input = new SQLScratchPadInput();

		try {
			String defaultDB = SettingsManager.get(DefaultDBManager.DEFAULT_DB_SETTING);
			if (defaultDB != null && defaultDB.trim().length() > 0)
				input.setDBConnectionFile(defaultDB);
		} catch (Exception e) {

		}

		try {
			IDE.openEditor(page, input, SQLScratchPadEditor.ID);
		} catch (Exception e) {

		}

	}

}
