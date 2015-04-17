package com.ss.speedtransfer.ui;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XMLContentProvider implements ITreeContentProvider {

	Document document;

	public XMLContentProvider() {
		super();
	}

	public Document getDocument() {
		return document;

	}

	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof Document) {
			NodeList childNodes = document.getDocumentElement().getChildNodes();
			Node[] elements = new Node[childNodes.getLength()];
			for (int i = 0; i < childNodes.getLength(); i++) {
				elements[i] = childNodes.item(i);
			}

			return elements;
		}

		return new Object[0];
	}

	public void dispose() {
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.document = (Document) newInput;

	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof Node) {
			NodeList childNodes = ((Node) parentElement).getChildNodes();
			Node[] elements = new Node[childNodes.getLength()];
			for (int i = 0; i < childNodes.getLength(); i++) {
				elements[i] = childNodes.item(i);
			}

			return elements;
		}

		return new Object[0];
	}

	@Override
	public Object getParent(Object element) {
		if (element instanceof Node)
			return ((Node) element).getParentNode();

		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof Node)
			return ((Node) element).getChildNodes().getLength() > 0;

		return false;
	}
}
