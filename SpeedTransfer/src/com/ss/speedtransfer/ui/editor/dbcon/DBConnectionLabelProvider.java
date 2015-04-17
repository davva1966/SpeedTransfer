package com.ss.speedtransfer.ui.editor.dbcon;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.w3c.dom.Element;

import com.ss.speedtransfer.model.DBConnection;
import com.ss.speedtransfer.model.QueryDefinition;
import com.ss.speedtransfer.util.UIHelper;


public class DBConnectionLabelProvider extends LabelProvider {

	public String getText(Object obj) {
		String text = "Unknown";
		if (obj instanceof Element) {
			Element elem = (Element) obj;
			try {
				if (elem.getTagName().equalsIgnoreCase(DBConnection.DB_CONNECTION_DEF))
					text = "Database Connection Definition";
				else if (elem.getTagName().equalsIgnoreCase(DBConnection.CONNECTION))
					text = "Connection";
				else if (elem.getTagName().equalsIgnoreCase(DBConnection.PROPERTY)) {
					text = "Property";
					text = text + " (" + elem.getAttribute(DBConnection.NAME) + " = " + elem.getAttribute(DBConnection.VALUE) + ")";
				} else if (elem.getTagName().equalsIgnoreCase(QueryDefinition.COMMENT))
					text = "Comments";

			} catch (Exception e) {
			}
		}

		if (text == null || text.trim().length() == 0)
			text = obj.toString();

		return text;
	}

	public Image getImage(Object obj) {
		Image image = null;
		if (obj instanceof Element) {
			Element elem = (Element) obj;
			try {
				if (elem.getTagName().equalsIgnoreCase(DBConnection.DB_CONNECTION_DEF))
					image = UIHelper.instance().getImage("db_connection.png");
				else if (elem.getTagName().equalsIgnoreCase(DBConnection.CONNECTION))
					image = UIHelper.instance().getImage("connection.gif");
				else if (elem.getTagName().equalsIgnoreCase(DBConnection.PROPERTY))
					image = UIHelper.instance().getImage("property.gif");
				else if (elem.getTagName().equalsIgnoreCase(DBConnection.COMMENT))
					image = UIHelper.instance().getImage("help.gif");
			} catch (Exception e) {
			}
		}

		if (image == null)
			image = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);

		return image;
	}
}