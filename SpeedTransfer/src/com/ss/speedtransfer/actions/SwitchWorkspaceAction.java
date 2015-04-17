package com.ss.speedtransfer.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

import com.ss.speedtransfer.util.PickWorkspaceDialog;


public class SwitchWorkspaceAction extends Action {

	private Image _titleImage;

	public SwitchWorkspaceAction(Image titleImage) {
		super("Switch Workspace");
		_titleImage = titleImage;
	}

	@Override
	public void run() {
		PickWorkspaceDialog pwd = new PickWorkspaceDialog(true, _titleImage);
		int pick = pwd.open();
		if (pick == Dialog.CANCEL)
			return;

		MessageDialog.openInformation(Display.getDefault().getActiveShell(), "Switch Workspace", "The application will now restart with the new workspace");

		// restart client
		PickWorkspaceDialog.setIsRestarting(true);
		PlatformUI.getWorkbench().restart();
	}
}
