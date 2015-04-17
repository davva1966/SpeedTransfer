// {{CopyrightNotice}}

package com.ss.speedtransfer.ui.wizard.qb;

import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
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
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * The <code>SQLTableJoinDialog</code> class
 */
public class SQLTableJoinComp extends SQLGenericEditComp {

	public SQLTableJoinComp(WizardPage page, Composite parent, SQLQueryData queryData) {
		this(page, parent, SWT.NONE, queryData);
	}

	public SQLTableJoinComp(WizardPage page, Composite parent, int style, SQLQueryData queryData) {
		super(page, parent, style, queryData);
		EDIT_PART_LENGTH = 4;
	}

	/**
	 * Load table with content.
	 */
	protected void fillTable() {
		table.removeAll();

		List joinSpecs = queryData.getJoinSpecifications();
		for (Iterator iter = joinSpecs.iterator(); iter.hasNext();) {
			SQLJoinSpec joinSpec = (SQLJoinSpec) iter.next();

			TableItem item = new TableItem(table, SWT.NONE);
			item.setText(0, joinSpec.mainTableName);
			item.setText(1, joinSpec.convertJoinType(joinSpec.joinType));
			item.setText(2, joinSpec.joinedTableName);
			item.setText(3, queryData.buildFilterSQL(joinSpec.getExpressions()));
		}

		packColumns();
	}

	protected void addTableColumns() {
		TableColumn tableColumn = new TableColumn(table, SWT.NONE);
		tableColumn.setText("Main Table");
		tableColumn.setWidth(140);
		tableColumn = new TableColumn(table, SWT.NONE);
		tableColumn.setText("Join");
		tableColumn.setWidth(135);
		tableColumn = new TableColumn(table, SWT.NONE);
		tableColumn.setText("Joined Table");
		tableColumn.setWidth(140);
		tableColumn = new TableColumn(table, SWT.NONE);
		tableColumn.setText("Join Expression");
		tableColumn.setWidth(150);
	}

	protected void updateQueryData() {

	}

	protected boolean edit(String[] editPart) {
		return true;
	}

	/**
	 * Delete selected column(s)
	 */
	protected void delete() {
		if (table.getSelectionIndex() != -1) {
			queryData.getJoinSpecifications().remove(table.getSelectionIndex());
			fillTable();
		}
	}

	/**
	 * Open new edit dialog
	 */
	protected void openEditDialog(boolean add) {

		int idx = table.getSelectionIndex();
		SQLJoinSpec updatedJoinSpec = null;
		if (add) {
			updatedJoinSpec = new SQLJoinSpec();
		} else {
			SQLJoinSpec joinSpec = (SQLJoinSpec) queryData.getJoinSpecifications().get(idx);
			updatedJoinSpec = new SQLJoinSpec(joinSpec);
		}
		SQLTableJoinDialog dialog = new SQLTableJoinDialog(getShell(), updatedJoinSpec);

		if (dialog.open()) {
			if (add)
				queryData.getJoinSpecifications().add(updatedJoinSpec);
			else
				queryData.getJoinSpecifications().set(idx, updatedJoinSpec);
			fillTable();
		}

	}

	/**
	 * The <code>SQLTableJoinDialog</code> class
	 */
	class SQLTableJoinDialog extends Dialog {

		protected Shell shell = null;

		protected Button okButton;

		protected Combo mainTableCombo = null;

		protected Combo joinTypeCombo = null;

		protected Combo joinedTableCombo = null;

		protected Table joinColumnTable = null;

		protected Button removeButton = null;

		protected SQLJoinSpec joinSpec = null;

		protected boolean update = false;

		public SQLTableJoinDialog(Shell parent, SQLJoinSpec joinSpec) {
			this(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE, joinSpec);
		}

		public SQLTableJoinDialog(Shell parent, int style, SQLJoinSpec joinSpec) {
			super(parent, style);
			shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE);
			Point location = parent.getLocation();
			shell.setBounds(location.x + 30, location.y + 40, 560, 400);
			shell.setText("Edit Join Specifications");

