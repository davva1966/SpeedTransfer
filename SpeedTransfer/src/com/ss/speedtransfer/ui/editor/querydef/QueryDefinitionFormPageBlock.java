package com.ss.speedtransfer.ui.editor.querydef;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.forms.DetailsPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.ss.speedtransfer.actions.ExportSQLAction;
import com.ss.speedtransfer.actions.ExportToCSVAction;
import com.ss.speedtransfer.actions.ExportToDBAction;
import com.ss.speedtransfer.actions.ExportToExcelAction;
import com.ss.speedtransfer.actions.ExportToPDFAction;
import com.ss.speedtransfer.actions.RunSQLAction;
import com.ss.speedtransfer.actions.RunSQLToExcelAction;
import com.ss.speedtransfer.actions.RunSQLToNatTableAction;
import com.ss.speedtransfer.actions.RunSQLToTableAction;
import com.ss.speedtransfer.model.QueryDefinition;
import com.ss.speedtransfer.ui.ActionDropDownMenuCreator;
import com.ss.speedtransfer.util.LicenseManager;
import com.ss.speedtransfer.util.SSUtil;
import com.ss.speedtransfer.util.UIHelper;
import com.ss.speedtransfer.xml.editor.XMLFormEditor;
import com.ss.speedtransfer.xml.editor.XMLFormPageBlock;


public class QueryDefinitionFormPageBlock extends XMLFormPageBlock {

	public static final String NEW_REPLACEMENT_VAR_ACTION_ID = "NEW_REPLACEMENT_VAR_ACTION";

	protected IAction newAction;
	protected IAction newReplacementVarAction;

	IAction actionRunSQL;
	IAction actionExport;

	protected Button buttonAddReplVar;

	public QueryDefinitionFormPageBlock(XMLFormEditor editor, QueryDefinition model) {
		super(editor, model);
	}

	protected ITreeContentProvider getTreeContentProvider() {
		return new QueryDefinitionMasterContentProvider(model);
	}

	protected ILabelProvider getTreeLabelProvider() {
		return new QueryDefinitionLabelProvider();
	}

	protected void registerPages(DetailsPart detailsPart) {
		super.registerPages(detailsPart);
		detailsPart.registerPage(QueryDefinition.QUERY_DEFINITION, new QueryDefinitionDetailsPage(getQueryDef()));
		detailsPart.registerPage(QueryDefinition.SQL, new QueryDefinitionSQLDetailsPage(getQueryDef(), this));
		detailsPart.registerPage(QueryDefinition.EXECUTION, new QueryDefinitionExecutionDetailsPage(getQueryDef()));
		detailsPart.registerPage(QueryDefinition.REPLACEMENT_VAR, new QueryDefinitionReplacementVariableDetailsPage(getQueryDef(), this));
		detailsPart.registerPage(QueryDefinition.COMMENT, new QueryDefinitionCommentsDetailPage(getQueryDef()));
		detailsPart.registerPage(QueryDefinition.EXCEL_DEFAULTS, new QueryDefinitionDefaultsDetailsPage(getQueryDef()));
		detailsPart.registerPage(QueryDefinition.PDF_DEFAULTS, new QueryDefinitionDefaultsDetailsPage(getQueryDef()));
		detailsPart.registerPage(QueryDefinition.CSV_DEFAULTS, new QueryDefinitionDefaultsDetailsPage(getQueryDef()));
		detailsPart.registerPage(QueryDefinition.SELECTION_DEFAULTS, new QueryDefinitionDefaultsDetailsPage(getQueryDef()));
		detailsPart.registerPage(QueryDefinition.DATABASE_DEFAULTS, new QueryDefinitionDefaultsDetailsPage(getQueryDef()));
	}

	public ILabelProvider getViewerLabelProvider() {
		return new QueryDefinitionLabelProvider();
	}

	protected void validatePageState(Element selectedElement) {

		removeAction.setEnabled(false);
		moveUpAction.setEnabled(false);
		moveDownAction.setEnabled(false);
		newReplacementVarAction.setEnabled(false);

		if (selectedElement == null)
			return;

		if ((selectedElement.getParentNode() instanceof Element)) {

			if (selectedElement.getTagName().equalsIgnoreCase(QueryDefinition.REPLACEMENT_VAR)) {
				removeAction.setEnabled(true);

				Node parent = selectedElement.getParentNode();

				int index = model.getIndexOf(selectedElement, parent);

				if (!(parent instanceof Document) && index > 0)
					moveUpAction.setEnabled(true);
				if (!(parent instanceof Document) && index < model.getChildren(parent).length - 1)
					moveDownAction.setEnabled(true);
			}
		}

		if (selectedElement.getTagName().equalsIgnoreCase(QueryDefinition.EXECUTION) || selectedElement.getTagName().equalsIgnoreCase(QueryDefinition.REPLACEMENT_VAR)) {
			newReplacementVarAction.setEnabled(true);
		}

		newAction.setEnabled(newReplacementVarAction.isEnabled());

	}

