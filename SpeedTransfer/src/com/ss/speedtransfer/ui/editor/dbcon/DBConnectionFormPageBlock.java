package com.ss.speedtransfer.ui.editor.dbcon;

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
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.forms.DetailsPart;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.ss.speedtransfer.model.DBConnection;
import com.ss.speedtransfer.ui.ActionDropDownMenuCreator;
import com.ss.speedtransfer.util.UIHelper;
import com.ss.speedtransfer.xml.editor.XMLFormEditor;
import com.ss.speedtransfer.xml.editor.XMLFormPageBlock;


public class DBConnectionFormPageBlock extends XMLFormPageBlock {

	public static final String NEW_PROPERTY_ACTION_ID = "NEW_PROPERTY_ACTION";

	protected IAction newAction;
	protected IAction newPropertyAction;

	protected Button buttonAddProperty;

	public DBConnectionFormPageBlock(XMLFormEditor editor, DBConnection model) {
		super(editor, model);
	}

	protected ILabelProvider getTreeLabelProvider() {
		return new DBConnectionLabelProvider();
	}

	protected void registerPages(DetailsPart detailsPart) {
		super.registerPages(detailsPart);
		detailsPart.registerPage(DBConnection.DB_CONNECTION_DEF, new DBConnectionDefinitionDetailsPage(getDBCon()));
		detailsPart.registerPage(DBConnection.CONNECTION, new DBConnectionDetailsPage(getDBCon()));
		detailsPart.registerPage(DBConnection.PROPERTY, new DBConnectionPropertyDetailsPage(getDBCon()));
		detailsPart.registerPage(DBConnection.COMMENT, new DBConnectionCommentDetailsPage(getDBCon()));
	}

	protected void validatePageState(Element selectedElement) {

		removeAction.setEnabled(false);
		moveUpAction.setEnabled(false);
		moveDownAction.setEnabled(false);
		newPropertyAction.setEnabled(false);

		if (selectedElement == null)
			return;

		if ((selectedElement.getParentNode() instanceof Element)) {

			if (selectedElement.getTagName().equalsIgnoreCase(DBConnection.PROPERTY)) {
				removeAction.setEnabled(true);

				Node parent = selectedElement.getParentNode();

				int index = model.getIndexOf(selectedElement, parent);

				if (!(parent instanceof Document) && index > 0)
					moveUpAction.setEnabled(true);
				if (!(parent instanceof Document) && index < model.getChildren(parent).length - 1)
					moveDownAction.setEnabled(true);
			}
		}

		if (selectedElement.getTagName().equalsIgnoreCase(DBConnection.CONNECTION) || selectedElement.getTagName().equalsIgnoreCase(DBConnection.PROPERTY)) {
			newPropertyAction.setEnabled(true);
		}

		newAction.setEnabled(newPropertyAction.isEnabled());

	}

	protected Composite createButtons(Composite client, FormToolkit toolkit) {

		Composite buttons = super.createButtons(client, toolkit);

		buttonAddProperty = toolkit.createButton(buttons, null, SWT.PUSH);
		buttonAddProperty.setToolTipText("Add property");
		buttonAddProperty.setImage(UIHelper.instance().getImage("property.gif"));
		buttonAddProperty.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				newPropertyAction.run();

			}
		});

		GridData gd = new GridData(GridData.VERTICAL_ALIGN_CENTER);
		buttonAddProperty.setLayoutData(gd);

		return buttons;

	}

	protected void createActions() {
		super.createActions();

		newPropertyAction = new Action("New Property") {

			public void run() {
				handleAction(NEW_PROPERTY_ACTION_ID, false);
			}
		};
		newPropertyAction.setImageDescriptor(UIHelper.instance().getImageDescriptor("property.gif"));
		newPropertyAction.setToolTipText("Add a new connection property");
		newPropertyAction.addPropertyChangeListener(new IPropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent event) {
				buttonAddProperty.setEnabled((Boolean) event.getNewValue());
			}
		});

		newAction = new Action("New") {
			public void run() {
			}
		};

		ActionDropDownMenuCreator newMenuCreator = new ActionDropDownMenuCreator();
		newMenuCreator.addAction(newPropertyAction);
		newAction.setMenuCreator(newMenuCreator);

	}

	protected void fillTreeContextMenu(IMenuManager menuManager) {
		menuManager.add(newAction);
		menuManager.add(new Separator());
		super.fillTreeContextMenu(menuManager);
	}

	public DBConnection getDBCon() {
		return (DBConnection) model;
	}

	public boolean handleAction(String actionName, Element selectedElement, IStructuredSelection selection) {

		if (super.handleAction(actionName, selectedElement, selection))
			return true;

		boolean actionHandled = false;

		Node parentNode = null;
		Element newNode = null;

		if (actionName.equals(NEW_PROPERTY_ACTION_ID)) {

			if (selectedElement.getNodeName().equals(DBConnection.CONNECTION))
				parentNode = selectedElement;
			else if (selectedElement.getNodeName().equals(DBConnection.PROPERTY))
				parentNode = selectedElement.getParentNode();

			newNode = (Element) model.addNode(parentNode, DBConnection.PROPERTY);
			newNode.setAttribute(DBConnection.NAME, "");
			newNode.setAttribute(DBConnection.VALUE, "");

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

	protected String getMasterTitle() {
		return "Edit Database Connection";
	}

	protected String getTreeTitle() {
		return "Database Connection Parts";
	}

}
