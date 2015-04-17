// {{CopyrightNotice}}

package com.ss.speedtransfer.ui.wizard.qb;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
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
import org.eclipse.swt.widgets.Text;

import com.ss.speedtransfer.util.SSUtil;
import com.ss.speedtransfer.util.UIHelper;
import com.ss.speedtransfer.util.ValueSelectionDialog;


/**
 * The <code>SQLTableSelectionComp</code> class
 */
public class SQLTableSelectionComp extends SQLQBComposite {

	Text tableSearchText;

	Text descSearchText;

	/** Source table composite */
	protected Table sourceTable;

	/** Destination table composite */
	protected Table destTable;

	/** Comparator */
	protected TableComparator comparator;

	/** Include system tables */
	protected boolean includeSystemTables = false;

	protected boolean includeViews = false;

	public SQLTableSelectionComp(WizardPage page, Composite parent, SQLQueryData queryData) {
		this(page, parent, SWT.RESIZE, queryData);
	}

	/**
	 * Initializes a newly created <code>SQLTableSelectionComp</code>
	 * 
	 * @param parent
	 * @param style
	 * @param con
	 * @param queryData
	 */
	public SQLTableSelectionComp(WizardPage page, Composite parent, int style, SQLQueryData queryData) {
		super(page, parent, style, queryData);

		// Init Comparator
		this.comparator = new TableComparator();
		comparator.setColumn(TableComparator.NAME);
		comparator.setDirection(TableComparator.ASCENDING);

		// Set default schema on on unqualifed selected tables
		updateSelectedTables();

		// Create all widgets for this composite
		createWidgets();
	}

	/**
	 * Create search labels
	 */
	protected void createSearchGroup() {

		// The search group
		Group group = createGroup(this);
		group.setText("Search");
		group.setLayout(new GridLayout(4, false));

		GridData gridData = new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1);
		gridData.widthHint = DFT_WIDTH_LEFT_COL;
		group.setLayoutData(gridData);

		// Table
		Label tableLabel = new Label(group, SWT.NONE);
		tableLabel.setText(" Table");

