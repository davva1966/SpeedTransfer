package com.ss.speedtransfer.util;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.progress.IProgressService;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ss.speedtransfer.model.QueryDefinition;
import com.ss.speedtransfer.model.SQLScratchPad;


public class ReplacementVariableTranslatorPrompt {

	protected QueryDefinition queryDef;
	protected Connection connection;

	protected Button okButton;
	protected boolean okPressed = false;
	protected boolean cancelled = false;

	protected Map<String, Control> replacementValueControls = new HashMap<String, Control>();
	protected Map<Control, String[]> errorControls = new HashMap<Control, String[]>();

	protected Map<String, String> valueMap = new HashMap<String, String>();
	protected List<String> excludeIfEmptyVars = new ArrayList<String>();

	protected Map<Combo, Node> comboVarMap = new HashMap<Combo, Node>();
	protected Map<Node, String[]> comboValuesMap = new HashMap<Node, String[]>();

	protected boolean useEclipse = false;
	protected boolean canSaveDefaults = false;
	protected Statement selectableValuesStmt = null;

	FormToolkit toolkit;
	ScrolledForm form;

	public boolean isCancelled() {
		return cancelled;
	}

	public ReplacementVariableTranslatorPrompt(QueryDefinition queryDef, Connection con) {
		super();
		this.queryDef = queryDef;
		this.connection = con;
//		useEclipse = false;
//		try {
//			if (PlatformUI.isWorkbenchRunning())
//				useEclipse = true;
//		} catch (Exception e) {
//		}
		useEclipse = EnvironmentHelper.isExecutableEnvironment() == false;
		if (useEclipse == false) {
			canSaveDefaults = false;
		} else {
			canSaveDefaults = (queryDef instanceof SQLScratchPad == false);
		}
	}

	public void run() {
		if (queryDef.hasReplacementVariables()) {
			prompt();
			if (isCancelled() == false) {
				if (okPressed)
					translateSQL();
				else
					cancelled = true;
			}
		} else {
			cancelled = false;
		}
		if (toolkit != null)
			toolkit.dispose();
	}

	protected void prompt() {

		Shell shell = UIHelper.instance().getActiveShell();
		final Shell settingsShell = new Shell(shell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE);

		settingsShell.setText("Query Value Selection");
		settingsShell.setSize(500, 200);
		settingsShell.setLayout(new FillLayout());
		
		UIHelper.instance().centerInParent(shell, settingsShell);
		
		settingsShell.setLayout(new FillLayout());

		toolkit = new FormToolkit(settingsShell.getDisplay());

		Composite comp = toolkit.createComposite(settingsShell);
		GridLayout layout = new GridLayout(3, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		comp.setLayout(layout);
		toolkit.adapt(comp);

		form = toolkit.createScrolledForm(comp);

		try {
			form.setBackgroundImage(UIHelper.instance().getImageDescriptor("form_banner.gif").createImage());
		} catch (Exception e) {
		}

		form.setText("Set values to use for the query");
		toolkit.decorateFormHeading(form.getForm());

		form.getBody().setLayout(new GridLayout(2, false));
		form.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));

		List<Node> usedVariableNodes = queryDef.getDefinedVariableNodes();

		List<String> usedVariables = queryDef.getReplacementVariables();

