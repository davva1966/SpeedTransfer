package com.ss.speedtransfer.xml.editor;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IFileEditorInput;
import org.w3c.dom.Node;

public class XMLMasterContentProvider implements ITreeContentProvider {

	XMLModel model;

	public XMLMasterContentProvider(XMLModel model) {
		super();
		this.model = model;
	}

	public XMLModel getModel() {
		return model;

	}

	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof IFileEditorInput)
			return model.getRootElements();

		return new Object[0];
	}

	public void dispose() {
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		return model.getChildren((Node) parentElement);
	}

	@Override
	public Object getParent(Object element) {
		return model.getParent((Node) element);
	}

	@Override
	public boolean hasChildren(Object element) {
		return model.hasChildren((Node) element);
	}
}
