package com.ss.speedtransfer.ui.editor.querydef;

import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.TextEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import com.ss.speedtransfer.model.QueryDefinition;
import com.ss.speedtransfer.ui.StyledEdit;


public class QueryDefinitionCommentsDetailPage extends AbstractQueryDefinitionDetailsPage {

	protected FormToolkit toolkit;
	protected Button showAtRuntimeButton;
	protected StyledEdit comment;

	public QueryDefinitionCommentsDetailPage(QueryDefinition model) {
		super(model);
	}

	public void createContents(Composite parent) {

		FillLayout fl = new FillLayout();
		fl.marginHeight = 10;
		parent.setLayout(fl);

		toolkit = mform.getToolkit();

		Section s1 = toolkit.createSection(parent, Section.TITLE_BAR);
		s1.marginWidth = 10;
		s1.setText("Query Definition Comments");
		s1.clientVerticalSpacing = 7;

		Composite client = toolkit.createComposite(s1);
		GridLayout gl = new GridLayout(2, false);
		client.setLayout(gl);

		createControls(client);

		toolkit.paintBordersFor(client);
		s1.setClient(client);
	}

	protected void createControls(Composite client) {

		Label l = toolkit.createLabel(client, "Show at runtime");
		l.setToolTipText("Should the comments be displayed in a confirmation dialog when executing this query");
		showAtRuntimeButton = toolkit.createButton(client, "", SWT.CHECK);
		showAtRuntimeButton.setSelection(false);
		showAtRuntimeButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				setAttributeValue(QueryDefinition.SHOW_COMMENTS_AT_RUNTIME, showAtRuntimeButton.getSelection());
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		comment = new StyledEdit(client, new TextSourceViewerConfiguration());
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
		gd.heightHint = 100;
		comment.getControl().setLayoutData(gd);
		comment.addTextListener(new ITextListener() {

			public void textChanged(TextEvent event) {
				setCDATA(comment.getText());
			}
		});

	}

	protected void internalUpdate() {
		updateBoolean(showAtRuntimeButton, getAttributeValueBoolean(QueryDefinition.SHOW_COMMENTS_AT_RUNTIME));
		updateText(comment, getCDATA());
		validatePage();

	}

	public void setFocus() {
		comment.getControl().setFocus();
	}

}
