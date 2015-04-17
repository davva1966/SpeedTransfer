// {{CopyrightNotice}}

package com.ss.speedtransfer.ui.wizard.qb;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * The <code>SQLDataFilterDialog</code> class
 */
public class SQLDataFilterComp extends SQLGenericEditComp {

	public SQLDataFilterComp(WizardPage page, Composite parent, SQLQueryData queryData) {
		this(page, parent, SWT.NONE, queryData);
	}

	public SQLDataFilterComp(WizardPage page, Composite parent, int style, SQLQueryData queryData) {
		super(page, parent, style, queryData);
		EDIT_PART_LENGTH = 6;

	}

	/**
	 * Fill filter table with content.
	 */
	protected void fillTable() {
		table.removeAll();

		List filterCond = queryData.getFilterCondition();
		if (filterCond != null && !filterCond.isEmpty()) {
			TableItem item;
			for (Iterator iter = filterCond.iterator(); iter.hasNext();) {
				String[] filter = (String[]) iter.next();
				if (filter != null) {
					item = new TableItem(table, SWT.RESIZE);
					for (int i = 0; i < filter.length; i++) {
						if (filter[i] != null) {
							// item.setText(i, getQualifiedColumnName(filter[i]));
							item.setText(i, filter[i]);
						}
					}
				}
			}

			if (table.getItemCount() > 0)
				table.getItem(0).setText(0, "");
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
		tableColumn.setText("Value2");
		tableColumn.setWidth(170);
		tableColumn = new TableColumn(table, SWT.NONE);
		tableColumn.setText("Bracket");
		tableColumn.setWidth(40);

	}

	/**
	 * Update query data
	 */
	protected void updateQueryData() {
		queryData.setFilterConditon(new ArrayList());
		String[] filterArray = null;
		// Update from table
		TableItem[] items = table.getItems();
		for (int i = 0; i < items.length; i++) {
			TableItem item = items[i];
			filterArray = new String[EDIT_PART_LENGTH];
			for (int j = 0; j < filterArray.length; j++) {
				String text = item.getText(j);
				filterArray[j] = text;
			}
			queryData.addFilterConditon(filterArray);
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

		SQLExpressionEditDialog dialog = new SQLExpressionEditDialog(getShell(), editPart, firstItem);

		return dialog.open();

	}

}
