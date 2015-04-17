// {{CopyrightNotice}}

package com.ss.speedtransfer.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;

public class ActionDropDownMenuCreator implements IMenuCreator{

	protected MenuManager dropDownMenuMgr;

	protected List<IAction> actions = new ArrayList<IAction>();

	/**
	 * Add an action to the drop down
	 */
	public void addAction(IAction action){
		actions.add(action);

	}

	/**
	 * Creates the menu manager for the drop-down.
	 */
	protected void createDropDownMenuMgr(){
		if(dropDownMenuMgr == null){
			dropDownMenuMgr = new MenuManager();
			for(IAction action : actions){
				dropDownMenuMgr.add(action);
			}
		}

	}

	public Menu getMenu(Control parent){
		createDropDownMenuMgr();
		return dropDownMenuMgr.createContextMenu(parent);
	}

	public Menu getMenu(Menu parent){
		createDropDownMenuMgr();
		Menu menu = new Menu(parent);
		IContributionItem[] items = dropDownMenuMgr.getItems();
		for(int i = 0; i < items.length; i++){
			IContributionItem item = items[i];
			IContributionItem newItem = item;
			if(item instanceof ActionContributionItem){
				newItem = new ActionContributionItem(((ActionContributionItem) item).getAction());
			}
			newItem.fill(menu, -1);
		}
		return menu;
	}

	public void dispose(){
		if(dropDownMenuMgr != null){
			dropDownMenuMgr.dispose();
			dropDownMenuMgr = null;
		}
	}

}
