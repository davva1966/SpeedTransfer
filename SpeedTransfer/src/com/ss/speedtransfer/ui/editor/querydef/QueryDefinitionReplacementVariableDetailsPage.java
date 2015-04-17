package com.ss.speedtransfer.ui.editor.querydef;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.TextEvent;
import org.eclipse.jface.text.TextViewerUndoManager;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.operations.UndoActionHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.ss.speedtransfer.SpeedTransferPlugin;
import com.ss.speedtransfer.actions.FormatSQLAction;
import com.ss.speedtransfer.actions.OpenSQLBuilderWizardAction;
import com.ss.speedtransfer.actions.RunSQLToTableAction;
import com.ss.speedtransfer.handlers.TextOperationHandler;
import com.ss.speedtransfer.model.QueryDefinition;
import com.ss.speedtransfer.ui.StyledEdit;
import com.ss.speedtransfer.util.ItemSelectionDialog;
import com.ss.speedtransfer.util.UIHelper;
import com.ss.speedtransfer.util.sql.SQLConfiguration;
import com.ss.speedtransfer.xml.editor.XMLModelListener;


public class QueryDefinitionReplacementVariableDetailsPage extends AbstractQueryDefinitionDetailsPage {

	protected FormToolkit toolkit;
	protected QueryDefinitionFormPageBlock block;

	protected Section valuesSect;
	protected Text variableName;
	protected Text variableDescription;
	protected Combo typeCombo;
	protected Text defaultValue;
	protected Button mandatoryButton;
	protected Button excludeIfEmptyButton;

	protected Button predefValuesButton;
	protected Button sqlValuesButton;
	protected Composite sqlValuesComposite;
	protected StyledEdit sqlEdit;
	protected Composite predefValuesComposite;
	protected Composite valuesClient;
	protected TableViewer predefValuesTableViewer;

	protected IAction actionFormatSQL;
	protected IAction actionOpenSQLBuilderWizard;
	protected IAction runSQLAction;

	protected IAction undoAction;
	protected IAction cutAction;
	protected IAction copyAction;
	protected IAction pasteAction;
	protected IAction deleteAction;

	protected Button buttonAdd;
	protected Button buttonUp;
	protected Button buttonDown;
	protected Button buttonRemove;

	protected String savedValuesType = "";

	protected class TextEditingSupport extends EditingSupport {

		private final TableViewer viewer;

		public TextEditingSupport(TableViewer viewer) {
			super(viewer);
			this.viewer = viewer;
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			return new TextCellEditor(viewer.getTable());
		}

		@Override
		protected boolean canEdit(Object element) {
			return true;
		}

		@Override
		protected Object getValue(Object element) {
			if (element instanceof Node) {
				return getText((Node) element);
			}
			return "";
		}

		@Override
		protected void setValue(Object element, Object value) {
			if (element instanceof Node)
				setText((Node) element, (String) value);

		}
	}

	public QueryDefinitionReplacementVariableDetailsPage(QueryDefinition model, QueryDefinitionFormPageBlock block) {
		super(model);
		this.block = block;
	}

	public void createContents(Composite parent) {
		toolkit = mform.getToolkit();

		GridLayout layout = new GridLayout();
		layout.marginHeight = 10;
		parent.setLayout(layout);

		Section variableSect = toolkit.createSection(parent, Section.TITLE_BAR | Section.TWISTIE);
		variableSect.marginWidth = 10;
		variableSect.setExpanded(true);
		variableSect.setText("Replacement Variable");
		variableSect.clientVerticalSpacing = 10;

		GridData gd = new GridData(SWT.FILL, GridData.VERTICAL_ALIGN_BEGINNING, true, false);

		Composite variableClient = toolkit.createComposite(variableSect);
		GridLayout gl = new GridLayout(3, false);
		gl.verticalSpacing = 10;
		variableClient.setLayout(gl);
		variableSect.setClient(variableClient);
		variableSect.setLayoutData(gd);

		createVariableArea(variableClient);

		valuesSect = toolkit.createSection(parent, Section.TITLE_BAR | Section.TWISTIE);
		valuesSect.marginWidth = 10;
		valuesSect.setExpanded(true);
		valuesSect.setText("Values");

		gd = new GridData(SWT.FILL, SWT.FILL, true, true);

		valuesClient = toolkit.createComposite(valuesSect);
		gl = new GridLayout(2, false);
		valuesClient.setLayout(gl);
		valuesSect.setClient(valuesClient);
		valuesSect.setLayoutData(gd);

		createValuesArea(valuesClient);

		createActions();
		createPopupMenu();
		createSectionToolbar(valuesSect);

		toolkit.paintBordersFor(variableClient);
		toolkit.paintBordersFor(valuesClient);

		validate();

	}

