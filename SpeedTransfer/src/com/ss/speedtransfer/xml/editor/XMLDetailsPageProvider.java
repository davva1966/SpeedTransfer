package com.ss.speedtransfer.xml.editor;

import org.eclipse.ui.forms.IDetailsPage;
import org.eclipse.ui.forms.IDetailsPageProvider;
import org.w3c.dom.Node;

public class XMLDetailsPageProvider implements IDetailsPageProvider {

	@Override
	public IDetailsPage getPage(Object key) {
		return null;
	}

	@Override
	public Object getPageKey(Object object) {
		if (object instanceof Node) {
			Node node = (Node) object;
			if (node.getNodeType() == Node.ELEMENT_NODE)
				return node.getNodeName();
		}
		return null;
	}

}