		// Table search
		tableSearchText = new Text(group, SWT.SINGLE | SWT.BORDER);
		gridData = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		tableSearchText.setLayoutData(gridData);
		tableSearchText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				fillSourceTable();
			}

		});

		// Description
		Label descLabel = new Label(group, SWT.NONE);
		descLabel.setText(" Description");

		// Description search
		descSearchText = new Text(group, SWT.SINGLE | SWT.BORDER);
		gridData = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		descSearchText.setLayoutData(gridData);
		descSearchText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				fillSourceTable();
			}

		});

	}

	/**
	 * Create settings group
	 */
	protected void createSettingsGroup() {

		// The settings group
		Group group = createGroup(this);
		group.setText("Table selection settings");
		group.setLayout(new GridLayout(2, false));

		GridData gridData = new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1);
		gridData.widthHint = DFT_WIDTH_RIGHT_COL;
		group.setLayoutData(gridData);

		final Button viewsCheck = new Button(group, SWT.CHECK);
		viewsCheck.setText("Show Views");

		gridData = new GridData(SWT.FILL, SWT.CENTER, true, true, 1, 1);
		gridData.horizontalIndent = 10;
		viewsCheck.setLayoutData(gridData);

		viewsCheck.addListener(SWT.Selection, new Listener() {

			public void handleEvent(Event event) {
				includeViews = (includeViews != true) ? true : false;
				BusyIndicator.showWhile(null, new Runnable() {

					public void run() {
						fillTables();
					}
				});
			}
		});

		// System table checkbox
		final Button sysTableCheck = new Button(group, SWT.CHECK);
		sysTableCheck.setText("   Show system tables");

		sysTableCheck.addListener(SWT.Selection, new Listener() {

			public void handleEvent(Event event) {
				includeSystemTables = (includeSystemTables != true) ? true : false;
				BusyIndicator.showWhile(null, new Runnable() {

					public void run() {
						fillTables();
					}
				});
			}
		});

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

		sourceTable.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent event) {
			}

			public void widgetDefaultSelected(SelectionEvent event) {
				add();
			}
		});

		TableColumn tableColumn = new TableColumn(sourceTable, SWT.NONE);
		tableColumn.setText("Table");

		tableColumn.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent event) {
				comparator.setColumn(TableComparator.NAME);
				comparator.reverseDirection();
				fillSourceTable();
			}
		});

		tableColumn = new TableColumn(sourceTable, SWT.NONE);
		tableColumn.setText("Description");

		tableColumn.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent event) {
				comparator.setColumn(TableComparator.DESCRIPTION);
				comparator.reverseDirection();
				fillSourceTable();
			}
		});

		tableColumn = new TableColumn(sourceTable, SWT.NONE);
		if (queryData.getQueryDefinition().getDBConnection().getTypeName().equalsIgnoreCase("iseries") || queryData.getQueryDefinition().getDBConnection().getTypeName().equalsIgnoreCase("as400"))
			tableColumn.setText("Library");
		else
			tableColumn.setText("Schema");

		tableColumn.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent event) {
				comparator.setColumn(TableComparator.SCHEMA);
				comparator.reverseDirection();
				fillSourceTable();
			}
		});

		tableColumn = new TableColumn(sourceTable, SWT.NONE);
		tableColumn.setText("Owner");

		tableColumn.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent event) {
				comparator.setColumn(TableComparator.OWNER);
				comparator.reverseDirection();
				fillSourceTable();
			}
		});

		tableColumn = new TableColumn(sourceTable, SWT.NONE);
		tableColumn.setText("Type");

		tableColumn.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent event) {
				comparator.setColumn(TableComparator.TYPE);
				comparator.reverseDirection();
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
		int tableStyle = SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION;
		destTable = new Table(this, tableStyle);
		destTable.setHeaderVisible(true);

		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gridData.verticalIndent = 3;
		gridData.minimumHeight = DFT_MIN_HEIGHT;
		gridData.widthHint = DFT_WIDTH_RIGHT_COL;
		destTable.setLayoutData(gridData);

		destTable.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent event) {
			}

			public void widgetDefaultSelected(SelectionEvent event) {
				edit();
			}
		});

		TableColumn tableColumn = new TableColumn(destTable, SWT.NONE);
		tableColumn.setText("Table");
		tableColumn = new TableColumn(destTable, SWT.NONE);
		tableColumn.setText("Description");
		tableColumn = new TableColumn(destTable, SWT.NONE);
		if (queryData.getQueryDefinition().getDBConnection().getTypeName().equalsIgnoreCase("iseries") || queryData.getQueryDefinition().getDBConnection().getTypeName().equalsIgnoreCase("as400"))
			tableColumn.setText("Library");
		else
			tableColumn.setText("Schema");

		for (int i = 0; i < 3; i++) {
			TableColumn tc = destTable.getColumn(i);
			tc.pack();
		}
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
		single2Dest.setToolTipText("Add selected tables");
		single2Dest.setBounds(xpos, ypos, width, height);

		int addY = height + 5;
		final Button single2Source = new Button(buttonComp, SWT.PUSH);
		single2Source.setText("<");
		single2Source.setToolTipText("Remove selected tables");
		single2Source.setBounds(xpos, ypos + addY, width, height);

		addY = addY + height + 5;
		final Button multi2Source = new Button(buttonComp, SWT.PUSH);
		multi2Source.setText("<<<");
		multi2Source.setToolTipText("Remove all tables");
		multi2Source.setBounds(xpos, ypos + addY, width, height);

		Listener listner = new Listener() {

			public void handleEvent(Event event) {
				if (event.widget == single2Dest) {
					add();
					return;
				}
				if (event.widget == single2Source) {
					delete();
					return;
				}
				if (event.widget == multi2Source) {
					queryData.removeAllTables();
					fillDestTable();
					return;
				}
			}
		};

		single2Dest.addListener(SWT.Selection, listner);
		single2Source.addListener(SWT.Selection, listner);
		multi2Source.addListener(SWT.Selection, listner);
	}

	/**
	 * Create all widgets and load content
	 */
	protected void createWidgets() {

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

		fillTables();

	}

	/**
	 * Create source table popup menu
	 */
	public void createSrcPopUpMenu() {

		Menu visualMenu = new Menu(this.getShell(), SWT.POP_UP);
		sourceTable.setMenu(visualMenu);

		MenuItem menuRefresh = new MenuItem(visualMenu, SWT.PUSH);
		menuRefresh.setText("Refresh");
		menuRefresh.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				queryData.refreshAvailableTables();
				fillSourceTable();
				setFocus();
			}
		});
		MenuItem sep1 = new MenuItem(visualMenu, SWT.SEPARATOR);

		MenuItem menuAdd = new MenuItem(visualMenu, SWT.PUSH);
		menuAdd.setText("Add");
		menuAdd.addListener(SWT.Selection, new Listener() {

			public void handleEvent(Event e) {
				add();
			}
		});

		MenuItem sep2 = new MenuItem(visualMenu, SWT.SEPARATOR);
		MenuItem menuSampleContents = new MenuItem(visualMenu, SWT.PUSH);
		menuSampleContents.setText("Sample Contents...");
		menuSampleContents.addListener(SWT.Selection, new Listener() {

			public void handleEvent(Event e) {
				sampleSrcContents();
			}
		});

	}

	/**
	 * Create destination table popup menu
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
		MenuItem menuSampleContents = new MenuItem(visualMenu, SWT.PUSH);
		menuSampleContents.setText("Sample Contents...");
		menuSampleContents.addListener(SWT.Selection, new Listener() {

			public void handleEvent(Event e) {
				sampleDestContents();
			}
		});

	}

	/**
	 * Edit selected table
	 */
	protected void edit() {
		int itemIndex = destTable.getSelectionIndex();
		SQLTable table = (SQLTable) queryData.getSelectedTables().get(itemIndex);
		SQLTable updatedTable = queryData.newTable(table);
		SQLTableFunctionDialog tf = new SQLTableFunctionDialog(getShell(), updatedTable);
		if (tf.open()) {
			queryData.getSelectedTables().set(itemIndex, updatedTable);
			if (table.getDisplayName().equalsIgnoreCase(updatedTable.getDisplayName()) == false)
				queryData.updateTableName(table.getDisplayName(), updatedTable.getDisplayName());
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

		queryData.removeTables(items);

		fillDestTable();
	}

	/**
	 * Move to destination table
	 */
	protected void add() {

		BusyIndicator.showWhile(null, new Runnable() {

			public void run() {

				TableItem[] items = sourceTable.getSelection();
				for (int i = 0; i < items.length; i++) {
					TableItem item = items[i];
					SQLTable table = queryData.newTable();
					table.name = item.getText(0);
					table.remark = item.getText(1);
					table.schema = item.getText(2);
					if (verifyAdd(table))
						queryData.addTable(table);
				}
				fillDestTable();
			}
		});
	}

	/**
	 * Fill tables with data
	 */
	protected void fillTables() {
		fillSourceTable();
		fillDestTable();

	}

	/**
	 * Fill source table with data
	 */
	protected void fillSourceTable() {

		BusyIndicator.showWhile(null, new Runnable() {

			public void run() {

				// Get tables
				List tempTableList = getSourceTables(tableSearchText.getText(), descSearchText.getText());

				// Turn off drawing to avoid flicker
				sourceTable.setRedraw(false);
				// We remove all the table entries, sort our
				// rows, then add the entries
				sourceTable.removeAll();

				for (Iterator itr = tempTableList.iterator(); itr.hasNext();) {
					SQLTable table = (SQLTable) itr.next();

					int c = 0;
					TableItem item = new TableItem(sourceTable, SWT.NONE);
					item.setText(c++, SSUtil.trimNull(table.name));
					item.setText(c++, SSUtil.trimNull(table.remark));
					item.setText(c++, SSUtil.trimNull(table.schema));
					item.setText(c++, SSUtil.trimNull(table.owner));
					item.setText(c++, SSUtil.trimNull(table.type));
				}

				// Turn drawing back on
				sourceTable.setRedraw(true);

				for (int i = 0; i < 5; i++) {
					TableColumn tc = sourceTable.getColumn(i);
					tc.pack();
				}

				updateWizardPage();

			}
		});
	}

	/**
	 * Get source tables
	 */
	protected List getSourceTables(String nameSearchArg, String descSearchArg) {

		List tables = new ArrayList();

		for (Iterator iter = queryData.getAvailableTables().keySet().iterator(); iter.hasNext();) {
			String key = (String) iter.next();
			SQLTable table = (SQLTable) queryData.getAvailableTables().get(key);

			if (table.type.equals("TABLE") || (table.type.equals("VIEW") && includeViews) || (table.type.equals("SYSTEM TABLE") && includeSystemTables)) {

				if (nameSearchArg != null && nameSearchArg.trim().length() > 0 && table.name.toUpperCase().indexOf(nameSearchArg.toUpperCase()) == -1)
					continue;
				if (descSearchArg != null && descSearchArg.trim().length() > 0 && table.remark == null)
					continue;
				if (descSearchArg != null && descSearchArg.trim().length() > 0 && table.remark.toUpperCase().indexOf(descSearchArg.toUpperCase()) == -1)
					continue;

				tables.add(queryData.getAvailableTables().get(key));
			}
		}

		Collections.sort(tables, comparator);
		return tables;

	}

	/**
	 * Fill dest table with data
	 */
	protected void fillDestTable() {

		BusyIndicator.showWhile(null, new Runnable() {

			public void run() {

				// Turn off drawing to avoid flicker
				destTable.setRedraw(false);
				// We remove all the table entries, then add the entries
				destTable.removeAll();

				for (Iterator iter = queryData.getSelectedTables().iterator(); iter.hasNext();) {
					SQLTable table = (SQLTable) iter.next();

					// Find table description
					String remarks = null;
					if (table.remark != null && table.remark.trim().length() > 0) {
						remarks = table.remark;
					} else {
						String qualName = table.getSchema().trim() + table.schemaSeparator + table.name.trim();
						SQLTable refTable = (SQLTable) queryData.getAvailableTables().get(qualName.toUpperCase());
						if (refTable == null) {
							Set nameSet = queryData.getAvailableTables().keySet();
							for (Iterator iterator = nameSet.iterator(); iterator.hasNext();) {
								String tName = (String) iterator.next();
								String n = table.schemaSeparator + table.name;
								if (tName.trim().endsWith(n.trim().toUpperCase())) {
									refTable = (SQLTable) queryData.getAvailableTables().get(tName);
									remarks = refTable.remark;
									break;
								}

							}
						} else {
							remarks = refTable.remark;
						}
					}

					if (remarks == null) {
						remarks = "";
						Connection jdbc = queryData.getConnection();

						ResultSet rs = null;
						try {
							DatabaseMetaData md = jdbc.getMetaData();
							rs = md.getTables(null, table.schema, table.name, null);
							if (rs.next())
								remarks = rs.getString(5);
						} catch (SQLException e) {
						} finally {
							try {
								if (rs != null)
									rs.close();
							} catch (Exception e) {
								// TODO: handle exception
							}
						}
					}

					table.remark = remarks;

					TableItem item = new TableItem(destTable, SWT.NONE);
					int c = 0;
					item.setText(c++, SSUtil.trimNull(table.getDisplayName()));
					item.setText(c++, SSUtil.trimNull(remarks));
					item.setText(c++, SSUtil.trimNull(table.getSchema()));
				}

				// Turn drawing back on
				destTable.setRedraw(true);

				for (int i = 0; i < 3; i++) {
					TableColumn tc = destTable.getColumn(i);
					tc.pack();
				}
				updateWizardPage();

			}
		});
	}

	/**
	 * This class does the comparisons for sorting Player objects.
	 */
	class TableComparator implements Comparator {

		/** Constant for First Name column */
		public static final int NAME = 0;

		/** Constant for description column */
		public static final int DESCRIPTION = 1;

		/** Constant for schema column */
		public static final int SCHEMA = 2;

		/** Constant for schema column */
		public static final int OWNER = 3;

		/** Constant for schema column */
		public static final int TYPE = 4;

		/** Constant for ascending */
		public static final int ASCENDING = 0;

		/** Constant for descending */
		public static final int DESCENDING = 1;

		/** Column no */
		private int column;

		/** direction mode */
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
			SQLTable p1 = (SQLTable) obj1;
			SQLTable p2 = (SQLTable) obj2;

			// Determine which field to sort on, then sort
			// on that field
			switch (column) {
			case NAME:
				rc = p1.name.compareTo(p2.name);
				break;
			case DESCRIPTION:
				rc = p1.remark.compareTo(p2.remark);
				break;
			case SCHEMA:
				rc = p1.schema.compareTo(p2.schema);
				break;
			case OWNER:
				rc = p1.owner.compareTo(p2.owner);
				break;
			case TYPE:
				rc = p1.type.compareTo(p2.type);
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
	 * The <code>SQLTableFunctionDialog</code> class
	 */
	class SQLTableFunctionDialog extends Dialog {

		protected Shell shell = null;

		protected Combo schemaCombo;

		protected Combo tableCombo;

		protected Combo aliasCombo;

		protected SQLTable table = null;

		protected boolean update = false;

		public SQLTableFunctionDialog(Shell parent, SQLTable table) {
			this(parent, SWT.PRIMARY_MODAL, table);
		}

		public SQLTableFunctionDialog(Shell parent, int style, SQLTable table) {
			super(parent, style);
			shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE);

			Point location = parent.getLocation();
			shell.setBounds(location.x + 50, location.y + 70, 500, 210);
			shell.setText("Table Properties");

			this.table = table;
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

			// Table
			final Group tableGroup = new Group(shell, SWT.NONE);
			tableGroup.setText("Table");
			layout = new GridLayout(2, false);
			tableGroup.setLayout(layout);
			gridData = new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1);
			tableGroup.setLayoutData(gridData);

			// Table combo
			tableCombo = new Combo(tableGroup, SWT.DROP_DOWN);
			gridData = new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1);
			tableCombo.setLayoutData(gridData);

			// Fill table combo
			fillTableCombo();

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
			aliasCombo.setText(table.alias);

			createOkButton();
			createCancelButton();

		}

		protected void createCancelButton() {
			Button cancelButton = new Button(shell, SWT.PUSH);
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

		protected void createOkButton() {
			Button okButton = new Button(shell, SWT.PUSH);
			okButton.setText("OK");
			GridData gridData = new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1);
			gridData.widthHint = 120;
			gridData.heightHint = 30;
			okButton.setLayoutData(gridData);

			Listener listener = new Listener() {

				public void handleEvent(Event event) {
					if (table != null) {
						table.schema = schemaCombo.getText();
						String name = tableCombo.getText();
						int end = name.indexOf(" - ");
						if (end > -1)
							name = name.substring(0, end);
						table.name = name;
						table.alias = aliasCombo.getText();
					}
					if (validate(table)) {
						update = true;
						shell.close();
					}

				}
			};
			okButton.addListener(SWT.Selection, listener);
		}

		protected void fillTableCombo() {

			BusyIndicator.showWhile(null, new Runnable() {

				public void run() {

					// Get tables
					List tempTableList = getSourceTables(null, null);

					String oldSel = tableCombo.getText();
					if (oldSel == null || oldSel.trim().length() == 0)
						oldSel = table.name;
					tableCombo.removeAll();

					int selected = -1;
					int count = 0;
					for (Iterator itr = tempTableList.iterator(); itr.hasNext();) {
						SQLTable tab = (SQLTable) itr.next();
						if (tab.name.equalsIgnoreCase(table.name))
							selected = count;
						String entry = tab.name.trim();
						if (tab.remark.trim().length() > 0)
							entry = entry + " - " + tab.remark.trim();
						tableCombo.add(entry);
						count++;
					}

					if (selected > -1)
						tableCombo.select(selected);
					else
						tableCombo.setText(oldSel);

				}
			});
		}

		protected boolean validate(SQLTable table) {

			for (Iterator iter = queryData.getSelectedTables().iterator(); iter.hasNext();) {
				SQLTable selTable = (SQLTable) iter.next();
				if (selTable.getDisplayName().equalsIgnoreCase(table.getDisplayName()) && selTable != table.tableRef) {
					UIHelper.instance()
							.showErrorMsg(getShell(), "Error", "A table with name (or alias) " + table.getDisplayName() + " is already selected", SWT.ICON_ERROR | SWT.OK | SWT.ON_TOP, null);
					return false;
				}
			}

			return true;

		}

	}

	/**
	 * Change button state for wizard page
	 */
	protected void updateWizardPage() {
		if (wizardPage.getWizard().getContainer().getCurrentPage() == null) {
			wizardPage.setPageComplete(false);
		} else {
			if (queryData.getSelectedTables() == null || queryData.getSelectedTables().isEmpty()) {
				wizardPage.setPageComplete(false);
			} else {
				wizardPage.setPageComplete(true);
			}
			wizardPage.getWizard().getContainer().updateButtons();
		}
	}

	public void setVisible(boolean visible) {
		updateWizardPage();
		super.setVisible(visible);

	}

	protected void updateSelectedTables() {

		for (Iterator iter = queryData.getSelectedTables().iterator(); iter.hasNext();) {
			SQLTable table = (SQLTable) iter.next();
			if (table.schema == null || table.schema.trim().length() == 0) {
				Set nameSet = queryData.getAvailableTables().keySet();
				for (Iterator iterator = nameSet.iterator(); iterator.hasNext();) {
					String tName = (String) iterator.next();
					if (table.schemaSeparator != null && table.schemaSeparator.trim().length() > 0) {
						String n = table.schemaSeparator + table.name;
						if (tName.trim().endsWith(n.trim().toUpperCase())) {
							SQLTable refTable = (SQLTable) queryData.getAvailableTables().get(tName);
							table.schema = refTable.schema;
							break;
						}
					}

				}

			}

		}
	}

	protected boolean verifyAdd(SQLTable table) {

		for (Iterator iter = queryData.getSelectedTables().iterator(); iter.hasNext();) {
			SQLTable selTable = (SQLTable) iter.next();
			if (selTable.getDisplayName().equalsIgnoreCase(table.getDisplayName())) {
				ValueSelectionDialog valuePrompt = new ValueSelectionDialog();
				valuePrompt.setText("Duplicate table names selected");
				valuePrompt.setSubtitle("A table with name (or alias) " + table.name + " is already selected");
				valuePrompt.setValueLabel("Specify the alias to use for table " + table.name);
				String alias = valuePrompt.open();
				if (alias == null)
					return false;
				table.alias = alias;
				return verifyAdd(table);
			}

		}

		return true;

	}

	/**
	 * Show source table contents dialog
	 */
	protected void sampleSrcContents() {
		TableItem[] items = sourceTable.getSelection();
		if (items != null && items.length > 0) {
			SQLTable table = queryData.newTable();
			table.name = items[0].getText(0);
			table.remark = items[0].getText(1);
			table.schema = items[0].getText(2);
			SQLTableContentsSampleDialog dialog = new SQLTableContentsSampleDialog(getShell(), queryData, table, null);
			dialog.open();
		}

	}

	/**
	 * Show destination table contents dialog
	 */
	protected void sampleDestContents() {
		int itemIndex = destTable.getSelectionIndex();
		SQLTable table = (SQLTable) queryData.getSelectedTables().get(itemIndex);
		SQLTableContentsSampleDialog dialog = new SQLTableContentsSampleDialog(getShell(), queryData, table, null);
		dialog.open();

	}

}
