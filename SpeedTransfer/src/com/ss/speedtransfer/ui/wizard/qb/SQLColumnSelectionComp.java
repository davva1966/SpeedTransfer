// {{CopyrightNotice}}

package com.ss.speedtransfer.ui.wizard.qb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
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
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import com.ss.speedtransfer.ui.StyledEdit;
import com.ss.speedtransfer.util.SSUtil;
import com.ss.speedtransfer.util.UIHelper;
import com.ss.speedtransfer.util.parser.sql.SQLParserHelper;
import com.ss.speedtransfer.util.sql.SQLConfiguration;


/**
 * The <code>SQLColumnSelectionDialog</code> class
 */
public class SQLColumnSelectionComp extends SQLQBComposite {

	/** Source table */
	protected Table sourceTable;

	/** Destination table */
	protected Table destTable;

	/** Move down buttons */
	ToolItem moveDownBtn;

	MenuItem moveDownMnu;

	/** Move up buttons */
	ToolItem moveUpBtn;

	MenuItem moveUpMnu;

	MenuItem menuSampleDestContents;

	Text columnSearchText;

	Text descSearchText;

	/** Table select combo */
	protected Combo tableSelectCombo;

	/** Source comparator */
	protected ColumnComparator sourceComparator;

	/** Destination comparator */
	protected ColumnComparator destinationComparator;

	/** Distinct values */
	protected boolean distinct = false;

	/** */
	protected static final String SQL_FUNCTION = "SQL_FUNCTION";

	/** Column size */
	protected static final int DFT_WIDTH_LEFT_COL = 300;

	/** Column size */
	protected static final int DFT_WIDTH_MID_COL = 75;

	/** Column size */
	protected static final int DFT_WIDTH_RIGHT_COL = 275;

	/** Column size */
	protected static final int DFT_MIN_HEIGHT = 250;

	public SQLColumnSelectionComp(WizardPage page, Composite parent, SQLQueryData queryData) {
		this(page, parent, SWT.RESIZE, queryData);
	}

	/**
	 * Initializes a newly created <code>SQLColumnSelectionComp</code>
	 * 
	 * @param parent
	 * @param style
	 * @param queryData
	 */
	public SQLColumnSelectionComp(WizardPage page, Composite parent, int style, SQLQueryData queryData) {
		super(page, parent, style, queryData);

		// Comparator for source table
		sourceComparator = new ColumnComparator();
		sourceComparator.setColumn(ColumnComparator.NAME);
		sourceComparator.setDirection(ColumnComparator.ASCENDING);

		// Comparator for destination table
		destinationComparator = new ColumnComparator();
		destinationComparator.setColumn(ColumnComparator.NAME);
		destinationComparator.setDirection(ColumnComparator.ASCENDING);

		// Create all widgets.
		createWidgets();
	}

	public void setVisible(boolean visible) {
		if (visible) {
			fillTableCombo(tableSelectCombo);
			updateSelectedColumns();
			fillTables();
		}
		super.setVisible(visible);

	}

	/**
	 * Create search labels
	 */
	protected void createSearchGroup() {

		// Search group
		Group group = createGroup(this);
		group.setText("Search");
		group.setLayout(new GridLayout(3, false));

		GridData gridData = new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1);
		gridData.widthHint = DFT_WIDTH_LEFT_COL;
		group.setLayoutData(gridData);

