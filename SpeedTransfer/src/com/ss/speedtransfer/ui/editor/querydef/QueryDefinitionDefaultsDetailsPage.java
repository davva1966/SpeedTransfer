package com.ss.speedtransfer.ui.editor.querydef;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

import com.ss.speedtransfer.model.QueryDefinition;


public class QueryDefinitionDefaultsDetailsPage extends AbstractQueryDefinitionDetailsPage {

	protected Button removeDefaults;

	public QueryDefinitionDefaultsDetailsPage(QueryDefinition model) {
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
		s1.setText("Query Definition Defaults");
		TableWrapData td = new TableWrapData(TableWrapData.FILL, TableWrapData.TOP);
		td.grabHorizontal = true;
		s1.setLayoutData(td);

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

		removeDefaults = toolkit.createButton(baseSection, "Remove Defaults", SWT.PUSH);
		removeDefaults.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				model.remove(input);
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

	}

	protected void internalUpdate() {

	}

	public void setFocus() {
		removeDefaults.setFocus();
	}

}
