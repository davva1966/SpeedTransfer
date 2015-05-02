package com.ss.speedtransfer.export;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.resource.ImageDescriptor;
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

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.ExceptionConverter;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;
import com.ss.speedtransfer.SpeedTransferPlugin;
import com.ss.speedtransfer.model.QueryDefinition;
import com.ss.speedtransfer.util.ImageConverter;
import com.ss.speedtransfer.util.ReplacementVariableTranslatorPrompt;
import com.ss.speedtransfer.util.SQLHelper;
import com.ss.speedtransfer.util.SSUtil;
import com.ss.speedtransfer.util.UIHelper;


public class QueryToPDFExporter extends AbstractQueryExporter implements QueryExporter {

	/** The page type property */
	public static final String PAGE_TYPE = "PAGE_TYPE";//$NON-NLS-1$

	/** The page orientation property */
	public static final String ORIENTATION = "ORIENTATION";//$NON-NLS-1$

	/** The print sql property */
	public static final String PRINT_SQL = "PRINT_SQL";//$NON-NLS-1$

	/**
	 * Lanscape orientation
	 */
	public static final int Landscape = 1;

	/**
	 * Portrait orientation
	 */
	public static final int Portrait = 2;

	/** The sql string */
	protected String sql = null;

	/** Current PDF document */
	protected Document document = null;

	/** Number of columns in the current result set */
	protected int numberOfColumns = 0;

	/** Image to print on document footer */
	protected Image footerImage = null;

	/** Heading background color */
	protected BaseColor headingBackround = new BaseColor(200, 220, 240);

	/** Column text alignments */
	protected int[] alignments = null;

	/** print sql */
	protected boolean printSQL = false;

	// Font used when printing the SQL string
	protected Font sqlFont = FontFactory.getFont(FontFactory.HELVETICA, 6, Font.BOLD);

	protected Text fileEdit;
	protected Button launchFile;
	protected Button printSQLButton;
	protected Combo pagesize;
	protected Button landscape;
	protected Button portrait;
	protected Button okButton;

	/**
	 * Initializes a newly created <code>QueryToExcelExporter</code>
	 */
	public QueryToPDFExporter(QueryDefinition queryDef) {
		super(queryDef);
		setJobName("Query to PDF Exporter");
		setJobImage(UIHelper.instance().getImageDescriptor("pdf.gif"));
		FILE_EXT = "*.pdf";
	}

	public void export() {
		if (showComments()) {

			properties = openSettingsDialog(queryDef.getProperties());
			if (cancel)
				return;

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

			super.export(queryDef.getSQL(), properties);
		}
	}

	public void export(String sql, Map<String, Object> properties, IProgressMonitor monitor) throws Exception {

		this.sql = sql;
		this.properties = properties;

		FileOutputStream fileout = null;
		BufferedOutputStream out = null;

		try {
			String file = null;
			if (properties != null)
				file = (String) properties.get(EXPORT_TO_FILE);
			if (file == null || file.trim().length() == 0)
				throw new Exception("No file specified");

			String pageType = "A4";
			if (properties != null)
				pageType = (String) properties.get(PAGE_TYPE);
			if (pageType == null || pageType.trim().length() == 0)
				pageType = "A4";

			int type = Landscape;
			if (properties != null)
				type = (Integer) properties.get(ORIENTATION);

			printSQL = false;
			if (properties != null) {
				String printSQLStr = (String) properties.get(PRINT_SQL);
				if (printSQLStr != null && printSQLStr.trim().length() > 0) {
					if (printSQLStr.trim().equalsIgnoreCase("true"))
						printSQL = true;
				}
			}

			boolean launch = false;
			if (properties != null) {
				String launchStr = (String) properties.get(LAUNCH);
				if (launchStr != null && launchStr.trim().length() > 0) {
					if (launchStr.trim().equalsIgnoreCase("true"))
						launch = true;
				}
			}

			if (monitor.isCanceled())
				return;

			monitor.subTask("Running query");
			ResultSet rs = getConnection().createStatement().executeQuery(sql);
			monitor.worked(1);

			if (monitor.isCanceled())
				return;

			if (rs != null) {

				monitor.subTask("Initializing result");
				numberOfColumns = rs.getMetaData().getColumnCount();

				fileout = new FileOutputStream(file);
				out = new BufferedOutputStream(fileout);

				initializeDocument(out, pageType, type);

				// Create the pdf table
				PdfPTable table = createPdfTable();
				monitor.worked(1);

				// Add table headers
				monitor.subTask("Adding header");
				doColumns(rs, table, monitor);
				if (monitor.isCanceled())
					return;
				monitor.worked(1);

				// Add detail lines
				monitor.subTask("Adding detail lines");
				doData(rs, table, monitor);
				if (monitor.isCanceled())
					return;

				document.close();
				document = null;
				out.close();
				out = null;

				if (launch) {
					monitor.subTask("Launching file");
					Program p = Program.findProgram(".pdf");
					if (p != null)
						p.execute(file);
					else
						throw new Exception("No program found to launch file");
				}
			}

		} catch (Exception e) {
			errorMessage = SSUtil.getMessage(e);
			throw e;
		} finally {
			closeConnection();
			try {
				if (document != null)
					document.close();
			} catch (Exception e) {
			}
			try {
				if (out != null)
					out.close();
			} catch (Exception e) {
			}
		}

	}

