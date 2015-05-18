package com.ss.speedtransfer.ui.nattable;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;
import org.eclipse.ui.progress.UIJob;

import com.ss.speedtransfer.model.QueryDefinition;
import com.ss.speedtransfer.ui.view.QueryResultView;
import com.ss.speedtransfer.util.ConnectionManager;
import com.ss.speedtransfer.util.MemoryWatcher;
import com.ss.speedtransfer.util.SQLHelper;
import com.ss.speedtransfer.util.SSUtil;
import com.ss.speedtransfer.util.UIHelper;

public class QueryDefinitionDataProvider implements IDataProvider {

	protected List<String[]> data = null;
	protected List<String[]> columnProperties = new ArrayList<String[]>();

	protected boolean cancelled = false;
	protected boolean sqlExecutionCompleted = false;
	protected String sqlExecutionError = null;

	protected int rowsToLoad = QueryDefinition.DEFAULT_ROWS_TO_PREVIEW;
	protected int rowsLoaded = 0;
	protected int totalRows = 0;

	protected QueryDefinition queryDefinition;

	protected QueryResultView viewToNotify;
	protected MemoryWatcher memoryWatcher = new MemoryWatcher();

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
		rowsToLoad = queryDefinition.getRowsToPreview();
		load();
	}

	public QueryResultView getViewToNotify() {
		return viewToNotify;
	}

	public void setViewToNotify(QueryResultView viewToNotify) {
		this.viewToNotify = viewToNotify;
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

	public int getTotalRows() {
		return totalRows;
	}

	public int getRowsLoaded() {
		return rowsLoaded;
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

	public List<String[]> getColumnProperties() {
		return columnProperties;
	}

	public String[] getColumnProperties(int columnIndex) {
		if (columnProperties == null)
			return null;
		return columnProperties.get(columnIndex);
	}

	public void getAll() {
		rowsToLoad = -1;
		load();
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
						if (rowsToLoad > 0)
							stmt.setMaxRows(rowsToLoad);

						Thread executionThread = new Thread(new Runnable() {
							public void run() {
								ResultSet resultSet = null;
								try {
									resultSet = stmt.executeQuery(getQueryDefinition().getSQL().trim());
									if (monitor.isCanceled() == false) {
										int columnCount = resultSet.getMetaData().getColumnCount();
										columnProperties = SQLHelper.getColumnProperties(connection, resultSet, getQueryDefinition().getColumnHeading(), monitor);
										String[] row = new String[columnCount];
										for (int i = 0; i < row.length; i++) {
											row[i] = columnProperties.get(i)[4];
										}
										data.add(row);

										totalRows = SQLHelper.getSQLRowCount(getQueryDefinition().getSQL().trim(), connection);

										int rowsBeingLoaded = totalRows;
										if (rowsToLoad > 0 && rowsToLoad < totalRows)
											rowsBeingLoaded = rowsToLoad;

										SubMonitor progress = SubMonitor.convert(monitor);

										progress.beginTask("Retrieving " + rowsBeingLoaded + " out of " + totalRows + " rows of data...", totalRows);

										int tempCount = 0;
										while (monitor.isCanceled() == false && resultSet.next()) {
											row = new String[columnCount];
											for (int i = 0; i < row.length; i++) {
												row[i] = resultSet.getString(i + 1);
											}
											data.add(row);
											progress.worked(1);

											tempCount++;
											if (tempCount > 1000) {
												if (memoryWatcher.runMemoryCheck() == false) {
													monitor.setCanceled(true);
													cancelled = true;
													break;
												}
												tempCount = 0;
											}
										}

										// Set the number of rows loaded
										// (subtract header row)
										rowsLoaded = data.size();
										if (rowsLoaded > 0)
											rowsLoaded = rowsLoaded - 1;
									}

									if (monitor.isCanceled())
										cancelled = true;
								} catch (Exception e) {
									sqlExecutionError = SSUtil.getMessage(e);
								} catch (OutOfMemoryError e) {
									Runtime.getRuntime().gc();
									monitor.setCanceled(true);
									PlatformUI.getWorkbench().close();
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

					notifyView();

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

	protected void notifyView() {
		if (viewToNotify == null)
			return;
		UIJob notifyView = new UIJob("NotifyView") {
			public IStatus runInUIThread(IProgressMonitor monitor) {
				try {
					viewToNotify.dataLoadCompleted();
				} catch (Exception e) {
				}

				return Status.OK_STATUS;
			}
		};

		notifyView.schedule();
	}

}
