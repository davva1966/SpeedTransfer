package com.ss.speedtransfer.ui.view;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;

import com.ss.speedtransfer.model.QueryDefinition;
import com.ss.speedtransfer.util.ConnectionManager;
import com.ss.speedtransfer.util.SQLHelper;
import com.ss.speedtransfer.util.SSUtil;
import com.ss.speedtransfer.util.UIHelper;

public class QueryDefinitionDataProvider implements IDataProvider {

	protected List<String[]> data = null;
	protected List<String[]> columnProperties = new ArrayList<String[]>();

	protected boolean cancelled = false;
	protected boolean sqlExecutionCompleted = false;
	protected String sqlExecutionError = null;

	protected QueryDefinition queryDefinition;

	public QueryDefinition getQueryDefinition() {
		return queryDefinition;
	}

	public QueryDefinitionDataProvider() {
		super();
	}

	public QueryDefinitionDataProvider(QueryDefinition queryDefinition) {
		super();
		setQueryDefinition(queryDefinition);
	}

	public void setQueryDefinition(QueryDefinition queryDefinition) {
		this.queryDefinition = queryDefinition;
		load();
	}

	@Override
	public Object getDataValue(int columnIndex, int rowIndex) {
		if (data == null)
			return null;
		try {
			return data.get(rowIndex)[columnIndex];
		} catch (Exception e) {
		}
		return null;

	}

	@Override
	public void setDataValue(int columnIndex, int rowIndex, Object newValue) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getColumnCount() {
		if (data == null)
			return 0;
		try {
			return data.get(0).length;
		} catch (Exception e) {
		}

		return 0;
	}

	@Override
	public int getRowCount() {
		if (data == null)
			return 0;
		return data.size();
	}

	public String[] getColumnProperties(int columnIndex) {
		if (columnProperties == null)
			return null;
		return columnProperties.get(columnIndex);
	}

	protected void load() {
		try {
			data = new ArrayList<String[]>();
			runSQLExecutionJob();
		} catch (Exception e) {
		}

	}

	protected void runSQLExecutionJob() {

		IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
		try {
			progressService.busyCursorWhile(new IRunnableWithProgress() {
				public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

					monitor.beginTask("Running SQL in database...", IProgressMonitor.UNKNOWN);
					try {
						final Connection connection = ConnectionManager.getConnection(getQueryDefinition());
						final Statement stmt = connection.createStatement();

						Thread executionThread = new Thread(new Runnable() {
							public void run() {
								ResultSet resultSet = null;
								try {
									resultSet = stmt.executeQuery(getQueryDefinition().getSQL().trim());
									if (monitor.isCanceled() == false) {
										columnProperties = SQLHelper.getColumnProperties(connection, resultSet, getQueryDefinition().getColumnHeading(), monitor);
										
										int totalRows = SQLHelper.getSQLRowCount(getQueryDefinition().getSQL().trim(), connection);
										SubMonitor progress = SubMonitor.convert(monitor);
										progress.beginTask("Retrieving data...", totalRows);

										
										int columnCount = resultSet.getMetaData().getColumnCount();
										while (monitor.isCanceled() == false && resultSet.next()) {
											String[] row = new String[columnCount];
											for (int i = 0; i < row.length; i++) {
												row[i] = resultSet.getString(i + 1);
											}
											data.add(row);
											progress.worked(1);
										}
									}

									if (monitor.isCanceled())
										cancelled = true;
								} catch (Exception e) {
									sqlExecutionError = SSUtil.getMessage(e);
								} finally {
									try {
										if (resultSet != null)
											resultSet.close();
									} catch (Exception e) {
									}
									try {
										if (stmt != null)
											stmt.close();
									} catch (Exception e) {
									}
									try {
										if (connection != null)
											connection.close();
									} catch (Exception e) {
									}
								}
							}
						}, "SQL Excution");
						executionThread.start();

						boolean running = true;
						while (running) {
							Thread.sleep(200);
							running = executionThread.isAlive() && monitor.isCanceled() == false;
						}

						if (executionThread.isAlive()) {
							cancelled = true;
							stmt.cancel();
						}

					} catch (Exception e) {
						throw new InvocationTargetException(e);

					}

					monitor.done();
					sqlExecutionCompleted = true;

				}
			});

		} catch (InvocationTargetException e) {
			UIHelper.instance().showErrorMsg("Error", SSUtil.getMessage(e));
			cancelled = true;
			sqlExecutionCompleted = true;
		} catch (InterruptedException e) {
			cancelled = true;
			sqlExecutionCompleted = true;
		}

		while (sqlExecutionCompleted == false) {
			try {
				Thread.sleep(200);
			} catch (Exception e) {
			}
		}

		if (cancelled == false && sqlExecutionError != null) {
			UIHelper.instance().showErrorMsg("Error", sqlExecutionError);
			cancelled = true;
		}

	}

}
