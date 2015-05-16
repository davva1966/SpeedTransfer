package com.ss.speedtransfer.ui.nattable;

import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultRowHeaderDataProvider;

public class STSummaryRowHeaderDataProvider extends DefaultRowHeaderDataProvider implements IDataProvider {

	public static final String DEFAULT_SUMMARY_ROW_LABEL = "\u2211"; //$NON-NLS-1$
	private final String summaryRowLabel;

	public STSummaryRowHeaderDataProvider(IDataProvider bodyDataProvider) {
		this(bodyDataProvider, DEFAULT_SUMMARY_ROW_LABEL);
	}

	public STSummaryRowHeaderDataProvider(IDataProvider bodyDataProvider, String summaryRowLabel) {
		super(bodyDataProvider);
		this.summaryRowLabel = summaryRowLabel;
	}

	@Override
	public int getRowCount() {
		return super.getRowCount() + 1;
	}

	@Override
	public Object getDataValue(int columnIndex, int rowIndex) {
		if (rowIndex == super.getRowCount()) {
			return this.summaryRowLabel;
		}
		if (rowIndex == 0)
			return "";
		
		return super.getDataValue(columnIndex, rowIndex - 1);
	}
}
