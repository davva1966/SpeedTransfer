package com.ss.speedtransfer.model;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IExecutionListener;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.progress.IProgressService;
import org.eclipse.ui.progress.UIJob;

import com.ss.speedtransfer.handlers.ToggleLazyLoadHandler;
import com.ss.speedtransfer.ui.view.QueryResultView;
import com.ss.speedtransfer.util.ConnectionManager;
import com.ss.speedtransfer.util.MemoryWatcher;
import com.ss.speedtransfer.util.SQLHelper;
import com.ss.speedtransfer.util.SSUtil;
import com.ss.speedtransfer.util.UIHelper;


public class QueryResultLazyContentProvider implements ILazyContentProvider {

	protected QueryResultView view;
	protected ResultSet result;
	protected int columnCount;
	protected Connection connection;
	protected QueryDefinition queryDef;
	protected List<String[]> columnProperties = new ArrayList<String[]>();
	protected int rowCount = 1;
	protected boolean lazyLoading = true;
	protected boolean cancelled = false;
	protected boolean columnsAdjusted = false;
	protected int sqlRowCount = 0;
	protected List<List<String>> rows = new ArrayList<List<String>>();

	protected boolean sqlExecutionCompleted = false;
	protected String sqlExecutionError = null;

	protected boolean loadChunkExecutionCompleted = true;
	protected String loadChunkError = null;

	protected boolean loading = false;

	protected MemoryWatcher memoryWatcher = new MemoryWatcher();

	protected IExecutionListener lazyLoadCommandListener = new IExecutionListener() {
		public void preExecute(String commandId, ExecutionEvent event) {
		}

		public void postExecuteSuccess(String commandId, Object returnValue) {
			if (commandId.equals("com.ss.speedtransfer.toggleLazyLoadCommand"))
				setLazyLoading(ToggleLazyLoadHandler.isLazyLoading());
		}

		public void postExecuteFailure(String commandId, ExecutionException exception) {
		}

		public void notHandled(String commandId, NotHandledException exception) {
		}
	};

	public QueryResultLazyContentProvider(QueryResultView view) {
		super();
		this.view = view;
		this.lazyLoading = ToggleLazyLoadHandler.isLazyLoading();
		monitorLazyLoadState();
	}

	public void inputChanged(Viewer v, Object oldInput, final Object newInput) {
		try {
			if (result != null && result.isClosed() == false)
				result.close();
		} catch (Exception e) {
		}

		closeConnection();

		if (newInput == null)
			return;

		columnsAdjusted = false;

		view.getViewer().getTable().setVisible(false);

		queryDef = (QueryDefinition) newInput;
		rows = new ArrayList<List<String>>();
		rowCount = 1;

		try {
			runSQLExecutionJob();
			if (cancelled) {
				runDisposeJob();
				return;
			}

			view.getViewer().setItemCount(sqlRowCount);
			view.inputChanged(result, columnProperties, queryDef);

		} catch (Exception e) {
			view.getViewer().setItemCount(0);
			UIHelper.instance().showErrorMsg("Error", SSUtil.getMessage(e));
		} finally {
			view.getViewer().getTable().setVisible(true);
		}

	}

	public void dispose() {
		try {
			if (result != null && result.isClosed() == false)
				result.close();
		} catch (Exception e) {
		}
		closeConnection();
		ICommandService service = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
		service.removeExecutionListener(lazyLoadCommandListener);

	}

	public synchronized Object getElement(int index) {

		cancelled = false;
		if (result == null)
			return new ArrayList<String>();

		if (index < rows.size())
			return rows.get(index);

		while (loading) {
			try {
				wait(1000);
			} catch (InterruptedException e) {
				cancelled = true;
				break;
			}
		}

		if (!loading)
			loadElements(index);

		try {
			if (memoryWatcher.runMemoryCheck() == false)
				cancelled = true;
		} catch (OutOfMemoryError e) {
			Runtime.getRuntime().gc();
			cancelled = true;
		}

		if (cancelled)
			runDisposeJob();

		if (index < rows.size())
			return rows.get(index);

		return null;
	}