			this.joinSpec = joinSpec;

			createWidgets();
		}

		/**
		 * Open the dialog
		 */
		public boolean open() {
			shell.open();

			Display display = Display.getDefault();
			while (!shell.isDisposed()) {
				if (!display.readAndDispatch())
					display.sleep();
			}

			return update;
		}

		/**
		 * Create all widgets
		 */
		protected void createWidgets() {

			GridLayout gl = new GridLayout(2, false);
			shell.setLayout(gl);

			Group group = new Group(shell, SWT.NONE);
			group.setText("Join Table");
			gl = new GridLayout(3, false);
			group.setLayout(gl);
			GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
			group.setLayoutData(gd);

			// Main table label
			Label label = new Label(group, SWT.NONE);
			label.setText("Main table");
			gd = new GridData(SWT.FILL, SWT.BOTTOM, true, false);
			label.setLayoutData(gd);

			// Join type label
			label = new Label(group, SWT.NONE);
			label.setText("Type of join");
			gd = new GridData(SWT.LEFT, SWT.BOTTOM, false, false);
			label.setLayoutData(gd);

			// Joined table label
			label = new Label(group, SWT.NONE);
			label.setText("Joined table");
			gd = new GridData(SWT.FILL, SWT.BOTTOM, true, false);
			label.setLayoutData(gd);

			// Main table combo
			mainTableCombo = new Combo(group, SWT.READ_ONLY | SWT.DROP_DOWN);
			gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
			mainTableCombo.setLayoutData(gd);

			// Join type combo
			joinTypeCombo = new Combo(group, SWT.READ_ONLY | SWT.DROP_DOWN);
			gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
			joinTypeCombo.setLayoutData(gd);
			joinTypeCombo.setItems(SQLJoinSpec.JOIN_TYPE_DESC);

			// Joined table combo
			joinedTableCombo = new Combo(group, SWT.READ_ONLY | SWT.DROP_DOWN);
			gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
			joinedTableCombo.setLayoutData(gd);

			fillTableCombos();

			// Expression table
			Group tableGroup = new Group(shell, SWT.NONE);
			tableGroup.setText("Matching columns in the joined tables");
			gl = new GridLayout(2, false);
			tableGroup.setLayout(gl);
			gd = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
			tableGroup.setLayoutData(gd);

			createAddButton(tableGroup);
			createRemoveButton(tableGroup);
			createTableControl(tableGroup);

			createPopUpMenu();
			createOKButton(shell);
			createCancelButton(shell);

			fillTable();
		}

		protected void createTableControl(Composite parent) {
			joinColumnTable = new Table(parent, SWT.BORDER | SWT.FULL_SELECTION | SWT.RESIZE);
			GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
			joinColumnTable.setLayoutData(gridData);
			joinColumnTable.setHeaderVisible(true);

			joinColumnTable.addSelectionListener(new SelectionListener() {

				public void widgetSelected(SelectionEvent event) {
					int index = joinColumnTable.getSelectionIndex();
					if (index > -1) {
						removeButton.setEnabled(true);
					} else {
						removeButton.setEnabled(false);
					}
				}

				public void widgetDefaultSelected(SelectionEvent event) {
					edit();
				}
			});

			addTableColumns();

		}

		protected void addTableColumns() {
			TableColumn tableColumn = new TableColumn(joinColumnTable, SWT.NONE);
			tableColumn.setText("Logical");
			tableColumn.setWidth(60);
			tableColumn = new TableColumn(joinColumnTable, SWT.NONE);
			tableColumn.setText("Bracket");
			tableColumn.setWidth(40);
			tableColumn = new TableColumn(joinColumnTable, SWT.NONE);
			tableColumn.setText("Value1");
			tableColumn.setWidth(185);
			tableColumn = new TableColumn(joinColumnTable, SWT.NONE);
			tableColumn.setText("Condition");
			tableColumn.setWidth(70);
			tableColumn = new TableColumn(joinColumnTable, SWT.NONE);
			tableColumn.setText("Value2");
			tableColumn.setWidth(170);
			tableColumn = new TableColumn(joinColumnTable, SWT.NONE);
			tableColumn.setText("Bracket");
			tableColumn.setWidth(40);

			packColumns();

		}

