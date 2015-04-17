// {{CopyrightNotice}}

package com.ss.speedtransfer.ui.wizard.qb;

import java.util.ArrayList;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

/**
 * The <code>SQLQBComposite</code> class
 */
public abstract class SQLQBComposite extends Composite {

	/** Default width left column */
	protected static final int DFT_WIDTH_LEFT_COL = 300;

	/** Default widht middle column */
	protected static final int DFT_WIDTH_MID_COL = 75;

	/** Default widht right column */
	protected static final int DFT_WIDTH_RIGHT_COL = 275;

	/** Default minimum height for tables */
	protected static final int DFT_MIN_HEIGHT = 250;

	/** Query Data */
	protected SQLQueryData queryData = null;

	/** Wizard page for button control update */
	protected WizardPage wizardPage = null;

	/**
	 * Initializes a newly created <code>SQLQBComposite</code>
	 * 
	 * @param parent
	 * @param style
	 */
	public SQLQBComposite(WizardPage page, Composite parent, int style, SQLQueryData queryData) {
		super(parent, style);
		this.wizardPage = page;
		this.queryData = queryData;
	}

	public Group createGroup(Composite parent) {
		return new Group(parent, SWT.SHADOW_ETCHED_IN);
	}

	public Color getDefaultBackgroundColor() {
		return Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW);
	}

	/**
	 * Get all variables TODO Make generic ???
	 * 
	 * @return variables
	 */
	protected ArrayList getVariables() {
		ArrayList list = new ArrayList();

		return list;
	}

	/**
	 * Create a pop-up menu for the specified combo
	 * 
	 * @param the
	 *            combo box
	 * @return the pop-up menu
	 */
	static public Menu createMenu(final Combo combo, final SQLQueryData queryData) {

		String toolTip = "Double click to search for a column \n";
		toolTip = toolTip + "Right click to sample column values";
		combo.setToolTipText(toolTip);

		Menu menu = new Menu(combo.getShell(), SWT.POP_UP);
		combo.setMenu(menu);

		// Add menu items
		final MenuItem cut = new MenuItem(menu, SWT.PUSH);
		final MenuItem copy = new MenuItem(menu, SWT.PUSH);
		final MenuItem paste = new MenuItem(menu, SWT.PUSH);
		MenuItem sep = new MenuItem(menu, SWT.SEPARATOR);
		final MenuItem sampleContents = new MenuItem(menu, SWT.PUSH);
		sep = new MenuItem(menu, SWT.SEPARATOR);
		final MenuItem search = new MenuItem(menu, SWT.PUSH);

		// Cut
		cut.setText("Cut");
		cut.addListener(SWT.Selection, new Listener() {

			public void handleEvent(Event e) {
				combo.cut();
				enableDisableComboMenu(queryData, combo, cut, copy, sampleContents);
			}
		});

		// Copy
		copy.setText("Copy");
		copy.addListener(SWT.Selection, new Listener() {

			public void handleEvent(Event e) {
				combo.copy();
			}
		});

		// Paste
		paste.setText("Paste");
		paste.addListener(SWT.Selection, new Listener() {

			public void handleEvent(Event e) {
				combo.paste();
				enableDisableComboMenu(queryData, combo, cut, copy, sampleContents);

			}
		});

		// Sample contents
		sampleContents.setText("Sample Contents...");
		sampleContents.addListener(SWT.Selection, new Listener() {

			public void handleEvent(Event e) {
				queryData.sampleColumnContents(combo);
			}
		});

		// Search
		search.setText("Search...");
		search.addListener(SWT.Selection, new Listener() {

			public void handleEvent(Event e) {
				queryData.searchColumn(combo);
				enableDisableComboMenu(queryData, combo, cut, copy, sampleContents);

			}
		});

		combo.addListener(SWT.MouseDoubleClick, new Listener() {

			public void handleEvent(Event event) {
				queryData.searchColumn(combo);
				enableDisableComboMenu(queryData, combo, cut, copy, sampleContents);

			}
		});

		combo.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				enableDisableComboMenu(queryData, combo, cut, copy, sampleContents);
			}

		});

		enableDisableComboMenu(queryData, combo, cut, copy, sampleContents);

		return menu;
	}

	static protected void enableDisableComboMenu(SQLQueryData queryData, Combo combo, MenuItem cut, MenuItem copy, MenuItem sample) {

		boolean hasText = combo.getText().trim().length() > 0;
		cut.setEnabled(hasText);
		copy.setEnabled(hasText);
		sample.setEnabled(hasText && queryData.isColumnName(combo.getText().trim()));

	}
}
