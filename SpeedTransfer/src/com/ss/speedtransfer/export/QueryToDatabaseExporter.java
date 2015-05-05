package com.ss.speedtransfer.export;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.w3c.dom.Element;

import com.ss.speedtransfer.SpeedTransferPlugin;
import com.ss.speedtransfer.model.DBConnection;
import com.ss.speedtransfer.model.QueryDefinition;
import com.ss.speedtransfer.ui.editor.dbcon.DBConnectionFileSelectionDialog;
import com.ss.speedtransfer.util.ConnectionManager;
import com.ss.speedtransfer.util.ReplacementVariableTranslatorPrompt;
import com.ss.speedtransfer.util.SSUtil;
import com.ss.speedtransfer.util.UIHelper;
import com.ss.speedtransfer.util.parser.sql.SQLTypes;


public class QueryToDatabaseExporter extends AbstractQueryExporter implements QueryExporter {

	public static final String EXPORT_TO_CON = "EXPORT_TO_CON";
	public static final String EXPORT_TO_TABLE = "EXPORT_TO_TABLE";
	public static final String CREATE_TABLE = "CREATE_TABLE";
	public static final String CLEAR_TABLE = "CLEAR_TABLE";

	protected Text dbConEdit;
	protected Text tableEdit;
	protected Button createTable;
	protected Button clearTable;
	protected Button okButton;

	protected DBConnection dbcon;
	protected String table;
	protected PreparedStatement insertStatement;
	protected Map<Integer, Integer> dataTypeMap = new HashMap<Integer, Integer>();

	// The destination SQL Connection
	protected Connection destConnection = null;

