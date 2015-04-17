package com.ss.speedtransfer.ui.editor.dbcon;

import org.eclipse.core.resources.IFile;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;

import com.ss.speedtransfer.model.DBConnection;
import com.ss.speedtransfer.xml.editor.XMLFormEditor;
import com.ss.speedtransfer.xml.editor.XMLFormPageBlock;
import com.ss.speedtransfer.xml.editor.XMLModel;
import com.ss.speedtransfer.xml.editor.XMLPartActivationListener;


public class DBConnectionEditor extends XMLFormEditor {

	public static final String ID = "com.ss.speedtransfer.dbConnectionEditor";
	private XMLPartActivationListener activationListener;

	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		try {
			super.init(site, input);
			// we want to listen for our own activation
			activationListener = new DBConnectionEditorActivationListener(this, site.getWorkbenchWindow().getPartService());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected XMLModel createModel(IFile file) throws Exception {
		return new DBConnection(file);
	}

	protected XMLFormPageBlock createBlock() {
		return new DBConnectionFormPageBlock(this, (DBConnection) model);
	}

	public void dispose() {
		activationListener.dispose();
		super.dispose();

	}

	public String getRootNodeName() {
		return DBConnection.DB_CONNECTION_DEF;
	}

}
