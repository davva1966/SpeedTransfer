package com.ss.speedtransfer.ui.editor.dbcon;

import java.util.List;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.w3c.dom.Node;

import com.ss.speedtransfer.model.DBConnection;
import com.ss.speedtransfer.util.ConnectionManager;
import com.ss.speedtransfer.util.ItemTableSelectionDialog;
import com.ss.speedtransfer.util.StringHelper;
import com.ss.speedtransfer.util.UIHelper;
import com.ss.speedtransfer.xml.editor.XMLModelListener;


public class DBConnectionPropertyDetailsPage extends AbstractDBConnectionDetailsPage {

	protected Combo nameCombo;
	protected Combo valueCombo;

	protected ModifyListener nameComboModifyListener = new ModifyListener() {
		public void modifyText(ModifyEvent e) {
			setAttributeValue(DBConnection.NAME, nameCombo.getText());
			// Reload available property values
			if (valueCombo != null) {
				valueCombo.removeModifyListener(valueComboModifyListener);
				String value = valueCombo.getText();
				valueCombo.setItems(getPropertyValues());
				valueCombo.setText(value);
				validate();
				valueCombo.addModifyListener(valueComboModifyListener);
				model.fireModelChanged(null, XMLModelListener.STRUCTURE_CHANGED, null);
			}

		}
	};

	protected ModifyListener valueComboModifyListener = new ModifyListener() {
		public void modifyText(ModifyEvent e) {
			setAttributeValue(DBConnection.VALUE, valueCombo.getText());
			model.fireModelChanged(null, XMLModelListener.STRUCTURE_CHANGED, null);
			validate();
		}
	};

	public DBConnectionPropertyDetailsPage(DBConnection model) {
		super(model);
	}

	public void createContents(Composite parent) {
		TableWrapLayout layout = new TableWrapLayout();
		layout.topMargin = 10;
		layout.leftMargin = 5;
		layout.rightMargin = 2;
		layout.bottomMargin = 2;
		parent.setLayout(layout);

		FormToolkit toolkit = mform.getToolkit();
		Section s1 = toolkit.createSection(parent, Section.DESCRIPTION | Section.TITLE_BAR);
		s1.marginWidth = 10;
		s1.setText("Database Connection Property");
		TableWrapData td = new TableWrapData(TableWrapData.FILL, TableWrapData.TOP);
		td.grabHorizontal = true;
		s1.setLayoutData(td);

		Composite client = toolkit.createComposite(s1);
		GridLayout glayout = new GridLayout();
		glayout.marginWidth = toolkit.getBorderStyle() == SWT.BORDER ? 0 : 2;
		glayout.numColumns = 3;
		glayout.marginBottom = 10;
		client.setLayout(glayout);

		createControls(parent, client);

		toolkit.paintBordersFor(client);
		s1.setClient(client);
	}

	protected void createControls(Composite parent, Composite baseSection) {

		FormToolkit toolkit = mform.getToolkit();

		GridData gd = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
		gd.widthHint = 10;
		gd.horizontalIndent = 7;

		toolkit.createLabel(baseSection, "Property Name");
		nameCombo = createDropDown(baseSection, null, 0, SWT.DROP_DOWN);
		nameCombo.setLayoutData(gd);
		nameCombo.addModifyListener(nameComboModifyListener);

		GridData gd1 = new GridData();
		gd1.heightHint = 24;

		Button promptButton = toolkit.createButton(baseSection, "Browse" + "...", SWT.PUSH); //$NON-NLS-1$
		promptButton.setLayoutData(gd1);
		promptButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				selectProperty();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		gd = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
		gd.widthHint = 10;
		gd.horizontalSpan = 2;
		gd.horizontalIndent = 7;

		toolkit.createLabel(baseSection, "Property Value");
		valueCombo = createDropDown(baseSection, null, 0, SWT.DROP_DOWN);
		valueCombo.setLayoutData(gd);
		valueCombo.addModifyListener(valueComboModifyListener);

	}

	protected void internalUpdate() {
		updateCombo(nameCombo, getAttributeValue(DBConnection.NAME));
		updateCombo(valueCombo, getAttributeValue(DBConnection.VALUE));
		validatePage();
	}

