package com.ss.speedtransfer.model;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.ide.IDE;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ss.speedtransfer.ui.editor.dbcon.DBConnectionFileSelectionDialog;
import com.ss.speedtransfer.util.SSUtil;
import com.ss.speedtransfer.util.LicenseManager;
import com.ss.speedtransfer.util.SQLHelper;
import com.ss.speedtransfer.util.StringHelper;
import com.ss.speedtransfer.util.StringMatcher;
import com.ss.speedtransfer.util.UIHelper;
import com.ss.speedtransfer.xml.editor.XMLModel;


public class QueryDefinition extends XMLModel {

	public static final String CATALOG_KEY = "http://com.ss.speedtransfer.schema/QueryDefinition.xsd";

	// XML Node names
	public static final String QUERY_DEFINITION = "queryDefinition";
	public static final String SQL = "sql";
	public static final String REPLACEMENT_VAR = "replacementVariable";
	public static final String EXECUTION = "execution";
	public static final String COMMENT = "comment";
	public static final String DEFAULTS = "defaults";
	public static final String EXCEL_DEFAULTS = "excelDefaults";
	public static final String PDF_DEFAULTS = "pdfDefaults";
	public static final String CSV_DEFAULTS = "csvDefaults";
	public static final String DATABASE_DEFAULTS = "databaseDefaults";
	public static final String SELECTION_DEFAULTS = "selectionDefaults";
	public static final String SELECTION = "selection";
	public static final String VALUES = "values";

	// XML Attribute names
	public static final String DESCRIPTION = "description";
	public static final String COLUMN_HEADINGS = "columnHeadings";
	public static final String NAME = "name";
	public static final String TYPE = "type";
	public static final String DEFAULT_VALUE = "defaultValue";
	public static final String EXCLUDE_IF_EMPTY = "excludeIfEmpty";
	public static final String MANDATORY = "mandatory";
	public static final String DEFAULT_RUN_OPTION = "defaultRunOption";
	public static final String DB_CONNECTION_FILE = "dbConnectionFile";
	public static final String FILE_NAME = "fileName";
	public static final String REPLACE_FILE = "replaceFile";
	public static final String WORKSHEET = "worksheet";
	public static final String FILE_FORMAT = "fileFormat";
	public static final String EXPORT_HEADINGS = "exportHeadings";
	public static final String CLEAR_SHEET = "clearSheet";
	public static final String START_ROW_TYPE = "startRowType";
	public static final String START_ROW_NUMBER = "startRowNumber";
	public static final String REMOVE_REMAINING_ROWS = "removeRemainingRows";
	public static final String LAUNCH = "launch";
	public static final String VALUES_TYPE = "valuesType";
	public static final String SHOW_COMMENTS_AT_RUNTIME = "showAtRuntime";
	public static final String TABLE = "table";
	public static final String CREATE_TABLE = "createTable";
	public static final String CLEAR_TABLE = "clearTable";

	// XML Attribute values
	public static final String COLUMN_HEADING_NAME = "name";
	public static final String COLUMN_HEADING_DESCRIPTION = "description";
	public static final String COLUMN_HEADING_BOTH = "both";
	public static final String COLUMN_HEADING_NAME_TEXT = "Column Name";
	public static final String COLUMN_HEADING_DESCRIPTION_TEXT = "Column Description";
	public static final String COLUMN_HEADING_BOTH_TEXT = "Both Name And Description";
	public static final String STRING = "String";
	public static final String NUMERIC = "Numeric";
	public static final String DATE = "Date";
	public static final String TIME = "Time";
	public static final String VALUE_LIST = "List of values";
	public static final String EXPORT_TO_EXCEL = "excel";
	public static final String EXPORT_TO_PDF = "pdf";
	public static final String EXPORT_TO_CSV = "csv";
	public static final String EXPORT_TO_EXCEL_TEXT = "Export to excel";
	public static final String EXPORT_TO_PDF_TEXT = "Export to PDF";
	public static final String EXPORT_TO_CSV_TEXT = "Export to CSV";
	public static final String XLS = "xls";
	public static final String XLSX = "xlsx";
	public static final String FIRST = "first";
	public static final String LAST = "last";
	public static final String ROW = "row";
	public static final String PRINT_SQL = "printSQL";
	public static final String PAGE_SIZE = "pageSize";
	public static final String ORIENTATION = "orientation";
	public static final String LANDSCAPE = "landscape";
	public static final String PORTRAIT = "portrait";
	public static final String COLUMN_SEPARATOR = "columnSeparator";
	public static final String TAB_SEPARATED = "tabSeparated";
	public static final String VARIABLE_NAME = "variableName";
	public static final String VALUE = "value";
	public static final String PREFINED_VALUES = "predefined";
	public static final String SQL_VALUES = "sql";

