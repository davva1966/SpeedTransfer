package com.ss.speedtransfer.xml.editor;

import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.forms.IDetailsPage;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.Section;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ss.speedtransfer.ui.StyledEdit;


public abstract class XMLAbstractDetailsPage implements IDetailsPage, XMLModelListener {

	protected IManagedForm mform;
	protected Node input;
	protected XMLModel model;
	protected boolean suspendValidation;

	public XMLAbstractDetailsPage(XMLModel model) {
		this.model = model;
		model.addModelListener(this);
	}

	public void initialize(IManagedForm mform) {
		this.mform = mform;
	}

	public void commit(boolean onSave) {
	}

	public void setFocus() {
	}

	public void dispose() {
	}

	public boolean isDirty() {
		return false;
	}

	public boolean isStale() {
		return false;
	}

	public void refresh() {
		internalUpdateNoNotify();
	}

	public boolean setFormInput(Object input) {
		return false;
	}

	protected void internalUpdateNoNotify() {
		model.setSuspendUpdate(true);
		suspendValidation = true;
		try {
			internalUpdate();
		} catch (Exception e) {
		} finally {
			suspendValidation = false;
			model.setSuspendUpdate(false);
		}

	}

	protected abstract void internalUpdate();

	protected void validate() {
		if (suspendValidation)
			return;

		validatePage();
	}

	protected void validatePage() {
		clearAllMessages();

	}

	public abstract void createContents(Composite parent);

	public void selectionChanged(IFormPart part, ISelection selection) {
		IStructuredSelection sel = (IStructuredSelection) selection;
		if (sel.size() == 1) {
			setInput((Node) sel.getFirstElement());
		} else
			setInput((Node) sel.getFirstElement());
	}

	protected boolean getAttributeValueBoolean(String attributeName) {
		return getAttributeValueBoolean(null, attributeName);

	}

	protected boolean getAttributeValueBoolean(Node node, String attributeName) {
		if (node == null)
			node = input;
		return model.getAttributeBoolean(node, attributeName);

	}

	protected int getAttributeValueInt(String attributeName) {
		try {
			return Integer.parseInt(getAttributeValue(attributeName));
		} catch (Exception e) {
		}

		return -1;

	}

	protected String getAttributeValue(String attributeName) {
		return getAttributeValue(null, attributeName);

	}

	protected String getAttributeValue(Node node, String attributeName) {
		if (node == null)
			node = input;
		return model.getAttribute(node, attributeName);

	}

	protected synchronized void setAttributeValue(String attributeName, boolean value) {
		setAttributeValue(null, attributeName, value);

	}

	protected synchronized void setAttributeValue(Node node, String attributeName, boolean value) {
		if (value)
			setAttributeValue(node, attributeName, "true");
		else
			setAttributeValue(node, attributeName, "false");
	}

	protected synchronized void setAttributeValue(String attributeName, int value) {
		setAttributeValue(null, attributeName, value);
	}

	protected synchronized void setAttributeValue(Node node, String attributeName, int value) {
		setAttributeValue(node, attributeName, Integer.toString(value));
	}

	protected synchronized void setAttributeValue(String attributeName, String value) {
		setAttributeValue(null, attributeName, value);

	}

	protected synchronized void setAttributeValue(Node node, String attributeName, String value) {
		if (node == null)
			node = input;

		model.setAttribute(node, attributeName, value);
	}

	protected synchronized void removeAttribute(String attributeName) {
		model.removeAttribute(input, attributeName);

	}

	protected String getText() {
		return getText(null);
	}

	protected String getText(Node node) {
		if (node == null)
			node = input;
		return model.getText(node);

	}

	protected String getCDATA() {
		return getCDATA(null);
	}

	protected String getCDATA(Node node) {
		if (node == null)
			node = input;
		return model.getCDATA(node);

	}

	protected synchronized void setText(int value) {
		setText(null, value);
	}

	protected synchronized void setText(Node node, int value) {
		setText(node, Integer.toString(value));
	}

	protected synchronized void setText(String value) {
		setText(null, value);
	}

	protected synchronized void setText(Node node, String value) {
		if (node == null)
			node = input;
		model.setText(node, value);

	}

