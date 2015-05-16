package com.ss.speedtransfer.ui.nattable;

import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.IConfigLabelAccumulator;

public class STDataLabelAccumulator implements IConfigLabelAccumulator {

	public static final String HEADING_LABEL_LEFT = "headingLabelLeft";
	public static final String HEADING_LABEL_RIGHT = "headingLabelRight";
	public static final String HEADING_LABEL_CENTER = "headingLabelCenter";

	public static final String DATA_LABEL_LEFT = "dataLabelLeft";
	public static final String DATA_LABEL_RIGHT = "dataLabelRight";
	public static final String DATA_LABEL_CENTER = "dataLabelCenter";

	protected QueryDefinitionDataProvider dataProvider;

	public STDataLabelAccumulator(QueryDefinitionDataProvider dataProvider) {
		super();
		this.dataProvider = dataProvider;
	}

	@Override
	public void accumulateConfigLabels(LabelStack configLabels, int columnPosition, int rowPosition) {

		if (dataProvider.getColumnProperties() == null || dataProvider.getColumnProperties().size() == 0)
			return;
		String[] colProps = dataProvider.columnProperties.get(columnPosition);

		if (rowPosition == 0) {
			if (colProps[6].equalsIgnoreCase("left"))
				configLabels.addLabel(HEADING_LABEL_LEFT);
			else if (colProps[6].equalsIgnoreCase("right"))
				configLabels.addLabel(HEADING_LABEL_RIGHT);
			else if (colProps[6].equalsIgnoreCase("center"))
				configLabels.addLabel(HEADING_LABEL_CENTER);
			else
				configLabels.addLabel(HEADING_LABEL_CENTER);
		} else {
			if (colProps[6].equalsIgnoreCase("left"))
				configLabels.addLabel(DATA_LABEL_LEFT);
			else if (colProps[6].equalsIgnoreCase("right"))
				configLabels.addLabel(DATA_LABEL_RIGHT);
			else if (colProps[6].equalsIgnoreCase("center"))
				configLabels.addLabel(DATA_LABEL_CENTER);
			else
				configLabels.addLabel(DATA_LABEL_CENTER);
		}

	}
}