	protected void loadElements(final int index) {

		loading = true;
		try {

			// If more than 2000 rows to load run asynchronously
			if (index - rows.size() > 2000 || lazyLoading == false) {
				runLoadChunkJob(index);
			} else {
				BusyIndicator.showWhile(UIHelper.instance().getDisplay(), new Runnable() {
					public void run() {
						try {
							loadChunk(index, null);
						} catch (Exception e) {
							UIHelper.instance().showErrorMsg("Error", SSUtil.getMessage(e));
							cancelled = true;
						}

					}
				});
			}

		} catch (Exception e) {
		} finally {
			loading = false;
			notify();
		}

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

	@Override
	public void updateElement(int index) {
		if (cancelled)
			return;
		try {
			view.getViewer().replace(getElement(index), index);
		} catch (Exception e) {
		}
	}

	public void setLazyLoading(boolean flag) {
		if (flag == lazyLoading)
			return;

		lazyLoading = flag;
		if (lazyLoading == false && rows.size() < sqlRowCount)
			getElement(Integer.MAX_VALUE);

	}

	protected void runLoadChunkJob(final int index) {

		loadChunkExecutionCompleted = false;
		loadChunkError = null;

		Shell shell = UIHelper.instance().getActiveShell();
		try {
			ProgressMonitorDialog pmd = new ProgressMonitorDialog(shell);
			pmd.run(true, true, new IRunnableWithProgress() {
				public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

					int work = sqlRowCount - rows.size();
					if (lazyLoading)
						work = index - rows.size();

					monitor.beginTask("Loading data...", work);

					Thread executionThread = new Thread(new Runnable() {
						public void run() {
							try {
								loadChunk(index, monitor);
							} catch (Exception e) {
								loadChunkError = SSUtil.getMessage(e);
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
						executionThread.interrupt();
					}

					monitor.done();
					loadChunkExecutionCompleted = true;

				}

			});
		} catch (InvocationTargetException e) {
			UIHelper.instance().showErrorMsg("Error", SSUtil.getMessage(e));
			cancelled = true;
		} catch (InterruptedException e) {
			cancelled = true;
			loadChunkExecutionCompleted = true;
		}

		while (loadChunkExecutionCompleted == false) {
			try {
				Thread.sleep(200);
			} catch (Exception e) {
			}
		}

		if (cancelled == false && loadChunkError != null) {
			UIHelper.instance().showErrorMsg("Error", loadChunkError);
			cancelled = true;
		}

	}

	protected void runSQLExecutionJob() {

		sqlExecutionCompleted = false;
		sqlExecutionError = null;

		IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
		try {
			progressService.busyCursorWhile(new IRunnableWithProgress() {
				public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

					monitor.beginTask("Running SQL in database...", IProgressMonitor.UNKNOWN);
					try {

						final Statement stmt = getConnection().createStatement();
						Thread executionThread = new Thread(new Runnable() {
							public void run() {
								try {
									result = stmt.executeQuery(queryDef.getSQL().trim());
									ResultSetMetaData rsmd = result.getMetaData();
									columnCount = rsmd.getColumnCount();
									sqlRowCount = SQLHelper.getSQLRowCount(queryDef.getSQL(), getConnection());
									columnProperties = SQLHelper.getColumnProperties(getConnection(), result, queryDef.getColumnHeading(), monitor);
									if (monitor.isCanceled())
										cancelled = true;
								} catch (Exception e) {
									sqlExecutionError = SSUtil.getMessage(e);
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

	protected void loadChunk(int index, IProgressMonitor monitor) throws Exception {
		int tempCount = 0;
		try {
			while (result.next()) {
				List<String> row = new ArrayList<String>();
				row.add(Integer.toString(rowCount++));
				for (int j = 1; j <= columnCount; j++) {
					row.add(result.getString(j));
				}
				rows.add(row);
				if (monitor != null)
					monitor.worked(1);
				if (cancelled || (rows.size() > index && lazyLoading))
					break;

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
		} catch (OutOfMemoryError e) {
			Runtime.getRuntime().gc();
			monitor.setCanceled(true);
			throw new Exception("Out of memory", e);
		}
		
		if (rows.size() >= sqlRowCount)
			closeConnection();

	}

	protected void runDisposeJob() {
		UIJob job = new UIJob("Dispose") {
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().hideView(view);
				return Status.OK_STATUS;
			}
		};
		job.schedule();

	}

	protected void monitorLazyLoadState() {
		ICommandService service = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
		service.addExecutionListener(lazyLoadCommandListener);
	}

	protected void finalize() throws Throwable {
		closeConnection();
		super.finalize();
	}
}