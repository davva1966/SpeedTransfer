package com.ss.speedtransfer.ui.nattable;

import org.eclipse.nebula.widgets.nattable.selection.config.DefaultSelectionLayerConfiguration;

public class STSelectionLayerConfig extends DefaultSelectionLayerConfiguration {

	@Override
	protected void addSelectionStyleConfig() {
		addConfiguration(new STSelectionStyleConfiguration());
	}

	@Override
	protected void addSelectionUIBindings() {
		addConfiguration(new STSelectionBindings());
	}

}
