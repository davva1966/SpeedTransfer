package com.ss.speedtransfer.ui.editor.querydef;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.w3c.dom.Node;

import com.ss.speedtransfer.model.QueryDefinition;
import com.ss.speedtransfer.ui.view.QueryExcelResultView;
import com.ss.speedtransfer.ui.view.QueryResultView;
import com.ss.speedtransfer.util.DBConnectionResourceListener;
import com.ss.speedtransfer.util.SSUtil;
import com.ss.speedtransfer.util.ReplacementVariableTranslator;
import com.ss.speedtransfer.util.SQLWarningDialog;
import com.ss.speedtransfer.util.StringHelper;
import com.ss.speedtransfer.util.parser.sql.SQLParser;
import com.ss.speedtransfer.util.parser.sql.SQLParserTokenManager;
import com.ss.speedtransfer.util.parser.sql.SimpleCharStream;
import com.ss.speedtransfer.xml.editor.XMLFormEditor;
import com.ss.speedtransfer.xml.editor.XMLFormPageBlock;
import com.ss.speedtransfer.xml.editor.XMLModel;
import com.ss.speedtransfer.xml.editor.XMLPartActivationListener;


public class QueryDefinitonEditor extends XMLFormEditor {

	public static final String ID = "com.ss.speedtransfer.queryEditor";
	private XMLPartActivationListener activationListener;
	protected DBConnectionResourceListener dbConnectionListener;
	protected boolean validated = false;

	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		try {
			super.init(site, input);
			// we want to listen for our own activation
			activationListener = new QueryDefintionEditorActivationListener(this, site.getWorkbenchWindow().getPartService());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void setInput(IEditorInput input) {
		super.setInput(input);
		setPartName(input.getName());
		getQueryDefinition().setName(input.getName());

		// Listen for changes to the db connection file to refresh this editor
		// if needed
		if (dbConnectionListener != null)
			dbConnectionListener.dispose();
		dbConnectionListener = new DBConnectionResourceListener(getQueryDefinition());

	}

	protected XMLModel createModel(IFile file) throws Exception {
		return new QueryDefinition(file);
	}

	protected XMLFormPageBlock createBlock() {
		return new QueryDefinitionFormPageBlock(this, (QueryDefinition) model);
	}

	public void dispose() {
		if (dbConnectionListener != null)
			dbConnectionListener.dispose();
		activationListener.dispose();
		super.dispose();

	}

	public String getRootNodeName() {
		return QueryDefinition.QUERY_DEFINITION;
	}

	public void partActivated() {
		String id = getQueryDefinition().getName();
		if (getQueryDefinition().getFile() == null) {
			getQueryDefinition().setName("SQL Result");
			id = getQueryDefinition().getName();
		}

		try {
			IViewReference viewRef = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findViewReference(QueryResultView.ID, id);
			if (viewRef != null) {
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(QueryResultView.ID, id, IWorkbenchPage.VIEW_VISIBLE);
			} else {
				viewRef = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findViewReference(QueryExcelResultView.ID, id);
				if (viewRef != null)
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(QueryExcelResultView.ID, id, IWorkbenchPage.VIEW_VISIBLE);
			}

		} catch (Exception e) {
		}
	}

	public QueryDefinition getQueryDefinition() {
		return (QueryDefinition) model;
	}

	protected void performSave(boolean overwrite, IProgressMonitor progressMonitor) {
		getQueryDefinition().getSQL();
		if (validated || validate())
			super.performSave(overwrite, progressMonitor);
		validated = false;
	}

	protected void performSaveAs(IProgressMonitor progressMonitor) {
		if (validate())
			super.performSaveAs(progressMonitor);
		validated = false;
	}

	protected boolean validate() {
		String message = null;

		List<String> undefinedvars = getQueryDefinition().getUndefinedVariables();
		if (undefinedvars.size() > 0)
			message = "Undefined replacement variables found in SQL string. All variables must be defined before running this query.";

		if (message == null) {
			if (getQueryDefinition().getSQLFromModel().trim().length() == 0)
				message = "No sql string defined for this query definition.";
		}

		if (message == null) {

			String sqlEmpty = getQueryDefinition().getSQLFromModel();
			String sqlFull = getQueryDefinition().getSQLFromModel();

			if (getQueryDefinition().hasReplacementVariables()) {
				List<Node> usedVariableNodes = getQueryDefinition().getDefinedVariableNodes();
				List<String> usedVariables = getQueryDefinition().getReplacementVariables();

				Map<String, String> emptyValueMap = new HashMap<String, String>();
				Map<String, String> fullValueMap = new HashMap<String, String>();
				List<String> excludeIfEmptyVars = new ArrayList<String>();

				for (Node varNode : usedVariableNodes) {

					String varName = getQueryDefinition().getAttribute(varNode, QueryDefinition.NAME);
					if (usedVariables.contains(varName) == false)
						continue;

					boolean excludeIfEmpty = getQueryDefinition().getAttributeBoolean(varNode, QueryDefinition.EXCLUDE_IF_EMPTY);
					if (excludeIfEmpty) {
						excludeIfEmptyVars.add(varName);
						emptyValueMap.put(varName, "");
					}

					String type = getQueryDefinition().getAttribute(varNode, QueryDefinition.TYPE);
					if (type.equals(QueryDefinition.STRING))
						fullValueMap.put(varName, "A");
					else
						fullValueMap.put(varName, "0");

				}

				ReplacementVariableTranslator translator = new ReplacementVariableTranslator(fullValueMap, excludeIfEmptyVars);
				sqlFull = translator.translateSQL(sqlFull);

				if (message == null)
					message = parseSQL(sqlFull, false);

				if (message == null && excludeIfEmptyVars.size() > 0) {
					translator = new ReplacementVariableTranslator(emptyValueMap, excludeIfEmptyVars);
					sqlEmpty = translator.translateSQL(sqlEmpty);
					message = parseSQL(sqlEmpty, true);
				}

			}

		}

		if (message != null) {
			SQLWarningDialog dialog = new SQLWarningDialog("The SQL could potentially have errors." + StringHelper.getNewLine() + StringHelper.getNewLine() + "Do you want to save the query anyway?",
					message);
			int response = dialog.open();
			if (response == 0) {
				validated = true;
				return true;
			} else {
				return false;
			}
		}

		validated = true;
		return true;
	}

	protected String parseSQL(String sql, boolean empty) {
		try {
			ByteArrayInputStream in = new ByteArrayInputStream(sql.getBytes());
			SimpleCharStream stream = new SimpleCharStream(in, 1, 1);
			SQLParserTokenManager lexer = new SQLParserTokenManager(stream);
			SQLParser parser = new SQLParser(lexer);
			parser.Statement();
		} catch (Exception e) {
			StringBuilder sb = new StringBuilder();
			if (empty) {
				sb.append("Invalid SQL. Either the sql is invalid or omitting all replacement variables that has the \"Exclude if empty\" attribute set will make the sql invalid. ");
				sb.append("The sql string that will be generated in this case is displayed below: ");
				sb.append(StringHelper.getNewLine());
				sb.append(StringHelper.getNewLine());
				sb.append(sql);
			} else {
				sb.append("Invalid SQL. The specified sql string is invalid.");
			}
			sb.append(StringHelper.getNewLine());
			sb.append(StringHelper.getNewLine());
			sb.append("SQL Parser Error:");
			sb.append(StringHelper.getNewLine());
			sb.append(StringHelper.getNewLine());
			sb.append(SSUtil.getMessage(e));
			sb.append(StringHelper.getNewLine());
			return sb.toString();
		}

		return null;
	}

}
