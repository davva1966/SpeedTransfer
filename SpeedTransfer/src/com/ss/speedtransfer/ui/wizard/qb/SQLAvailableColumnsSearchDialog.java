// {{CopyrightNotice}}

package com.ss.speedtransfer.ui.wizard.qb;

import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
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
import org.eclipse.swt.widgets.Text;

import com.ss.speedtransfer.util.SSUtil;


/**
 * The <code>SQLAvailableColumnsSearchDialog</code> class
 */
public class SQLAvailableColumnsSearchDialog extends Dialog {

	protected Shell shell = null;

	protected SQLQueryData queryData = null;

	protected SQLColumn selectedColumn = null;

	protected Text columnSearchText;

	protected Text descSearchText;

	protected Combo tableSelectCombo;

	protected Table columnTable;

	protected Button okButton;

	protected boolean columnSelected = false;

	public SQLAvailableColumnsSearchDialog(Shell parent, SQLQueryData queryData) {
		this(parent, SWT.PRIMARY_MODAL, queryData);
	}

	public SQLAvailableColumnsSearchDialog(Shell parent, int style, SQLQueryData queryData) {
		super(parent, style);
		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE);

		Point location = parent.getLocation();
		shell.setBounds(location.x + 50, location.y + 70, 600, 600);
		shell.setText("Column Search");

