package com.ss.speedtransfer.ui.nattable;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractUiBindingConfiguration;
import org.eclipse.nebula.widgets.nattable.copy.command.CopyDataToClipboardCommand;
import org.eclipse.nebula.widgets.nattable.freeze.command.FreezeColumnCommand;
import org.eclipse.nebula.widgets.nattable.freeze.command.FreezeRowCommand;
import org.eclipse.nebula.widgets.nattable.freeze.command.UnFreezeGridCommand;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.ui.NatEventData;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.menu.IMenuItemProvider;
import org.eclipse.nebula.widgets.nattable.ui.menu.MenuItemProviders;
import org.eclipse.nebula.widgets.nattable.ui.menu.PopupMenuAction;
import org.eclipse.nebula.widgets.nattable.ui.menu.PopupMenuBuilder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

public class QueryDefinitionResultTableMenuConfiguration extends AbstractUiBindingConfiguration {

	public static final String COPY_SELECTION_TO_CLIPBOARD_MENU_ITEM_ID = "copySelectionToClipboardMenuItem"; //$NON-NLS-1$
	public static final String FREEZE_COLUMN_MENU_ITEM_ID = "freezeColumnMenuItem"; //$NON-NLS-1$
	public static final String FREEZE_ROW_MENU_ITEM_ID = "freezeRowMenuItem"; //$NON-NLS-1$
	public static final String UNFREEZE_ALL_MENU_ITEM_ID = "unfreezeAllMenuItem"; //$NON-NLS-1$

	protected SelectionLayer selectionLayer;

	protected Menu colHeaderMenu;
	protected Menu rowHeaderMenu;
	protected Menu cornerMenu;
	protected Menu bodyMenu;

	public QueryDefinitionResultTableMenuConfiguration(NatTable natTable, SelectionLayer selectionLayer) {
		super();
		this.selectionLayer = selectionLayer;
		this.colHeaderMenu = createColumnHeaderMenu(natTable).build();
		this.rowHeaderMenu = createRowHeaderMenu(natTable).build();
		this.cornerMenu = createCornerMenu(natTable).build();
		this.bodyMenu = createBodyMenu(natTable).build();
	}

	protected PopupMenuBuilder createColumnHeaderMenu(NatTable natTable) {
		PopupMenuBuilder builder = new PopupMenuBuilder(natTable).withHideColumnMenuItem().withShowAllColumnsMenuItem().withAutoResizeSelectedColumnsMenuItem().withColumnRenameDialog();
		builder.withSeparator();
		builder.withMenuItemProvider(FREEZE_COLUMN_MENU_ITEM_ID, freezeColumnMenuItemProvider("Freeze Column"));
		builder.withMenuItemProvider(UNFREEZE_ALL_MENU_ITEM_ID, unfreezeAllMenuItemProvider("Unfreeze"));
		return builder;
	}

	protected PopupMenuBuilder createRowHeaderMenu(NatTable natTable) {
		PopupMenuBuilder builder = new PopupMenuBuilder(natTable).withAutoResizeSelectedRowsMenuItem();
		builder.withSeparator();
		builder.withMenuItemProvider(FREEZE_ROW_MENU_ITEM_ID, freezeRowMenuItemProvider("Freeze Row"));
		builder.withMenuItemProvider(UNFREEZE_ALL_MENU_ITEM_ID, unfreezeAllMenuItemProvider("Unfreeze"));
		return builder;
	}

	protected PopupMenuBuilder createCornerMenu(NatTable natTable) {
		PopupMenuBuilder builder = new PopupMenuBuilder(natTable).withShowAllColumnsMenuItem();
		builder.withSeparator();
		builder.withMenuItemProvider(UNFREEZE_ALL_MENU_ITEM_ID, unfreezeAllMenuItemProvider("Unfreeze"));
		return builder;
	}

