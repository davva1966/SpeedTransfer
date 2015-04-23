package com.ss.speedtransfer.export;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URL;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.eclipse.ant.core.AntRunner;
import org.eclipse.core.internal.resources.Project;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.progress.IProgressConstants;

import com.ss.speedtransfer.SpeedTransferPlugin;
import com.ss.speedtransfer.model.QueryDefinition;
import com.ss.speedtransfer.util.SSUtil;
import com.ss.speedtransfer.util.StringHelper;
import com.ss.speedtransfer.util.UIHelper;


public class WindowsExecutableExporter {

	protected String errorMessage = null;

	protected Text fileEdit;
	protected Button okButton;
	protected Button expiryCheck;
	protected DateTime expDate;
	protected Text expMessage;
	protected Map<String, String> settings;
	protected boolean cancel = true;
	protected boolean exportCancelled = false;
	protected boolean overwriteChecked = false;

	FormToolkit toolkit;
	Form form;

	private final static ISchedulingRule jobRule = new ISchedulingRule() {
		public boolean contains(ISchedulingRule rule) {
			return rule == this;
		}

		public boolean isConflicting(ISchedulingRule rule) {
			return rule == this;
		}
	};

	public void export(final IFile queryFile) {

		try {
			QueryDefinition queryDef = new QueryDefinition(queryFile);

			IPath path = new Path(queryDef.getDBConnectionFile());
			IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
			if (file.exists() == false) {
				UIHelper.instance().showErrorMsg("Error", "The database connection file used in this query definition (" + queryDef.getDBConnectionFile() + ") could not be found.");
				return;
			}
			if (queryDef.getDBConnection() == null) {
				UIHelper.instance().showErrorMsg("Error", "No Database connection found for this query definition");
				return;
			}
			if (queryDef.getSQL().trim().length() == 0) {
				UIHelper.instance().showErrorMsg("Error", "No sql defined in this query definition.");
				return;
			}
		} catch (Exception e) {
			UIHelper.instance().showErrorMsg("Error", SSUtil.getMessage(e));
			return;
		}

		openSettingsDialog();
		if (cancel)
			return;

		Job job = new Job("Create Windows Executable") {
			public IStatus run(IProgressMonitor monitor) {

				IStatus status = Status.OK_STATUS;
				boolean error = false;

				try {
					runTask(queryFile, monitor);
					if (monitor.isCanceled()) {
						status = Status.CANCEL_STATUS;
						exportCancelled = true;
					} else {
						String msg = "Executable created.";
						status = new Status(IStatus.OK, SpeedTransferPlugin.PLUGIN_ID, IStatus.OK, msg, null);
					}
				} catch (Exception e) {
					error = true;
					errorMessage = SSUtil.getMessage(e);
					String msg = "Error during executable creation";
					status = new Status(IStatus.ERROR, SpeedTransferPlugin.PLUGIN_ID, IStatus.OK, msg, e);
				}

				if (isModal(this)) {
					if (error == false)
						showResults();
				} else {
					setProperty(IProgressConstants.KEEP_PROPERTY, Boolean.TRUE);
					setProperty(IProgressConstants.ACTION_PROPERTY, getCompletedAction());
					setProperty(IProgressConstants.NO_IMMEDIATE_ERROR_PROMPT_PROPERTY, Boolean.TRUE);
				}

				monitor.done();

				return status;
			}

			public boolean isModal(Job job) {
				Boolean isModal = (Boolean) job.getProperty(IProgressConstants.PROPERTY_IN_DIALOG);
				if (isModal == null)
					return false;
				return isModal.booleanValue();
			}

		};

		job.setUser(true);
		job.setRule(jobRule);
		job.schedule();

	}

	protected void showResults() {
		Display.getDefault().asyncExec(new Runnable() {

			public void run() {
				getCompletedAction().run();
			}
		});
	}

	protected Action getCompletedAction() {
		return new Action("View executable creation status") {

			public void run() {
				if (errorMessage != null && errorMessage.trim().length() > 0) {
					MessageDialog.openError(UIHelper.instance().getActiveShell(), "Error during creation of executable", errorMessage);
				} else {
					if (exportCancelled)
						MessageDialog.openInformation(UIHelper.instance().getActiveShell(), "Creation of executable cancelled", "The creation of the windows executable was cancelled. No executable created.");
					else
						MessageDialog.openInformation(UIHelper.instance().getActiveShell(), "Windows executable created", "Windows executable " + settings.get("TO_FILE") + " created.");
				}
			}
		};
	}

	protected String getCurrentDirectory() {

		String directory = null;
		try {
			URL relativeURL = SpeedTransferPlugin.getDefault().getBundle().getEntry("/");
			URL localURL = FileLocator.toFileURL(relativeURL);
			directory = localURL.getPath();
		} catch (Exception e) {
		}

		return directory;
	}

