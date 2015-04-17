package com.ss.speedtransfer.export;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.w3c.dom.Element;

import com.ss.speedtransfer.SpeedTransferPlugin;
import com.ss.speedtransfer.model.QueryDefinition;
import com.ss.speedtransfer.util.SSUtil;
import com.ss.speedtransfer.util.ReplacementVariableTranslatorPrompt;
import com.ss.speedtransfer.util.SQLHelper;
import com.ss.speedtransfer.util.StringHelper;
import com.ss.speedtransfer.util.UIHelper;
import com.ss.speedtransfer.util.poi.POIUtil;


public class QueryToExcelExporter extends AbstractQueryExporter implements QueryExporter {

	public static final String REPLACE = "REPLACE";
	public static final String WORK_SHEET = "WORK_SHEET";
	public static final String FORMAT = "FORMAT";
	public static final String EXPORT_HEADINGS = "EXPORT_HEADINGS";
	public static final String CLEAR_SHEET = "CLEAR_SHEET";
	public static final String START_ROW = "START_ROW";
	public static final String FIRST_ROW = "FIRST_ROW";
	public static final String AFTER_LAST_ROW = "AFTER_LAST_ROW";
	public static final String ACTUAL_ROW = "ACTUAL_ROW";
	public static final String START_ROW_NUMBER = "START_ROW_NUMBER";
	public static final String REMOVE_REMAINING_ROWS = "REMOVE_REMAINING_ROWS";

	protected Workbook workbook = null;
	protected Sheet worksheet = null;

	protected List<String> cellAlignments = null;
	protected List<Integer> cellTypes = null;
	protected Map<Integer, CellStyle> cellStyles = null;
	protected List<Integer> sqlTypes = null;
	protected Map<String, CellStyle> cellStyleMap = new HashMap<String, CellStyle>();

	protected Button buttonXLS;
	protected Button buttonXLSX;
	protected Text fileEdit;
	protected Button launchFile;
	protected Button okButton;
	protected Button replaceFile;
	protected Combo worksheetCombo;
	protected Button firstRow;
	protected Button afterLastRow;
	protected Button rowNumber;
	protected Text rowNumberEntry;
	protected Button exportHeadings;
	protected Button removeRemainingRows;
	protected Button clearSheet;

	protected String savedFilePath = "";
	protected int lastExcelRow = 0;

	protected ModifyListener worksheetComboModifyListener = new ModifyListener() {
		public void modifyText(ModifyEvent e) {
			validatePage();
		}
	};

	public QueryToExcelExporter(QueryDefinition queryDef) {
		super(queryDef);
		setJobName("Export to Excel");
		setJobImage(UIHelper.instance().getImageDescriptor("excel.gif"));
		FILE_EXT = "*.xls";

	}

	public void export() {

		if (showComments()) {

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

			properties = openSettingsDialog(queryDef.getProperties(), queryDef.getSQL());
			if (cancel)
				return;

			super.export(queryDef.getSQL(), properties);
		}
	}

	public void export(String sql, Map<String, Object> properties, IProgressMonitor monitor) throws Exception {

		this.properties = properties;
		FileOutputStream fileOut = null;

		try {

			String file = null;
			if (properties != null)
				file = (String) properties.get(EXPORT_TO_FILE);

			if (file == null || file.trim().length() == 0)
				throw new Exception("No file specified");

			boolean launch = false;
			if (properties != null)
				launch = (Boolean) properties.get(LAUNCH);

			if (monitor.isCanceled())
				return;

			monitor.subTask("Running query");
			ResultSet rs = getConnection().createStatement().executeQuery(sql);
			monitor.worked(1);

			if (monitor.isCanceled())
				return;

			if (rs != null) {

				monitor.subTask("Initialising result");
				initExcel(file, sql);

				if (monitor.isCanceled())
					return;
				monitor.worked(1);

				monitor.subTask("Adding Header");
				doColumns(rs, monitor);
				if (monitor.isCanceled())
					return;
				monitor.worked(1);

				monitor.subTask("Adding detail lines");

				doData(rs, monitor);
				if (monitor.isCanceled())
					return;

				maybeRemoveRemainingRows();

			}

			// Adjust cell widths (Takes time so do only if fewer than 10000
			// rows)
			if (getTotalRows(sql) < 10000) {
				monitor.subTask("Adjusting columns sizes");
				for (int i = 0; i < cellAlignments.size(); i++) {
					worksheet.autoSizeColumn(i);
					if (cellStyles.get(sqlTypes.get(i)) != null) {
						int width = worksheet.getColumnWidth(i);
						worksheet.setColumnWidth(i, width + 1100);
					}
				}
			}

			if (monitor.isCanceled())
				return;
			monitor.subTask("Writing to file");
			fileOut = new FileOutputStream(file);
			try {
				workbook.write(fileOut);
			} catch (OutOfMemoryError e) {
				Runtime.getRuntime().gc();
				monitor.setCanceled(true);
				throw new Exception("Out of memory", e);
			}

			fileOut.close();
			fileOut = null;

			if (monitor.isCanceled())
				return;

			if (launch) {
				monitor.subTask("Launching file");
				Program p = Program.findProgram(".xls");//$NON-NLS-1$
				if (p == null)
					p = Program.findProgram(".xlsx");//$NON-NLS-1$

				if (p != null)
					p.execute(file);
				else
					throw new Exception("No program found to launch file");
			}

		} catch (Exception e) {
			errorMessage = SSUtil.getMessage(e);
			throw e;
		} finally {
			try {
				closeConnection();
			} catch (Exception e) {
			}
			try {
				if (fileOut != null)
					fileOut.close();
			} catch (Exception e) {
			}
			fileOut = null;
			cellAlignments = null;
			cellTypes = null;
			cellStyles = null;
			sqlTypes = null;
			cellStyleMap = null;
			worksheet = null;
			workbook = null;
			try {
				Runtime.getRuntime().gc();
			} catch (Exception e2) {
			}
		}

	}

