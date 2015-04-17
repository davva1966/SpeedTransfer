package com.ss.speedtransfer.ui.editor.querydef;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.TextEvent;
import org.eclipse.jface.text.TextViewerUndoManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.forms.IMessage;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.operations.UndoActionHandler;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.ss.speedtransfer.actions.FormatSQLAction;
import com.ss.speedtransfer.actions.OpenSQLBuilderWizardAction;
import com.ss.speedtransfer.handlers.TextOperationHandler;
import com.ss.speedtransfer.model.DBConnection;
import com.ss.speedtransfer.model.QueryDefinition;
import com.ss.speedtransfer.ui.StyledEdit;
import com.ss.speedtransfer.ui.editor.dbcon.DBConnectionFileSelectionDialog;
import com.ss.speedtransfer.util.DBConnectionResourceListener;
import com.ss.speedtransfer.util.UIHelper;
import com.ss.speedtransfer.util.sql.SQLConfiguration;
import com.ss.speedtransfer.xml.editor.XMLModelListener;


public class QueryDefinitionSQLDetailsPage extends AbstractQueryDefinitionDetailsPage {

	public static final String REPL_VAR_WARN = "replvar warning";

	protected FormToolkit toolkit;
	protected QueryDefinitionFormPageBlock block;

	protected Section conSect;
	protected Hyperlink dbConLink;
	protected Text dbConFile;
	protected StyledEdit sqlEdit;

	protected DBConnectionResourceListener dbConnectionListener;

	protected IAction actionFormatSQL;
	protected IAction actionOpenSQLBuilderWizard;
	protected IAction actionGenerateReplVars;
	
	protected IAction undoAction;
	protected IAction cutAction;
	protected IAction copyAction;
	protected IAction pasteAction;
	protected IAction deleteAction;

	public QueryDefinitionSQLDetailsPage(QueryDefinition model, QueryDefinitionFormPageBlock block) {
		super(model);
		this.block = block;
	}

	public void dispose() {
		if (dbConnectionListener != null)
			dbConnectionListener.dispose();
		super.dispose();

	}

	protected void setInput(Node newInput) {
		super.setInput(newInput);
		// Listen for changes to the db connection file to refresh this editor if needed
		dbConnectionListener = new DBConnectionResourceListener(getQueryDef());
	}

	public void createContents(Composite parent) {

		toolkit = mform.getToolkit();

		GridLayout layout = new GridLayout();
		layout.marginHeight = 10;
		parent.setLayout(layout);

		conSect = toolkit.createSection(parent, Section.TITLE_BAR | Section.TWISTIE);
		conSect.marginWidth = 10;
		conSect.setExpanded(true);
		conSect.setText("Database Connection");

		GridData gd = new GridData(SWT.FILL, GridData.VERTICAL_ALIGN_BEGINNING, true, false);

		Composite conClient = toolkit.createComposite(conSect);
		GridLayout gl = new GridLayout(2, false);
		conClient.setLayout(gl);
		conSect.setClient(conClient);
		conSect.setLayoutData(gd);

		createDBConArea(conClient);

		Section sqlSect = toolkit.createSection(parent, Section.TITLE_BAR);

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

		toolkit.paintBordersFor(conClient);
		toolkit.paintBordersFor(sqlClient);

		mform.getForm().getForm().addMessageHyperlinkListener(new HyperlinkAdapter() {

			public void linkActivated(HyperlinkEvent e) {
				if (e.data instanceof IMessage[]) {
					IMessage[] msg = (IMessage[]) e.data;
					if (msg[0].getKey().equals(REPL_VAR_WARN)) {
						generateReplacementVariables();
						validateReplacementVars();
					}
				}
			}
		});

		validate();
	}

