package com.ss.speedtransfer.util;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.ss.speedtransfer.SpeedTransferPlugin;


/**
 * The <code>UIHelper</code> class
 */
public class UIHelper {

	protected static UIHelper instance = null;

	/**
	 * Initializes a newly created <code>UIHelper</code>
	 */
	public UIHelper() {
		super();
	}

	public static UIHelper instance() {
		if (instance == null) {
			instance = new UIHelper();
		}
		return instance;
	}

	public int showMessageWithOKCancel(String header, String message) {
		return showMessageWithOKCancel(header, message, null);
	}

	public int showMessageWithOKCancel(String header, String message, String viewID) {
		return showMessage(header, message, SWT.ICON_QUESTION | SWT.OK | SWT.CANCEL | SWT.ON_TOP, viewID);

	}

	public int showMessage(String header, String message) {
		return showMessage(header, message, null);

	}

	public int showMessage(String header, String message, String viewID) {
		return showMessage(header, message, SWT.ICON_QUESTION | SWT.OK | SWT.NO | SWT.ON_TOP, viewID);

	}

	public int showMessage(String header, String message, int types, String viewID) {
		return showMessage(null, header, message, types, viewID);

	}

	public int showMessage(Shell shell, String header, String message, int types, String viewID) {

		if (shell == null)
			shell = getShell(viewID);

		MessageBox messageBox = new MessageBox(shell, types);
		messageBox.setText(header);
		messageBox.setMessage(message);
		return messageBox.open();

	}

	public int showErrorMsgWithOKCancel(String header, String message) {
		return showErrorMsgWithOKCancel(header, message, null);
	}

	public int showErrorMsgWithOKCancel(String header, String message, String viewID) {
		return showErrorMsg(header, message, SWT.ICON_ERROR | SWT.OK | SWT.CANCEL | SWT.ON_TOP, viewID);

	}

	public int showErrorMsg(String header, String message) {
		return showErrorMsg(header, message, null);

	}

	public int showErrorMsg(Shell shell, String header, String message) {
		return showErrorMsg(shell, header, message, SWT.ICON_ERROR | SWT.OK | SWT.ON_TOP, null);

	}

	public int showErrorMsg(String header, String message, String viewID) {
		return showErrorMsg(header, message, SWT.ICON_ERROR | SWT.OK | SWT.ON_TOP, viewID);

	}

	public int showErrorMsg(String header, String message, int types, String viewID) {
		return showErrorMsg(null, header, message, types, viewID);

	}

	public int showErrorMsg(Shell shell, String header, String message, int types, String viewID) {

		if (shell == null)
			shell = getShell(viewID);

		MessageBox messageBox = new MessageBox(shell, types);
		messageBox.setText(header);
		messageBox.setMessage(message);
		return messageBox.open();

	}

	public String selectFile(Shell shell, String title, String extension, int types) {

		String[] exts = null;
		if (extension != null && extension.trim().length() > 0) {
			exts = new String[1];
			exts[0] = extension;
		}

		return selectFile(shell, title, exts, types);

	}

	public String selectFile(Shell shell, String title, String[] extensions, int types) {

		if (shell == null)
			shell = getActiveShell();

		FileDialog dialog = new FileDialog(shell, types);
		if (title != null && title.trim().length() > 0)
			dialog.setText(title);
		if (extensions != null && extensions.length > 0)
			dialog.setFilterExtensions(extensions);

		return dialog.open();
	}

	public String selectDirectory(Shell shell, String title) {

		if (shell == null)
			shell = getActiveShell();

		DirectoryDialog dialog = new DirectoryDialog(shell);
		if (title != null && title.trim().length() > 0)
			dialog.setText(title);

		return dialog.open();
	}

	public IWorkbenchWindow getActiveWindow() {
		try {
			return PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		} catch (Exception e) {
		}

		return null;

	}

	public Shell getActiveShell() {
		Shell shell = null;
		try {
			shell = getActiveWindow().getShell();
		} catch (Exception e) {
		}
		if (shell == null)
			shell = new Shell();

		return shell;

	}

	public IWorkbenchPage getActivePage() {
		try {
			return getActiveWindow().getActivePage();
		} catch (Exception e) {
		}

		return null;

	}

	public IViewPart getActiveView(String viewID) {
		IWorkbenchPage activePage = getActivePage();
		if (activePage == null)
			return null;

		IViewReference[] viewsReferenses = (IViewReference[]) activePage.getViewReferences();
		for (IViewReference viewRef : viewsReferenses) {
			IViewPart view = viewRef.getView(false);
			if (view.getViewSite().getSecondaryId() == viewID)
				return view;
		}

		return null;

	}

	public ImageDescriptor getImageDescriptor(String name) {

		return SpeedTransferPlugin.getImageDescriptor("resources/images/" + name);
	}

	public Image getImage(String name) {

		return getImageDescriptor(name).createImage();
	}

	public Shell getShell(String viewID) {

		Shell shell = null;
		if (viewID != null && viewID.trim().length() > 0) {
			IViewPart view = getActiveView(viewID);
			if (view != null)
				shell = view.getViewSite().getShell();
		}

		if (shell == null)
			shell = getActiveShell();

		if (shell == null)
			shell = new Shell();

		return shell;

	}

	public Display getDisplay() {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().getDisplay();
	}

}
