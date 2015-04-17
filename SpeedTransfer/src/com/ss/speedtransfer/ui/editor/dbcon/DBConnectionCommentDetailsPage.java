package com.ss.speedtransfer.ui.editor.dbcon;

import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.TextEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import com.ss.speedtransfer.model.DBConnection;
import com.ss.speedtransfer.ui.StyledEdit;


public class DBConnectionCommentDetailsPage extends AbstractDBConnectionDetailsPage {

	protected StyledEdit comment;

	public DBConnectionCommentDetailsPage(DBConnection model) {
		super(model);

	}

	public void createContents(Composite parent) {

		FillLayout fl = new FillLayout();
		fl.marginHeight = 10;
		parent.setLayout(fl);

		FormToolkit toolkit = mform.getToolkit();

		Section s1 = toolkit.createSection(parent, Section.TITLE_BAR);
		s1.marginWidth = 10;
		s1.setText("Database Connection Comments");
		s1.clientVerticalSpacing = 7;

		Composite client = toolkit.createComposite(s1);
		GridLayout gl = new GridLayout(2, false);
		client.setLayout(gl);

		createControls(client);

		toolkit.paintBordersFor(client);
		s1.setClient(client);
	}

	protected void createControls(Composite client) {

		comment = new StyledEdit(client, new TextSourceViewerConfiguration());
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.heightHint = 100;
		comment.getControl().setLayoutData(gd);
		comment.addTextListener(new ITextListener() {

			public void textChanged(TextEvent event) {
				setCDATA(comment.getText());
			}
		});

	}

	protected void internalUpdate() {
		updateText(comment, getCDATA());

	}

	public void setFocus() {
		comment.getControl().setFocus();
	}

}
