package com.ss.speedtransfer.ui.nattable;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum;
import org.eclipse.nebula.widgets.nattable.selection.action.AbstractKeySelectAction;
import org.eclipse.nebula.widgets.nattable.selection.command.MoveSelectionCommand;
import org.eclipse.swt.events.KeyEvent;

public class STMoveToLastCellAction extends AbstractKeySelectAction {

	public STMoveToLastCellAction(boolean shiftMask, boolean ctrlMask) {
		super(MoveDirectionEnum.DOWN, shiftMask, ctrlMask);
	}

	@Override
	public void run(NatTable natTable, KeyEvent event) {
		super.run(natTable, event);
		natTable.doCommand(new MoveSelectionCommand(MoveDirectionEnum.DOWN, SelectionLayer.MOVE_ALL, isShiftMask(), isControlMask()));
		natTable.doCommand(new MoveSelectionCommand(MoveDirectionEnum.RIGHT, SelectionLayer.MOVE_ALL, isShiftMask(), isControlMask()));
	}

}
