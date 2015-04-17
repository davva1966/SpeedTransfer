package com.ss.speedtransfer.util;

import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class ItemTableSelectionDialog extends Dialog {

	protected String[] headings;
	protected List<String[]> items;
	protected String[] selectedItem = null;
	protected String title;
	protected int heightHint = 100;
	protected int widthHint = 200;

	protected Table table;

	Listener tablePaintListener = new Listener() {
		public void handleEvent(Event event) {
			switch (event.type) {
			case SWT.MeasureItem: {
				TableItem item = (TableItem) event.item;
				String text = getText(item, event.index);
				Point size = event.gc.textExtent(text);
				event.width = size.x;
				event.height = Math.max(event.height, size.y);
				break;
			}
			case SWT.PaintItem: {
				TableItem item = (TableItem) event.item;
				String text = getText(item, event.index);
				Point size = event.gc.textExtent(text);
				int offset2 = event.index == 0 ? Math.max(0, (event.height - size.y) / 2) : 0;
				event.gc.drawText(text, event.x, event.y + offset2, true);
				break;
			}
			case SWT.EraseItem: {
				event.detail &= ~SWT.FOREGROUND;
				break;
			}
			}
		}

		String getText(TableItem item, int column) {
			String text = item.getText(column);
			if (column != 0) {
				int index = table.indexOf(item);
				if ((index + column) % 3 == 1) {
					text += "\n";
				}
				if ((index + column) % 3 == 2) {
					text += "\n\n";
				}
			}
			return text;
		}
	};

	public ItemTableSelectionDialog(Shell parentShell, String[] headings, List<String[]> items) {
		this(parentShell, headings, items, null);
	}

	public ItemTableSelectionDialog(Shell parentShell, String[] headings, List<String[]> items, String title) {
		super(parentShell);
		this.headings = headings;
		this.items = items;
		this.title = title;
		if (title == null || title.trim().length() == 0)
			title = "Item Selector";
		setShellStyle(SWT.DIALOG_TRIM | SWT.RESIZE | SWT.APPLICATION_MODAL);
	}

	protected void configureShell(Shell newShell) {
		newShell.setText(title);
		super.configureShell(newShell);
	}

	public void setHeightHint(int heightHint) {
		this.heightHint = heightHint;
	}

	public void setWidthHint(int widthHint) {
		this.widthHint = widthHint;
	}

	protected void cancelPressed() {
		selectedItem = null;
		super.cancelPressed();
	}

	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		getButton(IDialogConstants.OK_ID).setEnabled(false);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);

	}

	protected Control createDialogArea(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		composite.setLayout(gridLayout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		if (items.size() == 0) {
			Label l = new Label(composite, SWT.NONE);
			l.setText("Nothing to select");
		}

		createTable(composite);

		return composite;
	}

	public String[] getSelectedItem() {
		return selectedItem;
	}

	protected void createTable(Composite parent) {

		Composite composite = new Composite(parent, SWT.BORDER);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		composite.setLayout(layout);

		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);

		data.horizontalSpan = 2;
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = true;
		data.heightHint = heightHint;
		data.widthHint = widthHint;

		composite.setLayoutData(data);

		table = new Table(composite, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		table.setLayoutData(data);

		for (String heading : headings) {
			TableColumn column = new TableColumn(table, SWT.NONE);
			column.setText(heading);
		}

		for (String[] item : items) {
			TableItem tblItem = new TableItem(table, SWT.NONE);
			for (int i = 0; i < item.length; i++) {
				if (item[i] != null)
					// tblItem.setText(i, item[i]);
					tblItem.setText(i, StringHelper.wordWrapString(item[i], 250));
				else
					tblItem.setText(i, "");
			}
		}

		for (int i = 0; i < headings.length; i++) {
			table.getColumn(i).pack();
		}

		table.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				int idx = table.getSelectionIndex();
				if (idx >= 0) {
					getButton(IDialogConstants.OK_ID).setEnabled(true);
					selectedItem = items.get(idx);
				} else {
					getButton(IDialogConstants.OK_ID).setEnabled(false);
				}
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				int idx = table.getSelectionIndex();
				widgetSelected(e);
				if (idx >= 0)
					close();
			}

		});

		table.addListener(SWT.MeasureItem, tablePaintListener);
		table.addListener(SWT.PaintItem, tablePaintListener);
		table.addListener(SWT.EraseItem, tablePaintListener);

	}
}
