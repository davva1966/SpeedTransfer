package com.ss.speedtransfer.util;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class DatabaseLogonPromptDialog extends Dialog {

	protected String user;
	protected String password;
	protected String title;
	protected boolean cancelled = false;

	public DatabaseLogonPromptDialog(Shell parentShell, String title, String user, String password) {
		super(parentShell);
		setShellStyle(SWT.DIALOG_TRIM | SWT.RESIZE | SWT.APPLICATION_MODAL);
		this.title = title;
		this.user = user;
		this.password = password;
	}

	protected void configureShell(Shell newShell) {
		newShell.setText(title);
		super.configureShell(newShell);
	}

	protected void cancelPressed() {
		user = null;
		password = null;
		cancelled = true;
		super.cancelPressed();
	}

	public boolean isCancelled() {
		return cancelled;
	}

	public String getUser() {
		return user;
	}

	public String getPassword() {
		return password;
	}

	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		getButton(IDialogConstants.OK_ID).setEnabled(false);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
		validatePage();

	}

	protected Control createDialogArea(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout(1, true);
		composite.setLayout(gridLayout);

		GridData data = new GridData(GridData.FILL_BOTH);
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = true;
		data.heightHint = 90;
		data.widthHint = 350;
		composite.setLayoutData(data);

		createControls(composite);

		return composite;
	}

	protected void createControls(Composite parent) {

		Composite composite = new Composite(parent, SWT.BORDER);
		GridLayout layout = new GridLayout(2, false);
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.grabExcessHorizontalSpace = true;
		composite.setLayoutData(data);

		GridData gd = new GridData(SWT.FILL, GridData.VERTICAL_ALIGN_BEGINNING, true, false);
		data.heightHint = 21;
		data.widthHint = 25;

		final Label userLabel = new Label(composite, SWT.NONE);
		userLabel.setText("User");

		final Text userText = new Text(composite, SWT.BORDER);
		userText.setText(user);
		userText.setLayoutData(gd);
		userText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				user = userText.getText();
				validatePage();
			}
		});

		final Label passwordLabel = new Label(composite, SWT.NONE);
		passwordLabel.setText("Password");

		final Text passwordText = new Text(composite, SWT.BORDER | SWT.PASSWORD);
		passwordText.setText(password);
		passwordText.setLayoutData(gd);
		passwordText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				password = passwordText.getText();
				validatePage();
			}
		});

	}

	protected void validatePage() {

		getButton(IDialogConstants.OK_ID).setEnabled(true);

		if (user == null || user.trim().length() == 0)
			getButton(IDialogConstants.OK_ID).setEnabled(false);

		if (password == null || password.trim().length() == 0)
			getButton(IDialogConstants.OK_ID).setEnabled(false);

	}

}
