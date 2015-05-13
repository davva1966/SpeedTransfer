/*******************************************************************************
 * Copyright (c) 2012 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package com.ss.speedtransfer.ui.nattable;

import org.eclipse.nebula.widgets.nattable.data.IDataProvider;

public class QueryDefinitionColumnHeadingDataProvider implements IDataProvider {

	private final QueryDefinitionDataProvider queryDefinitionDataProvider;

	public QueryDefinitionColumnHeadingDataProvider(QueryDefinitionDataProvider queryDefinitionDataProvider) {
		this.queryDefinitionDataProvider = queryDefinitionDataProvider;
	}

	@Override
	public int getColumnCount() {
		return queryDefinitionDataProvider.getColumnCount();
	}

	@Override
	public int getRowCount() {
		return 1;
	}

	@Override
	public Object getDataValue(int columnIndex, int rowIndex) {
		String[] props = queryDefinitionDataProvider.getColumnProperties(columnIndex);
		if (props == null || props.length == 0)
			return "Column " + (columnIndex + 1); //$NON-NLS-1$
		return props[4];
	}

	@Override
	public void setDataValue(int columnIndex, int rowIndex, Object newValue) {
		throw new UnsupportedOperationException();
	}

}
