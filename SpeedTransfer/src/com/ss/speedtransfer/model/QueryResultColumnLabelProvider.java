package com.ss.speedtransfer.model;

import java.util.List;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

public class QueryResultColumnLabelProvider extends ColumnLabelProvider {

	int colIndex;
	Color rowColumnColor = null;

	public QueryResultColumnLabelProvider(int colIndex) {
		super();
		this.colIndex = colIndex;
	}

	public String getText(Object element) {
		if (element instanceof List<?>) {
			List<String> row = (List<String>) element;
			return row.get(colIndex);
		} else {
			return "";
		}
	}

	public Color getBackground(Object element) {
		if (colIndex == 0)
			return getRowColor();

		return null;

	}

	protected Color getRowColor() {
		if (rowColumnColor == null) {
			Display display = Display.getCurrent();
			if (display == null)
				display = Display.getDefault();
			rowColumnColor = display.getSystemColor(SWT.COLOR_INFO_BACKGROUND);
		}

		return rowColumnColor;
	}

}