		/**
		 * Fill filter table with content.
		 */
		protected void fillTable() {
			joinColumnTable.removeAll();
			List expressions = joinSpec.getExpressions();
			for (Iterator iter = expressions.iterator(); iter.hasNext();) {
				String[] expressionPart = (String[]) iter.next();
				TableItem item = new TableItem(joinColumnTable, SWT.RESIZE);
				for (int i = 0; i < expressionPart.length; i++) {
					if (expressionPart[i] != null) {
						// item.setText(i, getQualifiedColumnName(expressionPart[i]));
						item.setText(i, expressionPart[i]);
					}
				}
			}

			if (joinColumnTable.getItemCount() > 0)
				joinColumnTable.getItem(0).setText(0, "");

			packColumns();

			enableDisableWidgets();

		}

		protected void packColumns() {
			for (int i = 0; i < joinColumnTable.getColumnCount(); i++) {
				TableColumn tc = joinColumnTable.getColumn(i);
				tc.pack();
			}
		}

		/**
		 * Creating a pop up menu
		 */
		protected Menu createPopUpMenu() {
			Menu visualMenu = new Menu(joinColumnTable.getShell(), SWT.POP_UP);
			joinColumnTable.setMenu(visualMenu);

			MenuItem menuAdd = new MenuItem(visualMenu, SWT.PUSH);
			menuAdd.setText("Add");
			menuAdd.addListener(SWT.Selection, new Listener() {

				public void handleEvent(Event e) {
					add();
				}
			});

			MenuItem menuDelete = new MenuItem(visualMenu, SWT.PUSH);
			menuDelete.setText("Delete");
			menuDelete.addListener(SWT.Selection, new Listener() {

				public void handleEvent(Event e) {
					delete();
				}
			});

			MenuItem menuEdit = new MenuItem(visualMenu, SWT.PUSH);
			menuEdit.setText("Edit...");
			menuEdit.addListener(SWT.Selection, new Listener() {

				public void handleEvent(Event e) {
					edit();
				}
			});

			return visualMenu;
		}

		protected void createCancelButton(Composite parent) {
			Button cancelButton = new Button(parent, SWT.PUSH);
			cancelButton.setText("Cancel");
			GridData gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
			gd.widthHint = 100;
			gd.heightHint = 25;
			cancelButton.setLayoutData(gd);
			Listener listener = new Listener() {

				public void handleEvent(Event event) {
					update = false;
					shell.close();
				}
			};
			cancelButton.addListener(SWT.Selection, listener);
		}

		/**
		 * Get columns
		 * 
		 * @return columns
		 */
		protected String getColumns() {
			StringBuffer columnBuff = new StringBuffer();
			boolean firstTime = true;
			for (int i = 0, n = joinColumnTable.getItemCount(); i < n; i++) {
				TableItem item = joinColumnTable.getItem(i);
				if (!firstTime) {
					columnBuff.append(",");
				}
				firstTime = false;
				if (item.getText(0).indexOf(".") == -1)
					columnBuff.append(mainTableCombo.getText() + "." + item.getText(0));
				else
					columnBuff.append(item.getText(0));
				columnBuff.append(item.getText(1));
				if (item.getText(2).indexOf(".") == -1)
					columnBuff.append(joinedTableCombo.getText() + "." + item.getText(2));
				else
					columnBuff.append(item.getText(2));

			}
			return columnBuff.toString();
		}

		protected void createOKButton(Composite parent) {
			okButton = new Button(parent, SWT.PUSH);
			okButton.setText("OK");
			GridData gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
			gd.widthHint = 100;
			gd.heightHint = 25;
			okButton.setLayoutData(gd);
			Listener listener = new Listener() {

				public void handleEvent(Event event) {
					joinSpec.mainTableName = mainTableCombo.getText().trim();
					joinSpec.joinType = SQLJoinSpec.convertJoinType(joinTypeCombo.getText());
					joinSpec.joinedTableName = joinedTableCombo.getText().trim();
					update = true;
					shell.close();
				}
			};
			okButton.addListener(SWT.Selection, listener);
		}

