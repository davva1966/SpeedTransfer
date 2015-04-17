package com.ss.speedtransfer.handlers;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.TextEvent;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;

import com.ss.speedtransfer.util.UIHelper;


public class TextOperationHandler extends Action implements ISelectionChangedListener, ITextListener {
	protected SourceViewer viewer;
	protected int operation = -1;
	protected boolean runOnReadOnly = false;

	public TextOperationHandler(SourceViewer viewer, int operation, String text) {
		this(viewer, operation, text, false);
	}

	public TextOperationHandler(SourceViewer viewer, int operation, String text, boolean runsOnReadOnly) {
		this.viewer = viewer;
		this.operation = operation;
		this.runOnReadOnly = runsOnReadOnly;
		setText(text);
		viewer.getSelectionProvider().addSelectionChangedListener(this);
		viewer.addTextListener(this);
		update();
	}
	
	public void runWithEvent(Event event) {
		run();
	}

	public void run() {
		if (operation == -1 || viewer == null)
			return;

		if (runOnReadOnly == false && canModifyViewer() == false)
			return;

		Display display = null;
		Shell shell = UIHelper.instance().getActiveShell();
		;
		if (shell != null && !shell.isDisposed())
			display = shell.getDisplay();

		BusyIndicator.showWhile(display, new Runnable() {
			public void run() {
				viewer.doOperation(operation);
			}
		});
	}

	public void selectionChanged(SelectionChangedEvent event) {
		update();
	}
	
	public void textChanged(TextEvent event) {
		update();
	}

	protected boolean canModifyViewer() {
		return viewer.isEditable();
	}

	public void update() {

		if (!runOnReadOnly && !canModifyViewer()) {
			setEnabled(false);
			return;
		}

		boolean isEnabled = (viewer != null && viewer.canDoOperation(operation));
		setEnabled(isEnabled);

	}

}
