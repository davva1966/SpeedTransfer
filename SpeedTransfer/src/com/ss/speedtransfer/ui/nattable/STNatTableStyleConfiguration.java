package com.ss.speedtransfer.ui.nattable;

import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.TextPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.PaddingDecorator;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.HorizontalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;

public class STNatTableStyleConfiguration extends AbstractRegistryConfiguration {

	public ICellPainter cellPainter = new PaddingDecorator(new TextPainter(), 3);

	@Override
	public void configureRegistry(IConfigRegistry configRegistry) {

		configRegistry.unregisterConfigAttribute(CellConfigAttributes.CELL_PAINTER);
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, this.cellPainter);

		// Heading style left
		Style cellStyle = new Style();
		cellStyle.setAttributeValue(CellStyleAttributes.FONT, GUIHelper.getFont(new FontData("Arial", 10, SWT.BOLD)));
		cellStyle.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignmentEnum.LEFT);
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle, DisplayMode.NORMAL, STDataLabelAccumulator.HEADING_LABEL_LEFT);

		// Heading style right
		cellStyle = new Style();
		cellStyle.setAttributeValue(CellStyleAttributes.FONT, GUIHelper.getFont(new FontData("Arial", 10, SWT.BOLD)));
		cellStyle.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignmentEnum.RIGHT);
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle, DisplayMode.NORMAL, STDataLabelAccumulator.HEADING_LABEL_RIGHT);

		// Heading style center
		cellStyle = new Style();
		cellStyle.setAttributeValue(CellStyleAttributes.FONT, GUIHelper.getFont(new FontData("Arial", 10, SWT.BOLD)));
		cellStyle.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignmentEnum.CENTER);
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle, DisplayMode.NORMAL, STDataLabelAccumulator.HEADING_LABEL_CENTER);

		// Data style left
		cellStyle = new Style();
		cellStyle.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignmentEnum.LEFT);
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle, DisplayMode.NORMAL, STDataLabelAccumulator.DATA_LABEL_LEFT);

		// Data style right
		cellStyle = new Style();
		cellStyle.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignmentEnum.RIGHT);
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle, DisplayMode.NORMAL, STDataLabelAccumulator.DATA_LABEL_RIGHT);

		// Data style center
		cellStyle = new Style();
		cellStyle.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignmentEnum.CENTER);
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, cellStyle, DisplayMode.NORMAL, STDataLabelAccumulator.DATA_LABEL_CENTER);

	}

}
