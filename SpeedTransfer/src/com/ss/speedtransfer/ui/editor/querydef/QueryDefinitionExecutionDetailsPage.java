package com.ss.speedtransfer.ui.editor.querydef;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

import com.ss.speedtransfer.model.QueryDefinition;


public class QueryDefinitionExecutionDetailsPage extends AbstractQueryDefinitionDetailsPage {

	protected Combo columnHeadingCombo;
	protected Combo defaultRunOptionCombo;

	public QueryDefinitionExecutionDetailsPage(QueryDefinition model) {
		super(model);
	}

	public void createContents(Composite parent) {
		TableWrapLayout layout = new TableWrapLayout();
		layout.topMargin = 10;
		layout.leftMargin = 5;
		layout.rightMargin = 2;
		layout.bottomMargin = 2;
		parent.setLayout(layout);

		FormToolkit toolkit = mform.getToolkit();

		Section s1 = toolkit.createSection(parent, Section.TITLE_BAR);
		s1.marginWidth = 10;
		s1.setText("Query Definition Execution");
		TableWrapData td = new TableWrapData(TableWrapData.FILL, TableWrapData.TOP);
		td.grabHorizontal = true;
		s1.setLayoutData(td);
		s1.clientVerticalSpacing = 10;

		Composite client = toolkit.createComposite(s1);
		GridLayout glayout = new GridLayout();
		glayout.marginWidth = toolkit.getBorderStyle() == SWT.BORDER ? 0 : 2;
		glayout.numColumns = 3;
		glayout.marginBottom = 10;
		client.setLayout(glayout);

		createControls(parent, client);

		toolkit.paintBordersFor(client);
		s1.setClient(client);
	}

	protected void createControls(Composite parent, Composite baseSection) {

		FormToolkit toolkit = mform.getToolkit();

		GridData gd = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
		gd.widthHint = 10;
		gd.horizontalIndent = 7;
		gd.horizontalSpan = 2;

		toolkit.createLabel(baseSection, "Column Heading");
		String[] headings = new String[3];
		headings[0] = QueryDefinition.COLUMN_HEADING_NAME_TEXT;
		headings[1] = QueryDefinition.COLUMN_HEADING_DESCRIPTION_TEXT;
		headings[2] = QueryDefinition.COLUMN_HEADING_BOTH_TEXT;
		columnHeadingCombo = createDropDown(baseSection, headings, 0);
		columnHeadingCombo.setLayoutData(gd);
		columnHeadingCombo.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				String value = ((Combo) e.getSource()).getText();
				setAttributeValue(QueryDefinition.COLUMN_HEADINGS, QueryDefinition.translateColumnHeading(value));
			}
		});

		toolkit.createLabel(baseSection, "Default run option");
		String[] options = new String[3];
		options[0] = QueryDefinition.EXPORT_TO_EXCEL_TEXT;
		options[1] = QueryDefinition.EXPORT_TO_PDF_TEXT;
		options[2] = QueryDefinition.EXPORT_TO_CSV_TEXT;
		defaultRunOptionCombo = createDropDown(baseSection, options, 0);
		defaultRunOptionCombo.setLayoutData(gd);
		defaultRunOptionCombo.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				String value = ((Combo) e.getSource()).getText();
				setAttributeValue(QueryDefinition.DEFAULT_RUN_OPTION, QueryDefinition.translateRunOption(value));
			}
		});

	}

	protected void internalUpdate() {
		updateCombo(columnHeadingCombo, QueryDefinition.translateColumnHeading(getAttributeValue(QueryDefinition.COLUMN_HEADINGS)));
		updateCombo(defaultRunOptionCombo, QueryDefinition.translateRunOption(getAttributeValue(QueryDefinition.DEFAULT_RUN_OPTION)));
		validatePage();
	}

	public void setFocus() {
		columnHeadingCombo.setFocus();
	}

}
