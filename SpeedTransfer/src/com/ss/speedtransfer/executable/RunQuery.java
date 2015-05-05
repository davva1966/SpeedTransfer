package com.ss.speedtransfer.executable;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;

import com.ss.speedtransfer.export.QueryExporter;
import com.ss.speedtransfer.export.QueryToCSVExporter;
import com.ss.speedtransfer.export.QueryToExcelExporter;
import com.ss.speedtransfer.export.QueryToPDFExporter;
import com.ss.speedtransfer.model.QueryDefinition;
import com.ss.speedtransfer.util.SSUtil;
import com.ss.speedtransfer.util.StringHelper;
import com.ss.speedtransfer.util.UIHelper;

public class RunQuery {

	protected String resourcePath;

	public RunQuery(String resourcePath) {
		super();
		this.resourcePath = resourcePath;
	}

	public static void main(String[] args) {

		File temp = new File(args[0]);

		// Delete old empty folders
		try {
			if (temp != null && temp.exists()) {
				File[] dirs = temp.getParentFile().listFiles(new FilenameFilter() {
					public boolean accept(File dir, String name) {
						return name.startsWith("7ZipSfx");
					}
				});

				for (File dir : dirs) {
					if (dir.isDirectory() && dir.listFiles().length == 0)
						dir.delete();
				}
			}

		} catch (Exception e) {
		}

		try {
			String path = temp.getCanonicalPath() + File.separator + "launcher" + File.separator;

			Properties props = new Properties();
			props.load(new FileInputStream(path + "p.p"));

			String dateString = (String) props.get("expdate");
			dateString = StringHelper.decodePassword(dateString);

			if (dateString.trim().equalsIgnoreCase("notused") == false) {
				String expMessage = (String) props.get("expmessage");

				Date expDate = DateFormat.getDateInstance(DateFormat.SHORT, Locale.US).parse(dateString);
				Date today = new Date(System.currentTimeMillis());

				if (today.compareTo(expDate) > 0) {
					String message = "Program has expired. Expiry date was: " + DateFormat.getInstance().format(expDate);
					if (expMessage != null && expMessage.trim().length() > 0)
						message = message + StringHelper.getNewLine() + StringHelper.getNewLine() + expMessage;
					UIHelper.instance().showMessage("Information", message);
					return;
				}

			}

			RunQuery runner = new RunQuery(path);
			runner.run();

		} catch (Throwable e) {
			UIHelper.instance().showErrorMsg("Error", "Error during execution. Error: " + SSUtil.getMessage(e));
		}

	}

	public void run() throws Throwable {
		QueryDefinition qd = new QueryDefinition(resourcePath + "qd.xml");
		qd.setDBConnectionFile(resourcePath + "dbcon.xml");
		QueryExporter exporter = null;
		String runOption = qd.getDefaultRunOption();
		if (runOption.equals(QueryDefinition.EXPORT_TO_CSV))
			exporter = new QueryToCSVExporter(qd);
		else if (runOption.equals(QueryDefinition.EXPORT_TO_EXCEL))
			exporter = new QueryToExcelExporter(qd);
		else if (runOption.equals(QueryDefinition.EXPORT_TO_PDF))
			exporter = new QueryToPDFExporter(qd);

		exporter.export();

	}

	public static void log1(String entry) {
		BufferedWriter writer = null;
		try {
			long time = System.currentTimeMillis();
			FileWriter fileWriter = new FileWriter("D://executable_log//log.txt", true);
			writer = new BufferedWriter(fileWriter);
			writer.write(time + ": " + entry);
			writer.newLine();

		} catch (Exception e) {
		} finally {
			try {
				if (writer != null)
					writer.close();
			} catch (Exception e2) {
			}
		}
	}

}
