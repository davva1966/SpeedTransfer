// {{CopyrightNotice}}

package com.ss.speedtransfer.ui.wizard.qb;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.ss.speedtransfer.util.UIHelper;
import com.ss.speedtransfer.util.parser.sql.SQLParserHelper;


/**
 * The <code>SQLSequenceSelectionComp</code> class
 */
public class SQLSequenceSelectionComp extends SQLGenericEditComp {

	public SQLSequenceSelectionComp(WizardPage page, Composite parent, SQLQueryData queryData) {
		this(page, parent, SWT.NONE, queryData);
	}

	public SQLSequenceSelectionComp(WizardPage page, Composite parent, int style, SQLQueryData queryData) {
		super(page, parent, style, queryData);
		EDIT_PART_LENGTH = 2;

	}

	/**
	 * Create widgets
	 */
	protected void createWidgets() {
		ITEMS_MOVEABLE = true;
		super.createWidgets();

	}

	/**
	 * Fill table with content.
	 */
	protected void fillTable() {
		table.removeAll();

		List sequenceColumns = queryData.getOrderByColumns();
		if (sequenceColumns != null && !sequenceColumns.isEmpty()) {
			TableItem item;
			for (Iterator iter = sequenceColumns.iterator(); iter.hasNext();) {
				String[] sequenceCol = (String[]) iter.next();
				if (sequenceCol != null) {
					item = new TableItem(table, SWT.RESIZE);
					item.setText(0, queryData.buildColumnFunctionSQL(sequenceCol[0], sequenceCol[1], false));
					item.setText(1, sequenceCol[2]);
				}
			}
		}

		packColumns();

	}

	/**
	 * Update query data
	 */
	protected void updateQueryData() {
		queryData.setOrderByColumns(new ArrayList());

		// Update from table
		TableItem[] items = table.getItems();
		for (int i = 0; i < items.length; i++) {
			TableItem item = items[i];
			String[] colArr = new String[3];

			String[] funcArr = queryData.splitColumnFunctionSQL(item.getText(0));
			colArr[0] = funcArr[0];
			colArr[1] = funcArr[1];
			colArr[2] = item.getText(1);

			queryData.addOrderByColumn(colArr);
		}

	}

	protected void addTableColumns() {
		TableColumn tableColumn = new TableColumn(table, SWT.NONE);
		tableColumn.setText("Column");
		tableColumn.setWidth(185);
		tableColumn = new TableColumn(table, SWT.NONE);
		tableColumn.setText("Sequence");
		tableColumn.setWidth(70);

	}

	/**
	 * Open new edit dialog
	 */
	protected boolean edit(String[] editPart) {

		String[] part = new String[3];

		String[] funcArr = queryData.splitColumnFunctionSQL(editPart[0]);
		part[0] = funcArr[0];
		part[1] = funcArr[1];
		part[2] = editPart[1];

		SQLSequenceEditDialog dialog = new SQLSequenceEditDialog(getShell(), part);

		boolean retc = dialog.open();
		if (retc == true) {
			editPart[0] = queryData.buildColumnFunctionSQL(part[0], part[1], false);
			editPart[1] = part[2];
		}

		return retc;

	}

	/**
	 * The <code>SQLSequenceEditDialog</code> class
	 */
	class SQLSequenceEditDialog extends Dialog {

		protected Shell shell = null;

		protected Combo colCombo;

		protected Button okButton;

		protected String[] sequencePart;

		protected boolean update = false;

		public SQLSequenceEditDialog(Shell parent, String[] sequencePart) {
			this(parent, SWT.PRIMARY_MODAL, sequencePart);
		}

		public SQLSequenceEditDialog(Shell parent, int style, String[] sequencePart) {
			super(parent, style);
			shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE);

			Point location = parent.getLocation();
			shell.setBounds(location.x + 50, location.y + 70, 550, 150);
			shell.setText("Edit Expression");

			this.sequencePart = sequencePart;

			createWidgets();
		}

