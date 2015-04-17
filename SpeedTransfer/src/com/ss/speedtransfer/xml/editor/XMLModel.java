package com.ss.speedtransfer.xml.editor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.resources.IFile;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;

public class XMLModel {

	protected List<XMLModelListener> modelListeners = new ArrayList<XMLModelListener>();;
	protected Document document;
	protected String rootNodeName = "root";
	protected boolean suspendUpdate = false;

	public XMLModel() {
		super();

	}

	public Document getDocument() {
		return document;
	}

	public XMLModel(String file, String rootNodeName) throws Exception {
		this(new FileInputStream(file), rootNodeName);

	}

	public XMLModel(IFile file) throws Exception {
		this(file.getContents(), "root");
	}

	public XMLModel(IFile file, String rootNodeName) throws Exception {
		this(file.getContents(), rootNodeName);
	}

	public XMLModel(InputStream stream, String rootNodeName) throws Exception {
		super();
		this.rootNodeName = rootNodeName;
		initialize(stream);

	}

	public XMLModel(Document document, String rootNodeName) {
		super();
		this.rootNodeName = rootNodeName;
		this.document = document;
	}

	public void dispose() {
		if (modelListeners != null)
			modelListeners.clear();
	}

	protected void initialize(InputStream stream) throws Exception {

		try {
			InputSource input = new InputSource(stream);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			document = dBuilder.parse(input);
		} catch (Exception e) {
			throw e;
		} finally {
			try {
				if (stream != null)
					stream.close();
			} catch (Exception e2) {
			}
		}

	}

	public void setSuspendUpdate(boolean flag) {
		this.suspendUpdate = flag;
	}

	public void setFile(IFile file) throws Exception {
		initialize(file.getContents());
		fireModelChanged(null, XMLModelListener.REPLACED, null);
	}

	public void addModelListener(XMLModelListener listener) {
		if (!modelListeners.contains(listener))
			modelListeners.add(listener);
	}

	public void removeModelListener(XMLModelListener listener) {
		modelListeners.remove(listener);
	}

	public void fireModelChanged(Object object, String type, String property) {
		Object[] objects = new Object[1];
		objects[0] = object;
		fireModelChanged(objects, type, property);
	}

	public void fireModelChanged(Object[] objects, String type, String property) {
		if (suspendUpdate)
			return;
		for (int i = 0; i < modelListeners.size(); i++) {
			((XMLModelListener) modelListeners.get(i)).modelChanged(objects, type, property);
		}
	}