		for (Node varNode : usedVariableNodes) {

			String varName = queryDef.getAttribute(varNode, QueryDefinition.NAME);

			if (usedVariables.contains(varName) == false)
				continue;

			String varDesc = queryDef.getAttribute(varNode, QueryDefinition.DESCRIPTION);
			String varType = queryDef.getAttribute(varNode, QueryDefinition.TYPE);
			String defaultValue = queryDef.getAttribute(varNode, QueryDefinition.DEFAULT_VALUE);
			boolean mandatory = queryDef.getAttributeBoolean(varNode, QueryDefinition.MANDATORY);
			boolean excludeIfEmpty = queryDef.getAttributeBoolean(varNode, QueryDefinition.EXCLUDE_IF_EMPTY);

			if (excludeIfEmpty)
				excludeIfEmptyVars.add(varName);

			if (varType == null || varType.trim().length() == 0)
				varType = QueryDefinition.STRING;

			Label label = toolkit.createLabel(form.getBody(), varDesc + ": ", SWT.NONE);
			label.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));

			Control replacementValueControl = null;

			// String
			if (varType.trim().equalsIgnoreCase(QueryDefinition.STRING) || varType.trim().equalsIgnoreCase(QueryDefinition.NUMERIC)) {
				final Text textInput = toolkit.createText(form.getBody(), "", SWT.SINGLE);
				if (defaultValue != null && defaultValue.trim().length() > 0)
					textInput.setText(defaultValue);
				textInput.addModifyListener(new ModifyListener() {
					public void modifyText(ModifyEvent e) {
						validateInput(textInput);
						validatePage();

					}
				});
				replacementValueControl = textInput;
			}

			// Date
			if (varType.trim().equalsIgnoreCase(QueryDefinition.DATE)) {
				replacementValueControl = new DateTime(form.getBody(), SWT.BORDER | SWT.DATE | SWT.DROP_DOWN);
				toolkit.adapt((DateTime) replacementValueControl);
			}

			// Time
			if (varType.trim().equalsIgnoreCase(QueryDefinition.TIME)) {
				replacementValueControl = new DateTime(form.getBody(), SWT.TIME | SWT.SHORT);
				toolkit.adapt((DateTime) replacementValueControl);
			}

			// List of values
			if (varType.trim().equalsIgnoreCase(QueryDefinition.VALUE_LIST)) {
				final Combo combo = new Combo(form.getBody(), SWT.READ_ONLY);
				comboVarMap.put(combo, varNode);
				toolkit.adapt(combo);
				combo.addModifyListener(new ModifyListener() {
					public void modifyText(ModifyEvent e) {
						validateInput(combo);
						validatePage();

					}
				});
				replacementValueControl = combo;
			}

			replacementValueControl.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

			replacementValueControl.setData(QueryDefinition.NAME, varName);
			replacementValueControl.setData(QueryDefinition.TYPE, varType);
			replacementValueControl.setData(QueryDefinition.DEFAULT_VALUE, defaultValue);
			replacementValueControl.setData(QueryDefinition.MANDATORY, mandatory);
			validateInput(replacementValueControl);

			replacementValueControls.put(varName, replacementValueControl);

		}

		// OK button
		okButton = toolkit.createButton(comp, "OK", SWT.PUSH);
		GridData gd = new GridData(SWT.LEFT, SWT.TOP, false, false);
		gd.widthHint = 100;
		okButton.setLayoutData(gd);
		okButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				cancelled = false;
				okPressed = true;
				createValueMap();
				settingsShell.close();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});

		// Cancel button
		Button cancelButton = toolkit.createButton(comp, "Cancel", SWT.PUSH);
		gd = new GridData(SWT.LEFT, SWT.TOP, false, false);
		gd.widthHint = 100;
		cancelButton.setLayoutData(gd);
		cancelButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				cancelled = true;
				settingsShell.close();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});

		// Set as default button
		if (canSaveDefaults) {
			Button setDefaultButton = toolkit.createButton(comp, "Save as default", SWT.PUSH);
			setDefaultButton.setToolTipText("Save these settings as the default settings for this query");
			gd = new GridData(SWT.RIGHT, SWT.TOP, false, false);
			gd.widthHint = 100;
			setDefaultButton.setLayoutData(gd);
			setDefaultButton.addSelectionListener(new SelectionListener() {
				public void widgetSelected(SelectionEvent e) {
					try {
						saveDefaults();
					} catch (Exception e2) {
						UIHelper.instance().showErrorMsg(settingsShell, "Error", "Failed to set defaults. Error: " + SSUtil.getMessage(e2));
					}

				}

				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});
		}

		Label dummy = toolkit.createLabel(comp, "");
		gd = new GridData(SWT.LEFT, SWT.TOP, false, false, 3, 1);
		gd.heightHint = 3;
		dummy.setLayoutData(gd);
		
		Point shellSize = settingsShell.computeSize(SWT.DEFAULT, (replacementValueControls.size() * 27) + 80);
		int height = Math.min(shellSize.y, 500);
		settingsShell.setSize(500, height);
		
		UIHelper.instance().centerInParent(shell, settingsShell);

		settingsShell.setDefaultButton(okButton);

		validatePage();

		fillCombos(settingsShell);
		if (cancelled) {
			settingsShell.close();
			return;
		}

		applyDefaults();

		// Open shell
		settingsShell.open();

		Display display = Display.getDefault();
		while (!settingsShell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}

	}

	protected void validatePage() {
		okButton.setEnabled(true);

		clearAllMessages(form);
		for (Control control : errorControls.keySet()) {
			String type = errorControls.get(control)[0];
			String msg = errorControls.get(control)[1];
			if (type.equals("INFO"))
				issueMessage(form, null, IMessageProvider.INFORMATION, msg, control);
			else if (type.equals("WARN"))
				issueMessage(form, null, IMessageProvider.WARNING, msg, control);
			else if (type.equals("ERROR"))
				issueMessage(form, null, IMessageProvider.ERROR, msg, control);
			okButton.setEnabled(false);
		}

	}

	protected Map<String, String> createValueMap() {
		valueMap = new HashMap<String, String>();

		for (String varName : replacementValueControls.keySet()) {
			Control control = replacementValueControls.get(varName);
			String value = getText(control);
			valueMap.put(varName, value);
		}

		return valueMap;

	}

	public static String getText(Control control) {
		String value = "";
		if (control instanceof Text) {
			value = ((Text) control).getText();
		} else if (control instanceof Combo) {
			value = ((Combo) control).getText();
		} else if (control instanceof DateTime) {
			DateTime dateTime = (DateTime) control;
			String varType = (String) dateTime.getData(QueryDefinition.TYPE);
			StringBuilder sb = new StringBuilder();
			if (varType.equalsIgnoreCase(QueryDefinition.DATE)) {
				sb.append(dateTime.getYear());
				if (dateTime.getMonth() + 1 < 10)
					sb.append("0");
				sb.append(dateTime.getMonth() + 1);
				if (dateTime.getDay() < 10)
					sb.append("0");
				sb.append(dateTime.getDay());
			} else if (varType.equalsIgnoreCase(QueryDefinition.TIME)) {
				if (dateTime.getHours() < 10)
					sb.append("0");
				sb.append(dateTime.getHours());
				if (dateTime.getMinutes() < 10)
					sb.append("0");
				sb.append(dateTime.getMinutes());
				if (dateTime.getSeconds() < 10)
					sb.append("0");
				sb.append(dateTime.getSeconds());
			}
			value = sb.toString();
		}

		return value.trim();
	}

	public static void setText(Control control, String text) {
		if (text != null)
			text = text.trim();
		if (control instanceof Text) {
			((Text) control).setText(text);
		} else if (control instanceof Combo) {
			((Combo) control).setText(text);
		} else if (control instanceof DateTime) {
			DateTime dateTime = (DateTime) control;
			String varType = (String) dateTime.getData(QueryDefinition.TYPE);

			String yearStr = null;
			String monthStr = null;
			String dayStr = null;

			if (text.length() >= 4)
				yearStr = text.substring(0, 4);
			if (text.length() >= 6)
				monthStr = text.substring(4, 6);
			if (text.length() >= 8)
				dayStr = text.substring(6, 8);

			if (varType.equalsIgnoreCase(QueryDefinition.DATE)) {
				if (yearStr != null)
					dateTime.setYear(Integer.parseInt(yearStr));
				if (monthStr != null)
					dateTime.setMonth(Integer.parseInt(monthStr) - 1);
				if (dayStr != null)
					dateTime.setDay(Integer.parseInt(dayStr));

			} else if (varType.equalsIgnoreCase(QueryDefinition.TIME)) {
				String hourStr = null;
				String minuteStr = null;
				String secondStr = null;

				if (text.length() >= 2)
					hourStr = text.substring(0, 2);
				if (text.length() >= 4)
					minuteStr = text.substring(2, 4);
				if (text.length() >= 6)
					secondStr = text.substring(4, 6);

				if (hourStr != null)
					dateTime.setHours(Integer.parseInt(hourStr));
				if (minuteStr != null)
					dateTime.setMinutes(Integer.parseInt(minuteStr));
				if (secondStr != null)
					dateTime.setSeconds(Integer.parseInt(secondStr));

			}

		}

	}

	protected void validateInput(Control control) {
		errorControls.remove(control);
		String varType = (String) control.getData(QueryDefinition.TYPE);
		boolean mandatory = (Boolean) control.getData(QueryDefinition.MANDATORY);

		if (control instanceof Combo) {
			Combo input = (Combo) control;

			if (mandatory && input.getText().trim().length() == 0) {
				errorControls.put(input, new String[] { "INFO", "Value must be entered" });
				return;
			}
		}

		if (control instanceof Text) {
			Text input = (Text) control;

			if (mandatory && input.getText().trim().length() == 0) {
				errorControls.put(input, new String[] { "INFO", "Value must be entered" });
				return;
			}

			if (varType.equalsIgnoreCase(QueryDefinition.NUMERIC)) {
				if (input.getText().trim().length() > 0) {
					try {
						new Double(input.getText().trim());
					} catch (Exception exc) {
						errorControls.put(input, new String[] { "ERROR", "Invalid numeric value entered" });
					}
				}
			}
		}
	}

	protected void translateSQL() {

		ReplacementVariableTranslator translator = new ReplacementVariableTranslator(valueMap, excludeIfEmptyVars);
		String sql = queryDef.getSQLFromModel();

		String newSQL = translator.translateSQL(sql);
		queryDef.setSQL(newSQL);

	}

	protected void saveDefaults() {
		try {
			Element defaultsNode = (Element) queryDef.getSelectionDefaultsNode(true);
			if (defaultsNode == null)
				return;

			NodeList selectionNodes = defaultsNode.getElementsByTagName(QueryDefinition.SELECTION);
			Node[] nodesToRemoove = new Node[selectionNodes.getLength()];
			for (int i = 0; i < selectionNodes.getLength(); i++)
				nodesToRemoove[i] = selectionNodes.item(i);
			for (Node selection : nodesToRemoove)
				defaultsNode.removeChild(selection);

			for (String key : replacementValueControls.keySet()) {
				Control control = replacementValueControls.get(key);
				Element selectionNode = (Element) queryDef.getNewSelectionNode();
				selectionNode.setAttribute(QueryDefinition.VARIABLE_NAME, (String) control.getData(QueryDefinition.NAME));
				selectionNode.setAttribute(QueryDefinition.TYPE, (String) control.getData(QueryDefinition.TYPE));
				selectionNode.setAttribute(QueryDefinition.VALUE, getText(control));
			}

			if (queryDef.getFile() != null)
				queryDef.getFile().setContents(queryDef.getInputStream(), true, false, null);

		} catch (Exception e) {
			UIHelper.instance().showErrorMsg("Error", "Error when saving defaults. Error: " + SSUtil.getMessage(e));
		}

		UIHelper.instance().showMessage("Information", "Defaults saved");

	}

	protected boolean applyDefaults() {
		Element defaultsNode = (Element) queryDef.getSelectionDefaultsNode(false);
		if (defaultsNode == null)
			return false;

		NodeList selectionNodes = defaultsNode.getElementsByTagName(QueryDefinition.SELECTION);
		for (int i = 0; i < selectionNodes.getLength(); i++) {
			Element selection = (Element) selectionNodes.item(i);
			String varName = selection.getAttribute(QueryDefinition.VARIABLE_NAME);
			String value = selection.getAttribute(QueryDefinition.VALUE);
			Control control = replacementValueControls.get(varName);
			if (control != null)
				setText(control, value);
		}

		return true;

	}

	protected void issueMessage(ScrolledForm form, String key, int type, String message) {
		issueMessage(form, key, type, message, null);
	}

	protected void issueMessage(ScrolledForm form, String key, int type, String message, Control control) {
		if (control == null)
			form.getMessageManager().addMessage(key, message, null, type);
		else
			form.getMessageManager().addMessage(key, message, null, type, control);
	}

	protected void clearAllMessages(ScrolledForm form) {
		form.getMessageManager().removeAllMessages();
	}

	protected void clearMessage(ScrolledForm form, String key) {
		clearMessage(form, key, null);
	}

	protected void clearMessage(ScrolledForm form, String key, Control control) {
		if (control == null)
			form.getMessageManager().removeMessage(key);
		else
			form.getMessageManager().removeMessage(key, control);
	}

	protected void fillCombos(Shell shell) {
		if (comboVarMap.size() == 0)
			return;

		try {
			if (useEclipse)
				runGetComboValuesEclipseJob(shell);
			else
			runGetComboValuesJob(shell);
		} catch (InvocationTargetException e) {
			UIHelper.instance().showErrorMsg("Error", SSUtil.getMessage(e));
			cancelled = true;
		} catch (InterruptedException e) {
			cancelled = true;
		}

	}

	protected void loadComboValues(Node varNode, IProgressMonitor monitor) throws SQLException {

		Node valuesNode = queryDef.getValuesNode(varNode, false);
		String valuesType = queryDef.getAttribute(valuesNode, QueryDefinition.VALUES_TYPE);
		String varDesc = queryDef.getAttribute(varNode, QueryDefinition.DESCRIPTION);
		boolean mandatory = queryDef.getAttributeBoolean(varNode, QueryDefinition.MANDATORY);

		List<String> items = new ArrayList<String>();
		if (!mandatory)
			items.add("");

		if (valuesType.equalsIgnoreCase(QueryDefinition.PREFINED_VALUES)) {
			Node[] predefvalues = queryDef.getPredefValueNodes(varNode, false);
			for (int i = 0; i < predefvalues.length; i++) {
				items.add(queryDef.getText(predefvalues[i]).trim());
			}
			comboValuesMap.put(varNode, items.toArray(new String[0]));

		} else {
			Node sqlNode = queryDef.getSQLValuesNode(varNode, false);
			if (sqlNode != null) {
				String sql = queryDef.getCDATA(sqlNode);
				try {
					if (monitor != null)
						monitor.subTask("Retrieving selectable values for " + varDesc + "...");
					selectableValuesStmt = connection.createStatement();
					ResultSet rs = selectableValuesStmt.executeQuery(sql);
					int cancelCheckCounter = 0;
					while (rs.next()) {

						items.add(rs.getString(1).trim());

						if (monitor != null) {
							if (cancelCheckCounter >= 50) {
								if (monitor.isCanceled()) {
									cancelled = true;
									break;
								}
								cancelCheckCounter = 0;
							}
						}
					}
					if (!cancelled)
						comboValuesMap.put(varNode, items.toArray(new String[0]));
				} catch (SQLException e) {
					throw new SQLException("Error when executing selectable values query for " + varDesc + ". Error: " + SSUtil.getMessage(e));
				} finally {
					try {
						if (selectableValuesStmt != null)
							selectableValuesStmt.close();
					} catch (Exception e2) {
					}
				}

			}

		}

	}

	protected void runGetComboValuesEclipseJob(Shell shell) throws InvocationTargetException, InterruptedException {
		IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
		progressService.busyCursorWhile(new IRunnableWithProgress() {
			public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				getComboValues(monitor);
			}
		});

	}

	protected void runGetComboValuesJob(Shell shell) throws InvocationTargetException, InterruptedException {
		ProgressMonitorDialog pmd = new ProgressMonitorDialog(shell);
		pmd.run(true, true, new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				getComboValues(monitor);
			}

		});
	}

	protected void getComboValues(IProgressMonitor monitor) throws InvocationTargetException {
		monitor.beginTask("Retrieving selectable values...", IProgressMonitor.UNKNOWN);
		try {
			for (final Combo combo : comboVarMap.keySet()) {
				final Node varNode = comboVarMap.get(combo);
				loadComboValues(varNode, monitor);
				if (monitor.isCanceled()) {
					cancelled = true;
					break;
				}
				monitor.worked(1);

				String varName = queryDef.getAttribute(varNode, QueryDefinition.NAME);
				final String defaultValue = queryDef.getAttribute(varNode, QueryDefinition.DEFAULT_VALUE);

				monitor.subTask("Setting selectable values for " + varName + "...");
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						combo.setItems(comboValuesMap.get(varNode));
						if (defaultValue != null && defaultValue.trim().length() > 0)
							combo.setText(defaultValue);
					}
				});
				if (monitor.isCanceled()) {
					cancelled = true;
					break;
				}
				monitor.worked(1);
			}

		} catch (Exception e) {
			throw new InvocationTargetException(e);
		} finally {
			monitor.done();
		}
	}

}
