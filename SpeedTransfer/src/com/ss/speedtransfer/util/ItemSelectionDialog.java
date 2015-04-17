package com.ss.speedtransfer.util;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;

public class ItemSelectionDialog extends Dialog {

	protected String[] items = null;
	protected String selectedItem = null;
	protected String title;

	public ItemSelectionDialog(Shell parentShell, String[] items) {
		this(parentShell, items, null);
	}

	public ItemSelectionDialog(Shell parentShell, String[] items, String title) {
		super(parentShell);
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

		if (items.length == 0) {
			Label l = new Label(composite, SWT.NONE);
			l.setText("Nothing to select");
		}

		createList(composite);

		return composite;
	}

	public String getSelectedItem() {
		return selectedItem;
	}

	protected void createList(Composite parent) {

		Composite composite = new Composite(parent, SWT.BORDER);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		composite.setLayout(layout);

		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);

		data.horizontalSpan = 2;
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = true;
		data.heightHint = 100;
		data.widthHint = 100;

		composite.setLayoutData(data);

		List list = new List(composite, SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL);
		list.setLayoutData(data);
		list.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (e.getSource() instanceof List) {
					int idx = ((List) e.getSource()).getSelectionIndex();
					if (idx >= 0) {
						getButton(IDialogConstants.OK_ID).setEnabled(true);
						selectedItem = items[idx];
					} else {
						getButton(IDialogConstants.OK_ID).setEnabled(false);
					}

				}

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				if (e.getSource() instanceof List) {
					int idx = ((List) e.getSource()).getSelectionIndex();
					widgetSelected(e);
					if (idx >= 0)
						close();
				}

			}
		});

		list.setItems(items);

	}

}
