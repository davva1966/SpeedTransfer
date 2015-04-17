package com.ss.speedtransfer.handlers;

import com.ss.speedtransfer.actions.RunSQLToExcelAction;
import com.ss.speedtransfer.model.QueryDefinition;
import com.ss.speedtransfer.util.SSUtil;
import com.ss.speedtransfer.util.UIHelper;

public class RunQueryToExcelHandler extends AbstractRunQueryHandler {

	protected void runQuery(QueryDefinition queryDef) throws Exception {
		try {
			RunSQLToExcelAction action = new RunSQLToExcelAction(queryDef);
			action.run();
		} catch (Exception e) {
			UIHelper.instance().showErrorMsg("Error", SSUtil.getMessage(e));
		}
	}

}
