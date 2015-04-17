// {{CopyrightNotice}}

package com.ss.speedtransfer.export;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.progress.IProgressConstants;

import com.ss.speedtransfer.SpeedTransferPlugin;
import com.ss.speedtransfer.model.QueryDefinition;
import com.ss.speedtransfer.model.SQLScratchPad;
import com.ss.speedtransfer.util.CommentPrompt;
import com.ss.speedtransfer.util.ConnectionManager;
import com.ss.speedtransfer.util.SSUtil;
import com.ss.speedtransfer.util.MemoryWatcher;
import com.ss.speedtransfer.util.ReplacementVariableTranslatorPrompt;
import com.ss.speedtransfer.util.SQLExecuter;
import com.ss.speedtransfer.util.SQLHelper;
import com.ss.speedtransfer.util.UIHelper;


/**
 * The <code>AbstractQueryExporter</code> class
 */
public abstract class AbstractQueryExporter implements QueryExporter {

	/** The job name for the exporter */
	protected String jobName = null;

	/** The job image descriptor for the exporter */
	protected ImageDescriptor jobImage = null;

	/** The file extension of exported files */
	protected String FILE_EXT = "*.*";//$NON-NLS-1$

	/** Export properties */
	protected Map<String, Object> properties = null;

	/** Cancel exporter */
	protected boolean cancel = false;

	/** Error message */
	protected String errorMessage = null;

	// The SQL Connection
	protected Connection connection = null;

	// Total number of rows
	protected int totalRows = -1;

	protected boolean useEclipse = false;
	protected boolean canSaveDefaults = false;

	protected boolean exportCancelled = false;

	protected QueryDefinition queryDef = null;

	protected MemoryWatcher memoryWatcher = new MemoryWatcher();

	FormToolkit toolkit;
	Form form;

	public AbstractQueryExporter(QueryDefinition queryDef) {
		super();
		this.queryDef = queryDef;
		useEclipse = false;
		try {
			if (PlatformUI.isWorkbenchRunning())
				useEclipse = true;
		} catch (Exception e) {
		}

		if (useEclipse == false) {
			canSaveDefaults = false;
		} else {
			canSaveDefaults = (queryDef instanceof SQLScratchPad == false);
		}
	}

	public void export(String sql, Map<String, Object> properties) {

		try {
			if (properties == null)
				properties = new HashMap<String, Object>();
			String file = (String) properties.get(EXPORT_TO_FILE);
			if (file == null || file.trim().length() == 0) {
				file = UIHelper.instance().selectFile(null, "Select file to export to", FILE_EXT, SWT.SAVE);
				if (file == null || file.trim().length() == 0)
					return;
				properties.put(EXPORT_TO_FILE, file);
			}

			if (SQLHelper.isSingleStatementQuery(sql) == false)
				runSQLJob();
			if (cancel == true)
				return;

			if (SQLHelper.hasMultipleStatements(sql))
				sql = SQLHelper.getLastSQLStatement(sql);

			if (useEclipse)
				runEclipseJob(sql, properties);
			else
				runJob(sql, properties);

		} catch (Exception e) {
			UIHelper.instance().showErrorMsg("Error", SSUtil.getMessage(e));
		}

		if (toolkit != null)
			toolkit.dispose();

	}

	public abstract void export(String querySQL, Map<String, Object> properties, IProgressMonitor monitor) throws Exception;

	protected Action getExporterCompletedAction() {
		return new Action("View export status") {

			public void run() {
				if (errorMessage != null && errorMessage.trim().length() > 0) {
					MessageDialog.openError(UIHelper.instance().getActiveShell(), "Error during export", errorMessage);
				} else {
					if (exportCancelled)
						MessageDialog.openInformation(UIHelper.instance().getActiveShell(), "Export cancelled", "Export was cancelled.");
					else
						MessageDialog.openInformation(UIHelper.instance().getActiveShell(), "Export completed normally", "Export has been completed. " + totalRows + " rows exported.");
				}
			}
		};
	}