		public void createWidgets() {

			GridLayout gl = new GridLayout(2, false);
			shell.setLayout(gl);

			int tmpStyle = SWT.SINGLE | SWT.BORDER;
			Group group = new Group(shell, SWT.NONE);
			group.setText("Column");
			gl = new GridLayout(3, false);
			group.setLayout(gl);

			GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
			group.setLayoutData(gd);

			if (!queryData.isGrouped())
				group.setToolTipText("Functions on columns used to sort the query can only be used in grouped queries");

			// Function label
			Label label = new Label(group, SWT.NONE);
			label.setText("Function");
			gd = new GridData(SWT.LEFT, SWT.BOTTOM, false, false);
			label.setLayoutData(gd);

			// Column label
			label = new Label(group, SWT.NONE);
			label.setText("Column");
			gd = new GridData(SWT.FILL, SWT.BOTTOM, true, false);
			label.setLayoutData(gd);

			// Sequence label
			label = new Label(group, SWT.NONE);
			label.setText("Sequence");
			gd = new GridData(SWT.LEFT, SWT.BOTTOM, false, false);
			label.setLayoutData(gd);

			// Function combo
			final Combo funcCombo = new Combo(group, SWT.READ_ONLY | SWT.DROP_DOWN);
			gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
			funcCombo.setLayoutData(gd);
			funcCombo.setEnabled(false);
			// If this is not a grouped query, disable the function combo. If a function is already in use,
			// leave it enabled. (To allow the user to remove the function)
			if (queryData.isGrouped() || (sequencePart != null && sequencePart[0] != null && sequencePart[0].trim().length() > 0)) {
				funcCombo.setEnabled(true);
			}

			// Column combo
			colCombo = new Combo(group, tmpStyle);
			gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
			colCombo.setLayoutData(gd);
			colCombo.addModifyListener(new ModifyListener() {

				public void modifyText(ModifyEvent e) {
					enableDisableWidgets();
				}

			});

			// Sequence combo
			final Combo seqCombo = new Combo(group, SWT.READ_ONLY | SWT.DROP_DOWN);
			gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
			seqCombo.setLayoutData(gd);

			// Load func combo
			funcCombo.add("");
			for (int i = 0; i < SQLParserHelper.FUNCTIONS.length; i++) {
				String item = (String) SQLParserHelper.FUNCTIONS[i];
				funcCombo.add(item);
			}
			if (sequencePart != null) {
				String value = sequencePart[0];
				if (value != null)
					funcCombo.setText(value);
			} else {
				funcCombo.select(0);
			}

			// Load column combo
			TreeSet sortedSet = new TreeSet(queryData.getAvailableColumns().keySet());
			for (Iterator iter = sortedSet.iterator(); iter.hasNext();) {
				String col = (String) iter.next();
				colCombo.add(col);
			}
			if (sequencePart != null) {
				String value = sequencePart[1];
				if (value != null)
					colCombo.setText(value);
			} else {
				colCombo.select(0);
			}

			// Load sequence combo
			seqCombo.add("");
			seqCombo.add("ASC");
			seqCombo.add("DESC");

			if (sequencePart != null) {
				String value = sequencePart[2];
				if (value != null)
					seqCombo.setText(value);
			} else {
				seqCombo.select(0);
			}

			// OK button
			okButton = new Button(shell, SWT.PUSH);
			okButton.setText("OK");
			gd = new GridData(SWT.LEFT, SWT.BOTTOM, false, false);
			gd.widthHint = 100;
			gd.heightHint = 25;
			okButton.setLayoutData(gd);
			Listener listener = new Listener() {

				public void handleEvent(Event event) {
					sequencePart[0] = funcCombo.getText();
					sequencePart[1] = colCombo.getText();
					sequencePart[2] = seqCombo.getText().toLowerCase();
					if (validate(sequencePart)) {
						update = true;
						shell.close();
					}
				}
			};
			okButton.addListener(SWT.Selection, listener);

			// Cancel button
			Button cancelButton = new Button(shell, SWT.PUSH);
			cancelButton.setText("Cancel");
			gd = new GridData(SWT.LEFT, SWT.BOTTOM, false, false);
			gd.widthHint = 100;
			gd.heightHint = 25;
			cancelButton.setLayoutData(gd);
			listener = new Listener() {

				public void handleEvent(Event event) {
					update = false;
					shell.close();
				}
			};
			cancelButton.addListener(SWT.Selection, listener);

			enableDisableWidgets();

			// Add combo menues
			SQLQBComposite.createMenu(colCombo, queryData);

		}

		public boolean open() {

			shell.open();
			Display display = Display.getDefault();
			while (!shell.isDisposed()) {
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			}

			return update;

		}

		public boolean validate(String[] part) {

			// Ungrouped query, all ok
			if (queryData.getGroupByColumns().isEmpty())
				return true;

			// Grouped query, all columns ok in combination with a function
			if (part[0] != null && part[0].trim().length() > 0)
				return true;

			// Grouped query, columns used to group query ok even without a function
			for (Iterator iter = queryData.groupByColumns.iterator(); iter.hasNext();) {
				String[] groupCol = (String[]) iter.next();

				String groupName = groupCol[0];
				String groupTable = null;
				int idx = groupCol[0].indexOf(".");
				if (idx > -1) {
					groupName = groupCol[0].substring(idx + 1);
					groupTable = groupCol[0].substring(0, idx);
				}

				String editName = part[1];
				String editTable = null;
				idx = part[1].indexOf(".");
				if (idx > -1) {
					editName = part[1].substring(idx + 1);
					editTable = part[1].substring(0, idx);
				}
				if (editName.equalsIgnoreCase(groupName)) {
					String tab = editTable;
					if (tab == null)
						tab = "";
					if (groupTable == null)
						groupTable = "";
					if (tab.trim().length() == 0 || groupTable.trim().length() == 0 || tab.equalsIgnoreCase(groupTable))
						return true;
				}

			}

			UIHelper.instance().showErrorMsg(getShell(), "Error", "Column " + part[1] + " can not be used to sort this query. This is a grouped query. Only columns used to group the query can be used to sort the query. You can use other columns only in combination with a function.",
					SWT.ICON_ERROR | SWT.OK | SWT.ON_TOP, null);
			return false;
		}

		protected void enableDisableWidgets() {

			if (okButton == null)
				return;

			if (colCombo.getText().trim().length() == 0)
				okButton.setEnabled(false);
			else
				okButton.setEnabled(true);

		}
	}

}
