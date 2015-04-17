// {{CopyrightNotice}}

package com.ss.speedtransfer.actions;

import org.eclipse.core.commands.Command;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.RadioState;

import com.ss.speedtransfer.ui.ActionDropDownMenuCreator;


public class Remove_RunQueryDropDownMenuCreator extends ActionDropDownMenuCreator {

	protected void createDropDownMenuMgr() {
		dropDownMenuMgr = new MenuManager();
		for (IAction action : actions) {
			action.setChecked(getChecked(action));
			dropDownMenuMgr.add(action);
		}

	}

	protected boolean getChecked(IAction action) {
		if (action instanceof RunSQLAction) {
			String type = ((RunSQLAction) action).getType();
			if (type != null) {
				ICommandService commandService = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
				Command command = commandService.getCommand("com.ss.speedtransfer.runQueryCommand");
				String currentType = (String) command.getState(RadioState.STATE_ID).getValue();
				return type.trim().equalsIgnoreCase(currentType.trim());
			}
		}
		return false;
	}

}
