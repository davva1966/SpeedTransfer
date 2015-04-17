package com.ss.speedtransfer.ui.editor.dbcon;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.progress.UIJob;

import com.ss.speedtransfer.xml.editor.XMLFormEditor;
import com.ss.speedtransfer.xml.editor.XMLPartActivationListener;


public class DBConnectionEditorActivationListener extends XMLPartActivationListener {

	public DBConnectionEditorActivationListener(XMLFormEditor editor, IPartService partService) {
		super(editor, partService);
	}

	public void partOpened(IWorkbenchPart part) {
		super.partOpened(part);
		if (part == editor) {

			// Expand all items and select the connection node
			final TreeViewer viewer = editor.getFormPage().getBlock().getViewer();
			final ITreeContentProvider cp = (ITreeContentProvider) editor.getFormPage().getBlock().getViewer().getContentProvider();

			UIJob expandAndSelect = new UIJob("ExpandAndSelect") {
				public IStatus runInUIThread(IProgressMonitor monitor) {
					try {
						viewer.expandAll();

						Object[] elements = viewer.getExpandedElements();
						Object[] children = cp.getChildren(elements[0]);
						ISelection selection = null;
						for (Object item : children) {
							if (item.toString().toLowerCase().contains("connection")) {
								selection = new StructuredSelection(item);
								viewer.setSelection(selection);
								break;
							}
						}
					} catch (Exception e) {
					}

					return Status.OK_STATUS;
				}
			};

			expandAndSelect.schedule(100);

		}
	}
}