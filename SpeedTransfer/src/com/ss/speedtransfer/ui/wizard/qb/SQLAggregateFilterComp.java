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
 * The <code>SQLAggregateFilterComp</code> class
 */
public class SQLAggregateFilterComp extends SQLGenericEditComp {

	public SQLAggregateFilterComp(WizardPage page, Composite parent, SQLQueryData queryData) {
		this(page, parent, SWT.NONE, queryData);
	}

	public SQLAggregateFilterComp(WizardPage page, Composite parent, int style, SQLQueryData queryData) {
		super(page, parent, style, queryData);
		EDIT_PART_LENGTH = 6;

	}

	/**
	 * Fill filter table with content.
	 */
	protected void fillTable() {
		table.removeAll();

		List havingCond = queryData.getHavingCondition();
		if (havingCond != null && !havingCond.isEmpty()) {
			TableItem item;
			for (Iterator iter = havingCond.iterator(); iter.hasNext();) {
				String[] having = (String[]) iter.next();
				if (having != null) {
					item = new TableItem(table, SWT.RESIZE);
					if (having[0] != null)
						item.setText(0, having[0]);
					if (having[1] != null)
						item.setText(1, having[1]);
					item.setText(2, queryData.buildColumnFunctionSQL(having[2], having[3], false));
					if (having[4] != null)
						item.setText(3, having[4]);
					item.setText(4, queryData.buildColumnFunctionSQL(having[5], having[6], false));
					if (having[7] != null)
						item.setText(5, having[7]);
				}
			}
		}

		packColumns();
	}

	protected void addTableColumns() {
		TableColumn tableColumn = new TableColumn(table, SWT.NONE);
		tableColumn.setText("Logical");
		tableColumn.setWidth(60);
		tableColumn = new TableColumn(table, SWT.NONE);
		tableColumn.setText("Bracket");
		tableColumn.setWidth(40);
		tableColumn = new TableColumn(table, SWT.NONE);
		tableColumn.setText("Value1");
		tableColumn.setWidth(185);
		tableColumn = new TableColumn(table, SWT.NONE);
		tableColumn.setText("Condition");
		tableColumn.setWidth(70);
		tableColumn = new TableColumn(table, SWT.NONE);
		tableColumn.setText("value2");
		tableColumn.setWidth(170);
		tableColumn = new TableColumn(table, SWT.NONE);
		tableColumn.setText("Bracket");
		tableColumn.setWidth(40);

	}

	/**
	 * Update query data
	 */
	protected void updateQueryData() {
		queryData.setHavingConditon(new ArrayList());

		// Update from table
		TableItem[] items = table.getItems();
		for (int i = 0; i < items.length; i++) {
			TableItem item = items[i];
			String[] havingArr = new String[8];

			if (i == 0)
				havingArr[0] = "";
			else
				havingArr[0] = item.getText(0);

			havingArr[1] = item.getText(1);
			String[] funcArr = queryData.splitColumnFunctionSQL(item.getText(2));
			havingArr[2] = funcArr[0];
			havingArr[3] = funcArr[1];
			havingArr[4] = item.getText(3);
			funcArr = queryData.splitColumnFunctionSQL(item.getText(4));
			havingArr[5] = funcArr[0];
			havingArr[6] = funcArr[1];
			havingArr[7] = item.getText(5);

			queryData.addHavingConditon(havingArr);
		}

	}

	/**
	 * Get the edit dialog
	 */
	protected boolean edit(String[] editPart) {
		boolean firstItem = false;

		if (table.getItemCount() == 0) {
			firstItem = true;
		} else {
			int idx = table.getSelectionIndex();
			if (idx == 0)
				firstItem = true;
		}

		String[] part = new String[8];

		part[0] = editPart[0];
		part[1] = editPart[1];
		String[] funcArr = queryData.splitColumnFunctionSQL(editPart[2]);
		part[2] = funcArr[0];
		part[3] = funcArr[1];
		part[4] = editPart[3];
		funcArr = queryData.splitColumnFunctionSQL(editPart[4]);
		part[5] = funcArr[0];
		part[6] = funcArr[1];
		part[7] = editPart[5];

		SQLHavingExpressionEditDialog dialog = new SQLHavingExpressionEditDialog(getShell(), part, firstItem);

		boolean retc = dialog.open();
		if (retc == true) {
			editPart[0] = part[0];
			editPart[1] = part[1];
			editPart[2] = queryData.buildColumnFunctionSQL(part[2], part[3], false);
			editPart[3] = part[4];
			editPart[4] = queryData.buildColumnFunctionSQL(part[5], part[6], false);
			editPart[5] = part[7];
		}

		return retc;

	}

