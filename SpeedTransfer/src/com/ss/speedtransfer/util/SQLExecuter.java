package com.ss.speedtransfer.util;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

public class SQLExecuter {

	protected boolean cancelled = false;
	protected int affectedRows = 0;
	protected Exception executionException = null;
	protected Statement executionStmt = null;

	public SQLExecuter() {
		super();
	}

	public void run(final Connection connection, final String sql, boolean useTransaction, final IProgressMonitor monitor) throws Exception {

		if (useTransaction)
			connection.setAutoCommit(false);
		executionStmt = connection.createStatement();

		executionException = null;
		Thread executionThread = new Thread(new Runnable() {
			public void run() {
				try {
					execute(sql, monitor);
				} catch (Exception e) {
					executionException = e;
				}
			}
		}, "SQL Excution");
		executionThread.start();

		boolean running = true;
		while (running) {
			Thread.sleep(100);
			running = executionThread.isAlive() && monitor.isCanceled() == false;
		}

		if (executionThread.isAlive()) {
			executionStmt.cancel();
			if (useTransaction)
				connection.rollback();
			affectedRows = 0;
		} else {
			if (useTransaction)
				connection.commit();
		}

		if (executionException != null && monitor.isCanceled() == false) {
			if (useTransaction)
				connection.rollback();
			affectedRows = 0;
			cancelled = true;
			throw executionException;
		}

		if (monitor.isCanceled()) {
			cancelled = true;
		} else {
			cancelled = false;
		}

	}

	protected void execute(String sql, IProgressMonitor monitor) throws Exception {
		try {
			if (shouldReportResult(sql)) {
				affectedRows = executionStmt.executeUpdate(sql);
			} else {
				List<String> statements = SQLHelper.getStatements(sql);
				for (String stmt : statements) {
					monitor.subTask("Executing: " + stmt);
					try {
						executionStmt.execute(stmt);
						if (monitor.isCanceled()) {
							cancelled = true;
							break;
						}
					} catch (SQLException e) {
						// Quietly ignore table not found on drop for iSeries
						if (e.getMessage() != null) {
							if (e.getMessage().contains("SQL0204") == false)
								throw e;
						} else {
							throw e;
						}
					}
				}
			}
		} finally {
			if (executionStmt != null)
				executionStmt.close();
		}

	}

	public boolean shouldReportResult(String sql) {
		return SQLHelper.producesResult(sql);

	}

	public int getAffectedRows() {
		return affectedRows;
	}

}
