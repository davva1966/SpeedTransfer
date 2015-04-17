// {{CopyrightNotice}}

package com.ss.speedtransfer.ui.wizard.qb;

import java.util.Iterator;
import java.util.TreeSet;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
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
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import com.ss.speedtransfer.util.UIHelper;
import com.ss.speedtransfer.util.parser.sql.SQLParserHelper;


/**
 * The <code>SQLGenericEditComp</code> class
 */
public abstract class SQLGenericEditComp extends SQLQBComposite {

	protected int EDIT_PART_LENGTH = 0;

	protected boolean ITEMS_MOVEABLE = false;

	protected Table table;

	/** Move down buttons */
	ToolItem moveDownBtn;

	MenuItem moveDownMnu;

	/** Move up buttons */
	ToolItem moveUpBtn;

	MenuItem moveUpMnu;

	/** Remove button */
	protected Button removeButton = null;

	public SQLGenericEditComp(WizardPage page, Composite parent, SQLQueryData queryData) {
		this(page, parent, SWT.NONE, queryData);
	}

	public SQLGenericEditComp(WizardPage page, Composite parent, int style, SQLQueryData queryData) {
		super(page, parent, style, queryData);

		createWidgets();
		createPopUpMenu();
	}

	/**
	 * Create widgets
	 */
	protected void createWidgets() {

		GridLayout gl = new GridLayout(1, false);
		gl.horizontalSpacing = 0;
		gl.verticalSpacing = 3;
		gl.marginHeight = 0;
		gl.marginWidth = 0;
		this.setLayout(gl);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		this.setLayoutData(gridData);

		createButtons();
		createTableControl();

	}

	/**
	 * Create buttons
	 */
	protected void createButtons() {
		Composite comp = new Composite(this, SWT.NONE);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		gd.heightHint = 30;
		comp.setLayoutData(gd);
		comp.setLayout(new GridLayout(2, true));

		Button addButton = new Button(comp, SWT.PUSH);
		GridData gridData = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gridData.widthHint = 100;
		gridData.heightHint = 25;
		addButton.setLayoutData(gridData);

		addButton.setText("Add");
		Listener listener = new Listener() {

			public void handleEvent(Event event) {
				add();
				enableDisableWidgets();
			}
		};
		addButton.addListener(SWT.Selection, listener);

		removeButton = new Button(comp, SWT.PUSH);
		gridData = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gridData.widthHint = 100;
		gridData.heightHint = 25;
		gridData.horizontalIndent = 5;
		removeButton.setLayoutData(gridData);

		removeButton.setText("Remove");
		removeButton.setEnabled(false);
		listener = new Listener() {

			public void handleEvent(Event event) {
				delete();
				enableDisableWidgets();
			}
		};
		removeButton.addListener(SWT.Selection, listener);
	}

	/**
	 * Create filter table control
	 */
	protected void createTableControl() {

		Composite tableComp = new Composite(this, SWT.NONE);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		tableComp.setLayoutData(gridData);
		GridLayout gl = new GridLayout(1, false);
		if (ITEMS_MOVEABLE)
			gl.numColumns = 2;
		gl.horizontalSpacing = 0;
		gl.verticalSpacing = 0;
		gl.marginHeight = 0;
		gl.marginWidth = 0;
		tableComp.setLayout(gl);

		table = new Table(tableComp, SWT.BORDER | SWT.FULL_SELECTION | SWT.RESIZE);
		gridData = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		table.setLayoutData(gridData);
		table.setHeaderVisible(true);

		table.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent event) {
				enableDisableWidgets();
			}