	protected void setInput(Node newInput) {
		// Reload available connection properties
		if (nameCombo != null) {
			nameCombo.removeModifyListener(nameComboModifyListener);
			String text = nameCombo.getText();
			nameCombo.setItems(getPropertyNames());
			nameCombo.setText(text);
			nameCombo.addModifyListener(nameComboModifyListener);
		}

		// Reload available property values
		if (valueCombo != null) {
			valueCombo.removeModifyListener(valueComboModifyListener);
			String text = valueCombo.getText();
			valueCombo.setItems(getPropertyValues());
			valueCombo.setText(text);
			valueCombo.addModifyListener(valueComboModifyListener);
		}

		super.setInput(newInput);

	}

	public void setFocus() {
		nameCombo.setFocus();
	}

	public void selectProperty() {

		String conType = getDBCon().getConnectionType();
		if (conType == null || conType.trim().length() == 0) {
			UIHelper.instance().showErrorMsg("Error", "No connection type is set. Properties can not be displayed");
			return;
		}

		String[] headings = new String[4];
		headings[0] = "Property";
		headings[1] = "Description";
		headings[2] = "Choices";
		headings[3] = "Required";

		try {
			List<String[]> items = ConnectionManager.getDriverPropertiesInfo(conType);

			ItemTableSelectionDialog dialog = new ItemTableSelectionDialog(UIHelper.instance().getActiveShell(), headings, items, "Select Property");
			dialog.setHeightHint(300);
			dialog.setWidthHint(700);
			int rc = dialog.open();
			if (rc == Window.CANCEL)
				return;

			nameCombo.setText(dialog.getSelectedItem()[0]);

		} catch (Exception e) {
			UIHelper.instance().showErrorMsg("Error", "Error while retrieving connection properties. Error: " + e);
		}

	}

	protected String[] getPropertyNames() {
		String[] names = new String[0];
		try {
			String conType = getDBCon().getConnectionType();
			List<String[]> items = ConnectionManager.getDriverPropertiesInfo(conType);
			names = new String[items.size()];
			for (int i = 0; i < items.size(); i++) {
				names[i] = items.get(i)[0];
			}

		} catch (Exception e) {
			UIHelper.instance().showErrorMsg("Error", "Error while retrieving connection properties. Error: " + e);
		}

		return names;

	}

	protected String[] getPropertyValues() {
		String[] values = new String[0];

		String propertyName = nameCombo.getText();
		if (propertyName == null || propertyName.trim().length() == 0)
			return values;

		try {
			String conType = getDBCon().getConnectionType();
			List<String[]> items = ConnectionManager.getDriverPropertiesInfo(conType);
			for (String[] item : items) {
				if (item[0].equalsIgnoreCase(propertyName))
					values = StringHelper.formatSeparetedToArray(item[2], "|", true);
			}
		} catch (Exception e) {
			UIHelper.instance().showErrorMsg("Error", "Error while retrieving connection properties. Error: " + e);
		}

		return values;

	}

	protected void validatePage() {

		clearMessage("Property empty", nameCombo);
		if (nameCombo.getText().trim().length() == 0)
			issueMessage("Property empty", IMessageProvider.ERROR, "Property name must be specified", nameCombo);

		clearMessage("Property invalid", nameCombo);
		if (nameCombo.getText().trim().length() > 0) {
			boolean error = true;
			for (String propName : nameCombo.getItems()) {
				if (nameCombo.getText().trim().equalsIgnoreCase(propName.trim())) {
					error = false;
					break;
				}
			}
			if (error)
				issueMessage("Property invalid", IMessageProvider.WARNING, "The property is not found in the supported properties list for the connection type", nameCombo);
		}

		clearMessage("Value empty", valueCombo);
		if (valueCombo.getText().trim().length() == 0)
			issueMessage("Value empty", IMessageProvider.ERROR, "Property value must be specified", valueCombo);

		clearMessage("Value invalid", valueCombo);
		if (valueCombo.getText().trim().length() > 0) {
			if (valueCombo.getItemCount() > 0) {
				boolean error = true;
				for (String value : valueCombo.getItems()) {
					if (valueCombo.getText().trim().equalsIgnoreCase(value)) {
						error = false;
						break;
					}
				}
				if (error)
					issueMessage("Value invalid", IMessageProvider.WARNING, "The value is not one of the allowed values for the specified property", valueCombo);
			}
		}

	}
}
