package com.ss.speedtransfer.ui.editor.querydef;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.TextEvent;
import org.eclipse.jface.text.TextViewerUndoManager;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.eclipse.ui.forms.IMessageManager;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.operations.UndoActionHandler;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;

import com.ss.speedtransfer.actions.ExportSQLAction;
import com.ss.speedtransfer.actions.ExportToCSVAction;
import com.ss.speedtransfer.actions.ExportToExcelAction;
import com.ss.speedtransfer.actions.ExportToPDFAction;
import com.ss.speedtransfer.actions.FormatSQLAction;
import com.ss.speedtransfer.actions.OpenSQLBuilderWizardAction;
import com.ss.speedtransfer.actions.RunSQLAction;
import com.ss.speedtransfer.handlers.TextOperationHandler;
import com.ss.speedtransfer.model.DBConnection;
import com.ss.speedtransfer.model.QueryDefinition;
import com.ss.speedtransfer.model.SQLScratchPad;
import com.ss.speedtransfer.model.SQLScratchPadInput;
import com.ss.speedtransfer.ui.StyledEdit;
import com.ss.speedtransfer.ui.editor.dbcon.DBConnectionFileSelectionDialog;
import com.ss.speedtransfer.ui.view.QueryResultView;
import com.ss.speedtransfer.util.DBConnectionResourceListener;
import com.ss.speedtransfer.util.SSUtil;
import com.ss.speedtransfer.util.UIHelper;
import com.ss.speedtransfer.util.sql.SQLConfiguration;
import com.ss.speedtransfer.xml.editor.NewXMLFileGenerator;
import com.ss.speedtransfer.xml.editor.XMLModelListener;

public class SQLScratchPadEditor extends EditorPart implements XMLModelListener {
	public static final String ID = "com.ss.speedtransfer.sqlScratchPadEditor";

	FormToolkit toolkit;
	Form form;

	protected DBConnectionResourceListener dbConnectionListener;

	protected Section conSect;
	protected Hyperlink dbConLink;
	protected Text dbConFile;
	protected StyledEdit sqlEdit;
	protected SQLScratchPad scratchPad = null;
	protected boolean isDirty = false;

	protected IAction actionFormatSQL;
	protected IAction actionOpenSQLBuilderWizard;
	protected IAction actionRunSQL;
	protected IAction actionExport;

	protected IAction undoAction;
	protected IAction cutAction;
	protected IAction copyAction;
	protected IAction pasteAction;
	protected IAction deleteAction;

	@Override
	public void doSave(IProgressMonitor monitor) {
		if (scratchPad == null)
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

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		if (input instanceof IFileEditorInput == false && input instanceof SQLScratchPadInput == false)
			throw new PartInitException("Invalid Input");

		try {
			if (input instanceof IFileEditorInput) {
				IFile file = ((FileEditorInput) input).getFile();
				scratchPad = new SQLScratchPad(file);
			} else {
				scratchPad = new SQLScratchPad();
				scratchPad.setDBConnectionFile(((SQLScratchPadInput) input).getDBConnectionFile());
			}
			scratchPad.addModelListener(this);
		} catch (Exception e) {
			throw new PartInitException(SSUtil.getMessage(e));
		}

		setSite(site);
		setInput(input);

		isDirty = false;
		firePropertyChange(PROP_INPUT);
		firePropertyChange(PROP_DIRTY);

	}

	protected void setInput(IEditorInput input) {
		super.setInput(input);
		setPartName(input.getName());

		scratchPad.setName(input.getName());

		// Listen for changes to the db connection file to refresh this editor
		// if needed
		if (dbConnectionListener != null)
			dbConnectionListener.dispose();
		dbConnectionListener = new DBConnectionResourceListener(scratchPad);
	}

	@Override
	public boolean isDirty() {
		return isDirty;
	}

	@Override
	public boolean isSaveOnCloseNeeded() {
		// return isDirty();
		return false;
	}