		this.queryData = queryData;
		createWidgets();
	}

	public boolean open() {
		shell.open();

		Display display = Display.getDefault();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}

		return columnSelected;

	}

	/**
	 * Create all widgets
	 */
	public void createWidgets() {

		GridLayout layout = new GridLayout(2, false);
		layout.verticalSpacing = 10;
		shell.setLayout(layout);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		shell.setLayoutData(gridData);

		// Table name group
		Group group = new Group(shell, SWT.NONE);
		group.setText("Search");
		layout = new GridLayout(3, false);
		group.setLayout(layout);
		gridData = new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1);
		group.setLayoutData(gridData);

		// Column
		Label tableLabel = new Label(group, SWT.NONE);
		tableLabel.setText(" Column");
		gridData = new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1);
		gridData.widthHint = 60;
		tableLabel.setLayoutData(gridData);

		// Description
		Label descLabel = new Label(group, SWT.NONE);
		descLabel.setText(" Description");
		gridData = new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1);
		gridData.widthHint = 90;
		descLabel.setLayoutData(gridData);

		// Table
		Label schemaSelectLabel = new Label(group, SWT.NONE);
		schemaSelectLabel.setText("Table");
		gridData = new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1);
		schemaSelectLabel.setLayoutData(gridData);

		// Column search
		columnSearchText = new Text(group, SWT.SINGLE | SWT.BORDER);
		gridData = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		gridData.widthHint = 60;
		columnSearchText.setLayoutData(gridData);
		columnSearchText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				fillTable();
			}

		});

		// Description search
		descSearchText = new Text(group, SWT.SINGLE | SWT.BORDER);
		gridData = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		gridData.widthHint = 90;
		descSearchText.setLayoutData(gridData);
		descSearchText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				fillTable();
			}

		});

		// Table combo
		tableSelectCombo = new Combo(group, SWT.SINGLE | SWT.BORDER);
		gridData = new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1);
		tableSelectCombo.setLayoutData(gridData);
		tableSelectCombo.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				fillTable();
			}

		});

		List tables = queryData.getSelectedTables();
		for (Iterator iter = tables.iterator(); iter.hasNext();) {
			SQLTable table = (SQLTable) iter.next();
			tableSelectCombo.add(table.getDisplayName());
		}

		// Column table group
		Group tableGroup = new Group(shell, SWT.NONE);
		tableGroup.setText("Columns");
		layout = new GridLayout(1, true);
		tableGroup.setLayout(layout);
		gridData = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
		tableGroup.setLayoutData(gridData);

		int tableStyle = SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION;
		columnTable = new Table(tableGroup, tableStyle);
		columnTable.setHeaderVisible(true);

		gridData = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gridData.verticalIndent = 3;
		columnTable.setLayoutData(gridData);

		columnTable.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent event) {
				enableDisableWidgets();
			}

			public void widgetDefaultSelected(SelectionEvent event) {
				select();
			}
		});

		TableColumn tableColumn = new TableColumn(columnTable, SWT.NONE);
		tableColumn.setText("Name");
		tableColumn.setWidth(100);

		tableColumn = new TableColumn(columnTable, SWT.NONE);
		tableColumn.setText("Description");
		tableColumn.setWidth(150);

		tableColumn = new TableColumn(columnTable, SWT.NONE);
		tableColumn.setText("Length");
		tableColumn.setWidth(60);

		tableColumn = new TableColumn(columnTable, SWT.NONE);
		tableColumn.setText("Decimals");
		tableColumn.setWidth(60);

		tableColumn = new TableColumn(columnTable, SWT.NONE);
		tableColumn.setText("Type");
		tableColumn.setWidth(60);

		for (int i = 0; i < columnTable.getColumnCount(); i++) {
			TableColumn tc = columnTable.getColumn(i);
			tc.pack();
		}

		// OK Button
		okButton = new Button(shell, SWT.PUSH);
		okButton.setText("OK");
		gridData = new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1);
		gridData.widthHint = 120;
		gridData.heightHint = 30;
		okButton.setLayoutData(gridData);
		Listener listener = new Listener() {

			public void handleEvent(Event event) {
				columnSelected = true;
				select();
			}
		};
		okButton.addListener(SWT.Selection, listener);
		okButton.setEnabled(false);

		// Cancel Button
		Button cancelButton = new Button(shell, SWT.PUSH);
		cancelButton.setText("Cancel");
		gridData = new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1);
		gridData.widthHint = 120;
		gridData.heightHint = 30;
		cancelButton.setLayoutData(gridData);
		listener = new Listener() {

			public void handleEvent(Event event) {
				columnSelected = false;
				shell.close();
			}
		};
		cancelButton.addListener(SWT.Selection, listener);

		fillTable();

		createPopUpMenu();

		tableSelectCombo.select(0);

	}

	/**
	 * Create source table popup menu
	 */
	public void createPopUpMenu() {

		Menu visualMenu = new Menu(columnTable.getShell(), SWT.POP_UP);
		columnTable.setMenu(visualMenu);

		MenuItem menuAdd = new MenuItem(visualMenu, SWT.PUSH);
		menuAdd.setText("Select");
		menuAdd.addListener(SWT.Selection, new Listener() {

			public void handleEvent(Event e) {
				select();
			}
		});

		MenuItem sep = new MenuItem(visualMenu, SWT.SEPARATOR);
		MenuItem menuSampleContents = new MenuItem(visualMenu, SWT.PUSH);
		menuSampleContents.setText("Sample Contents...");
		menuSampleContents.addListener(SWT.Selection, new Listener() {

			public void handleEvent(Event e) {
				sampleContents();
			}
		});

	}

	/**
	 * Show column contents dialog
	 */
	protected void sampleContents() {
		TableItem[] items = columnTable.getSelection();
		if (items != null && items.length > 0) {
			String selectedTable = tableSelectCombo.getText();
			SQLTable table = (SQLTable) queryData.getSelectedTable(selectedTable);
			SQLColumn column = (SQLColumn) table.getAvailableColumn(items[0].getText(0));
			SQLTableContentsSampleDialog dialog = new SQLTableContentsSampleDialog(columnTable.getShell(), queryData, table, column);
			dialog.open();
		}

	}

	protected void fillTable() {

		columnTable.setRedraw(false);
		columnTable.removeAll();

		SQLTable table = queryData.getSelectedTable(tableSelectCombo.getText());
		if (table != null) {
			List columns = (List) table.getAvailableColumns();

			if (columns != null) {

				String columnSearch = columnSearchText.getText();
				String descSearch = descSearchText.getText();

				for (Iterator itr = columns.iterator(); itr.hasNext();) {
					SQLColumn column = (SQLColumn) itr.next();

					// Add in source table
					if (columnSearch != null && columnSearch.trim().length() > 0 && column.name.toUpperCase().indexOf(columnSearch.toUpperCase()) == -1)
						continue;
					if (descSearch != null && descSearch.trim().length() > 0 && column.remark.toUpperCase().indexOf(descSearch.toUpperCase()) == -1)
						continue;

					TableItem item = new TableItem(columnTable, SWT.NONE);
					int c = 0;
					item.setText(c++, SSUtil.trimNull(column.name));
					item.setText(c++, SSUtil.trimNull(column.remark));
					item.setText(c++, SSUtil.trimNull(column.length));
					item.setText(c++, SSUtil.trimNull(column.decimals));
					item.setText(c++, SSUtil.trimNull(column.dataTypeName));

				}
			}
		}

		columnTable.setRedraw(true);

		for (int i = 0; i < columnTable.getColumnCount(); i++) {
			TableColumn tc = columnTable.getColumn(i);
			tc.pack();
		}
	}

	protected void enableDisableWidgets() {

		if (okButton == null)
			return;

		if (columnTable.getSelectionIndex() == -1)
			okButton.setEnabled(false);
		else
			okButton.setEnabled(true);

	}

	protected void select() {

		TableItem item = columnTable.getItem(columnTable.getSelectionIndex());
		String colName = item.getText(0);

		SQLTable table = queryData.getSelectedTable(tableSelectCombo.getText());
		selectedColumn = table.getAvailableColumn(colName);

		columnSelected = true;
		shell.close();

	}

	public SQLColumn getSelectedColumn() {
		return selectedColumn;

	}

}
