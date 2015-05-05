package com.ss.speedtransfer.export;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.w3c.dom.Element;

import com.ss.speedtransfer.SpeedTransferPlugin;
import com.ss.speedtransfer.model.QueryDefinition;
import com.ss.speedtransfer.util.ReplacementVariableTranslatorPrompt;
import com.ss.speedtransfer.util.SQLHelper;
import com.ss.speedtransfer.util.SSUtil;
import com.ss.speedtransfer.util.UIHelper;


public class QueryToCSVExporter extends AbstractQueryExporter implements QueryExporter {

	/** The separator property */
	public static final String SEPARATOR = "SEPARATOR";//$NON-NLS-1$

	/** TAB separated property */
	public static final String TAB_SEPARATED = "TAB_SEPARATED";//$NON-NLS-1$

	protected Text fileEdit;
	protected Button launchFile;
	protected Combo colsep;
	protected Button tabSepFile;
	protected Button okButton;

	/** Separator used to separate columns */
	protected String separator = ",";

	/** Use TAB to separate columns */
	protected boolean tabsep = false;

	public QueryToCSVExporter(QueryDefinition queryDef) {
		super(queryDef);
		setJobName("Query to CSV exporter");
		setJobImage(UIHelper.instance().getImageDescriptor("csv.gif"));
		FILE_EXT = "*.csv";//$NON-NLS-1$
	}

	public void export() {

		if (showComments()) {

			properties = openSettingsDialog(queryDef.getProperties());
			if (cancel)
				return;

			try {
				if (queryDef.hasReplacementVariables()) {
					ReplacementVariableTranslatorPrompt transl = getReplacementVariableTranslator(queryDef);
					transl.run();
					if (transl.isCancelled())
						return;
				}
			} catch (Exception e) {
				UIHelper.instance().showErrorMsg("Error", "Error occured. Error: " + SSUtil.getMessage(e));
			}

			super.export(queryDef.getSQL(), properties);
		}
	}

	public void export(String sql, Map<String, Object> properties, IProgressMonitor monitor) throws Exception {

		this.properties = properties;
		BufferedWriter writer = null;

		try {

			// Create the CSV File
			if (properties != null)
				separator = (String) properties.get(SEPARATOR);
			if (separator == null || separator.trim().length() == 0)
				separator = ",";

			if (properties != null) {
				String tabsepStr = (String) properties.get(TAB_SEPARATED);
				if (tabsepStr != null && tabsepStr.trim().length() > 0) {
					if (tabsepStr.trim().equalsIgnoreCase("true"))//$NON-NLS-1$
						tabsep = true;
				}
			}

			String file = null;
			if (properties != null)
				file = (String) properties.get(EXPORT_TO_FILE);

			if (file == null || file.trim().length() == 0)
				throw new Exception("No file specified");

			boolean launch = false;
			if (properties != null) {
				String launchStr = (String) properties.get(LAUNCH);
				if (launchStr != null && launchStr.trim().length() > 0) {
					if (launchStr.trim().equalsIgnoreCase("true"))//$NON-NLS-1$
						launch = true;
				}
			}

			if (monitor.isCanceled())
				return;

			monitor.subTask("Running query");
			ResultSet rs = getConnection().createStatement().executeQuery(sql);
			monitor.worked(1);

			if (monitor.isCanceled())
				return;

			if (rs != null) {

				monitor.subTask("Initializing result");

				FileWriter fileWriter = new FileWriter(file);
				writer = new BufferedWriter(fileWriter);
				monitor.worked(1);

				// Add table headers
				monitor.subTask("Adding header");
				doColumns(rs, writer, monitor);
				if (monitor.isCanceled())
					return;
				monitor.worked(1);

				// Add detail lines
				monitor.subTask("Adding detail lines");
				doData(rs, writer, monitor);
				if (monitor.isCanceled())
					return;

			}

			writer.close();
			writer = null;

			if (launch) {
				monitor.subTask("Launching file");
				Program p = Program.findProgram(".csv");//$NON-NLS-1$
				if (p == null)
					p = Program.findProgram(".txt");//$NON-NLS-1$
				if (p == null)
					p = Program.findProgram(".xls");//$NON-NLS-1$

				if (p != null)
					p.execute(file);
				else
					throw new Exception("No program found to launch file");

			}

		} catch (Exception e) {
			errorMessage = SSUtil.getMessage(e);
			throw e;
		} finally {
			closeConnection();
			try {
				if (writer != null)
					writer.close();
			} catch (IOException e) {
			}
		}

	}