			public void widgetDefaultSelected(SelectionEvent event) {
				edit();
			}
		});

		addTableColumns();
		packColumns();

		if (ITEMS_MOVEABLE) {

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

	}

	/**
	 * Creating a pop up menu
	 */
	protected Menu createPopUpMenu() {
		Menu visualMenu = new Menu(this.getShell(), SWT.POP_UP);
		table.setMenu(visualMenu);

		MenuItem menuAdd = new MenuItem(visualMenu, SWT.PUSH);
		menuAdd.setText("Add");
		menuAdd.addListener(SWT.Selection, new Listener() {

			public void handleEvent(Event e) {
				add();
				enableDisableWidgets();
			}
		});

		MenuItem menuDelete = new MenuItem(visualMenu, SWT.PUSH);
		menuDelete.setText("Delete");
		menuDelete.addListener(SWT.Selection, new Listener() {

			public void handleEvent(Event e) {
				delete();
				enableDisableWidgets();
			}
		});

		MenuItem menuEdit = new MenuItem(visualMenu, SWT.PUSH);
		menuEdit.setText("Edit...");
		menuEdit.addListener(SWT.Selection, new Listener() {

			public void handleEvent(Event e) {
				edit();
				enableDisableWidgets();
			}
		});

		if (ITEMS_MOVEABLE) {

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

		}

		return visualMenu;
	}

	/**
	 * add column
	 */
	protected void add() {
		openEditDialog(true);
	}

	/**
	 * Edit selected column
	 */
	protected void edit() {
		openEditDialog(false);
	}

	/**
	 * Delete selected column(s)
	 */
	protected void delete() {
		if (table.getSelectionIndex() != -1) {
			table.remove(table.getSelectionIndex());
			updateQueryData();
			fillTable();
			packColumns();
		}
	}

	/**
	 * Move selected item up
	 */
	protected void moveUp() {

		int itemIndex = table.getSelectionIndex();
		if (itemIndex > 0) {
			TableItem curr = table.getItem(itemIndex);
			TableItem above = table.getItem(itemIndex - 1);
			for (int i = 0; i < table.getColumnCount(); i++) {
				String currText = curr.getText(i);
				String aboveText = above.getText(i);
				curr.setText(i, aboveText);
				above.setText(i, currText);
			}

			updateQueryData();
			fillTable();
			table.select(itemIndex - 1);
		}

		enableDisableWidgets();

	}

	/**
	 * Move selected item down
	 */
	protected void moveDown() {
		int itemIndex = table.getSelectionIndex();
		if (itemIndex > -1 && itemIndex < table.getItemCount() - 1) {
			TableItem curr = table.getItem(itemIndex);
			TableItem below = table.getItem(itemIndex + 1);
			for (int i = 0; i < table.getColumnCount(); i++) {
				String currText = curr.getText(i);
				String belowText = below.getText(i);
				curr.setText(i, belowText);
				below.setText(i, currText);
			}

			updateQueryData();
			fillTable();
			table.select(itemIndex + 1);
		}

		enableDisableWidgets();

	}

	protected void enableDisableWidgets() {

		if (removeButton == null)
			return;

		int[] selections = table.getSelectionIndices();

		if (selections.length > 0) {
			removeButton.setEnabled(true);
		} else {
			removeButton.setEnabled(false);
		}

		if (ITEMS_MOVEABLE) {

			if (selections.length == 0 || selections.length > 1) {
				moveDownBtn.setEnabled(false);
				moveDownMnu.setEnabled(false);
				moveUpBtn.setEnabled(false);
				moveUpMnu.setEnabled(false);
				return;
			}

			int selected = selections[0];

			if (selected == 0) {
				moveUpBtn.setEnabled(false);
				moveUpMnu.setEnabled(false);
				moveDownBtn.setEnabled(true);
				moveDownMnu.setEnabled(true);
				return;
			}

			if (selected == table.getItemCount() - 1) {
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
	}

	protected void packColumns() {
		for (int i = 0; i < table.getColumnCount(); i++) {
			TableColumn tc = table.getColumn(i);
			tc.pack();
		}
	}

	/**
	 * Open new edit dialog
	 */
	protected void openEditDialog(boolean add) {

		String[] editPart = new String[EDIT_PART_LENGTH];

		TableItem item = null;
		if (add == false) {
			TableItem[] items = table.getSelection();
			if (items.length > 0) {
				item = items[0];
				for (int i = 0; i < editPart.length; i++) {
					editPart[i] = item.getText(i);
				}
			}
		}

		if (edit(editPart)) {
			if (add)
				item = new TableItem(table, SWT.RESIZE);
			for (int i = 0; i < editPart.length; i++) {
				item.setText(i, editPart[i]);
			}
			updateQueryData();
		}

		packColumns();

	}

	public void setVisible(boolean visible) {
		if (visible)
			fillTable();
		super.setVisible(visible);

	}

	/**
	 * Add table columns
	 */
	protected abstract void addTableColumns();

	/**
	 * Fill table with content.
	 */
	protected abstract void fillTable();

	/**
	 * Update query data
	 */
	protected abstract void updateQueryData();

	/**
	 * Get the edit dialog
	 */
	protected abstract boolean edit(String[] editPart);

	/**
	 * The <code>SQLExpressionEditDialog</code> class
	 */
	class SQLExpressionEditDialog extends Dialog {

		protected Shell shell = null;

		protected Combo value1Combo;

		protected Combo value2Combo;

		protected Button okButton;

		protected String[] filterPart;

		protected boolean firstItem = false;

		protected boolean update = false;

		public SQLExpressionEditDialog(Shell parent, String[] filterPart, boolean firstItem) {
			this(parent, SWT.PRIMARY_MODAL, filterPart, firstItem);
		}

		public SQLExpressionEditDialog(Shell parent, int style, String[] filterPart, boolean firstItem) {
			super(parent, style);
			shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE);

			Point location = parent.getLocation();
			shell.setBounds(location.x + 50, location.y + 70, 550, 150);
			shell.setText("Edit Expression");

			this.filterPart = filterPart;
			this.firstItem = firstItem;
			createWidgets();
		}

		public void createWidgets() {

			GridLayout gl = new GridLayout(2, false);
			shell.setLayout(gl);

			int tmpStyle = SWT.SINGLE | SWT.BORDER;
			Group group = new Group(shell, SWT.NONE);
			group.setText("Entry Fields");
			gl = new GridLayout(6, false);
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
			if (filterPart != null) {
				String value = filterPart[0];
				if (value != null)
					logicalCombo.setText(filterPart[0]);
			}

			// LPar combo
			final Combo lParCombo = new Combo(group, SWT.READ_ONLY | SWT.DROP_DOWN);
			gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
			lParCombo.setLayoutData(gd);
			lParCombo.add("");
			lParCombo.add("(");
			lParCombo.select(0);

			if (filterPart != null) {
				String value = filterPart[1];
				if (value == null)
					value = "";
				if (value.equals("("))
					lParCombo.select(1);
				else
					lParCombo.select(0);
			} else {
				lParCombo.select(0);
			}

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

			if (filterPart != null) {
				String value = filterPart[3];
				if (value == null)
					value = "=";
				conditionCombo.setText(value);
			} else {
				conditionCombo.select(3);
			}

			// Value2 combo
			value2Combo = new Combo(group, tmpStyle);
			gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
			value2Combo.setLayoutData(gd);

			// RPar combo
			final Combo rParCombo = new Combo(group, SWT.READ_ONLY | SWT.DROP_DOWN);
			gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
			rParCombo.setLayoutData(gd);
			rParCombo.add("");
			rParCombo.add(")");

			if (filterPart != null) {
				String value = filterPart[5];
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
					if (firstItem)
						filterPart[0] = "";
					else
						filterPart[0] = logicalCombo.getText().toLowerCase();
					filterPart[1] = lParCombo.getText();
					filterPart[2] = value1Combo.getText();
					filterPart[3] = conditionCombo.getText();
					filterPart[4] = value2Combo.getText();
					filterPart[5] = rParCombo.getText();
					update = true;
					shell.close();
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

			// Load value combos
			TreeSet sortedSet = new TreeSet(queryData.getAvailableColumns().keySet());
			for (Iterator iter = sortedSet.iterator(); iter.hasNext();) {
				String key = (String) iter.next();
				value1Combo.add(key);
				value2Combo.add(key);
			}

			if (filterPart != null) {
				String value = filterPart[2];
				if (value != null)
					value1Combo.setText(value);
				value = filterPart[4];
				if (value != null)
					value2Combo.setText(value);
			}

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