		protected void createAddButton(Composite parent) {
			Button addButton = new Button(parent, SWT.PUSH);
			addButton.setText("Add Column");
			GridData gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
			gd.widthHint = 100;
			gd.heightHint = 25;
			addButton.setLayoutData(gd);

			Listener listener = new Listener() {

				public void handleEvent(Event event) {
					add();
				}
			};
			addButton.addListener(SWT.Selection, listener);
		}

		protected void createRemoveButton(Composite parent) {
			removeButton = new Button(parent, SWT.PUSH);
			removeButton.setText("Remove Column");
			GridData gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
			gd.widthHint = 100;
			gd.heightHint = 25;
			removeButton.setLayoutData(gd);
			removeButton.setEnabled(false);
			Listener listener = new Listener() {

				public void handleEvent(Event event) {
					delete();
				}
			};
			removeButton.addListener(SWT.Selection, listener);
		}

		/**
		 * Fill main table combo and joined table combo with table names.
		 */
		protected void fillTableCombos() {

			mainTableCombo.add("");
			for (Iterator iter = queryData.getSelectedTables().iterator(); iter.hasNext();) {
				SQLTable sqltable = (SQLTable) iter.next();
				String value = sqltable.getDisplayName();
				mainTableCombo.add(value);
				joinedTableCombo.add(value);
			}

			boolean first = false;
			if (table.getItemCount() == 0)
				first = true;
			if (table.getItemCount() > 0 && joinSpec.mainTableName.trim().length() > 0)
				first = true;

			if (first) {
				if (joinSpec.mainTableName.trim().length() == 0)
					mainTableCombo.select(1);
				else
					mainTableCombo.setText(joinSpec.mainTableName);
				mainTableCombo.setEnabled(true);
			} else {
				mainTableCombo.setText("");
				mainTableCombo.setEnabled(false);
			}

			String type = SQLJoinSpec.convertJoinType(joinSpec.joinType);
			if (type.trim().length() == 0)
				joinTypeCombo.select(1);
			else
				joinTypeCombo.setText(type);

			if (joinSpec.joinedTableName != null && joinSpec.joinedTableName.trim().length() > 0)
				joinedTableCombo.setText(joinSpec.joinedTableName);
			else
				joinedTableCombo.select(1);

		}

		/**
		 * Edit selected column
		 */
		protected void edit() {
			int itemIndex = joinColumnTable.getSelectionIndex();
			String[] expressionPart = (String[]) joinSpec.getExpressions().get(itemIndex);
			String[] updatedExpressionPart = new String[expressionPart.length];
			System.arraycopy(expressionPart, 0, updatedExpressionPart, 0, expressionPart.length);
			SQLExpressionEditDialog dialog = new SQLExpressionEditDialog(getShell(), updatedExpressionPart, itemIndex == 0);
			if (dialog.open()) {
				joinSpec.getExpressions().set(itemIndex, updatedExpressionPart);
				fillTable();
			}
		}

		/**
		 * add column
		 */
		protected void add() {
			int itemCount = joinColumnTable.getItemCount();
			String[] updatedExpressionPart = new String[6];
			SQLExpressionEditDialog dialog = new SQLExpressionEditDialog(getShell(), updatedExpressionPart, itemCount == 0);
			if (dialog.open()) {
				joinSpec.getExpressions().add(updatedExpressionPart);
				fillTable();
			}

		}

		/**
		 * Delete selected column(s)
		 */
		protected void delete() {
			int itemIndex = joinColumnTable.getSelectionIndex();
			if (itemIndex == -1)
				return;

			joinSpec.getExpressions().remove(itemIndex);
			fillTable();
		}

		protected void enableDisableWidgets() {

			if (okButton == null)
				return;

			if (joinColumnTable.getItemCount() == 0)
				okButton.setEnabled(false);
			else
				okButton.setEnabled(true);

		}

	}

}