	protected void showResults() {
		Display.getDefault().asyncExec(new Runnable() {

			public void run() {
				getExporterCompletedAction().run();
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.ibs.qb.exporters.QueryExporter#getJobName()
	 */
	public String getJobName() {
		return jobName;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.ibs.qb.exporters.QueryExporter#getJobName(java.lang.String)
	 */
	public String getJobName(String arg) {
		return jobName;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.ibs.qb.exporters.QueryExporter#setJobName(java.lang.String)
	 */
	public void setJobName(String jobName) {
		this.jobName = jobName;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.ibs.qb.exporters.QueryExporter#getJobImage()
	 */
	public ImageDescriptor getJobImage() {
		return jobImage;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.ibs.qb.exporters.QueryExporter#setJobImage(org.eclipse.jface.resource
	 * .ImageDescriptor)
	 */
	public void setJobImage(ImageDescriptor jobImage) {
		this.jobImage = jobImage;

	}

	protected String adjustFileName(String name) {
		try {
			if (name.indexOf(".") == -1 && FILE_EXT.indexOf(".") > -1) {
				String ext = FILE_EXT.substring(FILE_EXT.indexOf(".") + 1);
				if (ext.length() > 0 && ext.trim().equals("*") == false)
					return name + FILE_EXT.substring(FILE_EXT.indexOf("."));
			}
		} catch (Exception e) {
		}

		return name;

	}

	public void setUseEclipse(boolean flag) {
		this.useEclipse = flag;

	}

	protected Connection getConnection() throws Exception {
		if (connection == null)
			connection = ConnectionManager.getConnection(queryDef);

		return connection;
	}

	protected void closeConnection() {
		try {
			if (connection != null)
				connection.close();
			connection = null;
		} catch (Exception e) {
		}
	}

	protected int getTotalRows(String sql) throws Exception {
		if (totalRows == -1)
			totalRows = SQLHelper.getSQLRowCount(sql, getConnection());

		return totalRows;
	}

	protected ReplacementVariableTranslatorPrompt getReplacementVariableTranslator(QueryDefinition queryDef) throws Exception {
		return new ReplacementVariableTranslatorPrompt(queryDef, getConnection());
	}

	protected void runEclipseJob(final String sql, Map<String, Object> properties) {

		final Map<String, Object> tempProps = properties;
		final String exportToFile = (String) properties.get(EXPORT_TO_FILE);

		Job job = new Job(getJobName()) {
			public IStatus run(IProgressMonitor monitor) {

				IStatus status = Status.OK_STATUS;
				boolean error = false;

				try {

					final int totalRows = getTotalRows(sql);

					setProperty(IProgressConstants.ICON_PROPERTY, getJobImage());

					if (totalRows > 0) {
						monitor.beginTask(getTaskName(exportToFile), totalRows + 3);
					} else {
						monitor.beginTask(getTaskName(exportToFile), IProgressMonitor.UNKNOWN);
					}

					// Run the exporter
					export(sql, tempProps, monitor);
					if (monitor.isCanceled()) {
						status = Status.CANCEL_STATUS;
						exportCancelled = true;
						cancel = true;
					} else {
						String msg = "Success. " + totalRows + " rows exported.";
						status = new Status(IStatus.OK, SpeedTransferPlugin.PLUGIN_ID, IStatus.OK, msg, null);
					}
				} catch (Exception e) {
					error = true;
					errorMessage = SSUtil.getMessage(e);
					String msg = "Error during export";
					status = new Status(IStatus.ERROR, SpeedTransferPlugin.PLUGIN_ID, IStatus.OK, msg, e);
				}

				if (isModal(this)) {
					// The progress dialog is still open so just open the
					// message
					if (error == false)
						showResults();
				} else {
					setProperty(IProgressConstants.KEEP_PROPERTY, Boolean.TRUE);
					setProperty(IProgressConstants.ACTION_PROPERTY, getExporterCompletedAction());
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
		job.setRule(new ExportSchedulingRule(exportToFile));
		job.schedule();
	}

	public void runJob(final String sql, Map<String, Object> properties) {

		final Map<String, Object> tempProps = properties;
		final String exportToFile = (String) properties.get(EXPORT_TO_FILE);

		Shell shell = new Shell();
		try {
			final int totalRows = getTotalRows(sql);

			ProgressMonitorDialog pmd = new ProgressMonitorDialog(shell);
			pmd.run(true, true, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					if (totalRows > 0) {
						monitor.beginTask(getTaskName(exportToFile), totalRows + 3);
					} else {
						monitor.beginTask(getTaskName(exportToFile), IProgressMonitor.UNKNOWN);
					}

					// Run the exporter
					try {
						export(sql, tempProps, monitor);
						if (monitor.isCanceled())
							throw new InterruptedException("Execution was cancelled");
					} catch (InterruptedException e) {
						throw e;
					} catch (Exception e) {
						throw new InvocationTargetException(e);
					}
					monitor.done();

				}
			});
		} catch (InterruptedException e) {
			cancel = true;
			MessageDialog.openInformation(shell, "Cancelled", SSUtil.getMessage(e));
		} catch (Exception e) {
			cancel = true;
			UIHelper.instance().showErrorMsg("Error", SSUtil.getMessage(e));
		}

	}

	@Override
	protected void finalize() throws Throwable {
		closeConnection();
		super.finalize();
	}

	protected void issueMessage(Form form, String key, int type, String message) {
		issueMessage(form, key, type, message, null);
	}

	protected void issueMessage(Form form, String key, int type, String message, Control control) {
		if (control == null)
			form.getMessageManager().addMessage(key, message, null, type);
		else
			form.getMessageManager().addMessage(key, message, null, type, control);
	}

	protected void clearAllMessages(Form form) {
		form.getMessageManager().removeAllMessages();
	}

	protected void clearMessage(Form form, String key) {
		clearMessage(form, key, null);
	}

	protected void clearMessage(Form form, String key, Control control) {
		if (control == null)
			form.getMessageManager().removeMessage(key);
		else
			form.getMessageManager().removeMessage(key, control);
	}

	protected void runSQLJob() throws InvocationTargetException, InterruptedException {

		Shell shell = UIHelper.instance().getActiveShell();
		ProgressMonitorDialog pmd = new ProgressMonitorDialog(shell);
		pmd.run(true, true, new IRunnableWithProgress() {
			public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

				monitor.beginTask("Running SQL...", IProgressMonitor.UNKNOWN);

				String sqlpart;
				if (queryDef.isQuery())
					sqlpart = SQLHelper.getAllButLastSQLStatement(queryDef.getSQL());
				else
					sqlpart = queryDef.getSQL();

				String sql = sqlpart;

				try {
					SQLExecuter executer = new SQLExecuter();
					executer.run(getConnection(), sql, queryDef.getDBConnection().hasTransactionSupport(), monitor);

					if (monitor.isCanceled())
						cancel = true;

				} catch (Exception e) {
					cancel = true;
					throw new InvocationTargetException(e);
				}

				monitor.done();

			}
		});

	}

	protected boolean showComments() {
		if (queryDef.getShowCommentAtRuntime()) {
			CommentPrompt prompter = new CommentPrompt(queryDef);
			return prompter.prompt();
		}

		return true;
	}

	public boolean isCancelled() {
		return cancel;

	}
}
