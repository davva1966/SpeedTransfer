package com.ss.speedtransfer.ui.editor.dbcon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
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
import org.eclipse.ui.ide.IDE;

import com.ss.speedtransfer.model.DBConnection;
import com.ss.speedtransfer.util.UIHelper;


/**
 * This class is used to select a MDObject.
 */
public class DBConnectionFileSelectionDialog extends Dialog {

	protected Object selectedObject = null;
	protected boolean found = true;

	class TreeContentProvider implements ITreeContentProvider {

		@Override
		public Object[] getChildren(Object parentElement) {
			try {
				if (parentElement instanceof IWorkspaceRoot)
					return ((IWorkspaceRoot) parentElement).getProjects();
				else if (parentElement instanceof IContainer)
					return getItems((IContainer) parentElement);
			} catch (Exception e) {
			}

			return null;
		}

		public Object[] getItems(IContainer container) {
			List<IResource> items = new ArrayList<IResource>();
			try {
				for (IResource resource : container.members()) {
					if (resource instanceof IContainer && containsDBConnectionFiles((IContainer) resource)) {
						items.add(resource);
					} else if (DBConnection.isDBConnectionFile(resource)) {
						items.add(resource);
					}
				}
			} catch (Exception e) {
			}

			return items.toArray(new IResource[0]);
		}

		@Override
		public Object getParent(Object element) {
			if (element instanceof IResource)
				return ((IResource) element).getParent();
			return null;
		}

		@Override
		public boolean hasChildren(Object element) {
			try {
				if (element instanceof IContainer)
					return containsDBConnectionFiles((IContainer) element);
			} catch (Exception e) {
			}

			return false;
		}

		@Override
		public Object[] getElements(Object inputElement) {
			if (inputElement instanceof IWorkspaceRoot) {
				IProject[] projects = ((IWorkspaceRoot) inputElement).getProjects();
				List<IProject> projectList = new ArrayList<IProject>();
				for (int i = 0; i < projects.length; i++) {
					if (containsDBConnectionFiles(projects[i]))
						projectList.add(projects[i]);
				}
				Collections.sort(projectList, new Comparator<IProject>() {
					public int compare(IProject proj1, IProject proj2) {
						return proj1.getName().compareToIgnoreCase(proj2.getName());
					}
				});
				return projectList.toArray(new IProject[0]);
			}

			return null;

		}

		@Override
		public void dispose() {

		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

		}

	}

	class TreeLabelProvider extends LabelProvider {

		public String getText(Object obj) {
			if (obj instanceof IResource)
				return ((IResource) obj).getName();

			return "Unknown";
		}

		public Image getImage(Object obj) {
			Image image = null;
			if (obj instanceof IProject)
				image = PlatformUI.getWorkbench().getSharedImages().getImage(IDE.SharedImages.IMG_OBJ_PROJECT);
			else if (obj instanceof IFolder)
				image = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
			else if (obj instanceof IResource)
				image = UIHelper.instance().getImage("db_connection.png");

			if (image == null)
				image = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);

			return image;
		}
	}

	public DBConnectionFileSelectionDialog(IShellProvider parentShell) {
		super(parentShell);
		setShellStyle(SWT.DIALOG_TRIM | SWT.RESIZE | SWT.APPLICATION_MODAL);
	}

	@Override
	protected void configureShell(Shell newShell) {
		newShell.setText("Database Connection File Selector");
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
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		composite.setLayout(gridLayout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		Label titleLabel = new Label(composite, SWT.NONE);
		titleLabel.setText("Select database connection file");
		FilteredTree filterTree = createFilteredTree(composite);

		if (filterTree.getViewer().getTree().getItemCount() == 0)
			UIHelper.instance().showMessage("Information", "No connection files found");

		return composite;
	}

	/**
	 * @return Returns the selected Object.
	 */
	public Object getSelectedObject() {
		return selectedObject;
	}

	protected FilteredTree createFilteredTree(Composite parent) {

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
		viewer.setContentProvider(new TreeContentProvider());
		viewer.setLabelProvider(new TreeLabelProvider());
		viewer.setInput(ResourcesPlugin.getWorkspace().getRoot());
		viewer.addFilter(filter);
		viewer.setAutoExpandLevel(TreeViewer.ALL_LEVELS);
		viewer.expandAll();

		viewer.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				selectedObject = selection.getFirstElement();
				if (selectedObject != null && isSelectable(selectedObject)) {
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
				if (selectedObject != null && isSelectable(selectedObject))
					close();
				else
					selectedObject = null;
			}
		});

		return filterTree;
	}

	protected boolean isSelectable(Object object) {
		return DBConnection.isDBConnectionFile(object);

	}

	protected boolean containsDBConnectionFiles(IContainer container) {
		found = false;
		try {
			container.accept(new IResourceVisitor() {
				public boolean visit(IResource resource) throws CoreException {
					if (DBConnection.isDBConnectionFile(resource)) {
						found = true;
						return false;
					}

					return true;
				}
			});
		} catch (Exception e) {
		}

		return found;

	}

}
