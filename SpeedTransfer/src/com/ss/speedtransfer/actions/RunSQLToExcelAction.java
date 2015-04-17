package com.ss.speedtransfer.actions;

import java.io.File;
import java.io.FileFilter;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.HandlerUtil;

import com.ss.speedtransfer.export.QueryToExcelExporter;
import com.ss.speedtransfer.model.QueryDefinition;
import com.ss.speedtransfer.ui.view.QueryExcelResultView;
import com.ss.speedtransfer.util.SSUtil;
import com.ss.speedtransfer.util.UIHelper;


public class RunSQLToExcelAction extends AbstractRunSQLAction {

	public RunSQLToExcelAction() {
		this(null);

	}

	public RunSQLToExcelAction(QueryDefinition queryDef) {
		super(queryDef);

	}

	public void run() {
		ICommandService commandService = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
		Command command = commandService.getCommand("com.ss.speedtransfer.runQueryCommand");

		try {
			HandlerUtil.updateRadioState(command, "excel");
		} catch (ExecutionException e) {
			UIHelper.instance().showErrorMsg("Error", "Unexpected error occurred. Error: " + SSUtil.getMessage(e));
		}

		super.run();

	}

	protected void showResult(String secondaryID) throws Exception {

		IViewReference viewRef = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findViewReference(QueryExcelResultView.ID, secondaryID);
		if (viewRef != null) {
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().hideView(viewRef);
		}

		String fileName = System.getProperty("java.io.tmpdir");
		if (fileName.endsWith(File.separator) == false)
			fileName = fileName + File.separator;
		fileName = fileName + File.separator + "SpeedTransferTemp" + File.separator;

		File f = new File(fileName);
		f.mkdirs();

		deleteOldFiles(f);

		String ext = "xls";
		if (SSUtil.excelXLSXInstalled())
			ext = "xlsx";

		String newFileName = fileName + queryDef.hashCode() + "." + ext;

		File f1 = new File(newFileName);
		int count = 1;
		while (f1.exists()) {
			newFileName = fileName + queryDef.hashCode() + "_" + count + "." + ext;
			f1 = new File(newFileName);
			count++;
		}

		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put(QueryToExcelExporter.EXPORT_TO_FILE, newFileName);
		properties.put(QueryToExcelExporter.WORK_SHEET, "sheet1");
		properties.put(QueryToExcelExporter.FORMAT, ext);
		properties.put(QueryToExcelExporter.REPLACE, true);
		properties.put(QueryToExcelExporter.CLEAR_SHEET, true);
		properties.put(QueryToExcelExporter.START_ROW_NUMBER, 0);
		properties.put(QueryToExcelExporter.START_ROW, QueryToExcelExporter.FIRST_ROW);
		properties.put(QueryToExcelExporter.LAUNCH, false);
		properties.put(QueryToExcelExporter.EXPORT_HEADINGS, true);
		properties.put(QueryDefinition.COLUMN_HEADINGS, queryDef.getColumnHeading());
		properties.put(QueryToExcelExporter.REMOVE_REMAINING_ROWS, true);

		QueryToExcelExporter exporter = new QueryToExcelExporter(queryDef);
		exporter.runJob(queryDef.getSQL(), properties);
		if (exporter.isCancelled())
			return;

		QueryExcelResultView viewPart = (QueryExcelResultView) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
				.showView(QueryExcelResultView.ID, secondaryID, IWorkbenchPage.VIEW_ACTIVATE);
		viewPart.setFileName(newFileName);
		viewPart.setName(queryDef.getName());
		viewPart.setTooltip(queryDef.getSQL());

	}

	protected void deleteOldFiles(File dir) {
		final long today = System.currentTimeMillis();
		try {
			if (dir != null && dir.exists()) {
				File[] files = dir.listFiles(new FileFilter() {
					public boolean accept(File file) {
						if (today - file.lastModified() > 3600 * 1000 * 2)
							return true;
						return false;
					}
				});

				for (File file : files) {
					try {
						if (file.exists())
							file.delete();
					} catch (Exception e) {
					}
				}
			}

		} catch (Exception e) {
		}
	}
}