	protected PopupMenuBuilder createBodyMenu(NatTable natTable) {
		PopupMenuBuilder builder = new PopupMenuBuilder(natTable);
		builder.withMenuItemProvider(COPY_SELECTION_TO_CLIPBOARD_MENU_ITEM_ID, copySelectionToClipboardMenuItemProvider("Copy"));
		builder.withEnabledState(COPY_SELECTION_TO_CLIPBOARD_MENU_ITEM_ID, new HasSelection(selectionLayer));
		return builder;
	}

	@Override
	public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {

		if (this.colHeaderMenu != null) {
			uiBindingRegistry.registerMouseDownBinding(new MouseEventMatcher(SWT.NONE, GridRegion.COLUMN_HEADER, MouseEventMatcher.RIGHT_BUTTON), new PopupMenuAction(this.colHeaderMenu));
		}

		if (this.rowHeaderMenu != null) {
			uiBindingRegistry.registerMouseDownBinding(new MouseEventMatcher(SWT.NONE, GridRegion.ROW_HEADER, MouseEventMatcher.RIGHT_BUTTON), new PopupMenuAction(this.rowHeaderMenu));
		}

		if (this.cornerMenu != null) {
			uiBindingRegistry.registerMouseDownBinding(new MouseEventMatcher(SWT.NONE, GridRegion.CORNER, MouseEventMatcher.RIGHT_BUTTON), new PopupMenuAction(this.cornerMenu));
		}

		if (this.bodyMenu != null) {
			uiBindingRegistry.registerMouseDownBinding(new MouseEventMatcher(SWT.NONE, GridRegion.BODY, MouseEventMatcher.RIGHT_BUTTON), new PopupMenuAction(this.bodyMenu));
		}

	}

	public static IMenuItemProvider copySelectionToClipboardMenuItemProvider(final String menuLabel) {
		return new IMenuItemProvider() {

			@Override
			public void addMenuItem(final NatTable natTable, final Menu popupMenu) {
				MenuItem menuItem = new MenuItem(popupMenu, SWT.PUSH);
				menuItem.setText(menuLabel);
				menuItem.setEnabled(true);

				menuItem.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent event) {
						natTable.doCommand(new CopyDataToClipboardCommand("\t", //$NON-NLS-1$
								System.getProperty("line.separator"), //$NON-NLS-1$
								natTable.getConfigRegistry()));
					}
				});
			}
		};
	}

	public static IMenuItemProvider freezeColumnMenuItemProvider(final String menuLabel) {
		return new IMenuItemProvider() {

			@Override
			public void addMenuItem(final NatTable natTable, final Menu popupMenu) {
				MenuItem menuItem = new MenuItem(popupMenu, SWT.PUSH);
				menuItem.setText(menuLabel);
				menuItem.setEnabled(true);

				menuItem.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent event) {
						NatEventData eventData = MenuItemProviders.getNatEventData(event);
						natTable.doCommand(new FreezeColumnCommand(natTable, eventData.getColumnPosition(), false, true));
					}
				});
			}
		};
	}

	public static IMenuItemProvider freezeRowMenuItemProvider(final String menuLabel) {
		return new IMenuItemProvider() {

			@Override
			public void addMenuItem(final NatTable natTable, final Menu popupMenu) {
				MenuItem menuItem = new MenuItem(popupMenu, SWT.PUSH);
				menuItem.setText(menuLabel);
				menuItem.setEnabled(true);

				menuItem.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent event) {
						NatEventData eventData = MenuItemProviders.getNatEventData(event);
						natTable.doCommand(new FreezeRowCommand(natTable, eventData.getRowPosition(), false, true));
					}
				});
			}
		};
	}

	public static IMenuItemProvider unfreezeAllMenuItemProvider(final String menuLabel) {
		return new IMenuItemProvider() {

			@Override
			public void addMenuItem(final NatTable natTable, final Menu popupMenu) {
				MenuItem menuItem = new MenuItem(popupMenu, SWT.PUSH);
				menuItem.setText(menuLabel);
				menuItem.setEnabled(true);

				menuItem.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent event) {
						natTable.doCommand(new UnFreezeGridCommand());
					}
				});
			}
		};
	}
}