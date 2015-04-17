package com.ss.speedtransfer.ui.editor.dbcon;

import java.io.IOException;

import org.eclipse.swt.widgets.Text;

import com.ss.speedtransfer.model.DBConnection;
import com.ss.speedtransfer.util.StringHelper;
import com.ss.speedtransfer.xml.editor.XMLAbstractDetailsPage;


public abstract class AbstractDBConnectionDetailsPage extends XMLAbstractDetailsPage {

	public AbstractDBConnectionDetailsPage(DBConnection model) {
		super(model);
	}

	protected synchronized void setAttributeValueEncrypted(String attributeName, String value) {
		try {
			if (value.trim().length() == 0)
				setAttributeValue(attributeName, value.trim());
			else
				setAttributeValue(attributeName, StringHelper.encodePassword(value).trim());
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	protected void updateTextEncrypted(Text text, String value) {
		try {
			String plainValue = StringHelper.decodePassword(value);

			// Avoid setting same value again
			if (text != null && text.getText().equals(plainValue) == false)
				text.setText(plainValue);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public DBConnection getDBCon() {
		return (DBConnection) model;
	}

}
