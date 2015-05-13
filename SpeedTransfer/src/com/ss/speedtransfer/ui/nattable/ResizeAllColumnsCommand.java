package com.ss.speedtransfer.ui.nattable;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.resize.command.InitializeAutoResizeColumnsCommand;
import org.eclipse.nebula.widgets.nattable.util.GCFactory;

public class ResizeAllColumnsCommand extends InitializeAutoResizeColumnsCommand {

	protected int columnCount;

	public ResizeAllColumnsCommand(NatTable natTable, int columnCount, int columnPosition, IConfigRegistry configRegistry, GCFactory gcFactory) {
		super(natTable, columnPosition, configRegistry, gcFactory);
		this.columnCount = columnCount;
	}

	protected ResizeAllColumnsCommand(ResizeAllColumnsCommand command) {
		super(command);
		this.columnCount = command.columnCount;
	}

	@Override
	public ILayerCommand cloneCommand() {
		return new ResizeAllColumnsCommand(this);
	}

	@Override
	public void setSelectedColumnPositions(int[] selectedColumnPositions) {
		super.setSelectedColumnPositions(selectedColumnPositions);
	}

	@Override
	public int[] getColumnPositions() {
		int[] columnPositions = new int[columnCount];
		for (int i = 0; i < columnPositions.length; i++) {
			columnPositions[i] = i;

		}
		return columnPositions;
	}

}
