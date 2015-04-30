package com.ss.speedtransfer.xml.editor;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.FileEditorInput;

import com.ss.speedtransfer.SpeedTransferPlugin;
import com.ss.speedtransfer.ui.UIHelper;

public class XMLFormEditor extends FormEditor implements XMLModelListener {

	protected XMLModel model;
	protected boolean isDirty = false;
	protected XMLFormEditorPage formPage;

	public XMLFormEditor() {
	}

	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		try {
			if (input instanceof IFileEditorInput) {
				IFileEditorInput fileInput = (IFileEditorInput) input;
				try {
					model = createModel(fileInput.getFile());
					model.addModelListener(this);
					formPage = new XMLFormEditorPage(this, createBlock(), model);
				} catch (Exception e) {
				}
				super.init(site, input);
			} else {
				throw new PartInitException("Invalid input. Must be IFileEditorInput");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		isDirty = false;
		firePropertyChange(PROP_INPUT);
		firePropertyChange(PROP_DIRTY);
	}

	protected void setInput(IEditorInput input) {
		super.setInput(input);
		setPartName(input.getName());
	}

	protected XMLModel createModel(IFile file) throws Exception {
		return new XMLModel(file, getRootNodeName());
	}

	protected XMLFormPageBlock createBlock() {
		return new XMLFormPageBlock(this, model);
	}

	public void dispose() {
		if (model != null)
			model.dispose();
		model = null;

	}

	public boolean isDirty() {
		return isDirty;
	}

	protected FormToolkit createToolkit(Display display) {
		return new FormToolkit(SpeedTransferPlugin.getDefault().getFormColors(display));
	}

	protected void addPages() {
		try {
			addPage(formPage);
		} catch (PartInitException e) {
		}
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		if (model == null)
			return;

		if (isDeleted()) {
			if (isSaveAsAllowed())
				performSaveAs(monitor);
		} else {
			performSave(false, monitor);
		}

	}

	@Override
	public void doSaveAs() {
		performSaveAs(getProgressMonitor());

	}

	@Override
	public boolean isSaveAsAllowed() {
		return true;
	}

	protected boolean isDeleted() {
		try {
			IFileEditorInput input = (IFileEditorInput) getEditorInput();
			IFile file = input.getFile();
			return file.exists() == false;
		} catch (Exception e) {
		}

		return true;

	}

	public String getRootNodeName() {
		return null;
	}

	public void modelChanged(Object[] objects, String type, String property) {
		if (type != XMLModelListener.REPLACED) {
			isDirty = true;
			firePropertyChange(PROP_DIRTY);
		}

	}

	protected void performSave(boolean overwrite, IProgressMonitor progressMonitor) {

		if (model == null)
			return;

		try {
			FileEditorInput input = (FileEditorInput) getEditorInput();
			IFile file = input.getFile();

			file.setContents(model.getInputStream(), true, false, progressMonitor);

			isDirty = false;
			firePropertyChange(PROP_DIRTY);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void performSaveAs(IProgressMonitor progressMonitor) {
		Shell shell = PlatformUI.getWorkbench().getModalDialogShellProvider().getShell();
		final IEditorInput input = getEditorInput();

		IEditorInput newInput;

		try {
			SaveAsDialog dialog = new SaveAsDialog(shell);

			IFile original = (input instanceof IFileEditorInput) ? ((IFileEditorInput) input).getFile() : null;
			if (original != null)
				dialog.setOriginalFile(original);
			else
				dialog.setOriginalName("NewFile.xml");

			dialog.create();

			if (isDeleted() && original != null) {
				String message = "Resource was deleted. Using Save as";
				dialog.setErrorMessage(null);
				dialog.setMessage(message, IMessageProvider.WARNING);
			}

			if (dialog.open() == Window.CANCEL) {
				if (progressMonitor != null)
					progressMonitor.setCanceled(true);
				return;
			}

			IPath filePath = dialog.getResult();

			if (filePath == null) {
				if (progressMonitor != null)
					progressMonitor.setCanceled(true);
				return;
			}

			if (filePath.getFileExtension() == null || filePath.getFileExtension().equalsIgnoreCase("xml") == false) {
				filePath = filePath.removeFileExtension();
				filePath = filePath.addFileExtension("xml");
			}

			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			IFile newFile = workspace.getRoot().getFile(filePath);
			if (newFile.exists())
				newFile.delete(true, progressMonitor);
			newInput = new FileEditorInput(newFile);

			if (model == null) {
				// editor has programmatically been closed while the dialog was
				// open
				return;
			}

			boolean success = false;
			try {
				newFile.create(model.getInputStream(), true, progressMonitor);
				success = true;
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (success) {
					try {
						setInput(newInput);
						model.setFile(newFile);
						performSave(true, progressMonitor);
					} catch (Exception e2) {
					}
				}
			}

			if (progressMonitor != null)
				progressMonitor.setCanceled(!success);

		} catch (Exception e) {
			UIHelper.instance().showErrorMsg("Error", "Error during save. Error: " + e);
		}

	}

	protected IProgressMonitor getProgressMonitor() {

		IProgressMonitor pm = null;

		IStatusLineManager manager = getStatusLineManager();
		if (manager != null)
			pm = manager.getProgressMonitor();

		return pm != null ? pm : new NullProgressMonitor();
	}

	protected IStatusLineManager getStatusLineManager() {
		return getEditorSite().getActionBars().getStatusLineManager();
	}

	public XMLFormEditorPage getFormPage() {
		return formPage;
	}

	public XMLModel getModel() {
		return model;
	}

	protected void createPages() {
		super.createPages();
		if (getPageCount() == 1 && getContainer() instanceof CTabFolder) {
			((CTabFolder) getContainer()).setTabHeight(0);
		}
	}
}