	public Node[] getRootElements() {
		NodeList nodeList = document.getElementsByTagName(rootNodeName);
		if (nodeList.getLength() == 0)
			nodeList = document.getDocumentElement().getElementsByTagName(rootNodeName);
		List<Node> temporaryNodeList = new ArrayList<Node>();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				temporaryNodeList.add(node);
			}
		}

		return temporaryNodeList.toArray(new Node[0]);
	}

	public Node[] getChildren(Node parent) {
		NodeList nodeList = parent.getChildNodes();
		List<Node> temporaryNodeList = new ArrayList<Node>();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				temporaryNodeList.add(node);
			}
		}

		return temporaryNodeList.toArray(new Node[0]);
	}

	public Node[] getChildren(Node parent, String nodeName) {
		NodeList nodeList = getNodes(parent, nodeName);
		List<Node> temporaryNodeList = new ArrayList<Node>();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				temporaryNodeList.add(node);
			}
		}

		return temporaryNodeList.toArray(new Node[0]);
	}

	public boolean hasChildren(Node parent) {
		return getChildren(parent).length > 0;
	}

	public Node getParent(Node child) {
		return child.getParentNode();
	}

	public Node getNode(String nodeName) {
		return getNode(document.getDocumentElement(), nodeName);

	}

	public Node getNode(Node parentNode, String nodeName) {
		if (parentNode == null)
			return null;
		return getNodes(parentNode, nodeName).item(0);

	}

	public NodeList getNodes(String nodeName) {
		return getNodes(document.getDocumentElement(), nodeName);

	}

	public NodeList getNodes(Node parentNode, String nodeName) {
		if (parentNode == null)
			return null;
		return ((Element) parentNode).getElementsByTagName(nodeName);

	}

	public Node addNode(Node parent, String nodeName) {
		Node newNode = document.createElement(nodeName);
		parent.appendChild(newNode);
		Node[] addedNodes = new Node[1];
		addedNodes[0] = newNode;
		fireModelChanged(addedNodes, XMLModelListener.STRUCTURE_CHANGED, null);
		return newNode;
	}

	public void remove(Node node) {
		node.getParentNode().removeChild(node);
		fireModelChanged(node, XMLModelListener.STRUCTURE_CHANGED, null);

	}

	public void remove(Node[] nodes) {
		for (int i = 0; i < nodes.length; i++) {
			nodes[i].getParentNode().removeChild(nodes[i]);
		}

		fireModelChanged(nodes, XMLModelListener.STRUCTURE_CHANGED, null);
	}

	public void moveUp(Node node) {
		handleMove(node, true);
		fireModelChanged(node, XMLModelListener.STRUCTURE_CHANGED, null);

	}

	public void moveDown(Node node) {
		handleMove(node, false);
		fireModelChanged(node, XMLModelListener.STRUCTURE_CHANGED, null);

	}

	protected void handleMove(Node node, boolean up) {

		Node parent = node.getParentNode();
		Node[] children = getChildren(parent);
		int index = getIndexOf(node, parent);
		int newIndex = up ? index - 1 : index + 1;
		Node child2 = children[newIndex];

		swap(node, child2, up);

	}

	public int getIndexOf(Node node, Node parent) {
		int index = -1;
		Node[] children = getChildren(parent);
		for (int i = 0; i < children.length; i++) {
			if (children[i].equals(node)) {
				index = i;
				break;
			}
		}
		return index;
	}

	public int getIndexOf(Node node, Node parent, String nodeName) {
		int index = -1;
		NodeList children = getNodes(parent, nodeName);
		for (int i = 0; i < children.getLength(); i++) {
			if (children.item(i).equals(node)) {
				index = i;
				break;
			}
		}
		return index;
	}

	public void swap(Node child1, Node child2, boolean up) {
		Node parent = child1.getParentNode();
		int index1 = getIndexOf(child1, parent);
		int index2 = getIndexOf(child2, parent);
		if (index1 == -1 || index2 == -1)
			return;

		if (up)
			parent.insertBefore(child1, child2);
		else
			parent.insertBefore(child2, child1);

	}

	public InputStream getInputStream() throws Exception {

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		Source xmlSource = new DOMSource(document);
		Result outputTarget = new StreamResult(outputStream);
		Transformer t = TransformerFactory.newInstance().newTransformer();
		t.setOutputProperty(OutputKeys.INDENT, "true");
		t.transform(xmlSource, outputTarget);

		return new ByteArrayInputStream(outputStream.toByteArray());
	}

	public String getAttribute(Node node, String name) {
		if (node == null)
			return null;
		String text = ((Element) node).getAttribute(name);
		if (text == null)
			text = "";
		return text;

	}

	public String getText(Node node) {
		String text = ((Text) node.getFirstChild()).getData();
		if (text == null)
			text = "";
		return text;

	}

	public void setText(Node node, String value) {
		if (suspendUpdate)
			return;
		Text textElement = (Text) node.getFirstChild();
		if (textElement == null) {
			textElement = node.getOwnerDocument().createTextNode("");
			node.appendChild(textElement);
		}
		textElement.setData(value);
		fireModelChanged(node, XMLModelListener.ATTR_CHANGED, null);

	}

	public String getCDATA(Node node) {
		if (node == null)
			return null;
		Node child = node.getFirstChild();
		while (child != null && child instanceof CDATASection == false)
			child = child.getNextSibling();

		if (child != null && child instanceof CDATASection)
			return ((CDATASection) child).getData();

		return "";

	}

	public void setCDATA(Node node, String value) {
		if (suspendUpdate)
			return;
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
		fireModelChanged(node, XMLModelListener.ATTR_CHANGED, ((Element) node).getTagName());

	}

	public void setAttribute(Node node, String name, String value) {
		if (suspendUpdate)
			return;
		if (value == null)
			value = "";
		((Element) node).setAttribute(name, value);
		fireModelChanged(node, XMLModelListener.ATTR_CHANGED, name);
	}

	public void removeAttribute(Node node, String name) {
		((Element) node).removeAttribute(name);
		fireModelChanged(node, XMLModelListener.ATTR_CHANGED, name);
	}

	public boolean getAttributeBoolean(Node node, String attributeName) {
		try {
			if (getAttribute(node, attributeName) == null)
				return false;
			if (getAttribute(node, attributeName).trim().length() == 0)
				return false;
			if (getAttribute(node, attributeName).trim().equalsIgnoreCase("false"))
				return false;
		} catch (Exception e) {
		}

		return true;

	}

}