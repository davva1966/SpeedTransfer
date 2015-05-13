package com.ss.speedtransfer.ui.nattable;

import org.eclipse.nebula.widgets.nattable.freeze.CompositeFreezeLayer;
import org.eclipse.nebula.widgets.nattable.ui.NatEventData;
import org.eclipse.nebula.widgets.nattable.ui.menu.IMenuItemState;

public class IsFrozen implements IMenuItemState {

	private final CompositeFreezeLayer freezeLayer;

	public IsFrozen(CompositeFreezeLayer freezeLayer) {
		if (freezeLayer == null) {
			throw new IllegalArgumentException("freezeLayer must not be null."); //$NON-NLS-1$
		}
		this.freezeLayer = freezeLayer;
	}

	@Override
	public boolean isActive(NatEventData natEventData) {
		return freezeLayer.isFrozen();
	}
}