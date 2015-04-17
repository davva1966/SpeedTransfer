package com.ss.speedtransfer.xml.editor;

import java.util.Iterator;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;
import org.eclipse.ui.forms.DetailsPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.MasterDetailsBlock;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.ss.speedtransfer.SpeedTransferPlugin;


public class XMLFormPageBlock extends MasterDetailsBlock implements XMLModelListener {

	public static final String REMOVE_ACTION_ID = "REMOVE_ACTION";
	public static final String MOVE_UP_ACTION_ID = "MOVE_UP_ACTION";
	public static final String MOVE_DOWN_ACTION_ID = "MOVE_DOWN_ACTION";

	protected XMLFormEditor editor;
	protected XMLModel model;

	protected FilteredTree filteredTree;
	protected Button buttonUp;
	protected Button buttonDown;
	protected Button buttonRemove;

	protected IAction expandAllAction;
	protected IAction collapseAllAction;
	protected IAction removeAction;
	protected IAction moveUpAction;
	protected IAction moveDownAction;

	public XMLFormPageBlock(XMLFormEditor editor, XMLModel model) {
		this.editor = editor;
		this.model = model;
		model.addModelListener(this);
	}

	protected void createMasterPart(final IManagedForm managedForm, Composite parent) {

		FormToolkit toolkit = managedForm.getToolkit();
		toolkit.decorateFormHeading(managedForm.getForm().getForm());

		Section section;
		if (getTreeDescription() != null)
			section = toolkit.createSection(parent, Section.DESCRIPTION | Section.TITLE_BAR);
		else
			section = toolkit.createSection(parent, Section.TITLE_BAR);

		section.setText(getTreeTitle());
		if (getTreeDescription() != null)
			section.setDescription(getTreeDescription());

		section.marginWidth = 10;
		section.marginHeight = 10;
		Composite client = toolkit.createComposite(section, SWT.WRAP);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = 2;
		layout.marginHeight = 2;
		client.setLayout(layout);

		filteredTree = createFilteredTree(client);

		toolkit.paintBordersFor(client);
		section.setClient(client);
		final SectionPart spart = new SectionPart(section);
		managedForm.addPart(spart);

		TreeViewer viewer = filteredTree.getViewer();
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				managedForm.fireSelectionChanged(spart, event.getSelection());
				validatePageState();
			}
		});

		viewer.setContentProvider(getTreeContentProvider());
		viewer.setLabelProvider(getTreeLabelProvider());
		viewer.setInput(editor.getEditorInput());

		createActions();
		createSectionToolbar(section, toolkit);

		createButtons(client, toolkit);
		createTreeMenu();
		validatePageState();

	}

	protected ITreeContentProvider getTreeContentProvider() {
		return new XMLMasterContentProvider(model);
	}

	protected ILabelProvider getTreeLabelProvider() {
		return new XMLMasterLabelProvider();
	}

	protected void createToolBarActions(IManagedForm managedForm) {
		final ScrolledForm form = managedForm.getForm();
		Action haction = new Action("horizontal", Action.AS_RADIO_BUTTON) {
			public void run() {
				sashForm.setOrientation(SWT.HORIZONTAL);
				form.reflow(true);
			}
		};
		haction.setChecked(true);
		haction.setToolTipText("Horizontal");
		haction.setImageDescriptor(SpeedTransferPlugin.getImageDescriptor("th_horizontal.gif"));
		Action vaction = new Action("vertical", Action.AS_RADIO_BUTTON) {
			public void run() {
				sashForm.setOrientation(SWT.VERTICAL);
				form.reflow(true);
			}
		};
		vaction.setChecked(false);
		vaction.setToolTipText("ScrolledPropertiesBlock.vertical");
		vaction.setImageDescriptor(SpeedTransferPlugin.getImageDescriptor("th_vertical.gif"));
		form.getToolBarManager().add(haction);
		form.getToolBarManager().add(vaction);
	}

	protected void registerPages(DetailsPart detailsPart) {
		detailsPart.setPageProvider(new XMLDetailsPageProvider());
	}

	protected FilteredTree createFilteredTree(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		composite.setLayout(layout);

		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);

		data.horizontalSpan = 1;
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = true;

		composite.setLayoutData(data);

		PatternFilter filter = new PatternFilter();
		filter.setIncludeLeadingWildcard(true);

		FilteredTree filterTree = new FilteredTree(composite, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER, filter, true);

		TreeViewer viewer = filterTree.getViewer();
		viewer.getTree().addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {
			}

			public void keyReleased(KeyEvent e) {
				if (e.keyCode == SWT.DEL) {
					removeAction.run();
				}
			}
		});

		viewer.addFilter(filter);
		return filterTree;
	}

	protected Composite createButtons(Composite client, FormToolkit toolkit) {

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

		buttonRemove = toolkit.createButton(buttons, null, SWT.PUSH);
		buttonRemove.setToolTipText("Remove the selected element");
		buttonRemove.setImage(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_DELETE).createImage());
		buttonRemove.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				removeAction.run();

			}
		});

		buttonUp = toolkit.createButton(buttons, null, SWT.PUSH);
		buttonUp.setToolTipText("Move the selected element on step up");
		buttonUp.setImage(SpeedTransferPlugin.getImageDescriptor("up.gif").createImage());

		buttonUp.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				moveUpAction.run();

			}
		});

		buttonDown = toolkit.createButton(buttons, null, SWT.PUSH);
		buttonDown.setToolTipText("Move the selected element on step down");
		buttonDown.setImage(SpeedTransferPlugin.getImageDescriptor("down.gif").createImage());
		buttonDown.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				moveDownAction.run();

			}
		});

		buttonRemove.setLayoutData(gd);
		buttonUp.setLayoutData(gd);
		buttonDown.setLayoutData(gd);

		return buttons;

	}

	protected void validatePageState() {

		ISelection selection = filteredTree.getViewer().getSelection();
		Object object = ((TreeSelection) selection).getFirstElement();
		Element selectedElement = null;
		if (object instanceof Element)
			selectedElement = (Element) object;

		validatePageState(selectedElement);

	}

	protected void validatePageState(Element selectedElement) {

		removeAction.setEnabled(false);
		moveUpAction.setEnabled(false);
		moveDownAction.setEnabled(false);

		if (selectedElement == null)
			return;

		if ((selectedElement.getParentNode() instanceof Element))
			removeAction.setEnabled(true);

		Node parent = selectedElement.getParentNode();

		int index = model.getIndexOf(selectedElement, parent);

		if (!(parent instanceof Document) && index > 0)
			moveUpAction.setEnabled(true);
		if (!(parent instanceof Document) && index < model.getChildren(parent).length - 1)
			moveDownAction.setEnabled(true);

	}

	protected void createSectionToolbar(Section section, FormToolkit toolkit) {
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

	protected void createActions() {

		expandAllAction = new Action("Expand All") {
			public void run() {
				filteredTree.getViewer().expandAll();
			}
		};
		expandAllAction.setImageDescriptor(SpeedTransferPlugin.getImageDescriptor("expandall.gif"));
		expandAllAction.setToolTipText("Collapse all items in the tree");

		collapseAllAction = new Action("Collapse All") {
			public void run() {
				filteredTree.getViewer().collapseAll();
			}
		};
		collapseAllAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_ELCL_COLLAPSEALL));
		collapseAllAction.setToolTipText("Collapse all items in the tree");

		removeAction = new Action("Remove") {
			public void run() {
				if (!isEnabled())
					return;
				handleAction(REMOVE_ACTION_ID, true);
				editor.getFormPage().getManagedForm().getMessageManager().removeAllMessages();
			}
		};
		removeAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_DELETE));
		removeAction.setToolTipText("Remove the selected element");
		removeAction.addPropertyChangeListener(new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if (event.getProperty().equals(IAction.ENABLED))
					buttonRemove.setEnabled((Boolean) event.getNewValue());
			}
		});

		moveUpAction = new Action("Move Up") {
			public void run() {
				if (!isEnabled())
					return;
				handleAction(MOVE_UP_ACTION_ID, false);
			}
		};
		moveUpAction.setImageDescriptor(SpeedTransferPlugin.getImageDescriptor("up.gif"));
		moveUpAction.setToolTipText("Move the selected element on step up");
		moveUpAction.addPropertyChangeListener(new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if (event.getProperty().equals(IAction.ENABLED))
					buttonUp.setEnabled((Boolean) event.getNewValue());
			}
		});

		moveDownAction = new Action("Move Down") {
			public void run() {
				if (!isEnabled())
					return;
				handleAction(MOVE_DOWN_ACTION_ID, false);
			}
		};
		moveDownAction.setImageDescriptor(SpeedTransferPlugin.getImageDescriptor("down.gif"));
		moveDownAction.setToolTipText("Move the selected element on step down");
		moveDownAction.addPropertyChangeListener(new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if (event.getProperty().equals(IAction.ENABLED))
					buttonDown.setEnabled((Boolean) event.getNewValue());
			}
		});

	}

	protected void createTreeMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {

			public void menuAboutToShow(IMenuManager manager) {
				fillTreeContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(filteredTree.getViewer().getControl());
		filteredTree.getViewer().getControl().setMenu(menu);

	}

	protected void fillTreeContextMenu(IMenuManager menuManager) {

		menuManager.add(moveUpAction);
		menuManager.add(moveDownAction);
		menuManager.add(new Separator());
		menuManager.add(removeAction);
		// Other plug-ins can contribute their actions here
		menuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	protected void fillSectionToolbar(ToolBarManager toolBarManager) {
		// Add collapse action to the tool bar
		toolBarManager.add(expandAllAction);
		toolBarManager.add(collapseAllAction);
	}

	// handle actions
	public boolean handleAction(String actionName, boolean allowMultipleSelection) {

		IStructuredSelection selection = (IStructuredSelection) filteredTree.getViewer().getSelection();
		if (selection.isEmpty())
			return false;

		boolean actionHandled = false;
		Iterator<Element> iter = selection.iterator();
		while (iter.hasNext()) {
			Element selectedElement = iter.next();
			actionHandled = handleAction(actionName, selectedElement, selection);

			if (allowMultipleSelection == false)
				break;
		}

		return actionHandled;

	}

	public boolean handleAction(String actionName, Element selectedElement, IStructuredSelection selection) {
		boolean actionHandled = false;

		if (selectedElement == null)
			return false;

		if (actionName.equals(REMOVE_ACTION_ID)) {
			model.remove(selectedElement);
			actionHandled = true;
		} else if (actionName.equals(MOVE_UP_ACTION_ID)) {
			model.moveUp(selectedElement);
			if (selection != null)
				filteredTree.getViewer().setSelection(selection);
			actionHandled = true;
		} else if (actionName.equals(MOVE_DOWN_ACTION_ID)) {
			model.moveDown(selectedElement);
			if (selection != null)
				filteredTree.getViewer().setSelection(selection);
			actionHandled = true;
		}

		return actionHandled;
	}

	@Override
	public void modelChanged(Object[] objects, String type, String property) {
		if (type == XMLModelListener.STRUCTURE_CHANGED || type == XMLModelListener.REPLACED)
			filteredTree.getViewer().refresh();

	}

	public TreeViewer getViewer() {
		return filteredTree.getViewer();
	}

	protected String getMasterTitle() {
		return "";
	}

	protected String getTreeTitle() {
		return "XML Tree";
	}

	protected String getTreeDescription() {
		return null;
	}

	public XMLFormEditor getEditor() {
		return editor;
	}
}