	protected void doColumns(ResultSet rs, PdfPTable table, IProgressMonitor monitor) throws Exception {

		monitor.subTask("Retrieving column information from database...");

		// Set table properties
		table.getDefaultCell().setPadding(3);
		table.setWidthPercentage(100);
		table.getDefaultCell().setBorderWidth(2);

		String headingType = (String) properties.get(QueryDefinition.COLUMN_HEADINGS);
		List<String[]> columnProperties = SQLHelper.getColumnProperties(getConnection(), rs, headingType);
		if (monitor.isCanceled())
			return;

		int idx = 1;
		alignments = new int[numberOfColumns];

		for (String[] columnProp : columnProperties) {

			String align = columnProp[6];

			// Set column alignment
			if (align.equals("center"))
				alignments[idx - 1] = Element.ALIGN_CENTER;
			else if (align.equals("right"))
				alignments[idx - 1] = Element.ALIGN_RIGHT;
			else
				alignments[idx - 1] = Element.ALIGN_LEFT;

			PdfPCell headerCell = new PdfPCell(new Paragraph(columnProp[4], getHeaderFont()));
			if (idx == 1) {
				headerCell.setBorder(Rectangle.LEFT | Rectangle.TOP | Rectangle.BOTTOM);
			} else if (idx == numberOfColumns) {
				headerCell.setBorder(Rectangle.RIGHT | Rectangle.TOP | Rectangle.BOTTOM);
			} else {
				headerCell.setBorder(Rectangle.TOP | Rectangle.BOTTOM);
			}
			headerCell.setBackgroundColor(headingBackround);
			headerCell.setHorizontalAlignment(alignments[idx - 1]);
			if (alignments[idx - 1] == Element.ALIGN_RIGHT)
				headerCell.setRightIndent(10f);
			table.addCell(headerCell);

			idx++;
		}

		table.setHeaderRows(1);
		table.getDefaultCell().setBorderWidth(1);

	}

	protected void doData(ResultSet rs, PdfPTable table, IProgressMonitor monitor) throws Exception {

		int counter = 0;
		int tempCount = 0;

		try {
			while (rs.next()) {
				for (int i = 1; i <= numberOfColumns; i++) {

					PdfPCell cell = new PdfPCell(new Paragraph(rs.getString(i), getDetailsFont()));

					cell.setBorder(0);
					cell.setHorizontalAlignment(alignments[i - 1]);
					if (alignments[i - 1] == Element.ALIGN_RIGHT)
						cell.setRightIndent(10f);

					// Alternate color in data table
					if (counter % 2 == 1)
						cell.setGrayFill(0.95f);
					else
						cell.setGrayFill(1f);

					table.addCell(cell);

				}

				tempCount++;
				if (tempCount > 2000) {
					if (memoryWatcher.runMemoryCheck() == false) {
						monitor.setCanceled(true);
						break;
					}
					tempCount = 0;
				}

				if (monitor.isCanceled())
					return;
				monitor.worked(1);

				counter++;
			}

			document.add(table);
			table.deleteBodyRows();
			table.setSkipFirstHeader(true);

		} catch (OutOfMemoryError e) {
			Runtime.getRuntime().gc();
			monitor.setCanceled(true);
			throw new Exception("Out of memory", e);
		}

	}

