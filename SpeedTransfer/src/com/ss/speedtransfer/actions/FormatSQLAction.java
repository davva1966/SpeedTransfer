package com.ss.speedtransfer.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;

import com.ss.speedtransfer.ui.StyledEdit;
import com.ss.speedtransfer.util.UIHelper;


public class FormatSQLAction extends Action {

	StyledEdit sqlEdit;

	public FormatSQLAction(StyledEdit sqlEdit) {
		super("Format SQL", IAction.AS_PUSH_BUTTON);
		this.sqlEdit = sqlEdit;
		setImageDescriptor(UIHelper.instance().getImageDescriptor("formatSQL.gif"));

	}

	public void run() {
		if (sqlEdit.canDoOperation(StyledEdit.FORMAT))
			sqlEdit.doOperation(StyledEdit.FORMAT);
	}

}
