package com.ss.speedtransfer.util;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;

public class SQLWarningDialog extends MessageDialog {

	protected String sqlWarning;

	public SQLWarningDialog(String messsage, String sqlWarning) {
		this(UIHelper.instance().getActiveShell(), "SQL Warning", null, messsage, MessageDialog.WARNING, new String[] { "OK", "Cancel" }, 0);
		this.sqlWarning = sqlWarning;
	}

	public SQLWarningDialog(Shell parentShell, String dialogTitle, Image dialogTitleImage, String dialogMessage, int dialogImageType, String[] dialogButtonLabels, int defaultIndex) {
		super(parentShell, dialogTitle, dialogTitleImage, dialogMessage, dialogImageType, dialogButtonLabels, defaultIndex);
	}

	protected Control createCustomArea(Composite parent) {
		parent.setLayout(new FillLayout());
		Group group = new Group(parent, SWT.NONE);
		group.setLayout(new GridLayout());
		Button b = new Button(group, SWT.PUSH);
		b.setText("View SQL parsing error");
		b.setLayoutData(new GridData());
		b.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				UIHelper.instance().showMessage("SQL Parsing problems", sqlWarning);
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		return group;

	}

}
