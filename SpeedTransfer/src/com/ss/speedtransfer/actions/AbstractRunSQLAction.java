package com.ss.speedtransfer.actions;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;

import com.ss.speedtransfer.model.QueryDefinition;
import com.ss.speedtransfer.model.SQLScratchPad;
import com.ss.speedtransfer.ui.StyledEdit;
import com.ss.speedtransfer.ui.editor.querydef.QueryDefinitonEditor;
import com.ss.speedtransfer.ui.editor.querydef.SQLScratchPadEditor;
import com.ss.speedtransfer.util.CommentPrompt;
import com.ss.speedtransfer.util.ConnectionManager;
import com.ss.speedtransfer.util.ReplacementVariableTranslatorPrompt;
import com.ss.speedtransfer.util.SQLExecuter;
import com.ss.speedtransfer.util.SQLHelper;
import com.ss.speedtransfer.util.SSUtil;
import com.ss.speedtransfer.util.StringHelper;
import com.ss.speedtransfer.util.UIHelper;


public abstract class AbstractRunSQLAction extends Action {

	protected QueryDefinition queryDef;
	protected QueryDefinition savedQueryDef;
	protected boolean cancelled = false;
	protected int affectedRows = 0;
	protected Connection connection = null;
	protected StyledEdit sqlEdit = null;

	public AbstractRunSQLAction() {
		this(null);

	}

	public AbstractRunSQLAction(QueryDefinition queryDef) {
		super("Run SQL", IAction.AS_PUSH_BUTTON);
		this.queryDef = queryDef;
		setImageDescriptor(UIHelper.instance().getImageDescriptor("execute.gif"));

	}

	public AbstractRunSQLAction(QueryDefinition queryDef, StyledEdit sqlEdit) {
		super("Run SQL", IAction.AS_PUSH_BUTTON);
		this.sqlEdit = sqlEdit;
		this.queryDef = queryDef;
		setImageDescriptor(UIHelper.instance().getImageDescriptor("execute.gif"));

	}

	public void run() {
		try {

			if (SSUtil.validateSelectOnly(queryDef) == false) {
				cancelled = true;
				return;
			}

			cancelled = false;

			savedQueryDef = queryDef;

			if (sqlEdit != null) {
				SQLScratchPad sp = new SQLScratchPad();
				sp.setDBConnectionFile(queryDef.getDBConnectionFile());
				sp.setSQL(sqlEdit.getText());
				sp.setName("SQL Result");
				queryDef = sp;
			}

			if (queryDef == null) {
				IEditorPart editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
				if (editor != null) {
					if (editor instanceof QueryDefinitonEditor)
						queryDef = ((QueryDefinitonEditor) editor).getQueryDefinition();
					if (editor instanceof SQLScratchPadEditor)
						queryDef = ((SQLScratchPadEditor) editor).getQueryDefinition();
				}
			}

			if (queryDef == null)
				return;

			boolean response = maybeShowComments();
			if (response == false)
				return;

			String tempID = queryDef.getName();
			if (queryDef.getFile() == null) {
				if (queryDef instanceof SQLScratchPad && sqlEdit == null) {
					queryDef.setName("ScratchPad");
					tempID = Integer.toString(queryDef.hashCode());
				} else {
					queryDef.setName("SQL Result");
					tempID = queryDef.getName();
				}
			}

			final String id = tempID;

			Connection con = null;
			try {
				con = ConnectionManager.getConnection(queryDef);

				if (queryDef.hasReplacementVariables()) {
					ReplacementVariableTranslatorPrompt transl = new ReplacementVariableTranslatorPrompt(queryDef, con);
					transl.run();
					if (transl.isCancelled())
						return;
				}

				if (confirmExecution() && queryDef.isSingleStatementQuery() == false)
					runSQLJob();
				if (cancelled)
					return;

				if (queryDef.isQuery()) {

					String savedSQL = null;
					if (SQLHelper.hasMultipleStatements(queryDef.getSQL())) {
						savedSQL = queryDef.getSQL();
						queryDef.setSQL(SQLHelper.getLastSQLStatement(queryDef.getSQL()));
					}

					showResult(id);

					if (savedSQL != null)
						queryDef.setSQL(savedSQL);

				} else {
					if (SQLHelper.producesResult(queryDef.getSQL()))
						UIHelper.instance().showMessage("SQL Executed", "" + affectedRows + " rows " + getSQLType() + ".");
					else
						UIHelper.instance().showMessage("SQL Executed", "SQL executed sucessfully");
				}

			} catch (Exception e) {
				UIHelper.instance().showErrorMsg("Error", SSUtil.getMessage(e));
			} finally {
				try {
					if (con != null && con.isClosed() == false)
						con.close();
				} catch (Exception e2) {
				}
			}

		} catch (Exception e) {
			UIHelper.instance().showErrorMsg("Error", SSUtil.getMessage(e));
		} finally {
			queryDef = savedQueryDef;
			closeConnection();
		}

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
					affectedRows = executer.getAffectedRows();

					if (monitor.isCanceled())
						cancelled = true;

				} catch (Exception e) {
					cancelled = true;
					throw new InvocationTargetException(e);
				}

				monitor.done();

			}
		});

	}

	protected Connection getConnection() throws Exception {
		if (connection == null)
			connection = ConnectionManager.getConnection(queryDef);

		return connection;
	}

	protected synchronized void closeConnection() {
		try {
			if (connection != null)
				connection.close();
			connection = null;
		} catch (Exception e) {
		}
	}

	@Override
	protected void finalize() throws Throwable {
		closeConnection();
		super.finalize();
	}

	protected String getSQLType() {
		String type = queryDef.getSQLType().trim();
		if (type.equalsIgnoreCase("update"))
			return "updated";
		if (type.equalsIgnoreCase("insert"))
			return "inserted";
		if (type.equalsIgnoreCase("delete"))
			return "deleted";

		return type;
	}

	protected boolean confirmExecution() {
		if (SQLHelper.hasMultipleStatements(queryDef.getSQL()))
			return true;
		if (queryDef.isSingleStatementQuery())
			return true;
		if (queryDef.getSQL().toLowerCase().contains(" where "))
			return true;

		if (queryDef.getSQLType().equalsIgnoreCase("update") || queryDef.getSQLType().equalsIgnoreCase("delete")) {
			int result = UIHelper.instance().showMessageWithOKCancel(
					"Confirm SQL Execution",
					"Warning! Executing this SQL will " + queryDef.getSQLType() + " all rows in the selected table." + StringHelper.getNewLine() + StringHelper.getNewLine()
							+ "Do you wish to proceed?");
			if (result == SWT.CANCEL)
				return false;
		}

		return true;
	}

	protected boolean maybeShowComments() {
		if (queryDef.getShowCommentAtRuntime() && queryDef.getComment().trim().length() > 0) {
			CommentPrompt prompter = new CommentPrompt(queryDef);
			return prompter.prompt();
		}

		return true;
	}

	protected abstract void showResult(String secondaryID) throws Exception;

}
