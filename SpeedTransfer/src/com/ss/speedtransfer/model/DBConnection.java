package com.ss.speedtransfer.model;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.ide.IDE;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ss.speedtransfer.ui.wizard.NewDBConnectionWizard;
import com.ss.speedtransfer.util.StringHelper;
import com.ss.speedtransfer.util.UIHelper;
import com.ss.speedtransfer.xml.editor.XMLModel;


public class DBConnection extends XMLModel {

	public static final String CATALOG_KEY = "http://com.ss.speedtransfer.schema/DBConnection.xsd";

	// XML Node names
	public static final String DB_CONNECTION_DEF = "dbConnectionDefinition";
	public static final String CONNECTION = "connection";
	public static final String PROPERTY = "property";
	public static final String COMMENT = "comment";

	// XML Attribute names
	public static final String DESCRIPTION = "description";
	public static final String NAME = "name";
	public static final String VALUE = "value";
	public static final String TYPE = "type";
	public static final String HOST = "host";
	public static final String PORT = "port";
	public static final String DATABASE = "database";
	public static final String USER = "user";
	public static final String PASSWORD = "password";
	public static final String PROMPT = "prompt";

	// XML Attribute values
	public static final String ISERIES = "iseries";
	public static final String MYSQL = "mysql";
	public static final String SQLSERVER = "sqlserver";
	public static final String ORACLE = "oracle";
	public static final String DB2 = "db2";
	public static final String POSTGRESQL = "postgresql";
	public static final String CSV = "csv";

	protected String user = null;
	protected String password = null;
	protected boolean promptComplete = false;

	public DBConnection(String file) throws Exception {
		this(new FileInputStream(file));

	}

	public DBConnection(IFile file) throws Exception {
		this(file.getContents());
	}

	public DBConnection(InputStream stream) throws Exception {
		super(stream, DB_CONNECTION_DEF);

	}

	public DBConnection(Document document) {
		super(document, DB_CONNECTION_DEF);
	}

	public List<Node> getPropertyNodes() {
		List<Node> propertyNodes = new ArrayList<Node>();

		NodeList nodeList = document.getDocumentElement().getElementsByTagName(PROPERTY);
		for (int i = 0; i < nodeList.getLength(); i++) {
			propertyNodes.add(nodeList.item(i));
		}

		return propertyNodes;

	}

	public Map<String, String> getConnectionProperties() {
		Map<String, String> props = new HashMap<String, String>();

		List<Node> propertyNodes = getPropertyNodes();

		if (propertyNodes.size() > 0) {
			for (Node propertyNode : propertyNodes) {
				String propName = getAttribute(propertyNode, NAME).trim();
				if (propName.length() > 0) {
					props.put(propName, getAttribute(propertyNode, VALUE).trim());
				}
			}
		}

		return props;

	}

	public boolean hasConnectionProperties() {
		List<Node> propertyNodes = getPropertyNodes();
		if (propertyNodes.size() == 0)
			return false;

		for (Node propertyNode : propertyNodes) {
			String propName = getAttribute(propertyNode, NAME).trim();
			if (propName.length() > 0)
				return true;
		}

		return false;

	}

	public Node getConnectionNode() {
		return getNode(CONNECTION);

	}

	public String getConnectionType() {
		return getAttribute(getConnectionNode(), TYPE);
	}

	public String getHost() {
		return getAttribute(getConnectionNode(), HOST);
	}

	public String getPort() {
		return getAttribute(getConnectionNode(), PORT);
	}

	public String getDatabase() {
		return getAttribute(getConnectionNode(), DATABASE);
	}

	public String getUser() {
		if (user != null && user.trim().length() > 0)
			return user;

		return getUserFromModel();
	}

	public String getUserFromModel() {
		return getAttribute(getConnectionNode(), USER);
	}

	public void setUser(String user) {
		if (suspendUpdate)
			return;
		this.user = user;
	}

	public String getPassword() {
		if (password != null && password.trim().length() > 0)
			return password;

		return getPasswordFromModel();
	}

	public String getPasswordFromModel() {
		String pwd = getAttribute(getConnectionNode(), PASSWORD);
		try {
			return StringHelper.decodePassword(pwd);
		} catch (Exception e) {
		}
		return null;
	}

	public void setPassword(String password) {
		if (suspendUpdate)
			return;
		this.password = password;
	}

	public boolean getPrompt() {
		if (promptComplete)
			return false;

		return getPromptInModel();
	}

	public boolean getPromptInModel() {
		return getAttributeBoolean(getConnectionNode(), PROMPT);
	}

	public static boolean isDBConnectionFile(Object resource) {
		if (resource instanceof IFile) {
			try {
				IContentType ct = IDE.getContentType((IFile) resource);
				if (ct.getId().equals("com.ss.speedtransfer.dbConnectionContentType"))
					return true;
			} catch (Exception e) {
			}

		}

		return false;
	}

	public String toString() {
		return getTypeName() + " " + getHost() + "/" + getDatabase();

	}

	public String getTypeName() {
		if (getConnectionType().equalsIgnoreCase(ISERIES))
			return "iSeries";
		if (getConnectionType().equalsIgnoreCase(MYSQL))
			return "MySQL";
		if (getConnectionType().equalsIgnoreCase(SQLSERVER))
			return "SQL Server";
		if (getConnectionType().equalsIgnoreCase(ORACLE))
			return "Oracle";
		if (getConnectionType().equalsIgnoreCase(DB2))
			return "DB2";

		return getConnectionType();

	}

	public void setPromptComplete(boolean flag) {
		if (suspendUpdate)
			return;
		this.promptComplete = flag;

	}

	public void reset() {
		this.user = null;
		this.password = null;
		this.promptComplete = false;

	}

	public static void editDBConnection(String filePath) {
		IFile file = null;
		IPath ipath = new Path(filePath);
		String filename = ipath.lastSegment();
		if (filePath.length() > 0) {
			try {
				file = ResourcesPlugin.getWorkspace().getRoot().getFile(ipath);
			} catch (Exception e) {
			}
			if (file != null && file.exists()) {
				try {
					if (isDBConnectionFile(file))
						IDE.openEditor(UIHelper.instance().getActivePage(), file, true);
				} catch (Exception e) {
				}
			} else {
				NewDBConnectionWizard wizard = new NewDBConnectionWizard(filename);
				wizard.init(UIHelper.instance().getActiveWindow().getWorkbench(), StructuredSelection.EMPTY);
				WizardDialog dialog = new WizardDialog(UIHelper.instance().getActiveWindow().getShell(), wizard);
				dialog.open();
			}
		}
	}

	public String getCatalogKey() {
		return CATALOG_KEY;
	}

	public boolean hasTransactionSupport() {
		return getConnectionType().equalsIgnoreCase(ISERIES) == false;
	}

	public InputStream getInputStream() throws Exception {

		Node commentsNode = getNode(COMMENT);
		if (commentsNode != null) {
			String comments = getCDATA(commentsNode);
			comments = comments.replaceAll("\r\n", "\n");
			setCDATA(commentsNode, comments);
		}

		return super.getInputStream();

	}

	public boolean canBeExportTarget() {
		return getConnectionType().equalsIgnoreCase(CSV) == false;
	}

}
