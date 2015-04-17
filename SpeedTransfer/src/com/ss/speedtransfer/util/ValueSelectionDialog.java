// {{CopyrightNotice}}

package com.ss.speedtransfer.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * The <code>ValueSelectionDialog</code> class
 */
public class ValueSelectionDialog extends Dialog {

	protected String selection = null;

	protected String subtitle = null;

	protected String valueLabel = null;

	protected Shell shell = null;

	/**
	 * Initializes a newly created <code>NewQueryDialog</code>
	 */
	public ValueSelectionDialog() {
		this(UIHelper.instance().getActiveWindow().getShell());
	}

	/**
	 * Initializes a newly created <code>NewQueryDialog</code>
	 * 
	 * @param parent
	 */
	public ValueSelectionDialog(Shell parent) {
		this(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
	}

	/**
	 * Initializes a newly created <code>NewQueryDialog</code>
	 * 
	 * @param parent
	 * @param style
	 */
	public ValueSelectionDialog(Shell parent, int style) {
		super(parent, style);

	}

	/**
	 * Set the subtitle
	 * 
	 * @param subtitle
	 */
	public void setSubtitle(String subtitle) {
		this.subtitle = subtitle;
	}

	/**
	 * Set the value label
	 * 
	 * @param valueLabel
	 */
	public void setValueLabel(String valueLabel) {
		this.valueLabel = valueLabel;
	}

	public String open() {
		Shell parent = getParent();
		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		Point location = shell.getLocation();
		shell.setBounds(location.x + 30, location.y + 150, 430, 153);
		shell.setText(getText());

		createWidgets();

		shell.open();
		Display display = parent.getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		return selection;
	}

	protected void createWidgets() {

		Group group = new Group(shell, SWT.NONE);
		group.setBounds(6, 6, 412, 81);

		if (subtitle != null && subtitle.trim().length() > 0) {
			Label label = new Label(group, SWT.NONE);
			label.setText(subtitle);
			label.setBounds(6, 12, 400, 15);
		}

		if (valueLabel == null || valueLabel.trim().length() == 0)
			valueLabel = "Value";
		Label label = new Label(group, SWT.NONE);
		label.setText(valueLabel);
		label.setBounds(6, 35, 400, 15);

		final Text text = new Text(group, SWT.SINGLE | SWT.BORDER);
		text.setTextLimit(250);
		text.setBounds(6, 55, 400, 20);

		// OK button
		Button okButton = new Button(shell, SWT.PUSH);
		okButton.setText("OK");
		okButton.setBounds(6, 92, 80, 25);
		Listener listener = new Listener() {

			public void handleEvent(Event event) {
				selection = text.getText().trim();
				shell.close();
			}
		};
		okButton.addListener(SWT.Selection, listener);

		// Cancel button
		Button cancelButton = new Button(shell, SWT.PUSH);
		cancelButton.setText("Cancel");
		cancelButton.setBounds(92, 92, 80, 25);
		listener = new Listener() {

			public void handleEvent(Event event) {
				shell.close();
			}
		};
		cancelButton.addListener(SWT.Selection, listener);

	}

}