		// Column
		Label tableLabel = new Label(group, SWT.NONE);
		tableLabel.setText(" Column");
		gridData = new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1);
		gridData.widthHint = 60;
		tableLabel.setLayoutData(gridData);

		// Description
		Label descLabel = new Label(group, SWT.NONE);
		descLabel.setText(" Description");
		gridData = new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1);
		gridData.widthHint = 90;
		descLabel.setLayoutData(gridData);

		// Table
		Label schemaSelectLabel = new Label(group, SWT.NONE);
		schemaSelectLabel.setText("Table");
		gridData = new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1);
		schemaSelectLabel.setLayoutData(gridData);

		// Column search
		columnSearchText = new Text(group, SWT.SINGLE | SWT.BORDER);
		gridData = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
		gridData.widthHint = 60;
		columnSearchText.setLayoutData(gridData);
		columnSearchText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				fillSourceTable();
			}

		});

		// Description search
		descSearchText = new Text(group, SWT.SINGLE | SWT.BORDER);
		gridData = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
		gridData.widthHint = 90;
		descSearchText.setLayoutData(gridData);
		descSearchText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				fillSourceTable();
			}

		});

		// Table combo
		tableSelectCombo = new Combo(group, SWT.SINGLE | SWT.BORDER);
		gridData = new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1);
		tableSelectCombo.setLayoutData(gridData);

		tableSelectCombo.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				// Comparator for source table
				sourceComparator.setColumn(ColumnComparator.NAME);
				sourceComparator.setDirection(ColumnComparator.ASCENDING);
				// Comparator for destination table
				destinationComparator.setColumn(ColumnComparator.NAME);
				destinationComparator.setDirection(ColumnComparator.ASCENDING);
				fillTables();
			}
		});

	}

	/**
	 * Create settings group
	 */
	protected void createSettingsGroup() {

		Group group = createGroup(this);
		group.setText("Column selection settings");
		group.setLayout(new GridLayout(1, false));

		GridData gridData = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
		gridData.widthHint = DFT_WIDTH_RIGHT_COL;
		group.setLayoutData(gridData);

		// Distinct
		final Button distinctCheck = new Button(group, SWT.CHECK);
		distinctCheck.setText("   Distinct values only");

		gridData = new GridData(SWT.FILL, SWT.CENTER, true, true, 1, 1);
		gridData.horizontalIndent = 10;
		distinctCheck.setLayoutData(gridData);

		distinctCheck.addListener(SWT.Selection, new Listener() {

			public void handleEvent(Event event) {
				distinct = (distinct != true) ? true : false;
				queryData.setDistinct(distinctCheck.getSelection());
			}
		});

		// Update from data holder
		distinctCheck.setSelection(queryData.isDistinct());

	}

	/**
	 * Create source table control
	 */
	protected void createSourceTableControl() {
		int tableStyle = SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION;
		sourceTable = new Table(this, tableStyle);
		sourceTable.setHeaderVisible(true);

		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gridData.widthHint = DFT_WIDTH_LEFT_COL;
		gridData.verticalIndent = 3;
		sourceTable.setLayoutData(gridData);

		sourceTable.addListener(SWT.MouseDoubleClick, new Listener() {

			public void handleEvent(Event event) {
				add(false);
			}
		});

		TableColumn tableColumn = new TableColumn(sourceTable, SWT.NONE);
		tableColumn.setText("Name");
		tableColumn.setWidth(100);
		tableColumn.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent event) {
				sourceComparator.setColumn(ColumnComparator.NAME);
				sourceComparator.reverseDirection();
				fillSourceTable();
			}
		});

		tableColumn = new TableColumn(sourceTable, SWT.NONE);
		tableColumn.setText("Description");
		tableColumn.setWidth(150);
		tableColumn.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent event) {
				sourceComparator.setColumn(ColumnComparator.DESCRIPTION);
				sourceComparator.reverseDirection();
				fillSourceTable();
			}
		});

		tableColumn = new TableColumn(sourceTable, SWT.NONE);
		tableColumn.setText("Length");
		tableColumn.setWidth(60);
		tableColumn.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent event) {
				sourceComparator.setColumn(ColumnComparator.LENGTH);
				sourceComparator.reverseDirection();
				fillSourceTable();
			}
		});

		tableColumn = new TableColumn(sourceTable, SWT.NONE);
		tableColumn.setText("Decimals");
		tableColumn.setWidth(60);
		tableColumn.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent event) {
				sourceComparator.setColumn(ColumnComparator.DECIMAL);
				sourceComparator.reverseDirection();
				fillSourceTable();
			}
		});

		tableColumn = new TableColumn(sourceTable, SWT.NONE);
		tableColumn.setText("Type");
		tableColumn.setWidth(60);
		tableColumn.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent event) {
				sourceComparator.setColumn(ColumnComparator.TYPE);
				sourceComparator.reverseDirection();
				fillSourceTable();
			}
		});

		for (int i = 0; i < 5; i++) {
			TableColumn tc = sourceTable.getColumn(i);
			tc.pack();
		}

	}

	/**
	 * Create destination table control
	 */
	protected void createDestTableControl() {

		Composite tableComp = new Composite(this, SWT.NONE);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		tableComp.setLayoutData(gridData);
		GridLayout gl = new GridLayout(2, false);
		gl.horizontalSpacing = 0;
		gl.verticalSpacing = 0;
		gl.marginHeight = 0;
		gl.marginWidth = 0;
		tableComp.setLayout(gl);

		int tableStyle = SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION;
		destTable = new Table(tableComp, tableStyle);
		destTable.setHeaderVisible(true);

		gridData = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gridData.minimumHeight = DFT_MIN_HEIGHT;
		gridData.widthHint = DFT_WIDTH_RIGHT_COL;
		destTable.setLayoutData(gridData);

		destTable.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent event) {
				enableDisableWidgets();
			}

			public void widgetDefaultSelected(SelectionEvent event) {
				edit();
			}
		});

		TableColumn tableColumn = new TableColumn(destTable, SWT.NONE);
		tableColumn.setText("Name");
		tableColumn.setWidth(100);

		tableColumn = new TableColumn(destTable, SWT.NONE);
		tableColumn.setText("Description");
		tableColumn.setWidth(150);

		tableColumn = new TableColumn(destTable, SWT.NONE);
		tableColumn.setText("Table");
		tableColumn.setWidth(100);

		tableColumn = new TableColumn(destTable, SWT.NONE);
		tableColumn.setText("Length");
		tableColumn.setWidth(60);
		tableColumn.setAlignment(SWT.RIGHT);

		tableColumn = new TableColumn(destTable, SWT.NONE);
		tableColumn.setText("Decimals");
		tableColumn.setWidth(60);
		tableColumn.setAlignment(SWT.RIGHT);

		tableColumn = new TableColumn(destTable, SWT.NONE);
		tableColumn.setText("Type");
		tableColumn.setWidth(60);

		for (int i = 0; i < 6; i++) {
			TableColumn tc = destTable.getColumn(i);
			tc.pack();
		}

		Composite buttonComp = new Composite(tableComp, SWT.NONE);
		gridData = new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1);
		buttonComp.setLayoutData(gridData);
		buttonComp.setLayout(new FillLayout());

		ToolBar tb = new ToolBar(buttonComp, SWT.VERTICAL | SWT.LEFT);

		// Move up button
		moveUpBtn = new ToolItem(tb, SWT.PUSH);
		Image upImage = UIHelper.instance().getImage("up.gif");
		if (upImage != null)
			moveUpBtn.setImage(upImage);
		else
			moveUpBtn.setText("U");
		moveUpBtn.setToolTipText("Move Up");
		moveUpBtn.setEnabled(false);
		moveUpBtn.addListener(SWT.Selection, new Listener() {

			public void handleEvent(Event e) {
				moveUp();
			}
		});

		// Move down button
		moveDownBtn = new ToolItem(tb, SWT.PUSH);
		Image downImage = UIHelper.instance().getImage("down.gif");
		if (downImage != null)
			moveDownBtn.setImage(downImage);
		else
			moveDownBtn.setText("D");
		moveDownBtn.setToolTipText("Move Down");
		moveDownBtn.setEnabled(false);
		moveDownBtn.addListener(SWT.Selection, new Listener() {

			public void handleEvent(Event e) {
				moveDown();
			}
		});

	}

	/**
	 * Create selection buttons
	 */
	protected void createSelectionButtons() {
		Composite buttonComp = new Composite(this, SWT.NONE);
		GridData gridData = new GridData(SWT.CENTER, SWT.CENTER, false, true, 1, 3);
		gridData.widthHint = DFT_WIDTH_MID_COL;
		gridData.minimumHeight = DFT_MIN_HEIGHT;
		buttonComp.setLayoutData(gridData);

		int xpos = 15;
		int ypos = 20;
		int width = 45;
		int height = 35;
		final Button single2Dest = new Button(buttonComp, SWT.PUSH);

		single2Dest.setText(">");
		single2Dest.setToolTipText("Add selected columns");
		single2Dest.setBounds(xpos, ypos, width, height);

		int addY = height + 5;
		final Button single2Source = new Button(buttonComp, SWT.PUSH);
		single2Source.setText("<");
		single2Source.setToolTipText("Remove selected columns");
		single2Source.setBounds(xpos, ypos + addY, width, height);

		addY = addY + height + 5;
		final Button multi2Dest = new Button(buttonComp, SWT.PUSH);
		multi2Dest.setText(">>>");
		multi2Dest.setToolTipText("Add all columns");
		multi2Dest.setBounds(xpos, ypos + addY, width, height);

		addY = addY + height + 5;
		final Button multi2Source = new Button(buttonComp, SWT.PUSH);
		multi2Source.setText("<<<");
		multi2Source.setToolTipText("Remove all columns");
		multi2Source.setBounds(xpos, ypos + addY, width, height);

		Listener listner = new Listener() {

			public void handleEvent(Event event) {
				if (event.widget == single2Dest) {
					add(false);
					return;
				}
				if (event.widget == single2Source) {
					delete();
					return;
				}
				if (event.widget == multi2Dest) {
					add(true);
					return;
				}
				if (event.widget == multi2Source) {
					queryData.removeAllColumns();
					fillDestTable();
					return;
				}
			}
		};

		single2Dest.addListener(SWT.Selection, listner);
		single2Source.addListener(SWT.Selection, listner);
		multi2Dest.addListener(SWT.Selection, listner);
		multi2Source.addListener(SWT.Selection, listner);

		addY = addY + height + 5;
		addColumnFunctionButton(buttonComp, xpos, ypos + addY, width, height);

	}

	protected void addColumnFunctionButton(Composite parent, int xpos, int ypos, int width, int height) {
		final Button columnFunction = new Button(parent, SWT.PUSH);
		columnFunction.setImage(UIHelper.instance().getImage("function.gif"));
		columnFunction.setToolTipText("Define a column function");
		columnFunction.setBounds(xpos, ypos, width, height);

		columnFunction.addListener(SWT.Selection, new Listener() {

			public void handleEvent(Event e) {
				if (e.widget == columnFunction) {
					TableItem[] items = sourceTable.getSelection();
					String selectedCol = "";
					if (items != null && items.length > 0)
						selectedCol = items[0].getText();

					SQLColumn column = new SQLColumn();
					column.name = selectedCol;
					column.table = tableSelectCombo.getText();

					SQLColumnFunctionDialog cf = new SQLColumnFunctionDialog(getShell(), column);
					if (cf.open())
						queryData.addColumn(column);
					fillDestTable();

				}
			}
		});

	}

	/**
	 * Create all widgets
	 */
	public void createWidgets() {

		GridLayout gl = new GridLayout(3, false);
		gl.horizontalSpacing = 0;
		gl.verticalSpacing = 3;
		gl.marginHeight = 0;
		gl.marginWidth = 0;
		this.setLayout(gl);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		this.setLayoutData(gridData);

		createSearchGroup();
		createSelectionButtons();
		createSettingsGroup();
		createSourceTableControl();
		createDestTableControl();
		createSrcPopUpMenu();
		createDestPopUpMenu();

	}

	/**
	 * Create source table popup menu
	 */
	public void createSrcPopUpMenu() {

		Menu visualMenu = new Menu(this.getShell(), SWT.POP_UP);
		sourceTable.setMenu(visualMenu);

		MenuItem menuAdd = new MenuItem(visualMenu, SWT.PUSH);
		menuAdd.setText("Add");
		menuAdd.addListener(SWT.Selection, new Listener() {

			public void handleEvent(Event e) {
				add(false);
			}
		});

		MenuItem sep = new MenuItem(visualMenu, SWT.SEPARATOR);
		MenuItem menuSampleContents = new MenuItem(visualMenu, SWT.PUSH);
		menuSampleContents.setText("Sample Contents...");
		menuSampleContents.addListener(SWT.Selection, new Listener() {

			public void handleEvent(Event e) {
				sampleSrcContents();
			}
		});

	}

	/**
	 * Create destinations table popup menu
	 */
	public void createDestPopUpMenu() {
		Menu visualMenu = new Menu(this.getShell(), SWT.POP_UP);
		destTable.setMenu(visualMenu);

		MenuItem menuAdd = new MenuItem(visualMenu, SWT.PUSH);
		menuAdd.setText("Edit...");
		menuAdd.addListener(SWT.Selection, new Listener() {

			public void handleEvent(Event e) {
				edit();
			}
		});

		MenuItem menuDelete = new MenuItem(visualMenu, SWT.PUSH);
		menuDelete.setText("Delete");
		menuDelete.addListener(SWT.Selection, new Listener() {

			public void handleEvent(Event e) {
				delete();
			}
		});

		MenuItem sep = new MenuItem(visualMenu, SWT.SEPARATOR);
		moveUpMnu = new MenuItem(visualMenu, SWT.PUSH);
		moveUpMnu.setText("Move Up");
		Image upImage = UIHelper.instance().getImage("up.gif");
		if (upImage != null)
			moveUpMnu.setImage(upImage);

		moveUpMnu.addListener(SWT.Selection, new Listener() {

			public void handleEvent(Event e) {
				moveUp();
			}
		});

		moveDownMnu = new MenuItem(visualMenu, SWT.PUSH);
		moveDownMnu.setText("Move Down");
		Image downImage = UIHelper.instance().getImage("down.gif");
		if (downImage != null)
			moveDownMnu.setImage(downImage);
		moveDownMnu.addListener(SWT.Selection, new Listener() {

			public void handleEvent(Event e) {
				moveDown();
			}
		});

		sep = new MenuItem(visualMenu, SWT.SEPARATOR);
		menuSampleDestContents = new MenuItem(visualMenu, SWT.PUSH);
		menuSampleDestContents.setText("Sample Contents...");
		menuSampleDestContents.addListener(SWT.Selection, new Listener() {

			public void handleEvent(Event e) {
				sampleDestContents();
			}
		});

	}

	/**
	 * Edit selected column
	 */
	protected void edit() {
		int itemIndex = destTable.getSelectionIndex();
		SQLColumn column = (SQLColumn) queryData.getSelectedColumns().get(itemIndex);
		SQLColumn updatedColumn = new SQLColumn(column);
		SQLColumnFunctionDialog cf = new SQLColumnFunctionDialog(getShell(), updatedColumn);
		if (cf.open()) {
			queryData.getSelectedColumns().set(itemIndex, updatedColumn);
			fillDestTable();
		}
	}

	/**
	 * Delete selected column(s)
	 */
	protected void delete() {
		int[] items = destTable.getSelectionIndices();
		if (items.length == 0)
			return;

		queryData.removeColumns(items);

		fillDestTable();
	}

	/**
	 * Move selected column up
	 */
	protected void moveUp() {

		int itemIndex = destTable.getSelectionIndex();
		if (itemIndex > 0) {
			SQLColumn curr = (SQLColumn) queryData.getSelectedColumns().get(itemIndex);
			SQLColumn above = (SQLColumn) queryData.getSelectedColumns().get(itemIndex - 1);
			queryData.getSelectedColumns().set(itemIndex, above);
			queryData.getSelectedColumns().set(itemIndex - 1, curr);
			fillDestTable();
			destTable.select(itemIndex - 1);
		}

		enableDisableWidgets();

	}

	/**
	 * Move selected column down
	 */
	protected void moveDown() {
		int itemIndex = destTable.getSelectionIndex();
		if (itemIndex > -1 && itemIndex < destTable.getItemCount() - 1) {
			SQLColumn curr = (SQLColumn) queryData.getSelectedColumns().get(itemIndex);
			SQLColumn below = (SQLColumn) queryData.getSelectedColumns().get(itemIndex + 1);
			queryData.getSelectedColumns().set(itemIndex, below);
			queryData.getSelectedColumns().set(itemIndex + 1, curr);
			fillDestTable();
			destTable.select(itemIndex + 1);
		}

		enableDisableWidgets();

	}

	/**
	 * add column
	 */
	protected void add(boolean all) {
		TableItem[] items;
		if (all)
			items = sourceTable.getItems();
		else
			items = sourceTable.getSelection();
		for (int i = 0; i < items.length; i++) {
			TableItem item = items[i];
			SQLColumn column = new SQLColumn();
			column.name = item.getText();
			column.table = tableSelectCombo.getText();
			queryData.addColumn(column);
		}
		fillDestTable();
	}

	/**
	 * Refresh content
	 */
	public void refresh() {
		fillTableCombo(tableSelectCombo);
		fillTables();
	}

	/**
	 * Fill tables
	 */
	protected void fillTables() {
		BusyIndicator.showWhile(null, new Runnable() {

			public void run() {
				fillSourceTable();
				fillDestTable();
			}
		});

	}

	/**
	 * Fill destination table with data
	 */
	protected void fillDestTable() {
		// Turn off drawing to avoid flicker
		destTable.setRedraw(false);
		// We remove all the table entries, then add the entries
		destTable.removeAll();

		Map availableColumns = queryData.getAvailableColumns();

		List selectedColumns = queryData.getSelectedColumns();
		for (Iterator iter = selectedColumns.iterator(); iter.hasNext();) {
			SQLColumn column = (SQLColumn) iter.next();

			String remarks = "";
			String length = "";
			String decimals = "";
			String type = "";

			TableItem item = new TableItem(destTable, SWT.NONE);
			int c = 0;

			if (column.hasFunction == false) {
				// Find column info
				SQLColumn refColumn = (SQLColumn) availableColumns.get(column.getQualifiedName().toUpperCase());
				if (refColumn == null) {
					Set nameSet = availableColumns.keySet();
					for (Iterator iterator = nameSet.iterator(); iterator.hasNext();) {
						String cName = (String) iterator.next();
						String n = "." + column.name;
						if (cName.trim().endsWith(n.trim().toUpperCase())) {
							refColumn = (SQLColumn) availableColumns.get(cName);
							remarks = refColumn.remark;
							length = refColumn.length;
							decimals = refColumn.decimals;
							type = refColumn.dataTypeName;
							break;
						}

					}
				} else {
					remarks = refColumn.remark;
					length = refColumn.length;
					decimals = refColumn.decimals;
					type = refColumn.dataTypeName;
				}

			}

			item.setText(c++, SSUtil.trimNull(column.getDisplayName()));
			if (column.getDescription() != null)
				item.setText(c++, column.getDescription());
			else
				item.setText(c++, SSUtil.trimNull(remarks));
			item.setText(c++, SSUtil.trimNull(column.table));
			item.setText(c++, SSUtil.trimNull(length));
			item.setText(c++, SSUtil.trimNull(decimals));
			item.setText(c++, SSUtil.trimNull(type));

		}

		// Turn drawing back on
		destTable.setRedraw(true);

		for (int i = 0; i < 6; i++) {
			TableColumn tc = destTable.getColumn(i);
			tc.pack();
		}

		enableDisableWidgets();

	}

	/**
	 * Fill source table with data
	 */
	protected void fillSourceTable() {

		// Turn off drawing to avoid flicker
		sourceTable.setRedraw(false);
		// We remove all the table entries, sort our rows, then add the entries
		sourceTable.removeAll();

		String selectedTable = tableSelectCombo.getText();
		SQLTable table = (SQLTable) queryData.getSelectedTable(selectedTable);
		List columns = (List) table.getAvailableColumns();

		if (columns != null) {
			Collections.sort(columns, sourceComparator);

			String columnSearch = columnSearchText.getText();
			String descSearch = descSearchText.getText();

			for (Iterator itr = columns.iterator(); itr.hasNext();) {
				SQLColumn column = (SQLColumn) itr.next();

				// Add in source table
				if (columnSearch != null && columnSearch.trim().length() > 0 && column.name.toUpperCase().indexOf(columnSearch.toUpperCase()) == -1)
					continue;
				if (descSearch != null && descSearch.trim().length() > 0 && column.remark == null)
					continue;
				if (descSearch != null && descSearch.trim().length() > 0 && column.remark.toUpperCase().indexOf(descSearch.toUpperCase()) == -1)
					continue;

				TableItem item = new TableItem(sourceTable, SWT.NONE);
				int c = 0;
				item.setText(c++, SSUtil.trimNull(column.name));
				item.setText(c++, SSUtil.trimNull(column.remark));
				item.setText(c++, SSUtil.trimNull(column.length));
				item.setText(c++, SSUtil.trimNull(column.decimals));
				item.setText(c++, SSUtil.trimNull(column.dataTypeName));

			}
		}

		// Turn drawing back on
		sourceTable.setRedraw(true);

		for (int i = 0; i < sourceTable.getColumnCount(); i++) {
			TableColumn tc = sourceTable.getColumn(i);
			tc.pack();
		}
	}

	/**
	 * Fill table combo with data
	 */
	protected void fillTableCombo(Combo tableSelectCombo) {
		tableSelectCombo.removeAll();
		List tables = queryData.getSelectedTables();
		for (Iterator iter = tables.iterator(); iter.hasNext();) {
			SQLTable table = (SQLTable) iter.next();
			tableSelectCombo.add(table.getDisplayName());
		}
		tableSelectCombo.select(0);
	}

	/**
	 * This class does the comparisons for sorting objects.
	 */
	class ColumnComparator implements Comparator {

		/** Constant for First Name column */
		public static final int NAME = 0;

		/** Constant for description column */
		public static final int DESCRIPTION = 1;

		/** Constant for length column */
		public static final int LENGTH = 2;

		/** Constant for type column */
		public static final int TYPE = 3;

		/** Constant for decimal column */
		public static final int DECIMAL = 4;

		/** Constant for table column */
		public static final int TABLE = 5;

		/** Constant for ascending */
		public static final int ASCENDING = 0;

		/** Constant for descending */
		public static final int DESCENDING = 1;

		private int column;

		private int direction;

		/**
		 * Compares two table objects
		 * 
		 * @param obj1
		 *            the first object
		 * @param obj2
		 *            the second object
		 * @return int
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(Object obj1, Object obj2) {
			int rc = 0;
			SQLColumn p1 = (SQLColumn) obj1;
			SQLColumn p2 = (SQLColumn) obj2;

			// Determine which field to sort on, then sort
			// on that field
			switch (column) {
			case NAME:
				rc = p1.name.compareTo(p2.name);
				break;
			case DESCRIPTION:
				rc = p1.remark.compareTo(p2.remark);
				break;
			case LENGTH:
				rc = p1.length.compareTo(p2.length);
				break;
			case TYPE:
				rc = p1.dataTypeName.compareTo(p2.dataTypeName);
				break;
			case DECIMAL:
				rc = p1.decimals.compareTo(p2.decimals);
				break;
			case TABLE:
				rc = p1.table.compareTo(p2.table);
				break;

			}

			// Check the direction for sort and flip the sign
			// if appropriate
			if (direction == DESCENDING) {
				rc = -rc;
			}
			return rc;
		}

		/**
		 * Sets the column for sorting
		 * 
		 * @param column
		 *            the column
		 */
		public void setColumn(int column) {
			this.column = column;
		}

		/**
		 * Sets the direction for sorting
		 * 
		 * @param direction
		 *            the direction
		 */
		public void setDirection(int direction) {
			this.direction = direction;
		}

		/**
		 * Reverses the direction
		 */
		public void reverseDirection() {
			direction = 1 - direction;
		}

	}

	/**
	 * The <code>SQLColumnFunctionDialog</code> class
	 */
	class SQLColumnFunctionDialog extends Dialog {

		protected Shell shell = null;

		protected Composite parent = null;

		protected Combo functionCombo;

		protected Combo tableCombo;

		protected Combo columnCombo;

		protected Button qualifyCheckButton;

		protected Button distinctCheckButton;

		protected Combo aliasCombo;

		protected StyledEdit calcEdit;

		protected Button okButton;

		protected Button cancelButton;

		protected String selectedTable;

		protected SQLColumn column = null;

		protected boolean update = false;

		/**
		 * Initializes a newly created <code>SQLColumnFunctionDialog</code>
		 * 
		 * @param parent
		 * @param columnData
		 * @param selectedColumn
		 */
		public SQLColumnFunctionDialog(Shell parent, SQLColumn column) {
			this(parent, SWT.PRIMARY_MODAL, column);
		}

		/**
		 * Initializes a newly created <code>SQLColumnFunctionDialog</code>
		 * 
		 * @param parent
		 * @param style
		 * @param columnData
		 * @param selectedColumn
		 */
		public SQLColumnFunctionDialog(Shell parent, int style, SQLColumn column) {
			super(parent, style);
			shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE);

			Point location = parent.getLocation();
			shell.setBounds(location.x + 50, location.y + 70, 550, 420);
			shell.setText("Column Function");

			this.column = column;
			createWidgets();
		}

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
		public void createWidgets() {

			GridLayout layout = new GridLayout(2, false);
			layout.verticalSpacing = 10;
			shell.setLayout(layout);
			GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
			shell.setLayoutData(gridData);

			// Column
			final Group colGroup = new Group(shell, SWT.NONE);
			colGroup.setText("Table/Column");
			layout = new GridLayout(3, false);
			colGroup.setLayout(layout);
			gridData = new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1);
			colGroup.setLayoutData(gridData);

			tableCombo = new Combo(colGroup, SWT.READ_ONLY | SWT.DROP_DOWN);
			gridData = new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1);
			gridData.widthHint = 75;
			tableCombo.setLayoutData(gridData);
			tableCombo.addSelectionListener(new SelectionAdapter() {

				public void widgetSelected(SelectionEvent e) {
					fillColumnCombo();
				}
			});

			columnCombo = new Combo(colGroup, SWT.DROP_DOWN);
			gridData = new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1);
			columnCombo.setLayoutData(gridData);
			columnCombo.addModifyListener(new ModifyListener() {

				public void modifyText(ModifyEvent e) {
					enableDisableWidgets();
				}

			});

			// Qualify check box
			qualifyCheckButton = new Button(colGroup, SWT.CHECK);
			qualifyCheckButton.setText("Qualify with table");
			gridData = new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1);
			qualifyCheckButton.setLayoutData(gridData);

			// Alias
			Group aliasGroup = new Group(shell, SWT.NONE);
			aliasGroup.setText("Alias Name");
			layout = new GridLayout(1, true);
			aliasGroup.setLayout(layout);
			gridData = new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1);
			aliasGroup.setLayoutData(gridData);

			aliasCombo = new Combo(aliasGroup, SWT.BORDER | SWT.FULL_SELECTION);
			gridData = new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1);
			aliasCombo.setLayoutData(gridData);

			// Function
			final Group functionGroup = new Group(shell, SWT.NONE);
			functionGroup.setText("Aggregate Functions");
			layout = new GridLayout(1, true);
			functionGroup.setLayout(layout);
			gridData = new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1);
			functionGroup.setLayoutData(gridData);

			functionCombo = new Combo(functionGroup, SWT.READ_ONLY | SWT.BORDER | SWT.FULL_SELECTION);
			gridData = new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1);
			functionCombo.setLayoutData(gridData);

			distinctCheckButton = new Button(functionGroup, SWT.CHECK);
			distinctCheckButton.setText("Distinct");
			gridData = new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1);
			distinctCheckButton.setLayoutData(gridData);

			// Calculation
			Group calcGroup = new Group(shell, SWT.NONE);
			calcGroup.setText("Calculated Value");
			// layout = new GridLayout(1, true);
			FillLayout fl = new FillLayout();
			fl.marginHeight = 6;
			fl.marginWidth = 6;
			calcGroup.setLayout(fl);
			gridData = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
			calcGroup.setLayoutData(gridData);

			SQLConfiguration config = new SQLConfiguration();
			calcEdit = new StyledEdit(calcGroup, config);
			calcEdit.getControl().addKeyListener(new KeyListener() {

				public void keyReleased(KeyEvent e) {
					enableDisableWidgets();
				}

				public void keyPressed(KeyEvent e) {
				}
			});

			createOkButton(330, 340, 70, 30);
			createCancelButton(405, 340, 70, 30);

			// Initialize values

			fillTableCombo(tableCombo);
			fillColumnCombo();
			fillFunctionCombo();

			functionCombo.addModifyListener(new ModifyListener() {

				public void modifyText(ModifyEvent e) {
					enableDisableWidgets();
				};
			});

			aliasCombo.setText(column.alias);

			tableCombo.select(tableSelectCombo.getSelectionIndex());
			String[] items = tableCombo.getItems();
			for (int i = 0; i < items.length; i++) {
				if (items[i].trim().toUpperCase().equalsIgnoreCase(column.table.trim().toUpperCase())) {
					tableCombo.select(i);
					break;
				}
			}
			fillColumnCombo();

			items = columnCombo.getItems();
			columnCombo.setText(column.name.trim());
			for (int i = 0; i < items.length; i++) {
				if (items[i].trim().toUpperCase().startsWith(column.name.trim().toUpperCase())) {
					columnCombo.select(i);
					break;
				}
			}

			qualifyCheckButton.setSelection(column.qualified);

			if (column.aggregateFunction != null && column.aggregateFunction.trim().length() > 0) {
				items = functionCombo.getItems();
				for (int i = 0; i < items.length; i++) {
					if (items[i].trim().toUpperCase().equalsIgnoreCase(column.aggregateFunction.trim().toUpperCase())) {
						functionCombo.select(i);
						break;
					}
				}
				distinctCheckButton.setSelection(column.aggregateFunctionDistinct);
			} else {

				calcEdit.setText(column.getDescription());
			}

			enableDisableWidgets();

			// Add combo menues
			SQLQBComposite.createMenu(columnCombo, queryData);

		}

		/**
		 * Create cancel button
		 * 
		 * @param xpos
		 * @param ypos
		 * @param width
		 * @param height
		 */
		protected void createCancelButton(int xpos, int ypos, int width, int height) {
			cancelButton = new Button(shell, SWT.PUSH);
			cancelButton.setText("Cancel");
			GridData gridData = new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1);
			gridData.widthHint = 120;
			gridData.heightHint = 30;
			cancelButton.setLayoutData(gridData);

			Listener listener = new Listener() {

				public void handleEvent(Event event) {
					update = false;
					shell.close();
				}
			};
			cancelButton.addListener(SWT.Selection, listener);
		}

		/**
		 * Create OK button
		 * 
		 * @param xpos
		 * @param ypos
		 * @param width
		 * @param height
		 */
		protected void createOkButton(int xpos, int ypos, int width, int height) {
			okButton = new Button(shell, SWT.PUSH);
			okButton.setText("OK");
			GridData gridData = new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1);
			gridData.widthHint = 120;
			gridData.heightHint = 30;
			okButton.setLayoutData(gridData);

			Listener listener = new Listener() {

				public void handleEvent(Event event) {
					if (column != null) {

						column.alias = aliasCombo.getText();
						column.hasFunction = false;

						if (calcEdit.getText().trim().length() == 0) {
							String name = columnCombo.getText();
							int end = name.indexOf(" - ");
							if (end > -1)
								name = name.substring(0, end);
							column.name = name;
							column.table = tableCombo.getText();
							column.qualified = qualifyCheckButton.getSelection();
							if (functionCombo.getText().trim().length() > 0) {
								column.aggregateFunction = functionCombo.getText();
								column.aggregateFunctionDistinct = distinctCheckButton.getSelection();
								column.hasFunction = true;
							} else {
								column.aggregateFunction = "";
								column.aggregateFunctionDistinct = false;
								column.hasFunction = false;
							}
						} else {
							column.name = "";
							column.table = "";
							column.qualified = false;
							column.aggregateFunction = "";
							column.aggregateFunctionDistinct = false;
							column.aggregateFunction = "";
							column.sqlString = calcEdit.getText();
							column.hasFunction = true;
						}

						column.updateSQLString();
					}
					update = true;
					shell.close();

				}
			};
			okButton.addListener(SWT.Selection, listener);
		}

		/**
		 * Fill column combo
		 */
		protected void fillColumnCombo() {

			// Turn off drawing to avoid flicker
			columnCombo.setRedraw(false);
			// We remove all the table entries, sort our rows, then add the
			// entries
			columnCombo.removeAll();

			selectedTable = tableCombo.getText();
			SQLTable table = queryData.getSelectedTable(selectedTable);
			if (table == null)
				return;

			List columns = table.getAvailableColumns();

			if (columns != null) {
				for (Iterator itr = columns.iterator(); itr.hasNext();) {
					SQLColumn column = (SQLColumn) itr.next();
					String colItemText = column.name + " - " + column.remark;
					columnCombo.add(colItemText);

				}
			}

			// Turn drawing back on
			columnCombo.setRedraw(true);

			columnCombo.select(0);

		}

		/**
		 * Fill function combo
		 */
		protected void fillFunctionCombo() {
			functionCombo.removeAll();
			functionCombo.add("");
			for (int i = 0; i < SQLParserHelper.FUNCTIONS.length; i++) {
				String item = (String) SQLParserHelper.FUNCTIONS[i];
				functionCombo.add(item);
			}
		}

		protected void enableDisableWidgets() {

			if (okButton == null)
				return;

			if (calcEdit.getText().trim().length() == 0 && columnCombo.getText().trim().length() == 0)
				okButton.setEnabled(false);
			else
				okButton.setEnabled(true);

			if (calcEdit.getText().trim().length() > 0) {
				tableCombo.setEnabled(false);
				columnCombo.setEnabled(false);
				qualifyCheckButton.setEnabled(false);
				functionCombo.setEnabled(false);
			} else {
				tableCombo.setEnabled(true);
				columnCombo.setEnabled(true);
				qualifyCheckButton.setEnabled(true);
				functionCombo.setEnabled(true);
			}

			if (functionCombo.isEnabled() && functionCombo.getText().trim().length() > 0)
				distinctCheckButton.setEnabled(true);
			else
				distinctCheckButton.setEnabled(false);

		}

	}

	protected void updateSelectedColumns() {

		List selectedColumns = queryData.getSelectedColumns();
		if (selectedColumns == null)
			selectedColumns = new ArrayList();

		Map availableColumns = queryData.getAvailableColumns();

		for (Iterator iter = selectedColumns.iterator(); iter.hasNext();) {
			SQLColumn column = (SQLColumn) iter.next();
			if (column.table == null || column.table.trim().length() == 0) {
				Set nameSet = availableColumns.keySet();
				for (Iterator iterator = nameSet.iterator(); iterator.hasNext();) {
					String cName = (String) iterator.next();
					String n = "." + column.name;
					if (cName.trim().endsWith(n.trim().toUpperCase())) {
						SQLColumn refColumn = (SQLColumn) availableColumns.get(cName);
						column.table = refColumn.table;
						column.qualified = false;
						break;
					}

				}

			}

		}
	}

	protected void enableDisableWidgets() {

		if (moveDownBtn == null || moveUpBtn == null)
			return;

		int[] selections = destTable.getSelectionIndices();
		if (selections.length == 0 || selections.length > 1) {
			moveDownBtn.setEnabled(false);
			moveDownMnu.setEnabled(false);
			moveUpBtn.setEnabled(false);
			moveUpMnu.setEnabled(false);
			moveUpMnu.setEnabled(false);
			menuSampleDestContents.setEnabled(false);
			return;
		}

		int selected = selections[0];
		SQLColumn column = (SQLColumn) queryData.getSelectedColumns().get(selected);
		if (column.hasFunction || column.name == null || column.name.trim().length() == 0)
			menuSampleDestContents.setEnabled(false);
		else
			menuSampleDestContents.setEnabled(true);

		if (selected == 0) {
			moveUpBtn.setEnabled(false);
			moveUpMnu.setEnabled(false);
			moveDownBtn.setEnabled(true);
			moveDownMnu.setEnabled(true);
			return;
		}

		if (selected == destTable.getItemCount() - 1) {
			moveUpBtn.setEnabled(true);
			moveUpMnu.setEnabled(true);
			moveDownBtn.setEnabled(false);
			moveDownMnu.setEnabled(false);
			return;
		}

		moveUpBtn.setEnabled(true);
		moveUpMnu.setEnabled(true);
		moveDownBtn.setEnabled(true);
		moveDownMnu.setEnabled(true);
	}

	/**
	 * Show source table contents dialog
	 */
	protected void sampleSrcContents() {
		TableItem[] items = sourceTable.getSelection();
		if (items != null && items.length > 0) {
			String selectedTable = tableSelectCombo.getText();
			SQLTable table = (SQLTable) queryData.getSelectedTable(selectedTable);
			SQLColumn column = (SQLColumn) table.getAvailableColumn(items[0].getText(0));
			SQLTableContentsSampleDialog dialog = new SQLTableContentsSampleDialog(getShell(), queryData, table, column);
			dialog.open();
		}

	}

	/**
	 * Show destination table contents dialog
	 */
	protected void sampleDestContents() {
		int itemIndex = destTable.getSelectionIndex();
		SQLColumn column = (SQLColumn) queryData.getSelectedColumns().get(itemIndex);
		SQLTable table = queryData.getSelectedTable(column.table);
		SQLTableContentsSampleDialog dialog = new SQLTableContentsSampleDialog(getShell(), queryData, table, column);
		dialog.open();

	}

}
