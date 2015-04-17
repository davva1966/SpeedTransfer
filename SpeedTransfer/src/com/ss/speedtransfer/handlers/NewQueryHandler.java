package com.ss.speedtransfer.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.handlers.HandlerUtil;

import com.ss.speedtransfer.ui.wizard.NewQueryDefinitionWizard;


@SuppressWarnings("restriction")
public class NewQueryHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().getSelection();
		if (selection instanceof IStructuredSelection) {
			NewQueryDefinitionWizard wizard = new NewQueryDefinitionWizard();
			wizard.init(HandlerUtil.getActiveWorkbenchWindow(event).getWorkbench(), (IStructuredSelection) selection);
			WizardDialog dialog = new WizardDialog(HandlerUtil.getActiveWorkbenchWindow(event).getShell(), wizard);
			dialog.open();
		}

		return null;
	}
}
