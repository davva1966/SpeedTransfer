package com.ss.speedtransfer.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import com.ss.speedtransfer.model.QueryDefinition;
import com.ss.speedtransfer.ui.StyledEdit;


public class CommentPrompt {

	protected QueryDefinition queryDef;
	protected boolean runConfirmed = true;

	public CommentPrompt(QueryDefinition queryDef) {
		super();
		this.queryDef = queryDef;
	}

	public boolean prompt() {

		Shell shell = UIHelper.instance().getActiveShell();
		final Shell settingsShell = new Shell(shell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE);
		settingsShell.setText("Comments");
		settingsShell.setSize(500, 280);
		
		UIHelper.instance().centerInParent(shell, settingsShell);

		settingsShell.setLayout(new GridLayout(2, false));

		FormToolkit toolkit = new FormToolkit(settingsShell.getDisplay());
		ScrolledForm form = toolkit.createScrolledForm(settingsShell);

		try {
			form.setBackgroundImage(UIHelper.instance().getImageDescriptor("form_banner.gif").createImage());
		} catch (Exception e) {
		}

		form.setText("Comments");
		form.getBody().setLayout(new FillLayout());
		form.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		toolkit.decorateFormHeading(form.getForm());

		Composite comp = toolkit.createComposite(form.getBody());
		GridLayout gl = new GridLayout(1, false);
		gl.marginHeight = 0;
		gl.marginWidth = 0;
		comp.setLayout(gl);

		StyledEdit text = new StyledEdit(comp, new TextSourceViewerConfiguration(), SWT.V_SCROLL | SWT.H_SCROLL | SWT.READ_ONLY, false);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.heightHint = 100;
		text.getControl().setLayoutData(gd);

		text.setText(queryDef.getComment());

		// OK button
		Button okButton = toolkit.createButton(settingsShell, "OK", SWT.PUSH);
		gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		gd.widthHint = 100;
		okButton.setLayoutData(gd);
		okButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				runConfirmed = true;
				settingsShell.close();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		// Cancel button
		Button cancelButton = toolkit.createButton(settingsShell, "Cancel", SWT.PUSH);
		gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		gd.widthHint = 100;
		cancelButton.setLayoutData(gd);
		cancelButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				runConfirmed = false;
				settingsShell.close();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		settingsShell.setDefaultButton(okButton);

		// Open shell
		settingsShell.open();
		Display display = Display.getDefault();
		while (!settingsShell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}

		return runConfirmed;

	}

}
