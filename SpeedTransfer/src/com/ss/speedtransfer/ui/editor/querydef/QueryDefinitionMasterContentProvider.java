package com.ss.speedtransfer.ui.editor.querydef;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Node;

import com.ss.speedtransfer.model.DBConnection;
import com.ss.speedtransfer.model.QueryDefinition;
import com.ss.speedtransfer.xml.editor.XMLMasterContentProvider;
import com.ss.speedtransfer.xml.editor.XMLModel;


class QueryDefinitionMasterContentProvider extends XMLMasterContentProvider {

	public QueryDefinitionMasterContentProvider(XMLModel model) {
		super(model);
	}

	public Object[] getChildren(Object parentElement) {
		Object[] children = super.getChildren(parentElement);
		List<Node> childrenList = new ArrayList<Node>();
		for (int i = 0; i < children.length; i++) {
			Node child = (Node) children[i];
			if (child.getNodeName().equals(DBConnection.CONNECTION) == false)
				childrenList.add(child);
		}

		return childrenList.toArray(new Node[0]);
	}

	public boolean hasChildren(Object element) {
		if (element instanceof Node) {
			if (((Node) element).getNodeName().equals(QueryDefinition.SELECTION_DEFAULTS))
				return false;
			if (((Node) element).getNodeName().equals(QueryDefinition.REPLACEMENT_VAR))
				return false;
		}
		return super.hasChildren(element);
	}

}