	protected void doColumns(ResultSet rs, BufferedWriter writer, IProgressMonitor monitor) throws Exception {

		monitor.subTask("Retrieving column information from database...");

		String headingType = (String) properties.get(QueryDefinition.COLUMN_HEADINGS);
		List<String[]> columnProperties = SQLHelper.getColumnProperties(getConnection(), rs, headingType);
		if (monitor.isCanceled())
			return;

		int counter = 0;
		for (String[] columnProps : columnProperties) {
			counter++;
			writer.write(columnProps[4]);
			if (counter < columnProperties.size()) {
				if (tabsep)
					writer.write("\t");//$NON-NLS-1$
				else
					writer.write(separator);
			}
		}

		writer.write("\r\n");
	}

	protected void doData(ResultSet rs, BufferedWriter writer, IProgressMonitor monitor) throws Exception {

		int columnCount = rs.getMetaData().getColumnCount();
		int tempCount = 0;
		try {

			while (rs.next()) {
				for (int i = 1; i <= columnCount; i++) {
					writer.write(rs.getString(i));
					if (i < columnCount) {
						if (tabsep)
							writer.write("\t");
						else
							writer.write(separator);
					}
				}
				writer.write("\r\n");

				tempCount++;
				if (tempCount > 5000) {
					if (memoryWatcher.runMemoryCheck() == false) {
						monitor.setCanceled(true);
						break;
					}
					tempCount = 0;
				}

				if (monitor.isCanceled())
					return;
				monitor.worked(1);
			}

		} catch (OutOfMemoryError e) {
			Runtime.getRuntime().gc();
			monitor.setCanceled(true);
			throw new Exception("Out of memory", e);
		}

	}

	public String getTaskName(String arg) {
		return "Query to CSV exporter " + arg;

	}

