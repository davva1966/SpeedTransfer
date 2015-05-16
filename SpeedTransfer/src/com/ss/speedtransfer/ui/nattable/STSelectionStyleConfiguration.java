package com.ss.speedtransfer.ui.nattable;

import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.selection.config.DefaultSelectionStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.graphics.Font;

public class STSelectionStyleConfiguration extends DefaultSelectionStyleConfiguration {

	public Font selectionFont = GUIHelper.DEFAULT_FONT;

	@Override
	protected void configureSelectionStyle(IConfigRegistry configRegistry) {
		Style cellStyle = new Style();
		cellStyle.setAttributeValue(CellStyleAttributes.FONT, this.selectionFont);
		cellStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, this.selectionBgColor);
		cellStyle.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, this.selectionFgColor);

		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle, DisplayMode.SELECT);
	}

}
