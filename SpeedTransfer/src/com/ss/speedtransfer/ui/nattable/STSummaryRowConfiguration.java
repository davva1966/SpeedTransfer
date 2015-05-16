package com.ss.speedtransfer.ui.nattable;

import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.HorizontalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.summaryrow.DefaultSummaryRowConfiguration;
import org.eclipse.nebula.widgets.nattable.summaryrow.SummaryRowConfigAttributes;
import org.eclipse.nebula.widgets.nattable.summaryrow.SummaryRowLayer;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

public class STSummaryRowConfiguration extends DefaultSummaryRowConfiguration {

	private final IDataProvider dataProvider;

	public STSummaryRowConfiguration(IDataProvider dataProvider) {
		this.dataProvider = dataProvider;
		this.summaryRowBgColor = new Color(Display.getDefault(), 85, 190, 90);
		this.summaryRowFgColor = GUIHelper.COLOR_WHITE;
	}

	protected void addSummaryRowStyleConfig(IConfigRegistry configRegistry) {
		Style cellStyle = new Style();
		cellStyle.setAttributeValue(CellStyleAttributes.FONT, this.summaryRowFont);
		cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, this.summaryRowBgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, this.summaryRowFgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.BORDER_STYLE, this.summaryRowBorderStyle);
		cellStyle.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignmentEnum.RIGHT);
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle, DisplayMode.NORMAL, SummaryRowLayer.DEFAULT_SUMMARY_ROW_CONFIG_LABEL);
	}

	@Override
	public void addSummaryProviderConfig(IConfigRegistry configRegistry) {
		// Summary provider
		STSumProvider sumProvider = new STSumProvider(this.dataProvider);
		configRegistry.registerConfigAttribute(SummaryRowConfigAttributes.SUMMARY_PROVIDER, sumProvider, DisplayMode.NORMAL, SummaryRowLayer.DEFAULT_SUMMARY_ROW_CONFIG_LABEL);

	}
}