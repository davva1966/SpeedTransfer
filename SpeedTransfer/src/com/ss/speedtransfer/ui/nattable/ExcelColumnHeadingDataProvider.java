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

public class ExcelColumnHeadingDataProvider implements IDataProvider {

	private final QueryDefinitionDataProvider queryDefinitionDataProvider;

	public ExcelColumnHeadingDataProvider(QueryDefinitionDataProvider queryDefinitionDataProvider) {
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
		return excelColumnName(columnIndex+1);
	}

	@Override
	public void setDataValue(int columnIndex, int rowIndex, Object newValue) {
		throw new UnsupportedOperationException();
	}

	private static String excelColumnName(int colNum) {
		int Base = 26;
		String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		String colName = "";

		while (colNum > 0) {
			int position = colNum % Base;
			colName = (position == 0 ? 'Z' : chars.charAt(position > 0 ? position - 1 : 0)) + colName;
			colNum = (colNum - 1) / Base;
		}
		return colName;
	}

}