	/**
	 * Create and initialize the pdf document
	 * 
	 * @param out
	 *            the output stream to direct the pdf stream to
	 * @param pageType
	 *            the page type, A4 or A3 for instance
	 * @param type
	 *            the page orientation. Must be one of <QueryToPDFExporter.Landscape> or <QueryToPDFExporter.Portrait>
	 * @throws Exception
	 *             if an error occurs
	 */
	protected void initializeDocument(OutputStream out, String pageType, int type) throws DocumentException {

		Rectangle pageRect = PageSize.A4;
		if (pageType.equals("A0"))
			pageRect = PageSize.A0;
		else if (pageType.equals("A1"))
			pageRect = PageSize.A1;
		else if (pageType.equals("A2"))
			pageRect = PageSize.A2;
		else if (pageType.equals("A3"))
			pageRect = PageSize.A3;
		else if (pageType.equals("A4"))
			pageRect = PageSize.A4;
		else if (pageType.equals("A5"))
			pageRect = PageSize.A5;
		else if (pageType.equals("A6"))
			pageRect = PageSize.A6;
		else if (pageType.equals("A7"))
			pageRect = PageSize.A7;
		else if (pageType.equals("A8"))
			pageRect = PageSize.A8;
		else if (pageType.equals("A9"))
			pageRect = PageSize.A9;
		else if (pageType.equals("A10"))
			pageRect = PageSize.A10;
		else if (pageType.equals("B0"))
			pageRect = PageSize.B0;
		else if (pageType.equals("B1"))
			pageRect = PageSize.B1;
		else if (pageType.equals("B2"))
			pageRect = PageSize.B2;
		else if (pageType.equals("B3"))
			pageRect = PageSize.B3;
		else if (pageType.equals("B4"))
			pageRect = PageSize.B4;
		else if (pageType.equals("B5"))
			pageRect = PageSize.B5;
		else if (pageType.equals("HALFLETTER"))
			pageRect = PageSize.HALFLETTER;
		else if (pageType.equals("LEDGER"))
			pageRect = PageSize.LEDGER;
		else if (pageType.equals("LEGAL"))
			pageRect = PageSize.LEGAL;
		else if (pageType.equals("LETTER"))
			pageRect = PageSize.LETTER;
		else if (pageType.equals("NOTE"))
			pageRect = PageSize.NOTE;

		// Create the PDf document
		if (type == Landscape)
			pageRect = pageRect.rotate();

		// Calculate bottom margin
		int bMargin = calculateBottomMargin(pageRect);

		document = new Document(pageRect, 10, 10, 5, bMargin);
		PdfWriter writer = PdfWriter.getInstance(document, out);

		// Sets the PageEvent for the document.
		writer.setPageEvent(new PdfPageEventHelper() {

			public void onEndPage(PdfWriter writer, Document document) {
				doEndPage(writer, document);

			}
		});

		// Add Meta data
		document.addAuthor("David Gustavsson");
		document.addCreator("David Gustavsson");
		document.addTitle("Query: " + sql.toUpperCase());
		document.addSubject("Query Result");

		document.open();
	}

	/**
	 * Create and initialize the pdf table to hold the query result
	 * 
	 * @return The created pdf table
	 * @throws Exception
	 *             if an sql error occurs
	 */
	protected PdfPTable createPdfTable() throws Exception {
		return new PdfPTable(numberOfColumns);

	}

	/**
	 * Get the font use when writing the header cells
	 * 
	 * @return The font to use for header cells
	 */
	protected Font getHeaderFont() {
		return FontFactory.getFont(FontFactory.HELVETICA, 8, Font.BOLD);
	}

	/**
	 * Get the font use when writing the detail cells
	 * 
	 * @return The font to use for detail cells
	 */
	protected Font getDetailsFont() {
		return FontFactory.getFont(FontFactory.HELVETICA, 6);
	}

	public String getTaskName(String arg) {
		return "Query to PDF exporter " + arg;

	}

