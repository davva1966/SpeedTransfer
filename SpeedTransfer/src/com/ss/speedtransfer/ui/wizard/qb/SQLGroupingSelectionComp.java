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
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.ss.speedtransfer.util.UIHelper;


/**
 * The <code>SQLGroupingSelectionComp</code> class
 */
public class SQLGroupingSelectionComp extends SQLGenericEditComp {

	public SQLGroupingSelectionComp(WizardPage page, Composite parent, SQLQueryData queryData) {
		this(page, parent, SWT.NONE, queryData);
	}

	public SQLGroupingSelectionComp(WizardPage page, Composite parent, int style, SQLQueryData queryData) {
		super(page, parent, style, queryData);
		EDIT_PART_LENGTH = 1;

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

		// Add selected group by columns
		List groupColumns = queryData.getGroupByColumns();
		if (groupColumns != null && !groupColumns.isEmpty()) {
			TableItem item;
			for (Iterator iter = groupColumns.iterator(); iter.hasNext();) {
				String[] groupCol = (String[]) iter.next();
				if (groupCol[0] != null) {
					item = new TableItem(table, SWT.RESIZE);
					// item.setText(getQualifiedColumnName(groupCol[0]));
					item.setText(groupCol[0]);
				}
			}
		}

		packColumns();

	}

	/**
	 * Update query data
	 */
	protected void updateQueryData() {
		queryData.setGroupByColumns(new ArrayList());
		// Update from table
		TableItem[] items = table.getItems();
		for (int i = 0; i < items.length; i++) {
			TableItem item = items[i];
			String[] arr = new String[1];
			arr[0] = item.getText().trim();
			queryData.addGroupByColumn(arr);
		}

	}

	protected void addTableColumns() {
		TableColumn tableColumn = new TableColumn(table, SWT.NONE);
		tableColumn.setText("Column");
		tableColumn.setWidth(185);

	}

	/**
	 * Delete selected column(s)
	 */
	protected void delete() {
		if (table.getSelectionIndex() != -1) {
			TableItem item = table.getItem(table.getSelectionIndex());
			String groupCol = item.getText();
			if (queryData.columnMandatoryInGroupBy(groupCol)) {
				UIHelper.instance().showErrorMsg(getShell(), "Error", "Column " + groupCol + " is specified in the column selection without an associated function and is therefore mandatory in the \"group by\" clause. Column " + groupCol + " can not be removed.",
						SWT.ICON_ERROR | SWT.OK | SWT.ON_TOP, null);
				return;
			}

			super.delete();

		}
	}

	/**
	 * Open new edit dialog
	 */
	protected boolean edit(String[] editPart) {

		SQLGroupingEditDialog dialog = new SQLGroupingEditDialog(getShell(), editPart);

		return dialog.open();

	}

	/**
	 * The <code>SQLSequenceEditDialog</code> class
	 */
	class SQLGroupingEditDialog extends Dialog {

		protected Shell shell = null;

		protected Combo colCombo;

		protected Button okButton;

		protected String[] groupPart;

		protected boolean update = false;

		public SQLGroupingEditDialog(Shell parent, String[] groupPart) {
			this(parent, SWT.PRIMARY_MODAL, groupPart);
		}

		public SQLGroupingEditDialog(Shell parent, int style, String[] groupPart) {
			super(parent, style);
			shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE);

			Point location = parent.getLocation();
			shell.setBounds(location.x + 50, location.y + 70, 350, 120);
			shell.setText("Edit Expression");

			this.groupPart = groupPart;

			createWidgets();
		}

		public void createWidgets() {

			GridLayout gl = new GridLayout(2, false);
			shell.setLayout(gl);

			int tmpStyle = SWT.SINGLE | SWT.BORDER;
			Group group = new Group(shell, SWT.NONE);
			group.setText("Column");
			gl = new GridLayout(1, false);
			group.setLayout(gl);

			GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
			group.setLayoutData(gd);

			// Column combo
			colCombo = new Combo(group, tmpStyle);
			gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
			colCombo.setLayoutData(gd);
			colCombo.addModifyListener(new ModifyListener() {

				public void modifyText(ModifyEvent e) {
					fillTable();
				}

			});

			if (groupPart != null && groupPart[0] != null && groupPart[0].trim().length() > 0)
				if (queryData.columnMandatoryInGroupBy(groupPart[0]))
					colCombo.setEnabled(false);

			// Load combo
			TreeSet sortedSet = new TreeSet(queryData.getAvailableColumns().keySet());
			for (Iterator iter = sortedSet.iterator(); iter.hasNext();) {
				String col = (String) iter.next();
				colCombo.add(col);
			}

			if (groupPart != null && groupPart.length > 0 && groupPart[0] != null) {
				colCombo.setText(groupPart[0]);
			} else {
				colCombo.select(0);
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
					groupPart[0] = colCombo.getText();
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