	protected void createVariableArea(Composite baseSection) {

		GridData gd = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
		gd.widthHint = 10;
		gd.horizontalIndent = 7;

		toolkit.createLabel(baseSection, "Variable");
		variableName = toolkit.createText(baseSection, "", SWT.SINGLE);
		variableName.setLayoutData(gd);
		variableName.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				setAttributeValue(QueryDefinition.NAME, variableName.getText());
				model.fireModelChanged(null, XMLModelListener.STRUCTURE_CHANGED, null);
				validate();
			}
		});

		gd = new GridData();
		gd.heightHint = 21;

		Button promptButton = toolkit.createButton(baseSection, "Browse" + "...", SWT.PUSH); //$NON-NLS-1$
		promptButton.setLayoutData(gd);
		promptButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				selectVariable();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		gd = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
		gd.widthHint = 10;
		gd.horizontalSpan = 2;
		gd.horizontalIndent = 7;

		toolkit.createLabel(baseSection, "Description");
		variableDescription = toolkit.createText(baseSection, "", SWT.SINGLE);
		variableDescription.setLayoutData(gd);
		variableDescription.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				setAttributeValue(QueryDefinition.DESCRIPTION, variableDescription.getText());
				validate();
			}
		});

		toolkit.createLabel(baseSection, "Variable Type");
		String[] types = new String[5];
		types[0] = QueryDefinition.STRING;
		types[1] = QueryDefinition.NUMERIC;
		types[2] = QueryDefinition.DATE;
		types[3] = QueryDefinition.TIME;
		types[4] = QueryDefinition.VALUE_LIST;
		typeCombo = createDropDown(baseSection, types, 0);
		typeCombo.setLayoutData(gd);
		typeCombo.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				setAttributeValue(QueryDefinition.TYPE, typeCombo.getText());
				validate();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		toolkit.createLabel(baseSection, "Default value");
		defaultValue = toolkit.createText(baseSection, "", SWT.SINGLE);
		defaultValue.setLayoutData(gd);
		defaultValue.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				setAttributeValue(QueryDefinition.DEFAULT_VALUE, defaultValue.getText());
				validate();
			}
		});

		gd = new GridData();
		gd.horizontalIndent = 7;

		toolkit.createLabel(baseSection, "Mandatory");
		mandatoryButton = toolkit.createButton(baseSection, "", SWT.CHECK);
		mandatoryButton.setLayoutData(gd);
		mandatoryButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				setAttributeValue(QueryDefinition.MANDATORY, mandatoryButton.getSelection());
				validate();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		// Dummy filler
		toolkit.createLabel(baseSection, "");

		toolkit.createLabel(baseSection, "Exclude if empty");
		excludeIfEmptyButton = toolkit.createButton(baseSection, null, SWT.CHECK);
		excludeIfEmptyButton.setLayoutData(gd);
		excludeIfEmptyButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				setAttributeValue(QueryDefinition.EXCLUDE_IF_EMPTY, excludeIfEmptyButton.getSelection());
				validate();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		// Dummy filler
		toolkit.createLabel(baseSection, "");

	}

	protected void createValuesArea(Composite baseSection) {

		predefValuesButton = toolkit.createButton(baseSection, "Predefined values", SWT.RADIO);
		predefValuesButton.setSelection(false);
		predefValuesButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				if (predefValuesButton.getSelection()) {
					setAttributeValue(getQueryDef().getValuesNode(input, true), QueryDefinition.VALUES_TYPE, QueryDefinition.PREFINED_VALUES);
					validate();
				}
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		sqlValuesButton = toolkit.createButton(baseSection, "Values from sql", SWT.RADIO);
		sqlValuesButton.setSelection(false);
		sqlValuesButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				if (sqlValuesButton.getSelection()) {
					setAttributeValue(getQueryDef().getValuesNode(input, true), QueryDefinition.VALUES_TYPE, QueryDefinition.SQL_VALUES);
					validate();
				}
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		// SQL edit area
		sqlValuesComposite = toolkit.createComposite(baseSection);
		sqlValuesComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		GridLayout gl = new GridLayout(1, false);
		gl.horizontalSpacing = 0;
		gl.marginWidth = 0;
		sqlValuesComposite.setLayout(gl);

		sqlEdit = new StyledEdit(sqlValuesComposite, new SQLConfiguration());
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.heightHint = 100;
		sqlEdit.getControl().setLayoutData(gd);
		sqlEdit.addTextListener(new ITextListener() {

			public void textChanged(TextEvent event) {
				getQueryDef().setValuesSQL(input, sqlEdit.getText());
				validate();
			}
		});

		// Predefined values edit area
		predefValuesComposite = toolkit.createComposite(baseSection);
		predefValuesComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		gl = new GridLayout(2, false);
		gl.horizontalSpacing = 5;
		gl.marginWidth = 0;
		predefValuesComposite.setLayout(gl);

		// Create value table
		predefValuesTableViewer = new TableViewer(predefValuesComposite);
		predefValuesTableViewer.getTable().setHeaderVisible(true);
		predefValuesTableViewer.getTable().setLinesVisible(true);

		gd = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd.heightHint = 100;
		predefValuesTableViewer.getTable().setLayoutData(gd);
		predefValuesTableViewer.setContentProvider(new IStructuredContentProvider() {

			XMLModelListener listener = new XMLModelListener() {
				public void modelChanged(Object[] objects, String type, String property) {
					predefValuesTableViewer.refresh();
				}
			};

			public void inputChanged(final Viewer viewer, Object oldInput, Object newInput) {
				getQueryDef().removeModelListener(listener);
				getQueryDef().addModelListener(new XMLModelListener() {
					public void modelChanged(Object[] objects, String type, String property) {
						viewer.refresh();
					}
				});
			}

			public void dispose() {
			}

			public Object[] getElements(Object inputElement) {
				return getQueryDef().getPredefValueNodes(input, false);
			}
		});

		predefValuesTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				Object object = selection.getFirstElement();
				Element selectedElement = null;
				if (object instanceof Element)
					selectedElement = (Element) object;

				buttonRemove.setEnabled(false);
				buttonUp.setEnabled(false);
				buttonDown.setEnabled(false);

				if (selectedElement == null)
					return;

				if ((selectedElement.getParentNode() instanceof Element))
					buttonRemove.setEnabled(true);

				Node parent = selectedElement.getParentNode();

				int index = model.getIndexOf(selectedElement, parent, QueryDefinition.VALUE);

				if (!(parent instanceof Document) && index > 0)
					buttonUp.setEnabled(true);
				if (!(parent instanceof Document) && index < model.getChildren(parent, QueryDefinition.VALUE).length - 1)
					buttonDown.setEnabled(true);

			}
		});

		// Create value table column
		TableViewerColumn col = new TableViewerColumn(predefValuesTableViewer, SWT.NONE);
		col.getColumn().setText("Selectable Values");
		col.getColumn().setWidth(250);
		col.setEditingSupport(new TextEditingSupport(predefValuesTableViewer));

		final class ValueTableColumnLabelProvider extends ColumnLabelProvider {
			public ValueTableColumnLabelProvider() {
				super();
			}

			public String getText(Object element) {
				if (element instanceof Node) {
					try {
						return getQueryDef().getText((Node) element);
					} catch (Exception e) {
					}
				}
				return "";
			}
		}

		col.setLabelProvider(new ValueTableColumnLabelProvider());

		createButtons(predefValuesComposite);

		((GridData) predefValuesComposite.getLayoutData()).exclude = true;
		predefValuesComposite.setVisible(false);
		((GridData) sqlValuesComposite.getLayoutData()).exclude = true;
		sqlValuesComposite.setVisible(false);

	}

	protected Composite createButtons(Composite client) {

		Composite buttons = toolkit.createComposite(client, SWT.NULL);
		toolkit.createLabel(buttons, "");
		buttons.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
		Color whiteColour = client.getDisplay().getSystemColor(SWT.COLOR_WHITE);
		buttons.setBackground(whiteColour);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 10;
		layout.marginWidth = 0;
		buttons.setLayout(layout);
		GridData gd = new GridData(GridData.VERTICAL_ALIGN_CENTER);

		buttonAdd = toolkit.createButton(buttons, null, SWT.PUSH);
		buttonAdd.setToolTipText("Add a new value");
		buttonAdd.setImage(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_ADD).createImage());
		buttonAdd.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				getQueryDef().addPredefValue(input, "New Value");

			}
		});

		buttonRemove = toolkit.createButton(buttons, null, SWT.PUSH);
		buttonRemove.setToolTipText("Remove the selected value(s)");
		buttonRemove.setImage(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_DELETE).createImage());
		buttonRemove.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				IStructuredSelection sel = (IStructuredSelection) predefValuesTableViewer.getSelection();
				Node[] nodes = new Node[sel.size()];
				int idx = 0;
				for (Object obj : sel.toList()) {
					nodes[idx] = (Node) obj;
					idx++;
				}
				getQueryDef().remove(nodes);
			}
		});

		buttonUp = toolkit.createButton(buttons, null, SWT.PUSH);
		buttonUp.setToolTipText("Move the selected value on step up");
		buttonUp.setImage(SpeedTransferPlugin.getImageDescriptor("up.gif").createImage());

		buttonUp.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				IStructuredSelection sel = (IStructuredSelection) predefValuesTableViewer.getSelection();
				getQueryDef().moveUp((Node) sel.getFirstElement());
			}
		});

		buttonDown = toolkit.createButton(buttons, null, SWT.PUSH);
		buttonDown.setToolTipText("Move the selected value on step down");
		buttonDown.setImage(SpeedTransferPlugin.getImageDescriptor("down.gif").createImage());
		buttonDown.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				IStructuredSelection sel = (IStructuredSelection) predefValuesTableViewer.getSelection();
				getQueryDef().moveDown((Node) sel.getFirstElement());
			}
		});

		buttonAdd.setLayoutData(gd);
		buttonRemove.setLayoutData(gd);
		buttonUp.setLayoutData(gd);
		buttonDown.setLayoutData(gd);

		return buttons;

	}

	protected void createActions() {
		actionFormatSQL = new FormatSQLAction(sqlEdit);
		actionOpenSQLBuilderWizard = new OpenSQLBuilderWizardAction(sqlEdit, getQueryDef());
		runSQLAction = new RunSQLToTableAction(getQueryDef(), sqlEdit);

		// Text operations
		undoAction = new TextOperationHandler(sqlEdit, ITextOperationTarget.UNDO, "Undo");
		cutAction = new TextOperationHandler(sqlEdit, ITextOperationTarget.CUT, "Cut");
		copyAction = new TextOperationHandler(sqlEdit, ITextOperationTarget.COPY, "Copy");
		pasteAction = new TextOperationHandler(sqlEdit, ITextOperationTarget.PASTE, "Paste");
		deleteAction = new TextOperationHandler(sqlEdit, ITextOperationTarget.DELETE, "Delete");
		
		TextViewerUndoManager undoManager = (TextViewerUndoManager)sqlEdit.getUndoManager();
		
		IAction undo = ActionFactory.UNDO.create(UIHelper.instance().getActiveWindow());
		block.getEditor().getEditorSite().getActionBars().setGlobalActionHandler(undo.getId(), new UndoActionHandler(block.getEditor().getEditorSite(), undoManager.getUndoContext()));
		block.getEditor().getEditorSite().getActionBars().updateActionBars();

	}

	protected void createPopupMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {

			public void menuAboutToShow(IMenuManager manager) {
				fillPopupMenu(manager);
			}
		});

		Menu menu = menuMgr.createContextMenu(sqlEdit.getControl());
		sqlEdit.getControl().setMenu(menu);

	}

	protected void fillPopupMenu(IMenuManager menuManager) {
		menuManager.add(undoAction);
		menuManager.add(new Separator());
		menuManager.add(cutAction);
		menuManager.add(copyAction);
		menuManager.add(pasteAction);
		menuManager.add(deleteAction);
		menuManager.add(new Separator());
		menuManager.add(actionOpenSQLBuilderWizard);
		menuManager.add(actionFormatSQL);
	}

	protected void createSectionToolbar(Section section) {
		ToolBarManager toolBarManager = new ToolBarManager(SWT.RIGHT);
		ToolBar toolbar = toolBarManager.createControl(section);
		final Cursor handCursor = new Cursor(Display.getCurrent(), SWT.CURSOR_HAND);
		toolbar.setCursor(handCursor);

		// Cursor needs to be explicitly disposed
		toolbar.addDisposeListener(new DisposeListener() {

			public void widgetDisposed(DisposeEvent e) {
				if ((handCursor != null) && (handCursor.isDisposed() == false)) {
					handCursor.dispose();
				}
			}
		});

		fillSectionToolbar(toolBarManager);

		toolBarManager.update(true);

		section.setTextClient(toolbar);
	}

	protected void fillSectionToolbar(ToolBarManager toolBarManager) {
		toolBarManager.add(actionOpenSQLBuilderWizard);
		toolBarManager.add(actionFormatSQL);
		toolBarManager.add(runSQLAction);

	}

	protected void internalUpdate() {
		updateText(variableName, getAttributeValue(QueryDefinition.NAME));
		updateText(variableDescription, getAttributeValue(QueryDefinition.DESCRIPTION));
		updateCombo(typeCombo, getAttributeValue(QueryDefinition.TYPE));
		updateText(defaultValue, getAttributeValue(QueryDefinition.DEFAULT_VALUE));
		updateBoolean(mandatoryButton, getAttributeValueBoolean(QueryDefinition.MANDATORY));
		updateBoolean(excludeIfEmptyButton, getAttributeValueBoolean(QueryDefinition.EXCLUDE_IF_EMPTY));

		updateBoolean(predefValuesButton, false);
		updateBoolean(sqlValuesButton, false);
		updateText(sqlEdit, "");

		Node valuesNode = getNode(input, QueryDefinition.VALUES);
		if (valuesNode != null) {
			if (getAttributeValue(valuesNode, QueryDefinition.VALUES_TYPE).equalsIgnoreCase(QueryDefinition.PREFINED_VALUES)) {
				updateBoolean(predefValuesButton, true);
			} else {
				updateBoolean(sqlValuesButton, true);
			}

			Node sqlNode = getNode(valuesNode, QueryDefinition.SQL);
			if (sqlNode != null)
				updateText(sqlEdit, getCDATA(sqlNode));

		}

		predefValuesTableViewer.setInput(input);

		validatePage();
	}

	public void setFocus() {
		variableName.setFocus();
	}

	protected void selectVariable() {
		String[] items = getQueryDef().getUndefinedVariables().toArray(new String[0]);
		ItemSelectionDialog dialog = new ItemSelectionDialog(UIHelper.instance().getActiveShell(), items, "Select Variable");
		int rc = dialog.open();
		if (rc == Window.CANCEL)
			return;

		updateText(variableName, dialog.getSelectedItem());

	}

	protected void validatePage() {

		if (typeCombo.getText().equals(QueryDefinition.DATE) || typeCombo.getText().equals(QueryDefinition.TIME)) {
			defaultValue.setEnabled(false);
			mandatoryButton.setEnabled(false);
			excludeIfEmptyButton.setEnabled(false);
		} else {
			defaultValue.setEnabled(true);
			mandatoryButton.setEnabled(true);
			excludeIfEmptyButton.setEnabled(!mandatoryButton.getSelection());
		}

		if (getQueryDef().isSingleStatementQuery() == false) {
			excludeIfEmptyButton.setSelection(false);
			excludeIfEmptyButton.setEnabled(false);
		}

		if (typeCombo.getText().equalsIgnoreCase(QueryDefinition.VALUE_LIST)) {
			valuesSect.setVisible(true);

			String newValuesType = savedValuesType;
			if (sqlValuesButton.getSelection())
				newValuesType = "sql";
			else if (predefValuesButton.getSelection())
				newValuesType = "predef";

			runSQLAction.setEnabled(false);
			actionFormatSQL.setEnabled(false);
			actionOpenSQLBuilderWizard.setEnabled(false);

			if (sqlValuesButton.getSelection()) {
				actionOpenSQLBuilderWizard.setEnabled(true);
				if (sqlEdit.getText().trim().length() > 0) {
					runSQLAction.setEnabled(true);
					actionFormatSQL.setEnabled(true);
				}

			}

			if (newValuesType.equals(savedValuesType) == false) {
				savedValuesType = newValuesType;

				((GridData) predefValuesComposite.getLayoutData()).exclude = true;
				predefValuesComposite.setVisible(false);
				((GridData) sqlValuesComposite.getLayoutData()).exclude = true;
				sqlValuesComposite.setVisible(false);

				if (predefValuesButton.getSelection()) {
					((GridData) predefValuesComposite.getLayoutData()).exclude = false;
					predefValuesComposite.setVisible(true);

				} else if (sqlValuesButton.getSelection()) {

					((GridData) sqlValuesComposite.getLayoutData()).exclude = false;
					sqlValuesComposite.setVisible(true);

					actionOpenSQLBuilderWizard.setEnabled(true);
					if (sqlEdit.getText().trim().length() > 0) {
						runSQLAction.setEnabled(true);
						actionFormatSQL.setEnabled(true);
					}

				}

			}

			valuesClient.layout();
		} else {
			valuesSect.setVisible(false);
		}

		if (excludeIfEmptyButton.isEnabled() == false)
			excludeIfEmptyButton.setSelection(false);

		clearMessage("Name empty", variableName);
		if (variableName.getText().trim().length() == 0)
			issueMessage("Name empty", IMessageProvider.ERROR, "Variable must be specified", variableName);

		clearMessage("Name invalid", variableName);
		if (variableName.getText().trim().length() > 0) {
			if (getQueryDef().hasReplacementVariable(variableName.getText().trim()) == false)
				issueMessage("Name invalid", IMessageProvider.WARNING, "Variable not found in sql", variableName);
		}

		clearMessage("Name already defined", variableName);
		if (variableName.getText().trim().length() > 0) {
			if (getQueryDef().countReplacementVariableDefs(variableName.getText().trim()) > 1)
				issueMessage("Name already defined", IMessageProvider.ERROR, "Variable already defined", variableName);
		}

		clearMessage("Description empty", variableDescription);
		if (variableDescription.getText().trim().length() == 0)
			issueMessage("Description empty", IMessageProvider.ERROR, "Description must be specified", variableDescription);

		clearMessage("Invalid default", defaultValue);
		if (typeCombo.getText().equalsIgnoreCase(QueryDefinition.NUMERIC)) {
			if (defaultValue.getText().trim().length() > 0) {
				try {
					new Double(defaultValue.getText().trim());
				} catch (Exception exc) {
					issueMessage("Invalid default", IMessageProvider.ERROR, "Default value must be numeric", defaultValue);
				}
			}
		}

		clearMessage("Exclude info", excludeIfEmptyButton);
		if (excludeIfEmptyButton.isEnabled() && excludeIfEmptyButton.getSelection())
			issueMessage("Exclude info", IMessageProvider.INFORMATION, "If variable is used to filter data, selections that are using this variable will be removed from the sql string at runtime",
					excludeIfEmptyButton);

		if (typeCombo.getText().equalsIgnoreCase(QueryDefinition.VALUE_LIST)) {
			clearMessage("Invalid Selection SQL", sqlEdit.getControl());
			if (sqlValuesButton.getSelection() && sqlEdit.getText().trim().toLowerCase().startsWith("select") == false)
				issueMessage("Invalid Selection SQL", IMessageProvider.ERROR, "The specified selection sql must be a select statement.", sqlEdit.getControl());
		}

	}

}
