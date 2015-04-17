// {{CopyrightNotice}}

package com.ss.speedtransfer.ui.wizard.qb;

import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
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
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * The <code>SQLTableContentsSampleDialog</code> class
 */
public class SQLTableContentsSampleDialog extends Dialog {

	protected Shell shell = null;

	protected Combo rowsToSampleCombo = null;

	protected Button distinctCheck = null;

	protected SQLQueryData queryData = null;

	protected SQLTable table = null;

	protected SQLColumn column = null;

	protected Table dataTable = null;

	public SQLTableContentsSampleDialog(Shell parent, SQLQueryData queryData, SQLTable table, SQLColumn column) {
		this(parent, SWT.PRIMARY_MODAL, queryData, table, column);
	}

	public SQLTableContentsSampleDialog(Shell parent, int style, SQLQueryData queryData, SQLTable table, SQLColumn column) {
		super(parent, style);
		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE);

		Point location = parent.getLocation();
		shell.setBounds(location.x + 50, location.y + 70, 600, 600);
		shell.setText("Table Contents Sample");

		this.queryData = queryData;
		this.table = table;
		this.column = column;
		createWidgets();
	}

	public void open() {
		shell.open();

		Display display = Display.getDefault();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}

	}

	/**
	 * Create all widgets
	 */
	public void createWidgets() {

		GridLayout layout = new GridLayout(1, false);
		layout.verticalSpacing = 10;
		shell.setLayout(layout);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		shell.setLayoutData(gridData);

		// Table name group
		Group tableGroup = new Group(shell, SWT.NONE);
		tableGroup.setText("Table");
		layout = new GridLayout(1, false);
		tableGroup.setLayout(layout);
		gridData = new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1);
		tableGroup.setLayoutData(gridData);

		// Table name
		Label label = new Label(tableGroup, SWT.NONE);
		String text = table.getDisplayName().trim();
		if (table.remark != null)
			text = text + " - " + table.remark.trim();
		label.setText(text);
		gridData = new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1);
		label.setLayoutData(gridData);

		// Selection group
		Group selectionGroup = new Group(shell, SWT.NONE);
		selectionGroup.setText("Selections");
		layout = new GridLayout(3, false);
		selectionGroup.setLayout(layout);
		gridData = new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1);
		selectionGroup.setLayoutData(gridData);

		label = new Label(selectionGroup, SWT.NONE);
		label.setText("Number of rows to sample");
		gridData = new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1);
		label.setLayoutData(gridData);

		// Schema combo
		rowsToSampleCombo = new Combo(selectionGroup, SWT.DROP_DOWN | SWT.READ_ONLY);
		gridData = new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1);
		rowsToSampleCombo.setLayoutData(gridData);
		rowsToSampleCombo.add("10");
		rowsToSampleCombo.add("25");
		rowsToSampleCombo.add("50");
		rowsToSampleCombo.add("100");
		rowsToSampleCombo.add("250");
		rowsToSampleCombo.add("500");
		rowsToSampleCombo.add("1000");
		rowsToSampleCombo.select(1);
		rowsToSampleCombo.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				fillDataTable();
			}
		});

		distinctCheck = new Button(selectionGroup, SWT.CHECK);
		distinctCheck.setText("   Distinct values only");

		gridData = new GridData(SWT.FILL, SWT.CENTER, true, true, 1, 1);
		gridData.horizontalIndent = 25;
		distinctCheck.setLayoutData(gridData);

		distinctCheck.addListener(SWT.Selection, new Listener() {

			public void handleEvent(Event event) {
				fillDataTable();
			}
		});

		// Table data group
		Group tableDataGroup = new Group(shell, SWT.NONE);
		tableDataGroup.setText("Table Data");
		layout = new GridLayout(1, false);
		tableDataGroup.setLayout(layout);
		gridData = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		tableDataGroup.setLayoutData(gridData);

		// Table data group
		createDataTable(tableDataGroup);

		// OK Button
		Button okButton = new Button(shell, SWT.PUSH);
		okButton.setText("OK");
		gridData = new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1);
		gridData.widthHint = 120;
		gridData.heightHint = 30;
		okButton.setLayoutData(gridData);
		Listener listener = new Listener() {

			public void handleEvent(Event event) {
				shell.close();
			}
		};
		okButton.addListener(SWT.Selection, listener);

		// Fill table with data
		fillDataTable();

	}

	protected void createDataTable(Composite parent) {

		int tableStyle = SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION;
		dataTable = new Table(parent, tableStyle);
		dataTable.setHeaderVisible(true);

		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gridData.verticalIndent = 3;
		dataTable.setLayoutData(gridData);

		// Add table columns
		if (column != null) {
			TableColumn tableColumn = new TableColumn(dataTable, SWT.NONE);
			tableColumn.setText(column.name.trim());
		} else {
			for (Iterator iter = table.getAvailableColumns(queryData.getConnection()).iterator(); iter.hasNext();) {
				SQLColumn sqlColumn = (SQLColumn) iter.next();
				TableColumn tableColumn = new TableColumn(dataTable, SWT.NONE);
				tableColumn.setText(sqlColumn.name.trim());
			}
		}

	}

	protected void fillDataTable() {

		BusyIndicator.showWhile(null, new Runnable() {

			public void run() {

				int rows = 10;
				try {
					rows = Integer.parseInt(rowsToSampleCombo.getText());
				} catch (NumberFormatException e) {
				}
				String colName = null;
				if (column != null)
					colName = column.name.trim();
				List dataArrays = table.getSampleData(queryData.getConnection(), rows, distinctCheck.getSelection(), colName);
				if (table.isCancelled())
					return;

				dataTable.setRedraw(false);
				dataTable.removeAll();

				for (Iterator itr = dataArrays.iterator(); itr.hasNext();) {
					String[] dataArr = (String[]) itr.next();
					TableItem item = new TableItem(dataTable, SWT.NONE);
					item.setText(dataArr);
				}

				for (int i = 0; i < dataTable.getColumnCount(); i++) {
					TableColumn tc = dataTable.getColumn(i);
					tc.pack();
				}

				dataTable.setRedraw(true);

			}
		});
	}

}