	protected synchronized void setCDATA(String value) {
		setCDATA(null, value);
	}

	protected synchronized void setCDATA(Node node, String value) {
		if (node == null)
			node = input;
		model.setCDATA(node, value);

	}

	protected void updateBoolean(Button button, boolean value) {
		// Avoid setting same value again
		if (button.getSelection() != value)
			button.setSelection(value);

	}

	protected void updateText(Text text, String value) {
		// Avoid setting same value again
		if (text != null && text.getText().equals(value) == false)
			text.setText(value);

	}

	protected void updateText(StyledEdit styledEdit, String value) {
		// Avoid setting same value again
		if (styledEdit != null && styledEdit.getText().equals(value) == false)
			styledEdit.setText(value);

	}

	protected void updateCombo(Combo combo, String value) {
		// Avoid setting same value again
		if (combo.getText().equals(value) == false)
			combo.setText(value);

	}

	protected void updateSpinner(Spinner spinner, String value) {
		// Avoid setting same value again
		if (spinner.getText().equals(value) == false) {
			if (value != null && value.trim().length() > 0) {
				try {
					int digits = spinner.getDigits();
					if (digits == 0) {
						spinner.setSelection(Integer.parseInt(value));
					} else {
						double doubleValue = Double.parseDouble(value);
						int mult = (int) Math.pow(10, digits);
						doubleValue = doubleValue * mult;
						spinner.setSelection((int) doubleValue);
					}
				} catch (Exception e) {
				}
			} else {
				spinner.setSelection(0);
			}
		}

	}

	protected void setInput(Node newInput) {
		this.input = newInput;
		clearAllMessages();
		internalUpdateNoNotify();
	}

	protected void createSectionToolbar(Section section) {
		ToolBarManager toolBarManager = new ToolBarManager(SWT.RIGHT);
		ToolBar toolbar = toolBarManager.createControl(section);
		final Cursor handCursor = new Cursor(Display.getCurrent(), SWT.CURSOR_HAND);
		toolbar.setCursor(handCursor);

		// Cursor needs to be explicitly disposed
		toolbar.addDisposeListener(new DisposeListener() {

			public void widgetDisposed(DisposeEvent e) {
				if ((handCursor != null) && (handCursor.isDisposed() == false)) {
					handCursor.dispose();
				}
			}
		});

		fillSectionToolbar(toolBarManager);

		toolBarManager.update(true);

		section.setTextClient(toolbar);
	}

	protected void fillSectionToolbar(ToolBarManager toolBarManager) {
	}

	protected void issueMessage(String key, int type, String message) {
		issueMessage(key, type, message, null);
	}

	protected void issueMessage(String key, int type, String message, Control control) {
		if (control == null)
			mform.getMessageManager().addMessage(key, message, null, type);
		else
			mform.getMessageManager().addMessage(key, message, null, type, control);
	}

	protected void clearAllMessages() {
		mform.getMessageManager().removeAllMessages();
	}

	protected void clearMessage(String key) {
		clearMessage(key, null);
	}

	protected void clearMessage(String key, Control control) {
		if (control == null)
			mform.getMessageManager().removeMessage(key);
		else
			mform.getMessageManager().removeMessage(key, control);
	}

	public static Combo createDropDown(Composite composite, String[] elements, int selectedElementNo) {
		return createDropDown(composite, elements, selectedElementNo, SWT.DROP_DOWN | SWT.READ_ONLY);

	}

	public static Combo createDropDown(Composite composite, String[] elements, int selectedElementNo, int type) {
		Combo combo = new Combo(composite, type);
		if (elements != null) {
			for (int i = 0; i < elements.length; i++) {
				combo.add(elements[i]);
			}
			combo.select(selectedElementNo);
		}
		return combo;
	}

	public Node getNode(String nodeName) {
		return getNode(input.getOwnerDocument(), nodeName);

	}

	public Node getNode(Node parent, String nodeName) {
		NodeList list = ((Element) parent).getElementsByTagName(nodeName);
		if (list.getLength() == 0)
			return null;

		return list.item(0);

	}

	public void modelChanged(Object[] objects, String type, String property) {
		if (type.equals(XMLModelListener.REPLACED))
			internalUpdateNoNotify();

	}
}
