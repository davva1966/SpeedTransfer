package com.ss.speedtransfer.ui.nattable;

import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.ui.NatEventData;
import org.eclipse.nebula.widgets.nattable.ui.menu.IMenuItemState;

public class HasSelection implements IMenuItemState {

	private final SelectionLayer selectionLayer;

	public HasSelection(SelectionLayer selectionLayer) {
		if (selectionLayer == null) {
			throw new IllegalArgumentException("selectionLayer must not be null."); //$NON-NLS-1$
		}
		this.selectionLayer = selectionLayer;
	}

	@Override
	public boolean isActive(NatEventData natEventData) {
		PositionCoordinate[] pc = selectionLayer.getSelectedCellPositions();
		return pc != null && pc.length > 0;
	}

}