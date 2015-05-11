package com.ss.speedtransfer.handlers;

import com.ss.speedtransfer.actions.RunSQLToNatTableAction;
import com.ss.speedtransfer.model.QueryDefinition;
import com.ss.speedtransfer.util.SSUtil;
import com.ss.speedtransfer.util.UIHelper;

public class RunQueryToNatTableHandler extends AbstractRunQueryHandler {

	protected void runQuery(QueryDefinition queryDef) throws Exception {
		try {
			RunSQLToNatTableAction action = new RunSQLToNatTableAction(queryDef);
			action.run();
		} catch (Exception e) {
			UIHelper.instance().showErrorMsg("Error", SSUtil.getMessage(e));
		}
	}

}
