package com.ss.speedtransfer.ui.nattable;

import java.math.BigDecimal;

import org.apache.commons.lang3.math.NumberUtils;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.summaryrow.ISummaryProvider;

public class STSumProvider implements ISummaryProvider {

	private final IDataProvider dataProvider;

	public STSumProvider(IDataProvider dataProvider) {
		this.dataProvider = dataProvider;
	}

	public Object summarize(int columnIndex) {
		int rowCount = this.dataProvider.getRowCount();
		double summaryValue = 0;

		for (int rowIndex = 1; rowIndex < rowCount; rowIndex++) {
			Object dataValue = this.dataProvider.getDataValue(columnIndex, rowIndex);

			if (dataValue != null) {
				if (dataValue instanceof Number) {
					summaryValue += ((Number) dataValue).doubleValue();
				} else if (dataValue instanceof String) {
					String value = (String) dataValue;
					if (value.length() == 0)
						continue;
					if (NumberUtils.isNumber(value)) {
						BigDecimal bd;
						try {
							bd = NumberUtils.createBigDecimal(value);
							summaryValue += bd.doubleValue();
						} catch (Exception e) {
						}
					} else {
						return "";
					}
				} else {
					return "";
				}
			}
		}

		return summaryValue;
	}

}
