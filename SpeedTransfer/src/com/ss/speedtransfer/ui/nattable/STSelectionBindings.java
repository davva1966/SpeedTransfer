package com.ss.speedtransfer.ui.nattable;

import org.eclipse.nebula.widgets.nattable.selection.config.DefaultSelectionBindings;
import org.eclipse.nebula.widgets.nattable.ui.action.IKeyAction;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.ui.matcher.KeyEventMatcher;
import org.eclipse.swt.SWT;

public class STSelectionBindings extends DefaultSelectionBindings {

	protected void configureMoveDownBindings(UiBindingRegistry uiBindingRegistry, IKeyAction action) {
		super.configureMoveDownBindings(uiBindingRegistry, action);
		uiBindingRegistry.unregisterKeyBinding(new KeyEventMatcher(SWT.SHIFT | SWT.MOD1, SWT.ARROW_DOWN));
		uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.SHIFT | SWT.MOD1, SWT.ARROW_DOWN), new STMoveToLastRowAction(true, true));
		uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.SHIFT | SWT.MOD1, SWT.END), new STMoveToLastCellAction(true, true));
	}

	protected void configureMoveUpBindings(UiBindingRegistry uiBindingRegistry, IKeyAction action) {
		super.configureMoveUpBindings(uiBindingRegistry, action);
		uiBindingRegistry.unregisterKeyBinding(new KeyEventMatcher(SWT.SHIFT | SWT.MOD1, SWT.ARROW_UP));
		uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.SHIFT | SWT.MOD1, SWT.ARROW_UP), new STMoveToFirstRowAction(true, true));
		uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.SHIFT | SWT.MOD1, SWT.HOME), new STMoveToFirstCellAction(true, true));
	}

}
