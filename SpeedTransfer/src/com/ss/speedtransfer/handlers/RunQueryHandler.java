package com.ss.speedtransfer.handlers;

import org.eclipse.jface.action.IAction;

import com.ss.speedtransfer.actions.RunSQLAction;
import com.ss.speedtransfer.model.QueryDefinition;


public class RunQueryHandler extends AbstractRunQueryHandler {

	protected String type = null;

	protected void runQuery(QueryDefinition queryDef) throws Exception {
		IAction action = new RunSQLAction(queryDef);
		action.run();

	}
}