	/**
	 * Open new edit dialog
	 */
	protected Map<String, Object> openSettingsDialog(Map<String, Object> settings) {

		this.properties = settings;

		IDialogSettings tempDialogSettings = null;
		try {
			tempDialogSettings = SpeedTransferPlugin.getDialogSettingsFor(this.getClass().getName());
		} catch (Exception e) {
		}
		final IDialogSettings dialogSettings = tempDialogSettings;

		Shell shell = UIHelper.instance().getActiveShell();
		final Shell settingsShell = new Shell(shell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE);
		settingsShell.setText("Export to CSV");
		settingsShell.setSize(500, 280);
		settingsShell.setLayout(new FillLayout());
		
		UIHelper.instance().centerInParent(shell, settingsShell);
		
		toolkit = new FormToolkit(settingsShell.getDisplay());
		form = toolkit.createForm(settingsShell);

		try {
			form.setBackgroundImage(UIHelper.instance().getImageDescriptor("form_banner.gif").createImage());
		} catch (Exception e) {
		}

		form.setText("Export to CSV");
		toolkit.decorateFormHeading(form);

		GridLayout layout = new GridLayout(3, false);
		form.getBody().setLayout(layout);

		GridData gd = new GridData(SWT.FILL, GridData.VERTICAL_ALIGN_BEGINNING, true, false, 3, 1);

		Group group = new Group(form.getBody(), SWT.NONE);
		group.setLayout(new GridLayout(2, false));
		group.setLayoutData(gd);
		toolkit.adapt(group);

		// Export to file label
		Label label = toolkit.createLabel(group, "Select file to export to", SWT.NONE);
		gd = new GridData(SWT.LEFT, SWT.TOP, false, false, 2, 1);
		label.setLayoutData(gd);

		// Export to file entry
		fileEdit = toolkit.createText(group, "", SWT.SINGLE);
		gd = new GridData(SWT.FILL, SWT.TOP, true, false);
		fileEdit.setLayoutData(gd);
		fileEdit.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				validatePage();
			}
		});

		// File browse button
		Button browseButton = toolkit.createButton(group, "Browse...", SWT.PUSH);
		gd = new GridData(SWT.LEFT, SWT.TOP, false, false);
		gd.widthHint = 90;
		gd.heightHint = 22;
		browseButton.setLayoutData(gd);
		browseButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				String file = UIHelper.instance().selectFile(null, "Select file to export to", FILE_EXT, SWT.SAVE);
				if (file == null)
					file = "";
				fileEdit.setText(file);
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		// Launch file check box
		launchFile = toolkit.createButton(group, "Launch file after export", SWT.CHECK);
		launchFile.setSelection(false);
		gd = new GridData(SWT.LEFT, SWT.TOP, false, false, 2, 1);
		gd.horizontalIndent = 5;
		gd.heightHint = 25;
		launchFile.setLayoutData(gd);

		Group sepGroup = new Group(form.getBody(), SWT.NONE);
		sepGroup.setLayout(new GridLayout(2, false));
		toolkit.adapt(sepGroup);

		gd = new GridData(SWT.FILL, GridData.VERTICAL_ALIGN_BEGINNING, true, false, 3, 1);
		gd.verticalIndent = 5;
		sepGroup.setLayoutData(gd);

		colsep = new Combo(sepGroup, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(SWT.LEFT, SWT.TOP, false, false);
		gd.widthHint = 100;
		colsep.setLayoutData(gd);
		colsep.add(",");//$NON-NLS-1$
		colsep.add(";");//$NON-NLS-1$
		colsep.add("|");//$NON-NLS-1$
		colsep.select(0);
		colsep.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				validatePage();
			}
		});
		toolkit.adapt(colsep);

		// Tab separated check box
		tabSepFile = toolkit.createButton(sepGroup, "Tab separated", SWT.CHECK);
		tabSepFile.setSelection(false);
		gd = new GridData(SWT.LEFT, SWT.TOP, false, false);
		gd.horizontalIndent = 10;
		tabSepFile.setLayoutData(gd);
		tabSepFile.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				validatePage();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		// OK button
		okButton = toolkit.createButton(form.getBody(), "OK", SWT.PUSH);
		okButton.setEnabled(false);
		gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		gd.widthHint = 100;
		okButton.setLayoutData(gd);
		okButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				if (properties == null)
					properties = new HashMap<String, Object>();
				properties.put(EXPORT_TO_FILE, adjustFileName(fileEdit.getText()));
				properties.put(SEPARATOR, colsep.getText());
				if (launchFile.getSelection())
					properties.put(LAUNCH, "true");
				else
					properties.put(LAUNCH, "false");
				if (tabSepFile.getSelection())
					properties.put(TAB_SEPARATED, "true");
				else
					properties.put(TAB_SEPARATED, "false");
				cancel = false;

				if (dialogSettings != null) {
					dialogSettings.put("file", fileEdit.getText());
					dialogSettings.put("launch", launchFile.getSelection());
					dialogSettings.put("columnSeparator", colsep.getText());
					dialogSettings.put("tabSeparated", tabSepFile.getSelection());
				}

				settingsShell.close();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		// Cancel button
		Button cancelButton = toolkit.createButton(form.getBody(), "Cancel", SWT.PUSH);
		gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		gd.widthHint = 100;
		cancelButton.setLayoutData(gd);
		cancelButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				cancel = true;
				settingsShell.close();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		// Set as default button
		if (canSaveDefaults) {
			Button setDefaultButton = toolkit.createButton(form.getBody(), "Save as default", SWT.PUSH);
			setDefaultButton.setToolTipText("Save these settings as the default settings for this query");
			gd = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
			gd.widthHint = 100;
			setDefaultButton.setLayoutData(gd);
			setDefaultButton.addSelectionListener(new SelectionListener() {
				public void widgetSelected(SelectionEvent e) {
					try {
						saveDefaults();
					} catch (Exception e2) {
						UIHelper.instance().showErrorMsg(settingsShell, "Error", "Failed to set defaults. Error: " + SSUtil.getMessage(e2));
					}

				}

				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});
		}

		boolean defaultsApplied = applyDefaults();
		if (defaultsApplied == false && dialogSettings != null) {
			if (dialogSettings.get("file") != null)
				fileEdit.setText(dialogSettings.get("file"));
			launchFile.setSelection(dialogSettings.getBoolean("launch"));
			if (dialogSettings.get("columnSeparator") != null && dialogSettings.get("columnSeparator").trim().length() > 0) //$NON-NLS-1$
				colsep.setText(dialogSettings.get("columnSeparator"));
			tabSepFile.setSelection(dialogSettings.getBoolean("tabSeparated"));
		}

		if (fileEdit.getText().trim().length() == 0) {
			String path = System.getProperty("user.home");
			fileEdit.setText(path + File.separator + "NewCSVFile.csv");
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

		return properties;

	}

	protected void validatePage() {

		okButton.setEnabled(true);
		colsep.setEnabled(true);

		if (fileEdit.getText().trim().length() == 0)
			okButton.setEnabled(false);

		if (tabSepFile.getSelection())
			colsep.setEnabled(false);

		if (colsep.getText().length() == 0 && tabSepFile.getSelection() == false)
			okButton.setEnabled(false);

		clearMessage(form, "File empty", fileEdit);
		if (fileEdit.getText().trim().length() == 0)
			issueMessage(form, "File empty", IMessageProvider.INFORMATION, "File must be specified", fileEdit);

	}

	protected void saveDefaults() {
		try {
			Element defaultsNode = (Element) queryDef.getCSVDefaultsNode(true);
			if (defaultsNode == null)
				return;

			defaultsNode.setAttribute(QueryDefinition.FILE_NAME, fileEdit.getText());
			defaultsNode.setAttribute(QueryDefinition.LAUNCH, Boolean.toString(launchFile.getSelection()));
			defaultsNode.setAttribute(QueryDefinition.COLUMN_SEPARATOR, colsep.getText());
			defaultsNode.setAttribute(QueryDefinition.TAB_SEPARATED, Boolean.toString(tabSepFile.getSelection()));

			if (queryDef.getFile() != null)
				queryDef.getFile().setContents(queryDef.getInputStream(), true, false, null);

		} catch (Exception e) {
			UIHelper.instance().showErrorMsg("Error", "Error when saving defaults. Error: " + SSUtil.getMessage(e));
		}

		UIHelper.instance().showMessage("Information", "Defaults saved");

	}

	protected boolean applyDefaults() {
		Element defaultsNode = (Element) queryDef.getCSVDefaultsNode(false);
		if (defaultsNode == null)
			return false;

		fileEdit.setText(defaultsNode.getAttribute(QueryDefinition.FILE_NAME));
		launchFile.setSelection(Boolean.parseBoolean(defaultsNode.getAttribute(QueryDefinition.LAUNCH)));
		colsep.setText(defaultsNode.getAttribute(QueryDefinition.COLUMN_SEPARATOR));
		tabSepFile.setSelection(Boolean.parseBoolean(defaultsNode.getAttribute(QueryDefinition.TAB_SEPARATED)));

		return true;

	}

}