	/**
	 * Called when a page is finished, just before being written to the document.
	 * 
	 * @param writer
	 * @param document
	 */
	public void doEndPage(PdfWriter writer, Document document) {
		try {
			// Create footer with three cells
			Rectangle page = document.getPageSize();
			float colWidths[] = new float[3];
			colWidths[0] = 15;
			colWidths[1] = 70;
			colWidths[2] = 15;
			PdfPTable foot = new PdfPTable(colWidths);

			// Set font for footer
			Font footerFont = FontFactory.getFont(FontFactory.HELVETICA, 6, Font.BOLD);

			// Left cell
			PdfPCell footerInfoCell = new PdfPCell(new Paragraph("       " + "Query Export", footerFont));
			footerInfoCell.setBorder(Rectangle.TOP);
			foot.addCell(footerInfoCell);

			// Middle cell
			Paragraph querySQLParagraph;
			if (printSQL)
				querySQLParagraph = new Paragraph(sql, sqlFont);
			else
				querySQLParagraph = new Paragraph("", sqlFont);
			querySQLParagraph.setAlignment(Paragraph.ALIGN_LEFT);
			footerInfoCell = new PdfPCell();
			footerInfoCell.addElement(querySQLParagraph);
			footerInfoCell.setBorder(Rectangle.TOP);
			foot.addCell(footerInfoCell);

			// Right cell
			Paragraph pageNumberParagraph = new Paragraph("Page: " + writer.getPageNumber(), footerFont);
			pageNumberParagraph.setAlignment(Paragraph.ALIGN_RIGHT);
			footerInfoCell = new PdfPCell();
			footerInfoCell.addElement(pageNumberParagraph);
			footerInfoCell.setBorder(Rectangle.TOP);
			foot.addCell(footerInfoCell);

			// Add cells to table and document
			foot.setTotalWidth(page.getWidth() - document.leftMargin() - document.rightMargin());
			foot.writeSelectedRows(0, -1, document.leftMargin(), foot.getTotalHeight(), writer.getDirectContent());

			// Add logo to left cell
			PdfContentByte cb = writer.getDirectContent();
			if (footerImage == null) {
				ImageDescriptor imageDesc = UIHelper.instance().getImageDescriptor("pdf.gif");
				if (imageDesc != null) {
					footerImage = Image.getInstance((java.awt.Image) ImageConverter.convertToAWT(imageDesc.createImage().getImageData()), null);
					footerImage.scaleToFit(11, 11);
					float imagePlacement = foot.getTotalHeight() - 12;
					footerImage.setAbsolutePosition(10, imagePlacement);
				}
			}
			if (footerImage != null)
				cb.addImage(footerImage);

		} catch (Exception e) {
			throw new ExceptionConverter(e);
		}
	}

	protected int calculateBottomMargin(Rectangle pageRect) {

		if (printSQL == false)
			return 30;

		float availSize = pageRect.getWidth() * 0.7f;
		BaseFont sqlBaseFont = sqlFont.getBaseFont();
		double totalLines = 0;
		String[] queryLines = sql.split("[\\n]");
		for (int i = 0; i < queryLines.length; i++) {
			totalLines++;
			float stringSize = sqlBaseFont.getWidthPoint(queryLines[i], sqlFont.getSize());
			double lines = Math.ceil(stringSize / availSize);
			if (lines > 1)
				totalLines = totalLines + (lines - 1);
		}

		if (totalLines < 2)
			totalLines = 2;

		return (int) totalLines * 10;

	}