	// Replacement variable markers
	public static final String REP_VAR_START_MARKER = "\\|";
	public static final String REP_VAR_STOP_MARKER = "\\|";

	protected String name = "";
	protected String sql = null;
	protected String dbConnectionFile = null;
	protected DBConnection dbConnection = null;
	protected IFile file = null;

	protected QueryDefinition() {
		super();

	}

	public QueryDefinition(String file) throws Exception {
		this(new FileInputStream(file));
		setName(file);

	}

	public QueryDefinition(IFile file) throws Exception {
		this(file.getContents());
		setName(file.getName());
		this.file = file;
	}

	public QueryDefinition(InputStream stream) throws Exception {
		this(stream, QUERY_DEFINITION);

	}

	protected QueryDefinition(InputStream stream, String rootNodeName) throws Exception {
		super(stream, rootNodeName);

	}

	public QueryDefinition(Document document) {
		this(document, QUERY_DEFINITION);
	}

	protected QueryDefinition(Document document, String rootNodeName) {
		super(document, rootNodeName);
	}

	public String getName() {
		if (name != null && name.trim().length() > 0)
			return name;

		if (file != null) {
			try {
				return file.getName();
			} catch (Exception e) {
			}

		}

		return null;
	}

	public void setName(String name) {
		if (suspendUpdate)
			return;
		this.name = name;
	}

	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<String, Object>();
		Node executionNode = getExecutionNode();
		if (executionNode != null) {
			properties.put(COLUMN_HEADINGS, getAttribute(executionNode, COLUMN_HEADINGS));
			properties.put(DEFAULT_RUN_OPTION, getAttribute(executionNode, DEFAULT_RUN_OPTION));
		} else {
			properties.put(COLUMN_HEADINGS, COLUMN_HEADING_NAME);
			properties.put(DEFAULT_RUN_OPTION, EXPORT_TO_EXCEL);
		}