	protected void doColumns(ResultSet rs, IProgressMonitor monitor) throws Exception {

		monitor.subTask("Retrieving column information from database...");

		boolean exportHeadings = (Boolean) properties.get(EXPORT_HEADINGS);

		Row row = null;
		if (exportHeadings) {
			row = worksheet.getRow(lastExcelRow);
			if (row == null)
				row = worksheet.createRow(lastExcelRow);
			lastExcelRow++;
		}

		cellAlignments = new ArrayList<String>();
		cellTypes = new ArrayList<Integer>();
		cellStyles = new HashMap<Integer, CellStyle>();
		sqlTypes = new ArrayList<Integer>();

		loadCellStyles();

		String headingType = (String) properties.get(QueryDefinition.COLUMN_HEADINGS);
		List<String[]> columnProperties = SQLHelper.getColumnProperties(getConnection(), rs, headingType);
		if (monitor.isCanceled())
			return;

		int idx = 0;
		for (String[] columnProp : columnProperties) {
			String text = columnProp[4];
			String alignment = columnProp[6];

			if (exportHeadings) {
				Cell cell = createCell(row, idx, alignment);
				cell.setCellValue(text);
			}

			cellAlignments.add(alignment);

			int sqlType = Integer.parseInt(columnProp[7]);
			sqlTypes.add(sqlType);
			cellTypes.add(getCellType(sqlType));

			idx++;
		}

	}

	protected void doData(ResultSet rs, IProgressMonitor monitor) throws Exception {

		Row row = null;
		int columnCount = rs.getMetaData().getColumnCount();
		int tempCount = 0;
		try {
			while (rs.next()) {
				row = worksheet.getRow(lastExcelRow);
				if (row == null)
					row = worksheet.createRow(lastExcelRow);
				lastExcelRow++;

				for (int i = 1; i <= columnCount; i++) {
					String alignment = cellAlignments.get(i - 1);
					int cellType = cellTypes.get(i - 1);
					int sqlType = sqlTypes.get(i - 1);

					Cell cell = createCell(row, i - 1, alignment);
					cell.setCellType(cellType);

					CellStyle cellStyle = cellStyles.get(sqlType);
					if (cellStyle != null && (Boolean) properties.get(CLEAR_SHEET))
						cell.setCellStyle(cellStyle);

					setCellValue(cell, rs, i, sqlType);

				}

				tempCount++;
				if (tempCount > 500) {
					if (memoryWatcher.runMemoryCheck() == false) {
						monitor.setCanceled(true);
						break;
					}
					tempCount = 0;
				}

				if (monitor.isCanceled())
					return;
				monitor.worked(1);

			}

		} catch (OutOfMemoryError e) {
			Runtime.getRuntime().gc();
			monitor.setCanceled(true);
			throw new Exception("Out of memory", e);
		}

	}

	protected Cell createCell(Row row, int column, String halign) {
		Cell cell = row.getCell(column);
		if (cell == null)
			cell = row.createCell(column);
		if (halign != null) {
			CellStyle cellStyle = getCellStyle(halign);
			if ((Boolean) properties.get(CLEAR_SHEET))
				cell.setCellStyle(cellStyle);
		}

		return cell;
	}

	protected CellStyle getCellStyle(String halign) {
		if (cellStyleMap.containsKey(halign) == false) {
			CellStyle cellStyle = workbook.createCellStyle();
			short align = CellStyle.ALIGN_LEFT;
			if (halign.equalsIgnoreCase("right"))
				align = CellStyle.ALIGN_RIGHT;
			if (halign.equalsIgnoreCase("center"))
				align = CellStyle.ALIGN_CENTER;
			cellStyle.setAlignment(align);
			cellStyleMap.put(halign, cellStyle);
		}
		return cellStyleMap.get(halign);

	}

	public String getTaskName(String arg) {
		return "Excel exporter" + " " + arg;

	}

