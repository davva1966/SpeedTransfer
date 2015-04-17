package com.ss.speedtransfer.ui.editor.querydef;

import com.ss.speedtransfer.model.QueryDefinition;
import com.ss.speedtransfer.xml.editor.XMLAbstractDetailsPage;


public abstract class AbstractQueryDefinitionDetailsPage extends XMLAbstractDetailsPage {

	public AbstractQueryDefinitionDetailsPage(QueryDefinition model) {
		super(model);
	}

	public QueryDefinition getQueryDef() {
		return (QueryDefinition) model;
	}

}