		return properties;

	}

	public String getSQL() {
		if (sql != null && sql.trim().length() > 0)
			return sql;

		return getSQLFromModel();
	}

	public String getSQLFromModel() {
		if (document == null)
			return "";

		return getCDATA(getSQLNode());

	}

	public void setSQL(String sql) {
		if (suspendUpdate)
			return;
		this.sql = sql;
	}

	public String getDefaultRunOption() {
		return (String) getProperties().get(DEFAULT_RUN_OPTION);

	}

	public List<String> getReplacementVariables() {
		List<String> sqlVars = new ArrayList<String>();
		try {
			StringMatcher matcher = new StringMatcher(REP_VAR_START_MARKER, REP_VAR_STOP_MARKER);
			sqlVars = matcher.findAll(getSQLFromModel());
		} catch (Exception e) {
		}

		return sqlVars;
	}

	public boolean hasReplacementVariables() {
		return getReplacementVariables().size() > 0;

	}

	public boolean hasReplacementVariable(String name) {
		return getReplacementVariables().contains(name);

	}

	public int countReplacementVariableDefs(String name) {
		List<String> definedVars = getDefinedVariables();

		int count = 0;
		for (String definedVar : definedVars) {
			if (definedVar.trim().equalsIgnoreCase(name.trim()))
				count++;
		}

		return count;

	}

	public List<String> getUndefinedVariables() {
		List<String> vars = getReplacementVariables();
		vars.removeAll(getDefinedVariables());
		return vars;

	}

	public List<String> getDefinedVariables() {
		List<String> definedVars = new ArrayList<String>();
		try {
			NodeList varNodeList = document.getDocumentElement().getElementsByTagName(REPLACEMENT_VAR);
			for (int i = 0; i < varNodeList.getLength(); i++) {
				Node node = varNodeList.item(i);
				definedVars.add(getAttribute(node, NAME));

			}
		} catch (Exception e) {
		}

		return definedVars;

	}

	public List<Node> getDefinedVariableNodes() {
		List<Node> definedVarNodes = new ArrayList<Node>();
		try {
			NodeList varNodeList = document.getDocumentElement().getElementsByTagName(REPLACEMENT_VAR);
			for (int i = 0; i < varNodeList.getLength(); i++) {
				definedVarNodes.add(varNodeList.item(i));
			}
		} catch (Exception e) {
		}

		return definedVarNodes;

	}

	public Node getSQLNode() {
		return getNode(SQL);

	}

	public Node getExecutionNode() {
		return getNode(EXECUTION);

	}

	public Node getExcelDefaultsNode(boolean create) {
		Node node = getNode(EXCEL_DEFAULTS);
		if (node == null && create)
			node = addNode(getDefaultsNode(create), EXCEL_DEFAULTS);

		return node;

	}

	public Node getPDFDefaultsNode(boolean create) {
		Node node = getNode(PDF_DEFAULTS);
		if (node == null && create)
			node = addNode(getDefaultsNode(create), PDF_DEFAULTS);

		return node;

	}

	public Node getCSVDefaultsNode(boolean create) {
		Node node = getNode(CSV_DEFAULTS);
		if (node == null && create)
			node = addNode(getDefaultsNode(create), CSV_DEFAULTS);

		return node;

	}
	
	public Node getDatabaseDefaultsNode(boolean create) {
		Node node = getNode(DATABASE_DEFAULTS);
		if (node == null && create)
			node = addNode(getDefaultsNode(create), DATABASE_DEFAULTS);

		return node;

	}

	public void setSQLInModel(String sql) {
		setCDATA(getSQLNode(), sql);
	}

	public String getColumnHeading() {
		return getAttribute(getExecutionNode(), COLUMN_HEADINGS);
	}

	public boolean getShowCommentAtRuntime() {
		try {
			return getAttributeBoolean(getCommentNode(), SHOW_COMMENTS_AT_RUNTIME);
		} catch (Exception e) {
		}

		return false;

	}

	public String getComment() {
		return getCDATA(getCommentNode());
	}

	public Node getSelectionDefaultsNode(boolean create) {
		Node node = getNode(SELECTION_DEFAULTS);
		if (node == null && create)
			node = addNode(getDefaultsNode(create), SELECTION_DEFAULTS);

		return node;

	}

	public Node getNewSelectionNode() {
		return addNode(getSelectionDefaultsNode(true), SELECTION);

	}

	public Node getDefaultsNode(boolean create) {
		Node node = getNode(DEFAULTS);
		if (node == null && create)
			node = addNode(getExecutionNode(), DEFAULTS);

		return node;

	}

	public Node getCommentNode() {
		return getNode(COMMENT);

	}

	public static String translateColumnHeading(final String fromString) {
		if (fromString.trim().equalsIgnoreCase(COLUMN_HEADING_NAME))
			return COLUMN_HEADING_NAME_TEXT;
		else if (fromString.trim().equalsIgnoreCase(COLUMN_HEADING_DESCRIPTION))
			return COLUMN_HEADING_DESCRIPTION_TEXT;
		else if (fromString.trim().equalsIgnoreCase(COLUMN_HEADING_BOTH))
			return COLUMN_HEADING_BOTH_TEXT;
		else if (fromString.trim().equalsIgnoreCase(COLUMN_HEADING_NAME_TEXT))
			return COLUMN_HEADING_NAME;
		else if (fromString.trim().equalsIgnoreCase(COLUMN_HEADING_DESCRIPTION_TEXT))
			return COLUMN_HEADING_DESCRIPTION;
		else if (fromString.trim().equalsIgnoreCase(COLUMN_HEADING_BOTH_TEXT))
			return COLUMN_HEADING_BOTH;

		return "";

	}

	public static String translateRunOption(final String fromString) {
		if (fromString.trim().equalsIgnoreCase(EXPORT_TO_CSV))
			return EXPORT_TO_CSV_TEXT;
		else if (fromString.trim().equalsIgnoreCase(EXPORT_TO_EXCEL))
			return EXPORT_TO_EXCEL_TEXT;
		else if (fromString.trim().equalsIgnoreCase(EXPORT_TO_PDF))
			return EXPORT_TO_PDF_TEXT;
		else if (fromString.trim().equalsIgnoreCase(EXPORT_TO_CSV_TEXT))
			return EXPORT_TO_CSV;
		else if (fromString.trim().equalsIgnoreCase(EXPORT_TO_EXCEL_TEXT))
			return EXPORT_TO_EXCEL;
		else if (fromString.trim().equalsIgnoreCase(EXPORT_TO_PDF_TEXT))
			return EXPORT_TO_PDF;

		return "";

	}

	public void resetDBConnection() {
		this.dbConnection = null;

	}

	public String getDBConnectionFile() {
		if (dbConnectionFile != null && dbConnectionFile.trim().length() > 0)
			return dbConnectionFile;

		return getDBConnectionFileFromModel();

	}

	public void setDBConnectionFile(String dbConnectionFile) {
		if (suspendUpdate)
			return;
		this.dbConnectionFile = dbConnectionFile;

	}

	public String getDBConnectionFileFromModel() {
		if (document == null)
			return "";

		return getAttribute(getSQLNode(), DB_CONNECTION_FILE);
	}

	public void setDBConnectionFileInModel(String file) {
		setAttribute(getSQLNode(), DB_CONNECTION_FILE, file);
	}

	public DBConnection getDBConnection() {
		return getDBConnection(true);
	}

	public DBConnection getDBConnection(boolean update) {
		if (dbConnection == null) {

			if (getDBConnectionFile() != null && getDBConnectionFile().trim().length() > 0) {
				try {
					IPath path = new Path(getDBConnectionFile());
					IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
					dbConnection = new DBConnection(file);
				} catch (Exception e) {
				}

				if (dbConnection == null) {
					try {
						dbConnection = new DBConnection(getDBConnectionFile());
					} catch (Exception e) {
					}
				}

			}
		}

		if (dbConnection == null) {
			try {
				dbConnection = getEmbeddedConnection();
			} catch (Exception e) {
			}
		}

		if (update && dbConnection == null && LicenseManager.isBrowserVersion()) {
			updateConnection();
			return getDBConnection(false);
		}

		return dbConnection;

	}

	public void setValuesSQL(Node node, String sql) {
		Node sqlValuesNode = getSQLValuesNode(node, true);
		setCDATA(sqlValuesNode, sql);
	}

	public Node[] getPredefValueNodes(Node parentNode, boolean create) {
		Node[] valueNodes = new Node[0];
		Node valuesNode = getValuesNode(parentNode, create);
		if (valuesNode != null) {
			NodeList list = getNodes(valuesNode, VALUE);
			if (list != null) {
				valueNodes = new Node[list.getLength()];
				for (int i = 0; i < valueNodes.length; i++) {
					valueNodes[i] = list.item(i);
				}
			}
		}

		return valueNodes;

	}

	public void addPredefValue(Node parentNode, String value) {
		Node valuesNode = getValuesNode(parentNode, true);
		Node newNode = addNode(valuesNode, VALUE);
		setText(newNode, value);

	}

	public Node getSQLValuesNode(Node parentNode, boolean create) {
		Node node = null;
		Node valuesNode = getValuesNode(parentNode, create);
		if (valuesNode != null) {
			node = getNode(valuesNode, SQL);
			if (node == null && create)
				node = addNode(valuesNode, SQL);
		}

		return node;

	}

	public Node getValuesNode(Node parentNode, boolean create) {
		Node node = getNode(parentNode, VALUES);
		if (node == null && create)
			node = addNode(parentNode, VALUES);

		return node;

	}

	public boolean isQuery() {
		return SQLHelper.isQuery(getSQL());
	}

	public boolean isSingleStatementQuery() {
		return SQLHelper.isSingleStatementQuery(getSQL());

	}

	public String getSQLType() {
		return SQLHelper.getSQLType(getSQL());
	}

	public IFile getFile() {
		return file;

	}

	public static String getCatalogKey() {
		return CATALOG_KEY;
	}

	public static QueryDefinition getModelFor(IFile file) {
		QueryDefinition model = null;

		// Try QueryDefinition
		try {
			model = new QueryDefinition(file);
			if (model.getRootElements().length > 0)
				return model;
		} catch (Exception e) {
		}

		// Try ScratchPad
		try {
			model = new SQLScratchPad(file);
			if (model.getRootElements().length > 0)
				return model;
		} catch (Exception e) {
		}

		return null;
	}

	public void setFile(IFile file) throws Exception {
		this.file = file;
		setName(file.getName());
		super.setFile(file);
	}

	public InputStream getInputStream() throws Exception {

		NodeList sqlNodes = getNodes(SQL);
		Node commentsNode = getNode(COMMENT);

		if (sqlNodes != null && sqlNodes.getLength() > 0) {
			for (int i = 0; i < sqlNodes.getLength(); i++) {
				Node sqlNode = sqlNodes.item(i);
				String sql = getCDATA(sqlNode);
				sql = sql.replaceAll("\r\n", "\n");
				setCDATA(sqlNode, sql);
			}
		}

		if (commentsNode != null) {
			String comments = getCDATA(commentsNode);
			comments = comments.replaceAll("\r\n", "\n");
			setCDATA(commentsNode, comments);
		}

		return super.getInputStream();

	}

	public static boolean isQueryDefinitionFile(Object resource) {
		if (resource instanceof IFile) {
			try {
				IContentType ct = IDE.getContentType((IFile) resource);
				if (ct.getId().equals("com.ss.speedtransfer.queryContentType") || ct.getId().equals("com.ss.speedtransfer.scratchPadContentType"))
					return true;
			} catch (Exception e) {
			}

		}

		return false;
	}

	protected void updateConnection() {

		IFile file = getFile();
		if (file == null)
			return;

		if (file.isLinked()) {
			UIHelper.instance().showMessage("Connection file not found",
					"The connection file: " + getDBConnectionFile() + " could not be found." + StringHelper.getNewLine() + StringHelper.getNewLine() + "Unable to run query.");
			return;
		}

		int response = UIHelper.instance().showMessageWithOKCancel("Connection file not found",
				"The connection file: " + getDBConnectionFile() + " could not be found." + StringHelper.getNewLine() + StringHelper.getNewLine() + "Would you like to select a new connection file?");
		if (response == SWT.OK) {
			IWorkbenchWindow window = UIHelper.instance().getActiveWindow();
			if (window != null) {
				DBConnectionFileSelectionDialog dialog = new DBConnectionFileSelectionDialog(window);
				dialog.open();
				if (dialog.getSelectedObject() != null || dialog.getSelectedObject() instanceof IFile) {
					IPath path = ((IFile) dialog.getSelectedObject()).getFullPath();
					saveConnectionFile(path.toOSString());

				}
			}
		}

	}

	protected void saveConnectionFile(String filePath) {
		try {
			setDBConnectionFileInModel(filePath);
			if (getFile() != null)
				getFile().setContents(getInputStream(), true, false, null);

		} catch (Exception e) {
			UIHelper.instance().showErrorMsg("Error", "Error when saving connection file. Error: " + SSUtil.getMessage(e));
		}

		UIHelper.instance().showMessage("Information", "Connection file updated");

	}

	public Document embedConnection() throws Exception {
		try {
			if (getEmbeddedConnectionNode() != null)
				remove(getEmbeddedConnectionNode());

			Node conNode = getDBConnection(false).getConnectionNode().cloneNode(true);
			Node rootNode = getRootElements()[0];
			rootNode.appendChild(getDocument().adoptNode(conNode));
			return getDocument();
		} catch (Exception e) {
			throw new Exception("Failed to embed connection");
		}

	}

	public DBConnection getEmbeddedConnection() throws Exception {

		Node conNode = getEmbeddedConnectionNode();
		if (conNode == null)
			return null;

		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document conDoc = dBuilder.newDocument();

		Element rootNode = conDoc.createElement(DBConnection.DB_CONNECTION_DEF);
		conDoc.appendChild(rootNode);

		rootNode.appendChild(conDoc.adoptNode(conNode.cloneNode(true)));

		return new DBConnection(conDoc);

	}

	public Node getEmbeddedConnectionNode() {
		return getNode(DBConnection.CONNECTION);

	}

}