	protected String getInstallDirectory() {

		String devMode = System.getProperty("com.ss.speedtransfer.devmode", "false");
		if (devMode.trim().equalsIgnoreCase("true"))
			return getCurrentDirectory();

		String directory = null;
		try {
			Location location = Platform.getInstallLocation();
			URL url = location.getURL();
			URL localURL = FileLocator.toFileURL(url);
			directory = localURL.getPath();
		} catch (Exception e) {
		}

		return directory;
	}

	protected String getEclipseJarDir() {

		String devMode = System.getProperty("com.ss.speedtransfer.devmode", "false");
		if (devMode.trim().equalsIgnoreCase("true"))
			return "C:\\eclipse_rcp_64\\plugins\\";

		return getInstallDirectory() + "plugins" + File.separator;

	}

	protected String getDevModeJarDir() {

		String devMode = System.getProperty("com.ss.speedtransfer.devmode", "false");
		if (devMode.trim().equalsIgnoreCase("true"))
			return getInstallDirectory() + File.separator;

		return "null";

	}

	protected void runTask(IFile queryFile, IProgressMonitor monitor) throws Exception {

		QueryDefinition qd = new QueryDefinition(queryFile);
		if (qd.getDBConnectionFile() == null || qd.getDBConnectionFile().trim().length() == 0)
			throw new Exception("No DB Connection file specified in the selected query definition");
		IPath path = new Path(qd.getDBConnectionFile());
		IFile conFile = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
		if (conFile.exists() == false)
			throw new Exception("The DB Connection file: " + qd.getDBConnectionFile() + " used in the query definition does not exist");

		String installDir = getInstallDirectory() + "create_exe" + File.separator;
		String workDir = System.getProperty("java.io.tmpdir") + File.separator + "create_exe" + File.separator;
		
		String buildDir = workDir + "build" + File.separator;
		String distDir = workDir + "dist" + File.separator;
		String launcherDir = workDir + "launcher" + File.separator;

		File bd = new File(buildDir);
		if (bd.exists() == false)
			bd.mkdirs();

		File dd = new File(distDir);
		if (dd.exists() == false)
			dd.mkdirs();
		
		File ld = new File(launcherDir);
		if (ld.exists() == false)
			ld.mkdirs();

		createProperties(launcherDir);

		AntRunner runner = new AntRunner();
		runner.setBuildFileLocation(installDir + "create_exe_antbuild.xml");
		runner.setArguments(buildAntArguments(queryFile, conFile));
		runner.setAntHome(getInstallDirectory());
		runner.run(monitor);

	}

	protected String buildAntArguments(IFile queryFile, IFile conFile) {
		String installDir = getInstallDirectory() + "create_exe" + File.separator;
		String workDir = System.getProperty("java.io.tmpdir") + File.separator + "create_exe" + File.separator;
		String jreDir = System.getProperty("java.home");
		String filePath = queryFile.getLocation().toOSString();
		String conFilePath = conFile.getLocation().toOSString();
		String devModeJarDirDir = getDevModeJarDir();

		StringBuilder sb = new StringBuilder();

		sb.append("-Dqueryfile.path=\"");
		sb.append(filePath);
		sb.append("\" ");

		sb.append("-Ddbconfile.path=\"");
		sb.append(conFilePath);
		sb.append("\" ");

		sb.append("-Dtargetfile.path=\"");
		sb.append(settings.get("TO_FILE"));
		sb.append("\" ");

		sb.append("-Dapp.file=\"");
		if (settings.get("DEBUG").equalsIgnoreCase("true"))
			sb.append("app_debug.tag");
		else
			sb.append("app.tag");
		sb.append("\" ");

		sb.append("-Djre.dir=\"");
		sb.append(jreDir);
		sb.append("\" ");

		sb.append("-Declipse.jar.dir=\"");
		sb.append(getEclipseJarDir());
		sb.append("\" ");

		sb.append("-Dbasedir=\"");
		sb.append(installDir);
		sb.append("\" ");
		
		sb.append("-Dworkdir=\"");
		sb.append(workDir);
		sb.append("\" ");

		sb.append("-Ddevmode.jar.dir=\"");
		sb.append(devModeJarDirDir);
		sb.append("\" ");

		sb.append("-Dmessage=Building -verbose");

		return sb.toString();

	}