	protected Composite createButtons(Composite client, FormToolkit toolkit) {

		Composite buttons = super.createButtons(client, toolkit);

		buttonAddReplVar = toolkit.createButton(buttons, null, SWT.PUSH);
		buttonAddReplVar.setToolTipText("Add replacement variable");
		buttonAddReplVar.setImage(UIHelper.instance().getImage("variable.gif"));
		buttonAddReplVar.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				newReplacementVarAction.run();

			}
		});

		GridData gd = new GridData(GridData.VERTICAL_ALIGN_CENTER);
		buttonAddReplVar.setLayoutData(gd);

		return buttons;

	}

	protected void createActions() {
		super.createActions();

		newReplacementVarAction = new Action("New Replacement Variable") {

			public void run() {
				handleAction(NEW_REPLACEMENT_VAR_ACTION_ID, false);
			}
		};
		newReplacementVarAction.setImageDescriptor(UIHelper.instance().getImageDescriptor("replacement_var.png"));
		newReplacementVarAction.setToolTipText("Add a new replacement variable");
		newReplacementVarAction.addPropertyChangeListener(new IPropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent event) {
				buttonAddReplVar.setEnabled((Boolean) event.getNewValue());
			}
		});

		newAction = new Action("New") {
			public void run() {
			}
		};

		ActionDropDownMenuCreator newMenuCreator = new ActionDropDownMenuCreator();
		newMenuCreator.addAction(newReplacementVarAction);
		newAction.setMenuCreator(newMenuCreator);

	}

	protected void fillTreeContextMenu(IMenuManager menuManager) {
		menuManager.add(newAction);
		menuManager.add(new Separator());
		super.fillTreeContextMenu(menuManager);
	}

	public boolean handleAction(String actionName, Element selectedElement, IStructuredSelection selection) {

		if (super.handleAction(actionName, selectedElement, selection))
			return true;

		boolean actionHandled = false;

		Node parentNode = null;
		Element newNode = null;

		if (actionName.equals(NEW_REPLACEMENT_VAR_ACTION_ID)) {

			if (selectedElement.getNodeName().equals(QueryDefinition.EXECUTION))
				parentNode = selectedElement;
			else if (selectedElement.getNodeName().equals(QueryDefinition.REPLACEMENT_VAR))
				parentNode = selectedElement.getParentNode();

			newNode = (Element) model.addNode(parentNode, QueryDefinition.REPLACEMENT_VAR);
			newNode.setAttribute(QueryDefinition.DESCRIPTION, "New Description");
			newNode.setAttribute(QueryDefinition.TYPE, QueryDefinition.STRING);

			actionHandled = true;

		}

		if (actionHandled) {
			List<Object> elementsList = new ArrayList<Object>();
			elementsList.add(newNode);

			parentNode = newNode.getParentNode();
			elementsList.add(parentNode);

			while (parentNode.getParentNode() != null && parentNode.getParentNode() instanceof Document == false) {
				parentNode = parentNode.getParentNode();
				elementsList.add(parentNode);
			}

			Collections.reverse(elementsList);
			Object[] nodeArray = new Object[elementsList.size()];
			elementsList.toArray(nodeArray);
			getViewer().setSelection(new TreeSelection(new TreePath(nodeArray)));

		}

		return actionHandled;
	}

	protected void createToolBarActions(IManagedForm managedForm) {

		IAction actionRunSQLToTable = new RunSQLToTableAction(getQueryDef());
		actionRunSQLToTable.setText("Run Query To Table");
		IAction actionRunSQLToExcel = new RunSQLToExcelAction(getQueryDef());
		actionRunSQLToExcel.setText("Run Query To Excel");
		IAction actionRunSQLToNatTable = new RunSQLToNatTableAction(getQueryDef());
		actionRunSQLToNatTable.setText("Run Query To Nat Table");

		List<IAction> runActions = new ArrayList<IAction>();
		runActions.add(actionRunSQLToTable);
		if (SSUtil.excelInstalled())
			runActions.add(actionRunSQLToExcel);

		actionRunSQL = new RunSQLAction(runActions, getQueryDef());

		IAction actionExportToExcel = new ExportToExcelAction(getQueryDef());
		IAction actionExportToPDF = new ExportToPDFAction(getQueryDef());
		IAction actionExportToCSV = new ExportToCSVAction(getQueryDef());
		IAction actionExportToDB = new ExportToDBAction(getQueryDef());

		if (LicenseManager.isStudioVersion() && LicenseManager.isSelectOnly() == false)
			actionExportToDB.setEnabled(true);
		else
			actionExportToDB.setEnabled(false);

		List<IAction> exportActions = new ArrayList<IAction>();
		exportActions.add(actionExportToExcel);
		exportActions.add(actionExportToPDF);
		exportActions.add(actionExportToCSV);
		exportActions.add(actionExportToDB);

		actionExport = new ExportSQLAction(exportActions, getQueryDef());

		ScrolledForm form = managedForm.getForm();
		form.getToolBarManager().add(actionRunSQL);
		form.getToolBarManager().add(new Separator());
		form.getToolBarManager().add(actionRunSQLToNatTable);
		form.getToolBarManager().add(new Separator());
		form.getToolBarManager().add(actionExport);
		form.getToolBarManager().add(new Separator());

		super.createToolBarActions(managedForm);

	}

	protected String getMasterTitle() {
		return "Edit Query Definition";
	}

	protected String getTreeTitle() {
		return "Query Definition Parts";
	}

	public QueryDefinition getQueryDef() {
		return (QueryDefinition) model;
	}

}
