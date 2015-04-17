package com.ss.speedtransfer.xml.editor;

import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

public class XMLPartActivationListener implements IPartListener, IWindowListener {

	protected IWorkbenchPart activePart;
	protected XMLFormEditor editor;
	protected IPartService partService;

	public XMLPartActivationListener(XMLFormEditor editor, IPartService partService) {
		this.editor = editor;
		this.partService = partService;
		partService.addPartListener(this);
		PlatformUI.getWorkbench().addWindowListener(this);
	}

	public void dispose() {
		partService.removePartListener(this);
		PlatformUI.getWorkbench().removeWindowListener(this);
		partService = null;
	}

	public void partActivated(IWorkbenchPart part) {
		activePart = part;

	}

	public void partBroughtToTop(IWorkbenchPart part) {

	}

	public void partClosed(IWorkbenchPart part) {

	}

	public void partDeactivated(IWorkbenchPart part) {
		activePart = null;
	}

	public void partOpened(IWorkbenchPart part) {

	}

	public void windowActivated(IWorkbenchWindow window) {

	}

	public void windowDeactivated(IWorkbenchWindow window) {

	}

	public void windowClosed(IWorkbenchWindow window) {

	}

	public void windowOpened(IWorkbenchWindow window) {

	}
}