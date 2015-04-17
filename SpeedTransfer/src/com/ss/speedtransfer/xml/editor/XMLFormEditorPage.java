package com.ss.speedtransfer.xml.editor;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import com.ss.speedtransfer.SpeedTransferPlugin;


public class XMLFormEditorPage extends FormPage {

	protected XMLFormPageBlock block;

	public XMLFormEditorPage(FormEditor editor, XMLFormPageBlock block, XMLModel model) {
		super(editor, "", "                        ");
		this.block = block;
	}

	protected void createFormContent(final IManagedForm managedForm) {
		final ScrolledForm form = managedForm.getForm();
		form.setText(block.getMasterTitle());
		form.setBackgroundImage(SpeedTransferPlugin.getImageDescriptor("form_banner.gif").createImage());
		block.createContent(managedForm);
	}

	public void init(IEditorSite site, IEditorInput input) {
		if (input instanceof IFileEditorInput == false)
			return;
		super.init(site, input);

	}

	public XMLFormPageBlock getBlock() {
		return block;
	}

}