	protected void createDBConArea(Composite baseSection) {

		GridData gd = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
		gd.widthHint = 10;
		gd.horizontalSpan = 2;

		dbConLink = toolkit.createHyperlink(baseSection, "Database Connection", SWT.WRAP);
		dbConLink.setToolTipText("Edit this connection");
		dbConLink.setEnabled(false);
		dbConLink.setLayoutData(gd);
		dbConLink.addHyperlinkListener(new IHyperlinkListener() {
			public void linkExited(HyperlinkEvent e) {
			}

			public void linkEntered(HyperlinkEvent e) {
			}

			public void linkActivated(HyperlinkEvent e) {
				DBConnection.editDBConnection(dbConFile.getText().trim());
			}
		});

		gd = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
		gd.widthHint = 10;

		dbConFile = toolkit.createText(baseSection, "", SWT.SINGLE);
		dbConFile.setLayoutData(gd);
		dbConFile.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				validate();
				setAttributeValue(QueryDefinition.DB_CONNECTION_FILE, dbConFile.getText());
				getQueryDef().resetDBConnection();
			}
		});

		GridData gd1 = new GridData();
		gd1.heightHint = 22;

		Button promptButton = toolkit.createButton(baseSection, "Browse" + "...", SWT.PUSH);
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
		sqlEdit.addTextListener(new ITextListener() {

			public void textChanged(TextEvent event) {
				setCDATA(sqlEdit.getText());
				getQueryDef().setSQL(null);
				validate();
			}
		});

	}

	protected void createActions() {
		actionFormatSQL = new FormatSQLAction(sqlEdit);
		actionOpenSQLBuilderWizard = new OpenSQLBuilderWizardAction(sqlEdit, getQueryDef());

		actionGenerateReplVars = new Action("Generate replacement variables") {
			public void run() {
				generateReplacementVariables();
			}
		};
		actionGenerateReplVars.setImageDescriptor(UIHelper.instance().getImageDescriptor("variable.gif"));
		
		// Text operations
		undoAction = new TextOperationHandler(sqlEdit, ITextOperationTarget.UNDO, "Undo");
		cutAction = new TextOperationHandler(sqlEdit, ITextOperationTarget.CUT, "Cut");
		copyAction = new TextOperationHandler(sqlEdit, ITextOperationTarget.COPY, "Copy");
		pasteAction = new TextOperationHandler(sqlEdit, ITextOperationTarget.PASTE, "Paste");
		deleteAction = new TextOperationHandler(sqlEdit, ITextOperationTarget.DELETE, "Delete");
		
		TextViewerUndoManager undoManager = (TextViewerUndoManager)sqlEdit.getUndoManager();
		
		IAction undo = ActionFactory.UNDO.create(UIHelper.instance().getActiveWindow());
		block.getEditor().getEditorSite().getActionBars().setGlobalActionHandler(undo.getId(), new UndoActionHandler(block.getEditor().getEditorSite(), undoManager.getUndoContext()));
		block.getEditor().getEditorSite().getActionBars().updateActionBars();
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

	protected void fillSectionToolbar(ToolBarManager toolBarManager) {
		toolBarManager.add(actionOpenSQLBuilderWizard);
		toolBarManager.add(actionFormatSQL);
		toolBarManager.add(actionGenerateReplVars);
		super.fillSectionToolbar(toolBarManager);

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
		menuManager.add(new Separator());
		menuManager.add(actionGenerateReplVars);
	}

	protected void internalUpdate() {
		updateText(dbConFile, getAttributeValue(QueryDefinition.DB_CONNECTION_FILE));
		updateText(sqlEdit, getCDATA());
		validatePage();
	}

	public void setFocus() {
		sqlEdit.getControl().setFocus();
	}

	protected void validatePage() {

		boolean error = false;
		dbConLink.setEnabled(true);
		dbConLink.setToolTipText("Create this connection");
		clearMessage("Invalid dbcon", dbConFile);
		if (dbConFile.getText().trim().length() > 0) {
			String path = dbConFile.getText().trim();
			try {
				IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(path));
				if (file.exists()) {
					if (DBConnection.isDBConnectionFile(file) == false) {
						issueMessage("Invalid dbcon", IMessageProvider.ERROR, "The specified file is not a database connection file", dbConFile);
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
		block.actionRunSQL.setEnabled(false);
		block.actionExport.setEnabled(false);
		actionGenerateReplVars.setEnabled(false);
		if (sqlEdit.getText().trim().length() > 0 && error == false) {
			actionFormatSQL.setEnabled(true);
			block.actionRunSQL.setEnabled(true);
			block.actionExport.setEnabled(true);
		}

		if (error == false) {
			actionOpenSQLBuilderWizard.setEnabled(true);
		}

		if (sqlEdit.getText().trim().length() > 0) {
			actionGenerateReplVars.setEnabled(getQueryDef().hasReplacementVariables());
			validateReplacementVars();
		}

		if (sqlEdit.getText().trim().length() > 0) {
			if (getQueryDef().isSingleStatementQuery() == false) {
				actionOpenSQLBuilderWizard.setEnabled(false);
				// actionFormatSQL.setEnabled(false);
			}
			if (getQueryDef().isQuery() == false)
				block.actionExport.setEnabled(false);
		}

	}

	protected void generateReplacementVariables() {
		List<String> vars = getQueryDef().getUndefinedVariables();
		if (vars.size() > 0) {
			Node executionNode = getQueryDef().getExecutionNode();
			if (executionNode != null) {
				for (String var : vars) {
					Element replVarNode = (Element) model.addNode(executionNode, QueryDefinition.REPLACEMENT_VAR);
					replVarNode.setAttribute(QueryDefinition.NAME, var);
					replVarNode.setAttribute(QueryDefinition.DESCRIPTION, "Description for " + var);
					replVarNode.setAttribute(QueryDefinition.TYPE, QueryDefinition.STRING);
					model.fireModelChanged(replVarNode, XMLModelListener.STRUCTURE_CHANGED, null);
				}
			}
		} else {
			UIHelper.instance().showMessage("Information", "All replacement variables are already defined");
		}

	}

	protected void validateReplacementVars() {
		mform.getMessageManager().removeMessage("replvar warning");
		if (getQueryDef().getUndefinedVariables().size() > 0)
			issueMessage(REPL_VAR_WARN, IMessageProvider.WARNING, "Undefined replacement variables found. Click here to generate definitions.");

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

}
