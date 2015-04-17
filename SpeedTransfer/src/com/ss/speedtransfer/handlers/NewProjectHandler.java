package com.ss.speedtransfer.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.ss.speedtransfer.util.UIHelper;


public class NewProjectHandler extends AbstractHandler {

	protected IWorkspaceRoot root;
	protected Button okButton;
	protected Text projectNameText;
	protected String projectName = null;
	protected Label errorLabel = null;

	public Object execute(ExecutionEvent event) throws ExecutionException {
		root = ResourcesPlugin.getWorkspace().getRoot();
		prompt();
		if (projectName != null && projectName.trim().length() > 0) {
			IProgressMonitor progressMonitor = new NullProgressMonitor();
			IProject project = root.getProject(projectName);
			try {
				project.create(progressMonitor);
				project.open(progressMonitor);
			} catch (Exception e) {
			}
		}

		return null;
	}

	protected String prompt() {

		Shell shell = UIHelper.instance().getActiveShell();
		final Shell settingsShell = new Shell(shell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE);
		settingsShell.setText("New Project");
		Point location = shell.getLocation();
		settingsShell.setBounds(location.x + 30, location.y + 150, 500, 180);

		GridLayout gl = new GridLayout(2, false);
		settingsShell.setLayout(gl);

		Group group = new Group(settingsShell, SWT.NONE);
		gl = new GridLayout(3, false);
		group.setLayout(gl);

		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
		group.setLayoutData(gd);

		Label label = new Label(group, SWT.NONE);
		label.setText("Project name:");
		gd = new GridData(SWT.LEFT, SWT.TOP, false, false, 2, 1);
		label.setLayoutData(gd);

		// Export to file entry
		projectNameText = new Text(group, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1);
		projectNameText.setLayoutData(gd);
		projectNameText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				validatePage();
			}
		});

		errorLabel = new Label(group, SWT.NONE);
		errorLabel.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
		gd = new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1);
		errorLabel.setLayoutData(gd);

		// OK button
		okButton = new Button(settingsShell, SWT.PUSH);
		okButton.setText("OK");
		okButton.setEnabled(false);
		gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		gd.widthHint = 100;
		okButton.setLayoutData(gd);
		okButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				projectName = projectNameText.getText();
				settingsShell.close();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});

		// Cancel button
		Button cancelButton = new Button(settingsShell, SWT.PUSH);
		cancelButton.setText("Cancel");
		gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		gd.widthHint = 100;
		cancelButton.setLayoutData(gd);
		cancelButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				projectName = null;
				settingsShell.close();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});
		
		settingsShell.setDefaultButton(okButton);

		validatePage();

		// Open shell
		settingsShell.open();
		Display display = Display.getDefault();
		while (!settingsShell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}

		return projectName;

	}

	protected void validatePage() {

		okButton.setEnabled(true);
		errorLabel.setText("");

		if (projectNameText.getText().trim().length() == 0) {
			okButton.setEnabled(false);
			return;
		}

		if (root.getProject(projectNameText.getText().trim()).exists()) {
			okButton.setEnabled(false);
			errorLabel.setText("Project name already exists");
		}

	}

}