	/**
	 * The <code>SQLExpressionEditDialog</code> class
	 */
	class SQLHavingExpressionEditDialog extends Dialog {

		protected Shell shell = null;

		protected Combo value1Combo;

		protected Button okButton;

		protected String[] havingPart;

		protected boolean firstItem = false;

		protected boolean update = false;

		public SQLHavingExpressionEditDialog(Shell parent, String[] havingPart, boolean firstItem) {
			this(parent, SWT.PRIMARY_MODAL, havingPart, firstItem);
		}

		public SQLHavingExpressionEditDialog(Shell parent, int style, String[] havingPart, boolean firstItem) {
			super(parent, style);
			shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE);

			Point location = parent.getLocation();
			shell.setBounds(location.x + 50, location.y + 70, 700, 150);
			shell.setText("Edit Expression");

			this.havingPart = havingPart;
			this.firstItem = firstItem;
			createWidgets();
		}

		public void createWidgets() {

			GridLayout gl = new GridLayout(2, false);
			shell.setLayout(gl);

			int tmpStyle = SWT.SINGLE | SWT.BORDER;
			Group group = new Group(shell, SWT.NONE);
			group.setText("Entry Fields");
			gl = new GridLayout(8, false);
			group.setLayout(gl);

			GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
			group.setLayoutData(gd);

			// Logical label
			Label label = new Label(group, SWT.NONE);
			label.setText("Logical");
			gd = new GridData(SWT.LEFT, SWT.BOTTOM, false, false);
			label.setLayoutData(gd);

			// Lpar label
			label = new Label(group, SWT.NONE);
			label.setText("Opening Bracket");
			gd = new GridData(SWT.LEFT, SWT.BOTTOM, false, false);
			label.setLayoutData(gd);

			// Value1 function label
			label = new Label(group, SWT.NONE);
			label.setText("Function");
			gd = new GridData(SWT.LEFT, SWT.BOTTOM, false, false);
			label.setLayoutData(gd);

			// Value1 label
			label = new Label(group, SWT.NONE);
			label.setText("Value1");
			gd = new GridData(SWT.FILL, SWT.BOTTOM, true, false);
			label.setLayoutData(gd);

			// Condition label
			label = new Label(group, SWT.NONE);
			label.setText("Condition");
			gd = new GridData(SWT.LEFT, SWT.BOTTOM, false, false);
			label.setLayoutData(gd);

			// Value2 function label
			label = new Label(group, SWT.NONE);
			label.setText("Function");
			gd = new GridData(SWT.LEFT, SWT.BOTTOM, false, false);
			label.setLayoutData(gd);

			// Value2 label
			label = new Label(group, SWT.NONE);
			label.setText("Value2");
			gd = new GridData(SWT.FILL, SWT.BOTTOM, true, false);
			label.setLayoutData(gd);

			// Rpar label
			label = new Label(group, SWT.NONE);
			label.setText("Closing Bracket");
			gd = new GridData(SWT.LEFT, SWT.BOTTOM, false, false);
			label.setLayoutData(gd);

			// Logical combo
			final Combo logicalCombo = new Combo(group, SWT.READ_ONLY | SWT.DROP_DOWN);
			gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
			logicalCombo.setLayoutData(gd);
			logicalCombo.add("AND");
			logicalCombo.add("OR");

			logicalCombo.select(0);
			if (havingPart != null) {
				String value = havingPart[0];
				if (value != null)
					logicalCombo.setText(havingPart[0]);
			}

			// LPar combo
			final Combo lParCombo = new Combo(group, SWT.READ_ONLY | SWT.DROP_DOWN);
			gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
			lParCombo.setLayoutData(gd);
			lParCombo.add("");
			lParCombo.add("(");
			lParCombo.select(0);

			if (havingPart != null) {
				String value = havingPart[1];
				if (value == null)
					value = "";
				if (value.equals("("))
					lParCombo.select(1);
				else
					lParCombo.select(0);
			} else {
				lParCombo.select(0);
			}

			// Value1 function combo
			final Combo func1Combo = new Combo(group, SWT.READ_ONLY | SWT.DROP_DOWN);
			gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
			func1Combo.setLayoutData(gd);

			// Value1 combo
			value1Combo = new Combo(group, tmpStyle);
			gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
			value1Combo.setLayoutData(gd);
			value1Combo.addModifyListener(new ModifyListener() {

				public void modifyText(ModifyEvent e) {
					enableDisableWidgets();
				}

			});

			// Condition combo
			final Combo conditionCombo = new Combo(group, SWT.READ_ONLY | SWT.DROP_DOWN);
			gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
			conditionCombo.setLayoutData(gd);
			conditionCombo.setItems(SQLParserHelper.CONDITION_ITEMS);

			conditionCombo.select(3);
			if (havingPart != null) {
				String value = havingPart[4];
				if (value != null)
					conditionCombo.setText(value);
			}

			// Value2 function combo
			final Combo func2Combo = new Combo(group, SWT.READ_ONLY | SWT.DROP_DOWN);
			gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
			func2Combo.setLayoutData(gd);

			// Value2 combo
			final Combo value2Combo = new Combo(group, tmpStyle);
			gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
			value2Combo.setLayoutData(gd);

			// Load function combos
			func1Combo.add("");
			func2Combo.add("");
			for (int i = 0; i < SQLParserHelper.FUNCTIONS.length; i++) {
				String item = (String) SQLParserHelper.FUNCTIONS[i];
				func1Combo.add(item);
				func2Combo.add(item);
			}
			if (havingPart != null) {
				String value = havingPart[2];
				if (value != null)
					func1Combo.setText(value);
				value = havingPart[5];
				if (value != null)
					func2Combo.setText(value);
			} else {
				func1Combo.select(0);
				func2Combo.select(0);
			}

			// Load value combos
			TreeSet sortedSet = new TreeSet(queryData.getAvailableColumns().keySet());
			for (Iterator iter = sortedSet.iterator(); iter.hasNext();) {
				String key = (String) iter.next();
				value1Combo.add(key);
				value2Combo.add(key);
			}
			if (havingPart != null) {
				String value = havingPart[3];
				if (value != null)
					value1Combo.setText(value);
				value = havingPart[6];
				if (value != null)
					value2Combo.setText(value);
			}

			// RPar combo
			final Combo rParCombo = new Combo(group, SWT.READ_ONLY | SWT.DROP_DOWN);
			gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
			rParCombo.setLayoutData(gd);
			rParCombo.add("");
			rParCombo.add(")");

			if (havingPart != null) {
				String value = havingPart[7];
				if (value == null)
					value = "";
				if (value.equals(")"))
					rParCombo.select(1);
				else
					rParCombo.select(0);
			} else {
				rParCombo.select(0);
			}

			if (firstItem) {
				logicalCombo.setEnabled(false);
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

					if (firstItem) {
						havingPart[0] = "";
					} else {
						havingPart[0] = logicalCombo.getText().toLowerCase();
					}

					havingPart[1] = lParCombo.getText();
					havingPart[2] = func1Combo.getText();
					havingPart[3] = value1Combo.getText();
					havingPart[4] = conditionCombo.getText();
					havingPart[5] = func2Combo.getText();
					havingPart[6] = value2Combo.getText();
					havingPart[7] = rParCombo.getText();

					if (validate(havingPart)) {
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
			SQLQBComposite.createMenu(value1Combo, queryData);
			SQLQBComposite.createMenu(value2Combo, queryData);

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

		protected boolean validate(String[] part) {

			if (validate(part[2], part[3]) == false) {
				UIHelper.instance().showErrorMsg(getShell(), "Error", "Column " + part[3] + " can not be used without a function. Only columns used to group the query can be used without a function.", SWT.ICON_ERROR | SWT.OK | SWT.ON_TOP, null);
				return false;
			}

			if (validate(part[5], part[6]) == false) {
				UIHelper.instance().showErrorMsg(getShell(), "Error", "Column " + part[6] + " can not be used without a function. Only columns used to group the query can be used without a function.", SWT.ICON_ERROR | SWT.OK | SWT.ON_TOP, null);
				return false;
			}

			return true;

		}

		public boolean validate(String func, String column) {
			if (column == null || column.trim().length() == 0)
				return true;

			if (queryData.isColumnName(column) == false)
				return true;

			// All columns allowed in combination with a function
			if (func != null && func.trim().length() > 0)
				return true;

			// Columns used to group query ok even without a function
			for (Iterator iter = queryData.groupByColumns.iterator(); iter.hasNext();) {
				String[] groupCol = (String[]) iter.next();

				String groupName = groupCol[0];
				String groupTable = null;
				int idx = groupCol[0].indexOf(".");
				if (idx > -1) {
					groupName = groupCol[0].substring(idx + 1);
					groupTable = groupCol[0].substring(0, idx);
				}

				String editName = column;
				String editTable = null;
				idx = column.indexOf(".");
				if (idx > -1) {
					editName = column.substring(idx + 1);
					editTable = column.substring(0, idx);
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

			return false;

		}

		protected void enableDisableWidgets() {

			if (okButton == null)
				return;

			if (value1Combo.getText().trim().length() == 0)
				okButton.setEnabled(false);
			else
				okButton.setEnabled(true);

		}

	}

}
