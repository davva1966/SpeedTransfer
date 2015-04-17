// {{CopyrightNotice}}

package com.ss.speedtransfer.util;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.swt.program.Program;

import com.ss.speedtransfer.model.QueryDefinition;


/**
 * The <code>UIHelper</code> class
 */
public class SSUtil {

	public static final String EXCEL_INSTALLED = "com.ss.speedtransfer.excel.installed";
	public static final String EXCEL_XLSX_INSTALLED = "com.ss.speedtransfer.excel.xlsx.installed";

	public static String trimNull(String string) {
		if (string == null)
			return "";
		return string;

	}

	public static String getMessage(Throwable e) {
		if (e.getMessage() != null)
			return e.getMessage();
		if (e instanceof InvocationTargetException) {
			String msg = getMessage(((InvocationTargetException) e).getTargetException());
			if (msg != null && msg.trim().length() > 0)
				return msg;
		}
		return e.toString();

	}

	public static boolean excelInstalled() {
		return System.getProperty(EXCEL_INSTALLED).equalsIgnoreCase("true");
	}

	public static boolean excelXLSXInstalled() {
		return System.getProperty(EXCEL_XLSX_INSTALLED).equalsIgnoreCase("true");
	}

	public static void determineExcelInstalled() {

		Program p = Program.findProgram(".xlsx");
		if (p != null) {
			System.setProperty(EXCEL_INSTALLED, "true");
			System.setProperty(EXCEL_XLSX_INSTALLED, "true");

		} else {
			p = Program.findProgram(".xls");
			if (p != null) {
				System.setProperty(EXCEL_INSTALLED, "true");
				System.setProperty(EXCEL_XLSX_INSTALLED, "false");

			}
		}
	}

	public static boolean validateSelectOnly(QueryDefinition queryDef) {
		if (LicenseManager.isSelectOnly() == false)
			return true;

		String sql = queryDef.getSQL().toUpperCase();

		// Split string omitting strings enclosed in "'" or """.
		String[] sqlParts = sql.split("(\"([^\\\"]|\\.)*\"|'([^\\']|\\.)*')");

		if (sqlParts.length == 0)
			return true;

		for (String part : sqlParts) {
			String illegalOper = SQLNonSelectOperations.getOperation(part);
			if (illegalOper != null) {
				UIHelper.instance().showErrorMsg("Illegal operation found", "Illegal operation " + illegalOper + " found. Only plain select statements are allowed. Query can not be executed.");
				return false;
			}
		}

		return true;

	}
}