	@Override
	public void createPartControl(Composite parent) {

		toolkit = new FormToolkit(parent.getDisplay());
		form = toolkit.createForm(parent);
		form.setBackgroundImage(UIHelper.instance().getImageDescriptor("form_banner.gif").createImage());
		form.setText("SQL Scratch Pad");

		GridLayout layout = new GridLayout();
		layout.marginTop = 5;
		form.getBody().setLayout(layout);
		toolkit.decorateFormHeading(form);

		conSect = toolkit.createSection(form.getBody(), Section.TITLE_BAR | Section.TWISTIE);
		conSect.marginWidth = 10;
		conSect.setExpanded(true);
		conSect.setText("Database Connection");

		GridData gd = new GridData(SWT.FILL, GridData.VERTICAL_ALIGN_BEGINNING, true, false);

		Composite conClient = toolkit.createComposite(conSect);
		GridLayout gl = new GridLayout(3, false);
		conClient.setLayout(gl);
		conSect.setClient(conClient);
		conSect.setLayoutData(gd);

		createDBConArea(conClient);

		Section sqlSect = toolkit.createSection(form.getBody(), Section.TITLE_BAR);
		sqlSect.marginWidth = 10;
		sqlSect.setText("SQL String");

		gd = new GridData(SWT.FILL, SWT.FILL, true, true);

		Composite sqlClient = toolkit.createComposite(sqlSect);
		gl = new GridLayout(1, false);
		sqlClient.setLayout(gl);
		sqlSect.setClient(sqlClient);
		sqlSect.setLayoutData(gd);

		createSQLEditArea(sqlClient);

		createActions();
		createPopupMenu();
		createSectionToolbar(sqlSect);
		createFormToolbar(form);

		toolkit.paintBordersFor(conClient);
		toolkit.paintBordersFor(sqlClient);

		validatePageState();

	}