	/**
	 * Open new edit dialog
	 */
	protected Map<String, Object> openSettingsDialog(Map<String, Object> settings, final String sql) {

		this.properties = settings;

		IDialogSettings tempDialogSettings = null;
		try {
			tempDialogSettings = SpeedTransferPlugin.getDialogSettingsFor(this.getClass().getName());
		} catch (Exception e) {
		}
		final IDialogSettings dialogSettings = tempDialogSettings;

		Shell shell = UIHelper.instance().getActiveShell();
		final Shell settingsShell = new Shell(shell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE);
		settingsShell.setText("Export to Excel");
		Point location = shell.getLocation();
		settingsShell.setBounds(location.x + 30, location.y + 150, 500, 520);
		settingsShell.setLayout(new FillLayout());

		toolkit = new FormToolkit(settingsShell.getDisplay());
		form = toolkit.createForm(settingsShell);

		try {
			form.setBackgroundImage(UIHelper.instance().getImageDescriptor("form_banner.gif").createImage());
		} catch (Exception e) {
		}

		form.setText("Export to Excel");
		toolkit.decorateFormHeading(form);

		GridLayout layout = new GridLayout(3, false);
		form.getBody().setLayout(layout);

		GridData gd = new GridData(SWT.FILL, GridData.VERTICAL_ALIGN_BEGINNING, true, false, 3, 1);

		Group fileGroup = new Group(form.getBody(), SWT.NONE);
		fileGroup.setText("File Options");
		fileGroup.setLayout(new GridLayout(2, false));
		fileGroup.setLayoutData(gd);
		toolkit.adapt(fileGroup);

		// File Section - Start

		// Export to file label
		Label label = toolkit.createLabel(fileGroup, "Select file to export to");
		gd = new GridData(SWT.LEFT, SWT.TOP, false, false, 2, 1);
		label.setLayoutData(gd);

		// Export to file entry
		fileEdit = toolkit.createText(fileGroup, "", SWT.SINGLE);
		gd = new GridData(SWT.FILL, SWT.TOP, true, false);
		fileEdit.setLayoutData(gd);
		fileEdit.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				validatePage();
			}
		});

		// File browse button
		Button browseButton = toolkit.createButton(fileGroup, "Browse...", SWT.PUSH);
		gd = new GridData(SWT.LEFT, SWT.TOP, false, false);
		gd.widthHint = 90;
		gd.heightHint = 22;
		browseButton.setLayoutData(gd);
		browseButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				String[] exts = new String[3];
				exts[0] = "*.xlsx;*.xls";
				exts[1] = "*.xls";
				exts[2] = "*.xlsx";
				String file = UIHelper.instance().selectFile(null, "Select file to export to", exts, SWT.SAVE);
				if (file == null)
					file = "";
				fileEdit.setText(file);
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		// Replace file check box
		replaceFile = toolkit.createButton(fileGroup, "Replace existing file", SWT.CHECK);
		replaceFile.setSelection(true);
		gd = new GridData(SWT.LEFT, SWT.TOP, false, false, 2, 1);
		gd.horizontalIndent = 5;
		gd.heightHint = 15;
		replaceFile.setLayoutData(gd);
		replaceFile.setToolTipText(StringHelper.wordWrapString(
				"Select if the existing file should be deleted before the export. If selected, a new file with the same name (using the specified format) will be created.", 60));
		replaceFile.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				validatePage();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		// Worksheet label
		label = toolkit.createLabel(fileGroup, "Select/Enter work sheet");
		gd = new GridData(SWT.LEFT, SWT.BOTTOM, false, false, 2, 1);
		label.setLayoutData(gd);

		// Worksheet combo
		worksheetCombo = new Combo(fileGroup, SWT.DROP_DOWN);
		gd = new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1);
		worksheetCombo.setLayoutData(gd);
		worksheetCombo.setToolTipText(StringHelper.wordWrapString("Select or enter the name of the work sheet to export to. If the sheet doesn't exist it will be created.", 60));
		worksheetCombo.addModifyListener(worksheetComboModifyListener);
		toolkit.adapt(worksheetCombo);

		// Clear worksheet
		clearSheet = toolkit.createButton(fileGroup, "Clear work sheet before export", SWT.CHECK);
		clearSheet.setText("Clear work sheet before export");
		clearSheet.setSelection(true);
		gd = new GridData(SWT.LEFT, SWT.TOP, false, false, 2, 1);
		gd.horizontalIndent = 5;
		gd.heightHint = 15;
		clearSheet.setLayoutData(gd);
		clearSheet
				.setToolTipText(StringHelper
						.wordWrapString(
								"If selected, the selected work sheet will be cleared of all data before new data is exported to it. If unchecked, existing data will remain unless it is overwritten by the exported data. This setting is only relevant if the selected work sheet already exist in the selected file.",
								60));

		clearSheet.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				validatePage();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		// Format group
		String formatGroupTT = StringHelper
				.wordWrapString(
						"Select the format of the excel file. \"xls\" files can hold up to a maximum of 65536 rows, \"xlsx\" files can hold up to a maximum of 1048576 row but might not be supported by all version of excel.",
						60);

		Group formatGroup = new Group(fileGroup, SWT.NONE);
		formatGroup.setText("File Format");
		formatGroup.setLayout(new GridLayout(1, false));
		formatGroup.setToolTipText(formatGroupTT);

		gd = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
		gd.verticalIndent = 5;
		formatGroup.setLayoutData(gd);
		toolkit.adapt(formatGroup);

		// XLS Button
		buttonXLS = toolkit.createButton(formatGroup, ".xls - Used by Excel versions prior to Excel 2007", SWT.RADIO);
		buttonXLS.setSelection(true);
		buttonXLS.setEnabled(false);
		gd = new GridData(SWT.LEFT, SWT.CENTER, true, false);
		buttonXLS.setLayoutData(gd);
		buttonXLS.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				if (buttonXLS.getSelection()) {
					String selectedFile = fileEdit.getText().trim();
					if (selectedFile.toLowerCase().endsWith("xlsx")) {
						selectedFile = selectedFile.substring(0, selectedFile.length() - 4) + "xls";
						fileEdit.setText(selectedFile);
					}
				}
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		buttonXLS.setToolTipText(formatGroupTT);

		// XLSX Button
		buttonXLSX = toolkit.createButton(formatGroup, ".xlsx - Used by Excel version from Excel 2007", SWT.RADIO);
		buttonXLSX.setSelection(false);
		buttonXLSX.setEnabled(false);
		gd = new GridData(SWT.LEFT, SWT.CENTER, true, false);
		buttonXLSX.setLayoutData(gd);
		buttonXLSX.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				if (buttonXLSX.getSelection()) {
					String selectedFile = fileEdit.getText().trim();
					if (selectedFile.toLowerCase().endsWith("xls")) {
						selectedFile = selectedFile.substring(0, selectedFile.length() - 3) + "xlsx";
						fileEdit.setText(selectedFile);
					}
				}
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		buttonXLSX.setToolTipText(formatGroupTT);

		// File Section - End

		gd = new GridData(SWT.FILL, GridData.VERTICAL_ALIGN_BEGINNING, true, false, 3, 1);

		Group exportGroup = new Group(form.getBody(), SWT.NONE);
		exportGroup.setText("Export Options");
		exportGroup.setLayout(new GridLayout(2, false));
		exportGroup.setLayoutData(gd);
		toolkit.adapt(exportGroup);

		// Export Section - Start

		// Export headings
		exportHeadings = toolkit.createButton(exportGroup, "Export column headings", SWT.CHECK);
		exportHeadings.setSelection(true);
		gd = new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1);
		gd.horizontalIndent = 5;
		gd.heightHint = 15;
		exportHeadings.setLayoutData(gd);
		exportHeadings.setToolTipText(StringHelper
				.wordWrapString("Select if column headings should be part of the export. If selected, column headings will be exported as the first row of data.", 60));

		// Start row group
		Group rowGroup = new Group(exportGroup, SWT.NONE);
		rowGroup.setText("Start start row for export");
		rowGroup.setLayout(new GridLayout(4, false));

		gd = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
		gd.verticalIndent = 5;
		rowGroup.setLayoutData(gd);
		toolkit.adapt(rowGroup);

		// First row
		firstRow = toolkit.createButton(rowGroup, "First row", SWT.RADIO);
		firstRow.setSelection(true);
		gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		gd.horizontalIndent = 5;
		gd.heightHint = 25;
		firstRow.setLayoutData(gd);
		firstRow.setToolTipText(StringHelper.wordWrapString("Start export on the first row. Existing data in affected rows will be replaced by the exported data.", 60));

		// After last row
		afterLastRow = toolkit.createButton(rowGroup, "After last current row", SWT.RADIO);
		afterLastRow.setSelection(false);
		gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		gd.horizontalIndent = 5;
		gd.heightHint = 25;
		afterLastRow.setLayoutData(gd);
		afterLastRow.setToolTipText(StringHelper.wordWrapString("Insert new rows after the last row currently in the work sheet.", 60));

		// Row number
		String rowNumberTT = StringHelper.wordWrapString(
				"Insert new rows starting at the specified row number in the work sheet. Existing data in affected rows will be replaced by the exported data.", 60);

		rowNumber = toolkit.createButton(rowGroup, "Row number", SWT.RADIO);
		rowNumber.setSelection(false);
		rowNumber.setSelection(false);
		gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		gd.horizontalIndent = 5;
		gd.heightHint = 25;
		rowNumber.setLayoutData(gd);
		rowNumber.setToolTipText(rowNumberTT);
		rowNumber.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				rowNumberEntry.setEnabled(rowNumber.getSelection());
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		// Row number entry
		rowNumberEntry = toolkit.createText(rowGroup, "", SWT.SINGLE);
		rowNumberEntry.setEnabled(false);
		gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
		rowNumberEntry.setLayoutData(gd);
		rowNumberEntry.setToolTipText(rowNumberTT);

		// Clear reamining rows
		removeRemainingRows = toolkit.createButton(rowGroup, "Remove remaining rows", SWT.CHECK);
		removeRemainingRows.setSelection(true);
		gd = new GridData(SWT.LEFT, SWT.CENTER, false, false, 4, 1);
		gd.horizontalIndent = 5;
		gd.heightHint = 15;
		removeRemainingRows.setLayoutData(gd);
		removeRemainingRows.setToolTipText(StringHelper.wordWrapString("Select if remaining rows in the spreadsheet (below the exported rows) should be removed.", 60));

		// Export Section - End

		toolkit.paintBordersFor(fileGroup);

		// Launch file check box
		launchFile = toolkit.createButton(form.getBody(), "Launch file after export", SWT.CHECK);
		launchFile.setSelection(true);
		gd = new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1);
		gd.horizontalIndent = 5;
		gd.heightHint = 25;
		launchFile.setLayoutData(gd);
		launchFile.setToolTipText(StringHelper.wordWrapString("launch excel on the exported file when export is completed.", 60));

		// OK button
		okButton = toolkit.createButton(form.getBody(), "OK", SWT.PUSH);
		okButton.setEnabled(false);
		gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		gd.widthHint = 100;
		okButton.setLayoutData(gd);
		okButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {

				int totalRows = -1;
				try {
					totalRows = getTotalRows(sql);
				} catch (Exception e2) {
				}

				try {
					WorkbookUtil.validateSheetName(worksheetCombo.getText());
				} catch (Exception e2) {
					UIHelper.instance().showErrorMsg(settingsShell, "Error", "Invalid work sheet name. Error: " + SSUtil.getMessage(e2));
					return;
				}

				try {
					if (rowNumber.getSelection() && rowNumberEntry.getText() != null && rowNumberEntry.getText().trim().length() > 0)
						Integer.parseInt(rowNumberEntry.getText().trim());
				} catch (Exception e2) {
					UIHelper.instance().showErrorMsg(settingsShell, "Error", "Invalid row number");
					return;
				}

				// Max rows error
				if (buttonXLS.getSelection() && totalRows > 65536) {
					String title = "Error";
					String message = "Maximum number of rows exceeded";
					String message2 = ".xls type Excel files supports a maximum of 65536 rows";
					UIHelper.instance().showErrorMsg(settingsShell, title, message + System.getProperty("line.separator") + message2);
					return;
				}
				if (buttonXLSX.getSelection() && totalRows > 1048576) {
					String title = "Error";
					String message = "Maximum number of rows exceeded";
					String message2 = ".xlsx type Excel files supports a maximum of 1048576 rows";
					UIHelper.instance().showErrorMsg(settingsShell, title, message + System.getProperty("line.separator") + message2);
					return;
				}

				// Large size warning
				String filePath = fileEdit.getText().trim();
				File file = new File(filePath);
				if (file.exists() && replaceFile.getSelection() == false) {
					if (totalRows >= 10000) {
						String title = "Warning";
						String message = "This query produces a large number of excel rows. Reusing an existing file will require a large amount of memory. Consider exporting to a non-existing file or selecting the \"Replace existing file\" function.";
						String message2 = "Don you still want to run the export with current settings?";
						int response = UIHelper.instance().showMessageWithOKCancel(title, message + StringHelper.getNewLine() + StringHelper.getNewLine() + message2);
						if (response == SWT.CANCEL)
							return;
					}

				}

				if (properties == null)
					properties = new HashMap<String, Object>();

				properties.put(EXPORT_TO_FILE, adjustFileName(fileEdit.getText()));
				properties.put(REPLACE, replaceFile.getSelection());
				properties.put(WORK_SHEET, worksheetCombo.getText());

				if (buttonXLS.getSelection())
					properties.put(FORMAT, "xls");
				if (buttonXLSX.getSelection())
					properties.put(FORMAT, "xlsx");

				properties.put(EXPORT_HEADINGS, exportHeadings.getSelection());

				if (replaceFile.getSelection())
					properties.put(CLEAR_SHEET, true);
				else
					properties.put(CLEAR_SHEET, clearSheet.getSelection());

				String startRow = FIRST_ROW;
				if (afterLastRow.getSelection()) {
					startRow = AFTER_LAST_ROW;
				} else if (rowNumber.getSelection()) {
					startRow = ACTUAL_ROW;
				}
				properties.put(START_ROW, startRow);

				String startRowStr = rowNumberEntry.getText().trim();
				if (startRowStr == null || startRowStr.trim().length() == 0)
					startRowStr = "0";
				int startRowInt = Integer.parseInt(startRowStr) - 1;
				if (startRowInt < 0)
					startRowInt = 0;
				properties.put(START_ROW_NUMBER, startRowInt);

				properties.put(REMOVE_REMAINING_ROWS, removeRemainingRows.getSelection());

				properties.put(LAUNCH, launchFile.getSelection());

				cancel = false;

				if (dialogSettings != null) {
					dialogSettings.put("file", fileEdit.getText());
					dialogSettings.put("replace", replaceFile.getSelection());
					dialogSettings.put("clearSheet", clearSheet.getSelection());
					dialogSettings.put("exportHeadings", exportHeadings.getSelection());
					dialogSettings.put("launch", launchFile.getSelection());
				}

				settingsShell.close();

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
			if (dialogSettings.get("file") != null)
				fileEdit.setText(dialogSettings.get("file"));
			replaceFile.setSelection(dialogSettings.getBoolean("replace"));
			exportHeadings.setSelection(dialogSettings.getBoolean("exportHeadings"));
			clearSheet.setSelection(dialogSettings.getBoolean("clearSheet"));
			launchFile.setSelection(dialogSettings.getBoolean("launch"));
		}

		if (fileEdit.getText().trim().length() == 0) {
			String path = System.getProperty("user.home");
			fileEdit.setText(path + File.separator + "NewExcelFile.xlsx");
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

		okButton.setEnabled(false);

		String filePath = fileEdit.getText().trim();

		if (filePath.length() > 0) {
			File f = new File(filePath);

			if (filePath.equalsIgnoreCase(savedFilePath) == false) {
				savedFilePath = filePath;
				if (filePath.toLowerCase().endsWith(".xls")) {
					buttonXLS.setSelection(true);
					buttonXLSX.setSelection(false);
				}
				if (filePath.toLowerCase().endsWith(".xlsx")) {
					buttonXLS.setSelection(false);
					buttonXLSX.setSelection(true);
				}
				if (f.exists()) {
					if (replaceFile.getSelection()) {
						buttonXLS.setEnabled(true);
						buttonXLSX.setEnabled(true);
					} else {
						buttonXLS.setEnabled(false);
						buttonXLSX.setEnabled(false);
					}
				} else {
					buttonXLS.setEnabled(true);
					buttonXLSX.setEnabled(true);
				}
				loadSheetCombo(f);
			}

			buttonXLS.setEnabled(f.exists() == false || replaceFile.getSelection());
			buttonXLSX.setEnabled(f.exists() == false || replaceFile.getSelection());

		} else {
			worksheetCombo.setEnabled(false);
		}

		removeRemainingRows.setEnabled(clearSheet.getSelection() == false);
		if (clearSheet.getSelection())
			removeRemainingRows.setSelection(false);

		if (filePath.length() > 0 && worksheetCombo.getText().trim().length() > 0)
			okButton.setEnabled(true);

		clearMessage(form, "File empty", fileEdit);
		if (fileEdit.getText().trim().length() == 0)
			issueMessage(form, "File empty", IMessageProvider.INFORMATION, "File must be specified", fileEdit);

		clearMessage(form, "Sheet empty", worksheetCombo);
		if (worksheetCombo.getText().trim().length() == 0)
			issueMessage(form, "Sheet empty", IMessageProvider.INFORMATION, "Work sheet must be specified", worksheetCombo);

		clearMessage(form, "Invalid file extension", fileEdit);
		String fileName = fileEdit.getText().trim().toLowerCase();
		if (fileName.endsWith("xlsx") == false && fileName.endsWith("xls") == false) {
			okButton.setEnabled(false);
			issueMessage(form, "Invalid file extension", IMessageProvider.ERROR, "Invalid file extension. Must be \"xlsx\" or \"xls\"", fileEdit);
		}

	}

	protected void initExcel(String fileName, String sql) throws Exception {

		String filePath = (String) properties.get(EXPORT_TO_FILE);
		String sheetName = (String) properties.get(WORK_SHEET);
		boolean replace = (Boolean) properties.get(REPLACE);
		boolean clearSheet = (Boolean) properties.get(CLEAR_SHEET);
		int startRow = (Integer) properties.get(START_ROW_NUMBER);
		String startRowType = (String) properties.get(START_ROW);

		if (replace)
			clearSheet = true;

		File file = new File(filePath);
		if (file.exists() && replace == false) {
			workbook = getWorkbook(file);
		} else {
			String format = (String) properties.get(FORMAT);
			if (format.equalsIgnoreCase("xlsx")) {
				if (getTotalRows(sql) > 5000)
					workbook = new SXSSFWorkbook();
				else
					workbook = new XSSFWorkbook();
			} else {
				workbook = new HSSFWorkbook();
			}
		}

		worksheet = workbook.getSheet(sheetName);
		if (worksheet == null)
			worksheet = workbook.createSheet(WorkbookUtil.createSafeSheetName(sheetName));

		if (clearSheet) {
			for (int i = 0; i <= worksheet.getLastRowNum(); i++) {
				Row row = worksheet.getRow(i);
				if (row != null)
					worksheet.removeRow(row);
			}
		}

		// else {
		// if (startRowType.equals(ACTUAL_ROW)) {
		// if (removeRemainingRows) {
		// for (int i = 0; i < worksheet.getLastRowNum(); i++) {
		// Row row = worksheet.getRow(i);
		// if (row != null && row.getRowNum() >= startRow)
		// worksheet.removeRow(row);
		// }
		// }
		// } else if (startRowType.equals(FIRST_ROW)) {
		// if (removeRemainingRows) {
		// for (int i = 0; i < worksheet.getLastRowNum(); i++) {
		// Row row = worksheet.getRow(i);
		// worksheet.removeRow(row);
		// }
		// }
		//
		// }
		// }

		if (startRowType.equals(FIRST_ROW)) {
			lastExcelRow = 0;
		} else if (startRowType.equals(AFTER_LAST_ROW)) {
			lastExcelRow = worksheet.getLastRowNum() + 1;
		} else if (startRowType.equals(ACTUAL_ROW)) {
			lastExcelRow = startRow;
		} else {
			throw new Exception("Start row in work sheet for export could not be determined");
		}

	}

	protected void loadSheetCombo(File file) {
		worksheetCombo.removeModifyListener(worksheetComboModifyListener);
		String text = worksheetCombo.getText();

		if (file.exists()) {
			try {
				// Workbook wb = getWorkbook(file);
				// String[] sheetNames = new String[wb.getNumberOfSheets()];
				// for (int k = 0; k < wb.getNumberOfSheets(); k++) {
				// sheetNames[k] = wb.getSheetName(k);
				// }
				String[] sheetNames = POIUtil.getAllSheetNames(file);
				worksheetCombo.setItems(sheetNames);
			} catch (Exception e) {
				UIHelper.instance().showErrorMsg("Error", "Error while loading work sheets. Error: " + SSUtil.getMessage(e));
			}

		} else {
			worksheetCombo.removeAll();
		}

		if (text != null && text.trim().length() > 0)
			worksheetCombo.setText(text);
		else
			worksheetCombo.select(0);
		worksheetCombo.addModifyListener(worksheetComboModifyListener);
	}

	protected Workbook getWorkbook(File file) throws Exception {
		Workbook wb = null;
		InputStream stream = new FileInputStream(file);
		try {

			if (file.getName().toLowerCase().endsWith("xlsx")) {
				try {
					wb = new XSSFWorkbook(stream);
				} catch (OutOfMemoryError e) {
					throw new Exception("File " + file.getName() + " is to large to load into memory");
				} catch (Throwable e) {
					throw new Exception("Unable to read file " + file.getName() + ". It may be corrupt.");
				}
			}

			if (file.getName().toLowerCase().endsWith("xls")) {
				try {
					wb = new HSSFWorkbook(stream);
				} catch (OutOfMemoryError e) {
					throw new Exception("File " + file.getName() + " is to large to load into memory");
				} catch (Throwable e) {
					throw new Exception("Unable to read file " + file.getName() + ". It may be corrupt.");
				}
			}

		} finally {
			if (stream != null)
				stream.close();
		}

		return wb;

	}

	protected void saveDefaults() {
		try {
			Element defaultsNode = (Element) queryDef.getExcelDefaultsNode(true);
			if (defaultsNode == null)
				return;

			defaultsNode.setAttribute(QueryDefinition.FILE_NAME, fileEdit.getText());
			defaultsNode.setAttribute(QueryDefinition.REPLACE_FILE, Boolean.toString(replaceFile.getSelection()));
			defaultsNode.setAttribute(QueryDefinition.WORKSHEET, worksheetCombo.getText());

			if (buttonXLS.getSelection())
				defaultsNode.setAttribute(QueryDefinition.FILE_FORMAT, QueryDefinition.XLS);
			if (buttonXLSX.getSelection())
				defaultsNode.setAttribute(QueryDefinition.FILE_FORMAT, QueryDefinition.XLSX);

			defaultsNode.setAttribute(QueryDefinition.EXPORT_HEADINGS, Boolean.toString(exportHeadings.getSelection()));
			defaultsNode.setAttribute(QueryDefinition.CLEAR_SHEET, Boolean.toString(clearSheet.getSelection()));

			if (firstRow.getSelection())
				defaultsNode.setAttribute(QueryDefinition.START_ROW_TYPE, QueryDefinition.FIRST);
			else if (afterLastRow.getSelection())
				defaultsNode.setAttribute(QueryDefinition.START_ROW_TYPE, QueryDefinition.LAST);
			else if (rowNumber.getSelection())
				defaultsNode.setAttribute(QueryDefinition.START_ROW_TYPE, QueryDefinition.ROW);

			defaultsNode.setAttribute(QueryDefinition.START_ROW_NUMBER, rowNumberEntry.getText());
			defaultsNode.setAttribute(QueryDefinition.REMOVE_REMAINING_ROWS, Boolean.toString(removeRemainingRows.getSelection()));
			defaultsNode.setAttribute(QueryDefinition.LAUNCH, Boolean.toString(launchFile.getSelection()));

			if (queryDef.getFile() != null)
				queryDef.getFile().setContents(queryDef.getInputStream(), true, false, null);

		} catch (Exception e) {
			UIHelper.instance().showErrorMsg("Error", "Error when saving defaults. Error: " + SSUtil.getMessage(e));
		}

		UIHelper.instance().showMessage("Information", "Defaults saved");

	}

	protected boolean applyDefaults() {
		Element defaultsNode = (Element) queryDef.getExcelDefaultsNode(false);
		if (defaultsNode == null)
			return false;

		fileEdit.setText(defaultsNode.getAttribute(QueryDefinition.FILE_NAME));
		replaceFile.setSelection(Boolean.parseBoolean(defaultsNode.getAttribute(QueryDefinition.REPLACE_FILE)));
		worksheetCombo.setText(defaultsNode.getAttribute(QueryDefinition.WORKSHEET));

		if (defaultsNode.getAttribute(QueryDefinition.FILE_FORMAT).equalsIgnoreCase(QueryDefinition.XLS)) {
			buttonXLS.setSelection(true);
			buttonXLSX.setSelection(false);
		} else if (defaultsNode.getAttribute(QueryDefinition.FILE_FORMAT).equalsIgnoreCase(QueryDefinition.XLSX)) {
			buttonXLS.setSelection(false);
			buttonXLSX.setSelection(true);
		}

		exportHeadings.setSelection(Boolean.parseBoolean(defaultsNode.getAttribute(QueryDefinition.EXPORT_HEADINGS)));
		clearSheet.setSelection(Boolean.parseBoolean(defaultsNode.getAttribute(QueryDefinition.CLEAR_SHEET)));

		if (defaultsNode.getAttribute(QueryDefinition.START_ROW_TYPE).equalsIgnoreCase(QueryDefinition.FIRST)) {
			firstRow.setSelection(true);
			afterLastRow.setSelection(false);
			rowNumber.setSelection(false);
		} else if (defaultsNode.getAttribute(QueryDefinition.START_ROW_TYPE).equalsIgnoreCase(QueryDefinition.LAST)) {
			firstRow.setSelection(false);
			afterLastRow.setSelection(true);
			rowNumber.setSelection(false);
		} else if (defaultsNode.getAttribute(QueryDefinition.START_ROW_TYPE).equalsIgnoreCase(QueryDefinition.ROW)) {
			firstRow.setSelection(false);
			afterLastRow.setSelection(false);
			rowNumber.setSelection(true);
		}

		rowNumberEntry.setText(defaultsNode.getAttribute(QueryDefinition.START_ROW_NUMBER));
		removeRemainingRows.setSelection(Boolean.parseBoolean(defaultsNode.getAttribute(QueryDefinition.REMOVE_REMAINING_ROWS)));
		launchFile.setSelection(Boolean.parseBoolean(defaultsNode.getAttribute(QueryDefinition.LAUNCH)));

		return true;

	}

	protected int getCellType(int sqlType) {
		switch (sqlType) {
		case Types.BIGINT:
		case Types.DATE:
		case Types.DECIMAL:
		case Types.DOUBLE:
		case Types.FLOAT:
		case Types.INTEGER:
		case Types.NUMERIC:
		case Types.SMALLINT:
		case Types.TIME:
		case Types.TIMESTAMP:
		case Types.TINYINT:
			return Cell.CELL_TYPE_NUMERIC;
		case Types.BOOLEAN:
			return Cell.CELL_TYPE_BOOLEAN;
		default:
			return Cell.CELL_TYPE_STRING;
		}

	}

	protected void setCellValue(Cell cell, ResultSet rs, int columnIndex, int sqlType) throws Exception {
		switch (sqlType) {
		case Types.BIGINT:
		case Types.DECIMAL:
		case Types.DOUBLE:
		case Types.FLOAT:
		case Types.INTEGER:
		case Types.NUMERIC:
		case Types.SMALLINT:
		case Types.TINYINT:
			cell.setCellValue(rs.getDouble(columnIndex));
			break;
		case Types.DATE:
			cell.setCellValue(rs.getDate(columnIndex));
			break;
		case Types.TIME:
			cell.setCellValue(rs.getTime(columnIndex));
			break;
		case Types.TIMESTAMP:
			cell.setCellValue(rs.getTimestamp(columnIndex));
			break;
		case Types.BOOLEAN:
			cell.setCellValue(rs.getBoolean(columnIndex));
			break;
		default:
			cell.setCellValue(rs.getString(columnIndex));
			break;
		}
	}

	protected void loadCellStyles() {

		// Date
		CellStyle cellStyle = workbook.createCellStyle();
		cellStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("m/d/yy"));
		cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
		cellStyles.put(Types.DATE, cellStyle);

		// Time
		cellStyle = workbook.createCellStyle();
		cellStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("h:mm:ss AM/PM"));
		cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
		cellStyles.put(Types.TIME, cellStyle);

		// Timestamp
		cellStyle = workbook.createCellStyle();
		cellStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("m/d/yy h:mm"));
		cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
		cellStyles.put(Types.TIMESTAMP, cellStyle);

	}

	protected void maybeRemoveRemainingRows() {

		boolean replace = (Boolean) properties.get(REPLACE);
		boolean clearSheet = (Boolean) properties.get(CLEAR_SHEET);
		boolean removeRemainingRows = (Boolean) properties.get(REMOVE_REMAINING_ROWS);

		if (replace || clearSheet || removeRemainingRows == false)
			return;

		for (int i = lastExcelRow; i <= worksheet.getLastRowNum(); i++) {
			Row row = worksheet.getRow(i);
			worksheet.removeRow(row);
		}
	}
}
