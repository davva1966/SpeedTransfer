package com.ss.speedtransfer.util.server;

import java.io.File;
import java.util.EmptyStackException;
import java.util.Stack;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.ss.speedtransfer.ui.XMLContentProvider;
import com.ss.speedtransfer.util.SSUtil;
import com.ss.speedtransfer.util.UIHelper;


/**
 * This class is used to select a MDObject.
 */
public class ServerFolderSelectionDialog extends Dialog {

	protected Object selectedObject = null;
	protected boolean found = true;
	protected Document document = null;

	class TreeLabelProvider extends LabelProvider {
		public String getText(Object element) {
			return ((Element) element).getAttribute("name");
		}

		public Image getImage(Object element) {
			return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
		}

	}

	public ServerFolderSelectionDialog(IShellProvider parentShell, Document document) {
		super(parentShell);
		this.document = document;
		setShellStyle(SWT.DIALOG_TRIM | SWT.RESIZE | SWT.APPLICATION_MODAL);
	}

	@Override
	protected void configureShell(Shell newShell) {
		newShell.setText("Server Folder Selector");
		super.configureShell(newShell);
	}

	protected void cancelPressed() {
		selectedObject = null;
		super.cancelPressed();
	}

	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		getButton(IDialogConstants.OK_ID).setEnabled(false);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);

	}

	protected Control createDialogArea(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		try {
			GridLayout gridLayout = new GridLayout();
			gridLayout.numColumns = 1;
			composite.setLayout(gridLayout);
			composite.setLayoutData(new GridData(GridData.FILL_BOTH));
			Label titleLabel = new Label(composite, SWT.NONE);
			titleLabel.setText("Select server folder");
			FilteredTree filterTree = createFilteredTree(composite);

			if (filterTree.getViewer().getTree().getItemCount() == 0)
				UIHelper.instance().showMessage("Information", "No server folders found");
		} catch (Exception e) {
			UIHelper.instance().showMessage("Error", "Unable to browse for folders. Error: " + SSUtil.getMessage(e));
		}

		return composite;
	}

	/**
	 * @return Returns the selected Object.
	 */
	public String getSelectedFolder() {
		if (selectedObject != null) {
			Stack<String> nameStack = new Stack<String>();
			nameStack.push(((Element) selectedObject).getAttribute("name"));
			Node parent = ((Node) selectedObject).getParentNode();
			while (parent != null && ((Element) parent).getTagName().equalsIgnoreCase("reportRoot") == false) {
				nameStack.push(((Element) parent).getAttribute("name"));
				parent = parent.getParentNode();
			}

			StringBuilder sb = new StringBuilder();
			String part = nameStack.pop();
			while (true) {
				sb.append(part);
				sb.append(File.separator);
				try {
					part = nameStack.pop();
				} catch (EmptyStackException e) {
					break;
				}
			}

			return sb.toString();

		}

		return null;
	}

	protected FilteredTree createFilteredTree(Composite parent) throws Exception {

		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		composite.setLayout(layout);

		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);

		data.horizontalSpan = 2;
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = true;

		composite.setLayoutData(data);

		PatternFilter filter = new PatternFilter();
		filter.setIncludeLeadingWildcard(true);

		FilteredTree filterTree = new FilteredTree(composite, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER, filter, true);

		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 1;
		gd.widthHint = 450;
		gd.heightHint = 600;

		filterTree.setLayoutData(gd);

		TreeViewer viewer = filterTree.getViewer();
		viewer.setContentProvider(new XMLContentProvider());
		viewer.setLabelProvider(new TreeLabelProvider());
		viewer.setInput(document);
		viewer.addFilter(filter);
		viewer.setAutoExpandLevel(TreeViewer.ALL_LEVELS);
		viewer.expandAll();

		viewer.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				selectedObject = selection.getFirstElement();
				if (selectedObject != null) {
					getButton(IDialogConstants.OK_ID).setEnabled(true);
				} else {
					getButton(IDialogConstants.OK_ID).setEnabled(false);
					selectedObject = null;
				}
			}
		});

		// Handle double click event (add selected view)
		viewer.addDoubleClickListener(new IDoubleClickListener() {

			public void doubleClick(DoubleClickEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				selectedObject = selection.getFirstElement();
				if (selectedObject != null)
					close();
				else
					selectedObject = null;
			}
		});

		return filterTree;
	}

}