	public QueryToDatabaseExporter(QueryDefinition queryDef) {
		super(queryDef);
		setJobName("Query to Database exporter");
		setJobImage(UIHelper.instance().getImageDescriptor("export_to_db.gif"));
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
		dataTypeMap = new HashMap<Integer, Integer>();

		try {

			dbcon = null;
			table = null;
			boolean create = false;
			boolean clear = false;

			if (properties != null) {

				String dbconStr = (String) properties.get(EXPORT_TO_CON);
				IPath path = new Path(dbconStr);
				IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
				dbcon = new DBConnection(file);

				table = (String) properties.get(EXPORT_TO_TABLE);
				String createStr = (String) properties.get(CREATE_TABLE);
				if (createStr != null && createStr.trim().length() > 0) {
					if (createStr.trim().equalsIgnoreCase("true"))
						create = true;
				}
				String clearStr = (String) properties.get(CLEAR_TABLE);
				if (clearStr != null && clearStr.trim().length() > 0) {
					if (clearStr.trim().equalsIgnoreCase("true"))
						clear = true;
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

				if (rs.next()) {

					monitor.subTask("Creating table");
					initTable(rs, create);

					if (clear) {
						monitor.subTask("Clearing table");
						clearTable();
					}

					if (monitor.isCanceled())
						return;

					monitor.subTask("Adding data");
					addData(rs);

				}

				while (rs.next()) {
					addData(rs);
					monitor.worked(1);
					if (monitor.isCanceled())
						return;
				}

			}

		} catch (Exception e) {
			errorMessage = SSUtil.getMessage(e);
			throw e;
		} finally {
			closeDestConnection();
			closeConnection();
		}

	}

	protected void initTable(ResultSet rs, boolean create) throws Exception {

		if (create) {
			boolean dropped = false;
			try {
				getDestConnection().createStatement().execute("DROP TABLE IF EXISTS " + table);
				dropped = true;
			} catch (Exception e) {
			}

			if (dropped == false) {
				try {
					getDestConnection().createStatement().execute("DROP TABLE " + table);
					dropped = true;
				} catch (SQLException e) {
				}
			}
		}

		StringBuilder sb = new StringBuilder();

		sb.append("CREATE TABLE ");
		sb.append(table);
		sb.append(" (");

		ResultSetMetaData rsmd = rs.getMetaData();

		int count = 1;
		for (int i = 1; i <= rsmd.getColumnCount(); i++) {
			String colname = rsmd.getColumnName(i);
			if (colname == null || colname.trim().length() == 0) {
				colname = "Column" + count;
				count++;
			}
			sb.append(colname);
			sb.append(" ");
			sb.append(getColumnType(rsmd, i));
			if (i < rsmd.getColumnCount())
				sb.append(", ");
		}

		sb.append(")");

		if (create)
			getDestConnection().createStatement().execute(sb.toString());

	}

	protected void clearTable() throws Exception {

		getDestConnection().createStatement().execute("DELETE FROM " + table);

	}

	protected void addData(ResultSet rs) throws Exception {

		try {
			ResultSetMetaData rsmd = rs.getMetaData();

			PreparedStatement stmt = getInsertStatement(rsmd);

			for (int i = 1; i <= rsmd.getColumnCount(); i++) {
				if (dataTypeMap.get(i).equals(SQLTypes.INTEGER))
					stmt.setInt(i, rs.getInt(i));
				else if (dataTypeMap.get(i).equals(SQLTypes.DECIMAL))
					stmt.setBigDecimal(i, new BigDecimal(rs.getString(i)));
				else if (dataTypeMap.get(i).equals(SQLTypes.CHAR))
					stmt.setString(i, rs.getString(i));
				else if (dataTypeMap.get(i).equals(SQLTypes.DATE))
					stmt.setDate(i, rs.getDate(i));
				else if (dataTypeMap.get(i).equals(SQLTypes.TIME))
					stmt.setTime(i, rs.getTime(i));
				else if (dataTypeMap.get(i).equals(SQLTypes.TIMESTAMP))
					stmt.setTimestamp(i, rs.getTimestamp(i));
				else
					stmt.setString(i, rs.getString(i));
			}

			stmt.executeUpdate();

		} catch (OutOfMemoryError e) {
			Runtime.getRuntime().gc();
			throw new Exception("Out of memory", e);
		}

	}

	public String getTaskName(String arg) {
		return "Query to Database exporter " + arg;

	}

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
		settingsShell.setText("Export to Database");
		settingsShell.setSize(500, 300);
		settingsShell.setLayout(new FillLayout());
		
		UIHelper.instance().centerInParent(shell, settingsShell);
		
		toolkit = new FormToolkit(settingsShell.getDisplay());
		form = toolkit.createForm(settingsShell);

		try {
			form.setBackgroundImage(UIHelper.instance().getImageDescriptor("form_banner.gif").createImage());
		} catch (Exception e) {
		}

		form.setText("Export to Database");
		toolkit.decorateFormHeading(form);

		GridLayout layout = new GridLayout(3, false);
		form.getBody().setLayout(layout);

		GridData gd = new GridData(SWT.FILL, GridData.VERTICAL_ALIGN_BEGINNING, true, false, 3, 1);

		Group group = new Group(form.getBody(), SWT.NONE);
		group.setLayout(new GridLayout(2, false));
		group.setLayoutData(gd);
		toolkit.adapt(group);

		// Export to connection label
		Label label = toolkit.createLabel(group, "Select export target connection", SWT.NONE);
		gd = new GridData(SWT.LEFT, SWT.TOP, false, false, 2, 1);
		label.setLayoutData(gd);

		// Export to connection entry
		dbConEdit = toolkit.createText(group, "", SWT.SINGLE);
		gd = new GridData(SWT.FILL, SWT.TOP, true, false);
		dbConEdit.setLayoutData(gd);
		dbConEdit.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				validatePage();
			}
		});

		// Connection browse button
		Button dbConBrowseButton = toolkit.createButton(group, "Browse...", SWT.PUSH);
		gd = new GridData(SWT.LEFT, SWT.TOP, false, false);
		gd.widthHint = 90;
		gd.heightHint = 22;
		dbConBrowseButton.setLayoutData(gd);
		dbConBrowseButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				selectDbConnFile();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		// Export to table label
		label = toolkit.createLabel(group, "Table to export to", SWT.NONE);
		gd = new GridData(SWT.LEFT, SWT.TOP, false, false, 2, 1);
		label.setLayoutData(gd);

		// Export to table entry
		tableEdit = toolkit.createText(group, "", SWT.SINGLE);
		gd = new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1);
		tableEdit.setLayoutData(gd);
		tableEdit.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				validatePage();
			}
		});

		// Create table check box
		createTable = toolkit.createButton(group, "(Re)create table", SWT.CHECK);
		createTable.setSelection(true);
		gd = new GridData(SWT.LEFT, SWT.TOP, false, false, 2, 1);
		gd.horizontalIndent = 5;
		gd.heightHint = 25;
		createTable.setLayoutData(gd);

		// Clear table check box
		clearTable = toolkit.createButton(group, "Clear table", SWT.CHECK);
		clearTable.setSelection(true);
		gd = new GridData(SWT.LEFT, SWT.TOP, false, false, 2, 1);
		gd.horizontalIndent = 5;
		gd.heightHint = 25;
		clearTable.setLayoutData(gd);

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
				properties.put(EXPORT_TO_CON, dbConEdit.getText());
				properties.put(EXPORT_TO_TABLE, tableEdit.getText());
				properties.put(EXPORT_TO_FILE, tableEdit.getText());
				if (createTable.getSelection())
					properties.put(CREATE_TABLE, "true");
				else
					properties.put(CREATE_TABLE, "false");
				if (clearTable.getSelection())
					properties.put(CLEAR_TABLE, "true");
				else
					properties.put(CLEAR_TABLE, "false");
				cancel = false;

				if (dialogSettings != null) {
					dialogSettings.put("dbcon", dbConEdit.getText());
					dialogSettings.put("table", tableEdit.getText());
					dialogSettings.put("createTable", createTable.getSelection());
					dialogSettings.put("clearTable", clearTable.getSelection());
				}

				if (createTable.getSelection() || clearTable.getSelection()) {
					int resp = UIHelper
							.instance()
							.showMessageWithOKCancel(
									"Confirm Create/Delete",
									"You have selected to create and/or clear the target table. Make absulutely sure you have specified the right target connection and table name before proceeding. The target table will be cleared or deleted and recreated and all current data in the table will be lost!");
					if (resp == SWT.OK)
						settingsShell.close();
				} else {
					settingsShell.close();
				}

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
			if (dialogSettings.get("dbcon") != null)
				dbConEdit.setText(dialogSettings.get("dbcon"));
			if (dialogSettings.get("table") != null)
				tableEdit.setText(dialogSettings.get("table"));
			createTable.setSelection(dialogSettings.getBoolean("createTable"));
			clearTable.setSelection(dialogSettings.getBoolean("clearTable"));
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

		if (dbConEdit.getText().trim().length() == 0)
			okButton.setEnabled(false);

		if (tableEdit.getText().trim().length() == 0)
			okButton.setEnabled(false);

		clearMessage(form, "DBCon empty", dbConEdit);
		if (dbConEdit.getText().trim().length() == 0) {
			issueMessage(form, "DBCon empty", IMessageProvider.INFORMATION, "Connection must be specified", dbConEdit);
			okButton.setEnabled(false);
		}

		clearMessage(form, "Invalid dbcon", dbConEdit);
		if (dbConEdit.getText().trim().length() > 0) {
			String path = dbConEdit.getText().trim();
			try {
				IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(path));
				if (file.exists()) {
					if (DBConnection.isDBConnectionFile(file) == false) {
						issueMessage(form, "Invalid dbcon", IMessageProvider.ERROR, "The specified file is not a database connection file", dbConEdit);
						okButton.setEnabled(false);
					} else {
						DBConnection tempCon = new DBConnection(file);
						if (tempCon.canBeExportTarget() == false) {
							issueMessage(form, "Invalid dbcon", IMessageProvider.ERROR, "The specified database connection is not allowed as an export target", dbConEdit);
							okButton.setEnabled(false);
						}
					}
				}
			} catch (Exception e) {
				issueMessage(form, "Invalid dbcon", IMessageProvider.ERROR, "The specified file is not a database connection file", dbConEdit);
				okButton.setEnabled(false);
			}
		}

		clearMessage(form, "Table empty", tableEdit);
		if (tableEdit.getText().trim().length() == 0) {
			issueMessage(form, "Table empty", IMessageProvider.INFORMATION, "Table must be specified", tableEdit);
			okButton.setEnabled(false);
		}

	}

	protected void saveDefaults() {
		try {
			Element defaultsNode = (Element) queryDef.getDatabaseDefaultsNode(true);
			if (defaultsNode == null)
				return;

			defaultsNode.setAttribute(QueryDefinition.DB_CONNECTION_FILE, dbConEdit.getText());
			defaultsNode.setAttribute(QueryDefinition.TABLE, tableEdit.getText());
			defaultsNode.setAttribute(QueryDefinition.CREATE_TABLE, Boolean.toString(createTable.getSelection()));
			defaultsNode.setAttribute(QueryDefinition.CLEAR_TABLE, Boolean.toString(clearTable.getSelection()));

			if (queryDef.getFile() != null)
				queryDef.getFile().setContents(queryDef.getInputStream(), true, false, null);

		} catch (Exception e) {
			UIHelper.instance().showErrorMsg("Error", "Error when saving defaults. Error: " + SSUtil.getMessage(e));
		}

		UIHelper.instance().showMessage("Information", "Defaults saved");

	}

	protected boolean applyDefaults() {
		Element defaultsNode = (Element) queryDef.getDatabaseDefaultsNode(false);
		if (defaultsNode == null)
			return false;

		dbConEdit.setText(defaultsNode.getAttribute(QueryDefinition.DB_CONNECTION_FILE));
		tableEdit.setText(defaultsNode.getAttribute(QueryDefinition.TABLE));
		createTable.setSelection(Boolean.parseBoolean(defaultsNode.getAttribute(QueryDefinition.CREATE_TABLE)));
		clearTable.setSelection(Boolean.parseBoolean(defaultsNode.getAttribute(QueryDefinition.CLEAR_TABLE)));

		return true;

	}

	protected void selectDbConnFile() {
		IWorkbenchWindow window = UIHelper.instance().getActiveWindow();
		if (window != null) {
			DBConnectionFileSelectionDialog dialog = new DBConnectionFileSelectionDialog(window);
			dialog.open();
			if (dialog.getSelectedObject() != null || dialog.getSelectedObject() instanceof IFile) {
				IPath path = ((IFile) dialog.getSelectedObject()).getFullPath();
				dbConEdit.setText(path.toOSString());

			}
		}
	}

	protected String getColumnType(ResultSetMetaData rsmd, int index) throws SQLException {
		int sqlType = rsmd.getColumnType(index);

		switch (sqlType) {

		case SQLTypes.TINYINT:
		case SQLTypes.SMALLINT:
		case SQLTypes.INTEGER:
		case SQLTypes.BIGINT:
			dataTypeMap.put(index, SQLTypes.INTEGER);
			return "INT";

		case SQLTypes.FLOAT:
		case SQLTypes.REAL:
		case SQLTypes.DOUBLE:
		case SQLTypes.NUMERIC:
		case SQLTypes.DECIMAL:
			dataTypeMap.put(index, SQLTypes.DECIMAL);
			return "DECIMAL(20,7)";

		case SQLTypes.CHAR:
		case SQLTypes.VARCHAR:
		case SQLTypes.LONGVARCHAR:
			dataTypeMap.put(index, SQLTypes.CHAR);
			return "VARCHAR(255)";

		case SQLTypes.DATE:
			dataTypeMap.put(index, SQLTypes.DATE);
			return "DATE";
		case SQLTypes.TIME:
			dataTypeMap.put(index, SQLTypes.TIME);
			return "TIME";
		case SQLTypes.TIMESTAMP:
			dataTypeMap.put(index, SQLTypes.TIMESTAMP);
			if (dbcon.getConnectionType().equalsIgnoreCase(DBConnection.SQLSERVER))
				return "DATETIME";
			else
				return "TIMESTAMP";

		default:
			break;
		}

		return "VARCHAR(255)";

	}

	protected PreparedStatement getInsertStatement(ResultSetMetaData rsmd) throws Exception {
		if (insertStatement == null) {
			StringBuilder sb = new StringBuilder();
			sb.append("INSERT INTO ");
			sb.append(table);
			sb.append(" (");
			for (int i = 1; i <= rsmd.getColumnCount(); i++) {
				sb.append(rsmd.getColumnName(i));
				if (i < rsmd.getColumnCount())
					sb.append(",");
			}
			sb.append(") VALUES(");
			for (int i = 1; i <= rsmd.getColumnCount(); i++) {
				sb.append("?");
				if (i < rsmd.getColumnCount())
					sb.append(",");
			}

			sb.append(")");

			insertStatement = getDestConnection().prepareStatement(sb.toString());
		}

		return insertStatement;
	}

	protected Connection getDestConnection() throws Exception {
		if (destConnection == null)
			destConnection = ConnectionManager.getConnection(dbcon);

		return destConnection;
	}

	protected void closeDestConnection() {
		try {
			if (destConnection != null)
				destConnection.close();
			destConnection = null;
		} catch (Exception e) {
		}
	}

}