	/**
	 * Open new edit dialog
	 */
	protected Map<String, Object> openSettingsDialog(Map<String, Object> settings) {

		this.properties = settings;

		IDialogSettings tempDialogSettings = null;
		try {
			tempDialogSettings = SpeedTransferPlugin.getDialogSettingsFor(this.getClass().getName());
		} catch (Exception e) {
		}
		final IDialogSettings dialogSettings = tempDialogSettings;

		Shell shell = UIHelper.instance().getActiveShell();
		final Shell settingsShell = new Shell(shell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE);
		settingsShell.setText("Export to PDF");
		Point location = shell.getLocation();
		settingsShell.setBounds(location.x + 30, location.y + 150, 500, 300);
		settingsShell.setLayout(new FillLayout());

		toolkit = new FormToolkit(settingsShell.getDisplay());
		form = toolkit.createForm(settingsShell);

		try {
			form.setBackgroundImage(UIHelper.instance().getImageDescriptor("form_banner.gif").createImage());
		} catch (Exception e) {
		}

		form.setText("Export to PDF");
		toolkit.decorateFormHeading(form);

		GridLayout layout = new GridLayout(3, false);
		form.getBody().setLayout(layout);

		GridData gd = new GridData(SWT.FILL, GridData.VERTICAL_ALIGN_BEGINNING, true, false, 3, 1);

		Group group = new Group(form.getBody(), SWT.NONE);
		group.setLayout(new GridLayout(2, false));
		group.setLayoutData(gd);
		toolkit.adapt(group);

		// Export to file label
		Label label = toolkit.createLabel(group, "Select file to export to", SWT.NONE);
		gd = new GridData(SWT.LEFT, SWT.TOP, false, false, 2, 1);
		label.setLayoutData(gd);

		// Export to file entry
		fileEdit = toolkit.createText(group, "", SWT.SINGLE);
		gd = new GridData(SWT.FILL, SWT.TOP, true, false);
		fileEdit.setLayoutData(gd);
		fileEdit.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				validatePage();
			}
		});

		// File browse button
		Button browseButton = toolkit.createButton(group, "Browse...", SWT.PUSH);
		gd = new GridData(SWT.LEFT, SWT.TOP, false, false);
		gd.widthHint = 90;
		gd.heightHint = 22;
		browseButton.setLayoutData(gd);
		browseButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				String file = UIHelper.instance().selectFile(null, "Select file to export to", FILE_EXT, SWT.SAVE);
				if (file == null)
					file = "";
				fileEdit.setText(file);
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		// Launch file check box
		launchFile = toolkit.createButton(group, "Launch file after export", SWT.CHECK);
		launchFile.setSelection(false);
		gd = new GridData(SWT.LEFT, SWT.TOP, false, false, 2, 1);
		gd.horizontalIndent = 5;
		gd.heightHint = 25;
		launchFile.setLayoutData(gd);

		// Print SQL check box
		printSQLButton = toolkit.createButton(group, "Print sql string in PDF", SWT.CHECK);
		printSQLButton.setSelection(false);
		gd = new GridData(SWT.LEFT, SWT.TOP, false, false, 2, 1);
		gd.horizontalIndent = 5;
		gd.heightHint = 18;
		printSQLButton.setLayoutData(gd);

		Group radioGroup = new Group(form.getBody(), SWT.NONE);
		radioGroup.setText("Page Properties");
		radioGroup.setLayout(new GridLayout(3, false));
		toolkit.adapt(radioGroup);

		gd = new GridData(SWT.FILL, SWT.TOP, true, false, 3, 1);
		gd.verticalIndent = 5;
		radioGroup.setLayoutData(gd);

		pagesize = new Combo(radioGroup, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY);
		gd = new GridData(SWT.FILL, SWT.TOP, true, false);
		pagesize.setLayoutData(gd);
		toolkit.adapt(pagesize);

		pagesize.add("A0");
		pagesize.add("A1");
		pagesize.add("A2");
		pagesize.add("A3");
		pagesize.add("A4");
		pagesize.add("A5");
		pagesize.add("A6");
		pagesize.add("A7");
		pagesize.add("A8");
		pagesize.add("A9");
		pagesize.add("A10");
		pagesize.add("B0");
		pagesize.add("B1");
		pagesize.add("B2");
		pagesize.add("B3");
		pagesize.add("B4");
		pagesize.add("B5");
		pagesize.add("HALFLETTER");
		pagesize.add("LEDGER");
		pagesize.add("LEGAL");
		pagesize.add("LETTER");
		pagesize.add("NOTE");
		pagesize.select(4);

		landscape = toolkit.createButton(radioGroup, "Landscape", SWT.RADIO);
		landscape.setSelection(true);
		gd = new GridData(SWT.LEFT, SWT.TOP, true, false);
		gd.horizontalIndent = 25;
		landscape.setLayoutData(gd);

		portrait = toolkit.createButton(radioGroup, "Portrait", SWT.RADIO);
		portrait.setSelection(false);
		gd = new GridData(SWT.LEFT, SWT.TOP, true, false);
		portrait.setLayoutData(gd);

		// OK button
		okButton = toolkit.createButton(form.getBody(), "OK", SWT.PUSH);
		okButton.setText("OK");
		okButton.setEnabled(false);
		gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		gd.widthHint = 100;
		okButton.setLayoutData(gd);
		okButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				if (properties == null)
					properties = new HashMap<String, Object>();
				properties.put(EXPORT_TO_FILE, adjustFileName(fileEdit.getText()));
				if (landscape.getSelection())
					properties.put(ORIENTATION, Landscape);
				if (portrait.getSelection())
					properties.put(ORIENTATION, Portrait);
				properties.put(PAGE_TYPE, pagesize.getText());
				if (printSQLButton.getSelection())
					properties.put(PRINT_SQL, "true");
				else
					properties.put(PRINT_SQL, "false");
				if (launchFile.getSelection())
					properties.put(LAUNCH, "true");
				else
					properties.put(LAUNCH, "false");
				cancel = false;

				if (dialogSettings != null) {
					dialogSettings.put("file", fileEdit.getText());
					dialogSettings.put("launch", launchFile.getSelection());
					dialogSettings.put("printSQL", printSQLButton.getSelection());
					dialogSettings.put("pageSize", pagesize.getText());
					dialogSettings.put("landscape", landscape.getSelection());
					dialogSettings.put("portrait", portrait.getSelection());
				}

				settingsShell.close();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		// Cancel button
		Button cancelButton = toolkit.createButton(form.getBody(), "Cancel", SWT.PUSH);
		cancelButton.setText("Cancel");
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
			setDefaultButton.setText("Save as default");
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
			launchFile.setSelection(dialogSettings.getBoolean("launch"));
			printSQLButton.setSelection(dialogSettings.getBoolean("printSQL"));
			if (dialogSettings.get("pageSize") != null && dialogSettings.get("pageSize").trim().length() > 0)
				pagesize.setText(dialogSettings.get("pageSize"));
			landscape.setSelection(dialogSettings.getBoolean("landscape"));
			portrait.setSelection(dialogSettings.getBoolean("portrait"));
			if (landscape.getSelection() == false && portrait.getSelection() == false)
				landscape.setSelection(true);
		}

		if (fileEdit.getText().trim().length() == 0) {
			String path = System.getProperty("user.home");
			fileEdit.setText(path + File.separator + "NewPDFFile.pdf");
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

		okButton.setEnabled(true);

		if (fileEdit.getText().trim().length() == 0)
			okButton.setEnabled(false);

		clearMessage(form, "File empty", fileEdit);
		if (fileEdit.getText().trim().length() == 0)
			issueMessage(form, "File empty", IMessageProvider.INFORMATION, "File must be specified", fileEdit);

	}

	protected void saveDefaults() {
		try {
			org.w3c.dom.Element defaultsNode = (org.w3c.dom.Element) queryDef.getPDFDefaultsNode(true);
			if (defaultsNode == null)
				return;

			defaultsNode.setAttribute(QueryDefinition.FILE_NAME, fileEdit.getText());
			defaultsNode.setAttribute(QueryDefinition.LAUNCH, Boolean.toString(launchFile.getSelection()));
			defaultsNode.setAttribute(QueryDefinition.PRINT_SQL, Boolean.toString(printSQLButton.getSelection()));
			defaultsNode.setAttribute(QueryDefinition.PAGE_SIZE, pagesize.getText());

			if (landscape.getSelection())
				defaultsNode.setAttribute(QueryDefinition.ORIENTATION, QueryDefinition.LANDSCAPE);
			if (portrait.getSelection())
				defaultsNode.setAttribute(QueryDefinition.ORIENTATION, QueryDefinition.PORTRAIT);

			if (queryDef.getFile() != null)
				queryDef.getFile().setContents(queryDef.getInputStream(), true, false, null);

		} catch (Exception e) {
			UIHelper.instance().showErrorMsg("Error", "Error when saving defaults. Error: " + SSUtil.getMessage(e));
		}

		UIHelper.instance().showMessage("Information", "Defaults saved");

	}

	protected boolean applyDefaults() {
		org.w3c.dom.Element defaultsNode = (org.w3c.dom.Element) queryDef.getPDFDefaultsNode(false);
		if (defaultsNode == null)
			return false;

		fileEdit.setText(defaultsNode.getAttribute(QueryDefinition.FILE_NAME));
		launchFile.setSelection(Boolean.parseBoolean(defaultsNode.getAttribute(QueryDefinition.LAUNCH)));
		printSQLButton.setSelection(Boolean.parseBoolean(defaultsNode.getAttribute(QueryDefinition.PRINT_SQL)));
		pagesize.setText(defaultsNode.getAttribute(QueryDefinition.PAGE_SIZE));

		if (defaultsNode.getAttribute(QueryDefinition.ORIENTATION).equalsIgnoreCase(QueryDefinition.LANDSCAPE)) {
			landscape.setSelection(true);
			portrait.setSelection(false);
		} else if (defaultsNode.getAttribute(QueryDefinition.ORIENTATION).equalsIgnoreCase(QueryDefinition.PORTRAIT)) {
			landscape.setSelection(false);
			portrait.setSelection(true);
		}

		return true;

	}

}
