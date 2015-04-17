package com.ss.speedtransfer.handlers;

import com.ss.speedtransfer.actions.RunSQLToTableAction;
import com.ss.speedtransfer.model.QueryDefinition;
import com.ss.speedtransfer.util.SSUtil;
import com.ss.speedtransfer.util.UIHelper;

public class RunQueryToTableHandler extends AbstractRunQueryHandler {

	protected void runQuery(QueryDefinition queryDef) throws Exception {
		try {
			RunSQLToTableAction action = new RunSQLToTableAction(queryDef);
			action.run();
		} catch (Exception e) {
			UIHelper.instance().showErrorMsg("Error", SSUtil.getMessage(e));
		}
	}

}