	protected void createDBConArea(Composite baseSection) {

		GridData gd = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
		gd.widthHint = 10;
		gd.horizontalIndent = 7;

		dbConLink = toolkit.createHyperlink(baseSection, "Database Connection", SWT.WRAP);
		dbConLink.setToolTipText("Edit this connection");
		dbConLink.setEnabled(false);
		dbConLink.addHyperlinkListener(new IHyperlinkListener() {
			public void linkExited(HyperlinkEvent e) {
			}

			public void linkEntered(HyperlinkEvent e) {
			}

			public void linkActivated(HyperlinkEvent e) {
				DBConnection.editDBConnection(dbConFile.getText().trim());
			}
		});
		dbConFile = toolkit.createText(baseSection, "", SWT.SINGLE);
		dbConFile.setLayoutData(gd);
		dbConFile.setText(scratchPad.getDBConnectionFile());
		dbConFile.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				validatePageState();
				scratchPad.setDBConnectionFile(dbConFile.getText());
				scratchPad.resetDBConnection();
				isDirty = true;
				firePropertyChange(PROP_DIRTY);
			}
		});

		GridData gd1 = new GridData();
		gd1.heightHint = 24;

		Button promptButton = toolkit.createButton(baseSection, "Browse" + "...", SWT.PUSH); //$NON-NLS-1$
		promptButton.setLayoutData(gd1);
		promptButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				selectDbConnFile();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

	}

	protected void createSQLEditArea(Composite client) {

		sqlEdit = new StyledEdit(client, new SQLConfiguration());
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.heightHint = 100;
		sqlEdit.getControl().setLayoutData(gd);
		sqlEdit.setText(scratchPad.getSQL());
		sqlEdit.addTextListener(new ITextListener() {
			public void textChanged(TextEvent event) {
				scratchPad.setSQL(sqlEdit.getText());
				validatePageState();
				isDirty = true;
				firePropertyChange(PROP_DIRTY);
			}
		});

	}

	protected void createActions() {
		actionFormatSQL = new FormatSQLAction(sqlEdit);
		actionOpenSQLBuilderWizard = new OpenSQLBuilderWizardAction(sqlEdit, scratchPad);

		actionRunSQL = new RunSQLAction(scratchPad);
		actionRunSQL.setText("Run Query");

		IAction actionExportToExcel = new ExportToExcelAction(scratchPad);
		IAction actionExportToPDF = new ExportToPDFAction(scratchPad);
		IAction actionExportToCSV = new ExportToCSVAction(scratchPad);

		List<IAction> exportActions = new ArrayList<IAction>();
		exportActions.add(actionExportToExcel);
		exportActions.add(actionExportToPDF);
		exportActions.add(actionExportToCSV);

		actionExport = new ExportSQLAction(exportActions, scratchPad);

		// Text operations
		undoAction = new TextOperationHandler(sqlEdit, ITextOperationTarget.UNDO, "Undo");
		cutAction = new TextOperationHandler(sqlEdit, ITextOperationTarget.CUT, "Cut");
		copyAction = new TextOperationHandler(sqlEdit, ITextOperationTarget.COPY, "Copy");
		pasteAction = new TextOperationHandler(sqlEdit, ITextOperationTarget.PASTE, "Paste");
		deleteAction = new TextOperationHandler(sqlEdit, ITextOperationTarget.DELETE, "Delete");

		TextViewerUndoManager undoManager = (TextViewerUndoManager) sqlEdit.getUndoManager();

		IAction undo = ActionFactory.UNDO.create(UIHelper.instance().getActiveWindow());
		getEditorSite().getActionBars().setGlobalActionHandler(undo.getId(), new UndoActionHandler(getEditorSite(), undoManager.getUndoContext()));
		getEditorSite().getActionBars().updateActionBars();

	}

	protected void createPopupMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {

			public void menuAboutToShow(IMenuManager manager) {
				fillPopupMenu(manager);
			}
		});

		Menu menu = menuMgr.createContextMenu(sqlEdit.getControl());
		sqlEdit.getControl().setMenu(menu);

	}

	protected void createSectionToolbar(Section section) {
		ToolBarManager toolBarManager = new ToolBarManager(SWT.RIGHT);
		ToolBar toolbar = toolBarManager.createControl(section);
		final Cursor handCursor = new Cursor(Display.getCurrent(), SWT.CURSOR_HAND);
		toolbar.setCursor(handCursor);

		// Cursor needs to be explicitly disposed
		toolbar.addDisposeListener(new DisposeListener() {

			public void widgetDisposed(DisposeEvent e) {
				if ((handCursor != null) && (handCursor.isDisposed() == false)) {
					handCursor.dispose();
				}
			}
		});

		fillSectionToolbar(toolBarManager);

		toolBarManager.update(true);

		section.setTextClient(toolbar);
	}

	protected void fillSectionToolbar(ToolBarManager toolBarManager) {
		toolBarManager.add(actionOpenSQLBuilderWizard);
		toolBarManager.add(actionFormatSQL);

	}

	protected void createFormToolbar(Form form) {

		IContributionItem[] items = form.getToolBarManager().getItems();
		for (int i = 0; i < items.length; i++) {
			form.getToolBarManager().remove(items[i]);
		}
		form.getToolBarManager().add(actionRunSQL);
		form.getToolBarManager().add(new Separator());
		form.getToolBarManager().add(actionExport);
		form.getToolBarManager().update(true);

	}

	protected void fillPopupMenu(IMenuManager menuManager) {

		menuManager.add(undoAction);
		menuManager.add(new Separator());
		menuManager.add(cutAction);
		menuManager.add(copyAction);
		menuManager.add(pasteAction);
		menuManager.add(deleteAction);
		menuManager.add(new Separator());
		menuManager.add(actionOpenSQLBuilderWizard);
		menuManager.add(actionFormatSQL);

	}

	@Override
	public void setFocus() {
		sqlEdit.getControl().setFocus();

	}

	public void partActivated() {
		String id = getQueryDefinition().getName();
		if (getQueryDefinition().getFile() == null) {
			getQueryDefinition().setName("ScratchPad");
			id = Integer.toString(getQueryDefinition().hashCode());
		}

		try {
			IViewReference viewRef = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findViewReference(QueryResultView.ID, id);
			if (viewRef != null)
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(QueryResultView.ID, id, IWorkbenchPage.VIEW_VISIBLE);
		} catch (Exception e) {
		}

	}

	public void dispose() {
		if (dbConnectionListener != null)
			dbConnectionListener.dispose();

		if (toolkit != null)
			toolkit.dispose();

		if (scratchPad != null)
			scratchPad.dispose();

		super.dispose();

		scratchPad = null;
	}

	protected void validatePageState() {

		IMessageManager msgman = form.getMessageManager();

		boolean error = false;
		dbConLink.setEnabled(true);
		dbConLink.setToolTipText("Create this connection");
		msgman.removeMessage("Invalid dbcon", dbConFile);
		if (dbConFile.getText().trim().length() > 0) {
			String path = dbConFile.getText().trim();
			try {
				IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(path));
				if (file.exists()) {
					if (DBConnection.isDBConnectionFile(file) == false) {
						msgman.addMessage("Invalid dbcon", "The specified file is not a database connection file", null, IMessageProvider.ERROR, dbConFile);
						dbConLink.setEnabled(false);
						error = true;
					} else {
						dbConLink.setToolTipText("Edit this connection");
					}
				}
			} catch (Exception e) {
			}
		} else {
			dbConLink.setEnabled(false);
		}

		String text = "Database Connection";
		if (error == false) {
			try {
				IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(dbConFile.getText().trim()));
				if (file.exists())
					text = text + " - " + new DBConnection(file).toString();
			} catch (Exception e2) {
			}
		}
		conSect.setText(text);

		actionOpenSQLBuilderWizard.setEnabled(false);
		actionFormatSQL.setEnabled(false);
		actionRunSQL.setEnabled(false);
		actionExport.setEnabled(false);
		if (sqlEdit.getText().trim().length() > 0 && error == false) {
			actionFormatSQL.setEnabled(true);
			actionRunSQL.setEnabled(true);
			actionExport.setEnabled(true);
		}

		if (error == false) {
			actionOpenSQLBuilderWizard.setEnabled(true);
		}

		if (sqlEdit.getText().trim().length() > 0) {
			if (scratchPad.isSingleStatementQuery() == false) {
				actionOpenSQLBuilderWizard.setEnabled(false);
				// actionFormatSQL.setEnabled(false);
			}
			if (scratchPad.isQuery() == false)
				actionExport.setEnabled(false);
		}

	}

	protected void selectDbConnFile() {
		IWorkbenchWindow window = UIHelper.instance().getActiveWindow();
		if (window != null) {
			DBConnectionFileSelectionDialog dialog = new DBConnectionFileSelectionDialog(window);
			dialog.open();
			if (dialog.getSelectedObject() != null || dialog.getSelectedObject() instanceof IFile) {
				IPath path = ((IFile) dialog.getSelectedObject()).getFullPath();
				dbConFile.setText(path.toOSString());

			}
		}
	}

	protected void performSave(boolean overwrite, IProgressMonitor progressMonitor) {

		if (scratchPad == null)
			return;

		try {

			scratchPad.setDBConnectionFileInModel(scratchPad.getDBConnectionFile());
			scratchPad.setSQLInModel(scratchPad.getSQL());

			FileEditorInput input = (FileEditorInput) getEditorInput();
			IFile file = input.getFile();

			file.setContents(scratchPad.getInputStream(), true, false, progressMonitor);

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
				dialog.setOriginalName("SQLScratchPad.xml");

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

			if (scratchPad == null) {
				// editor has programmatically been closed while the dialog was
				// open
				return;
			}

			boolean success = false;
			try {
				if (input instanceof SQLScratchPadInput) {
					ByteArrayInputStream source = new ByteArrayInputStream("".getBytes());
					newFile.create(source, true, progressMonitor);
					NewXMLFileGenerator xmlGenerator = new NewXMLFileGenerator(SQLScratchPad.getCatalogKey(), getRootElement(), newFile);
					success = xmlGenerator.generate();
				} else {
					newFile.create(scratchPad.getInputStream(), true, progressMonitor);
					success = true;
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (success) {
					try {
						setInput(newInput);
						scratchPad.setFile(newFile);
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

	protected boolean isDeleted() {
		try {
			FileEditorInput input = (FileEditorInput) getEditorInput();
			IFile file = input.getFile();
			return file.exists() == false;
		} catch (Exception e) {
		}

		return true;

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

	protected String getRootElement() {
		return SQLScratchPad.SQL_SCRATCH_PAD;
	}

	public QueryDefinition getQueryDefinition() {
		return scratchPad;
	}

	public void modelChanged(Object[] objects, String type, String property) {
		if (type != XMLModelListener.REPLACED) {
			isDirty = true;
			firePropertyChange(PROP_DIRTY);
		}

	}

}
