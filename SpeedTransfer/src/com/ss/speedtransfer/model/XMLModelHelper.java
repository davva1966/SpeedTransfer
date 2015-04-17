package com.ss.speedtransfer.model;

import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class XMLModelHelper {

	protected Document document;

	public XMLModelHelper(Document document) {
		super();
		this.document = document;
	}

	public Node getNode(String nodeName) {
		return document.getDocumentElement().getElementsByTagName(nodeName).item(0);

	}

	public static String getAttributeValue(Node node, String attributeName) {
		if (node == null)
			return null;
		String text = ""; //$NON-NLS-1$
		try {
			text = ((Element) node).getAttribute(attributeName);
			if (text == null)
				text = ""; //$NON-NLS-1$
		} catch (Exception e) {
		}

		return text;
	}

	public static boolean getAttributeValueBoolean(Node node, String attributeName) {
		try {
			if (getAttributeValue(node, attributeName) == null)
				return false;
			if (getAttributeValue(node, attributeName).trim().length() == 0)
				return false;
			if (getAttributeValue(node, attributeName).trim().equalsIgnoreCase("false"))
				return false;
		} catch (Exception e) {
		}

		return true;

	}

	public String getCDATA(Node node) {
		if (node == null)
			return null;
		String text = ""; //$NON-NLS-1$
		try {
			Node child = node.getFirstChild();
			while (child instanceof CDATASection == false)
				child = child.getNextSibling();

			if (child instanceof CDATASection)
				text = ((CDATASection) child).getData();

			if (text == null)
				text = ""; //$NON-NLS-1$
		} catch (Exception e) {
		}

		return text;

	}

	protected synchronized void setAttributeValue(Node node, String attributeName, boolean value) {
		if (value)
			setAttributeValue(node, attributeName, "true");
		else
			setAttributeValue(node, attributeName, "false");
	}

	protected synchronized void setAttributeValue(Node node, String attributeName, int value) {
		setAttributeValue(node, attributeName, Integer.toString(value));
	}

	protected synchronized void setAttributeValue(Node node, String attributeName, String value) {
		try {
			if (value == null)
				value = ""; //$NON-NLS-1$
			((Element) node).setAttribute(attributeName, value);
		} catch (Exception e) {
		}

	}

	protected synchronized void setCDATA(Node node, String value) {
		try {
			boolean hasCDATA = false;
			Node child = node.getFirstChild();
			while (child != null && child instanceof CDATASection == false)
				child = child.getNextSibling();

			if (child instanceof CDATASection)
				hasCDATA = true;

			if (hasCDATA == false) {
				child = node.getOwnerDocument().createCDATASection(value);
				node.appendChild(child);
			} else {
				((CDATASection) child).setData(value);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	protected Node createNode(Node parent, String nodeName) {
		Node newNode = document.createElement(nodeName);
		parent.appendChild(newNode);
		return newNode;

	}

}
