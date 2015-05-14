package com.ss.speedtransfer.ui.nattable;

import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer.MoveDirectionEnum;
import org.eclipse.nebula.widgets.nattable.selection.action.MoveSelectionAction;
import org.eclipse.nebula.widgets.nattable.selection.action.MoveToFirstRowAction;
import org.eclipse.nebula.widgets.nattable.selection.action.MoveToLastRowAction;
import org.eclipse.nebula.widgets.nattable.selection.config.DefaultSelectionBindings;
import org.eclipse.nebula.widgets.nattable.ui.action.IKeyAction;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.ui.matcher.KeyEventMatcher;
import org.eclipse.swt.SWT;

public class SelectionBindings extends DefaultSelectionBindings {

	protected void configureMoveDownBindings(UiBindingRegistry uiBindingRegistry, IKeyAction action) {
		uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.NONE, SWT.ARROW_DOWN), action);
		uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.SHIFT, SWT.ARROW_DOWN), action);
		uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.MOD1, SWT.ARROW_DOWN), new MoveToLastRowAction());
		uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.SHIFT | SWT.MOD1, SWT.ARROW_DOWN), new STMoveToLastRowAction());

		uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.NONE, SWT.CR), action);
		uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.MOD1, SWT.CR), action);
	}

	protected void configureMoveUpBindings(UiBindingRegistry uiBindingRegistry, IKeyAction action) {
		uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.NONE, SWT.ARROW_UP), action);
		uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.SHIFT, SWT.ARROW_UP), action);
		uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.MOD1, SWT.ARROW_UP), new MoveToFirstRowAction());
		uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.SHIFT | SWT.MOD1, SWT.ARROW_UP), new STMoveToFirstRowAction());

		uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.SHIFT, SWT.CR), new MoveSelectionAction(MoveDirectionEnum.UP, false, false));
		uiBindingRegistry.registerKeyBinding(new KeyEventMatcher(SWT.SHIFT | SWT.MOD1, SWT.CR), action);
	}

}