	protected Map<String, String> openSettingsDialog() {

		settings = new HashMap<String, String>();

		IDialogSettings tempDialogSettings = null;
		try {
			tempDialogSettings = SpeedTransferPlugin.getDialogSettingsFor(this.getClass().getName());
		} catch (Exception e) {
		}
		final IDialogSettings dialogSettings = tempDialogSettings;

		Shell shell = UIHelper.instance().getActiveShell();
		final Shell settingsShell = new Shell(shell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE);
		settingsShell.setText("Export to executable file");
		Point location = shell.getLocation();
		settingsShell.setBounds(location.x + 30, location.y + 150, 500, 420);
		settingsShell.setLayout(new FillLayout());

		toolkit = new FormToolkit(settingsShell.getDisplay());
		form = toolkit.createForm(settingsShell);

		try {
			form.setBackgroundImage(UIHelper.instance().getImageDescriptor("form_banner.gif").createImage());
		} catch (Exception e) {
		}

		form.setText("Create windows executable");
		toolkit.decorateFormHeading(form);

		GridLayout gl = new GridLayout(2, false);
		form.getBody().setLayout(gl);

		Group group = new Group(form.getBody(), SWT.NONE);
		gl = new GridLayout(3, false);
		group.setLayout(gl);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
		group.setLayoutData(gd);
		toolkit.adapt(group);

		// Export to file label
		Label label = toolkit.createLabel(group, "Select file to create");
		gd = new GridData(SWT.LEFT, SWT.TOP, false, false, 3, 1);
		label.setLayoutData(gd);

		// Export to file entry
		fileEdit = toolkit.createText(group, "", SWT.SINGLE);
		gd = new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1);
		fileEdit.setLayoutData(gd);
		fileEdit.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				overwriteChecked = false;
			}
		});

		// File browse button
		Button browseButton = toolkit.createButton(group, "Browse...", SWT.PUSH);
		gd = new GridData(SWT.LEFT, SWT.FILL, false, false);
		gd.widthHint = 80;
		browseButton.setLayoutData(gd);
		Listener listener = new Listener() {

			public void handleEvent(Event event) {
				FileDialog dialog = new FileDialog(settingsShell, SWT.SAVE);
				dialog.setText("Select file to create");
				String[] exts = new String[1];
				exts[0] = "*.exe";
				dialog.setFilterExtensions(exts);
				dialog.setOverwrite(true);

				String file = dialog.open();

				if (file == null)
					file = "";

				fileEdit.setText(file);
				overwriteChecked = true;
			}
		};
		browseButton.addListener(SWT.Selection, listener);

		Group expGroup = new Group(group, SWT.NONE);
		gl = new GridLayout(2, false);
		expGroup.setLayout(gl);
		gd = new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1);
		expGroup.setLayoutData(gd);
		toolkit.adapt(expGroup);

		// Use expiry
		expiryCheck = toolkit.createButton(expGroup, "Use expiry date", SWT.CHECK);
		expiryCheck.setSelection(false);
		gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		gd.horizontalIndent = 5;
		gd.heightHint = 25;
		gd.horizontalSpan = 2;
		expiryCheck.setLayoutData(gd);
		expiryCheck.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				validatePage();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		// Expiry date
		toolkit.createLabel(expGroup, "Exipiry date");
		expDate = new DateTime(expGroup, SWT.BORDER | SWT.DATE | SWT.DROP_DOWN);
		gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		gd.horizontalIndent = 5;
		gd.heightHint = 22;
		expDate.setLayoutData(gd);
		expDate.setToolTipText("The executable will expire on the selected date at 1 second past midnight.");
		toolkit.adapt(expDate);

		// Message
		Label exdMsgLbl = toolkit.createLabel(expGroup, "Message to display when expired");
		gd = new GridData(SWT.LEFT, SWT.BOTTOM, false, false);
		gd.heightHint = 22;
		gd.horizontalSpan = 2;
		exdMsgLbl.setLayoutData(gd);
		expMessage = toolkit.createText(expGroup, "", SWT.BORDER | SWT.MULTI);
		gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.heightHint = 22;
		gd.horizontalSpan = 2;
		expMessage.setLayoutData(gd);

		// Debug
		final Button remoteDebug = toolkit.createButton(expGroup, "Enable remote debug", SWT.CHECK);
		remoteDebug.setSelection(false);
		gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		gd.horizontalIndent = 5;
		gd.heightHint = 25;
		remoteDebug.setLayoutData(gd);

		// OK button
		okButton = toolkit.createButton(form.getBody(), "OK", SWT.PUSH);
		okButton.setEnabled(false);
		gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		gd.widthHint = 100;
		okButton.setLayoutData(gd);

		listener = new Listener() {

			public void handleEvent(Event event) {

				if (overwriteChecked == false) {
					File destFile = new File(fileEdit.getText());
					if (destFile.isFile() && destFile.exists()) {
						int returnCode = UIHelper.instance().showErrorMsgWithOKCancel("Warning", "File: " + fileEdit.getText() + " already exist. Do you want to replace it?");
						if (returnCode == SWT.CANCEL)
							return;
					}
				}

				settings.put("TO_FILE", fileEdit.getText());
				if (remoteDebug.getSelection())
					settings.put("DEBUG", "true");
				else
					settings.put("DEBUG", "false");

				if (expiryCheck.getSelection())
					settings.put("USE_EXPIRY", "true");
				else
					settings.put("USE_EXPIRY", "false");
				settings.put("EXP_DATE_DAY", Integer.toString(expDate.getDay()));
				settings.put("EXP_DATE_MONTH", Integer.toString(expDate.getMonth()));
				settings.put("EXP_DATE_YEAR", Integer.toString(expDate.getYear()));
				settings.put("EXP_MESSAGE", expMessage.getText());

				cancel = false;

				if (dialogSettings != null) {
					dialogSettings.put("file", fileEdit.getText());
					dialogSettings.put("useExpiry", expiryCheck.getSelection());
					dialogSettings.put("expDateDay", expDate.getDay());
					dialogSettings.put("expDateMonth", expDate.getMonth());
					dialogSettings.put("expDateYear", expDate.getYear());
					dialogSettings.put("expMessage", expMessage.getText());
					dialogSettings.put("debug", remoteDebug.getSelection());
				}

				settingsShell.close();
			}
		};
		okButton.addListener(SWT.Selection, listener);

		// Cancel button
		Button cancelButton = toolkit.createButton(form.getBody(), "Cancel", SWT.PUSH);
		gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		gd.widthHint = 100;
		cancelButton.setLayoutData(gd);
		listener = new Listener() {

			public void handleEvent(Event event) {
				cancel = true;
				settingsShell.close();
			}
		};
		cancelButton.addListener(SWT.Selection, listener);

		fileEdit.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				validatePage();
			}

		});

		if (dialogSettings != null) {
			if (dialogSettings.get("file") != null)
				fileEdit.setText(dialogSettings.get("file"));
			expiryCheck.setSelection(dialogSettings.getBoolean("useExpiry"));
			try {
				expDate.setDay(dialogSettings.getInt("expDateDay"));
				expDate.setMonth(dialogSettings.getInt("expDateMonth"));
				expDate.setYear(dialogSettings.getInt("expDateYear"));
			} catch (Exception e) {
			}
			try {
				expMessage.setText(dialogSettings.get("expMessage"));
			} catch (Exception e) {
			}

			remoteDebug.setSelection(dialogSettings.getBoolean("debug"));//$NON-NLS-1$
		}

		if (fileEdit.getText().trim().length() == 0) {
			String path = System.getProperty("user.home");
			fileEdit.setText(path + File.separator + "NewEXEFile.exe");
		}

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

		return settings;

	}

	protected void validatePage() {

		okButton.setEnabled(true);
		expDate.setEnabled(false);
		expMessage.setEnabled(false);

		if (expiryCheck.getSelection()) {
			expDate.setEnabled(true);
			expMessage.setEnabled(true);
		}

		if (fileEdit.getText().trim().length() == 0)
			okButton.setEnabled(false);

		clearMessage(form, "File empty", fileEdit);
		if (fileEdit.getText().trim().length() == 0)
			issueMessage(form, "File empty", IMessageProvider.INFORMATION, "File must be specified", fileEdit);

	}

	protected void createProperties(String path) throws Exception {
		Properties expiryProps = new Properties();

		if (settings.get("USE_EXPIRY").equalsIgnoreCase("true")) {
			Calendar cal = Calendar.getInstance();
			int year = Integer.parseInt(settings.get("EXP_DATE_YEAR"));
			int month = Integer.parseInt(settings.get("EXP_DATE_MONTH"));
			int day = Integer.parseInt(settings.get("EXP_DATE_DAY"));

			cal.set(year, month, day);
			Date expDate = cal.getTime();
			String expDateStr = DateFormat.getDateInstance(DateFormat.SHORT, Locale.US).format(expDate);

			expiryProps.put("expdate", StringHelper.encodePassword(expDateStr));
			expiryProps.put("expmessage", settings.get("EXP_MESSAGE"));
		} else {
			expiryProps.put("expdate", StringHelper.encodePassword("notused"));
			expiryProps.put("expmessage", "");

		}
		OutputStream out = new FileOutputStream(path + "p.p");
		expiryProps.store(out, "");
		out.close();

	}

	protected void clearMessage(Form form, String key, Control control) {
		if (control == null)
			form.getMessageManager().removeMessage(key);
		else
			form.getMessageManager().removeMessage(key, control);
	}

	protected void issueMessage(Form form, String key, int type, String message, Control control) {
		if (control == null)
			form.getMessageManager().addMessage(key, message, null, type);
		else
			form.getMessageManager().addMessage(key, message, null, type, control);
	}

}
