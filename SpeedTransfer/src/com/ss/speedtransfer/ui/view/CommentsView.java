package com.ss.speedtransfer.ui.view;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.part.ViewPart;
import org.w3c.dom.Node;

import com.ss.speedtransfer.model.QueryDefinition;
import com.ss.speedtransfer.ui.StyledEdit;
import com.ss.speedtransfer.ui.editor.querydef.SQLScratchPadEditor;
import com.ss.speedtransfer.util.UIHelper;
import com.ss.speedtransfer.xml.editor.XMLFormEditor;
import com.ss.speedtransfer.xml.editor.XMLModel;
import com.ss.speedtransfer.xml.editor.XMLModelListener;


public class CommentsView extends ViewPart implements XMLModelListener {
	public static final String ID = "com.ss.speedtransfer.commentsView";

	FormToolkit toolkit;
	private ScrolledForm form;
	private StyledEdit text;
	private XMLModel model;

	ISelectionListener selectionListener = new ISelectionListener() {
		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
			if (selection instanceof IStructuredSelection) {
				IStructuredSelection sel = (IStructuredSelection) selection;
				Object selectedItem = sel.getFirstElement();
				if (selectedItem instanceof IFile) {
					setName(((IFile) selectedItem).getName());
					try {
						XMLModel newModel = new XMLModel((IFile) selectedItem);
						setModel(newModel);
					} catch (Exception e) {
					}
				}
			}

		}
	};

	IPartListener partListener = new IPartListener() {
		public void partOpened(IWorkbenchPart part) {
		}

		public void partDeactivated(IWorkbenchPart part) {

		}

		public void partClosed(IWorkbenchPart part) {
		}

		public void partBroughtToTop(IWorkbenchPart part) {
		}

		public void partActivated(IWorkbenchPart part) {
			if (part instanceof XMLFormEditor) {
				setModel(((XMLFormEditor) part).getModel());
				setName(((XMLFormEditor) part).getTitle());
			} else if (part instanceof SQLScratchPadEditor) {
				setModel(((SQLScratchPadEditor) part).getQueryDefinition());
				setName(((SQLScratchPadEditor) part).getTitle());
			}
		}
	};

	public void createPartControl(Composite parent) {

		parent.setLayout(new FillLayout());

		toolkit = new FormToolkit(parent.getDisplay());

		form = toolkit.createScrolledForm(parent);

		try {
			form.setBackgroundImage(UIHelper.instance().getImageDescriptor("form_banner.gif").createImage());
		} catch (Exception e) {
		}

		form.setText("Comments");
		form.getBody().setLayout(new FillLayout());
		toolkit.decorateFormHeading(form.getForm());

		Composite comp = toolkit.createComposite(form.getBody());
		GridLayout gl = new GridLayout(1, false);
		gl.marginHeight = 0;
		gl.marginWidth = 0;
		comp.setLayout(gl);

		text = new StyledEdit(comp, new TextSourceViewerConfiguration(), SWT.V_SCROLL | SWT.H_SCROLL | SWT.READ_ONLY, false);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.heightHint = 100;
		text.getControl().setLayoutData(gd);

		getSite().getWorkbenchWindow().getSelectionService().addSelectionListener(selectionListener);
		getSite().getWorkbenchWindow().getPartService().addPartListener(partListener);
	}

	public void setModel(XMLModel newModel) {
		if (model != null)
			model.removeModelListener(this);
		model = newModel;
		model.addModelListener(this);
		try {
			text.setText(getComments());
		} catch (Exception e) {
		}

	}

	public void setName(String name) {
		form.setText("Comments for " + name);

	}

	@Override
	public void setFocus() {

	}

	public String getComments() {
		String text = "";
		Node node = model.getNode(QueryDefinition.COMMENT);
		if (node != null)
			text = model.getCDATA(node);

		if (text == null || text.trim().length() == 0)
			text = "** No Comments Found**";

		return text;
	}

	@Override
	public void dispose() {
		if (toolkit != null)
			toolkit.dispose();

		if (model != null)
			model.removeModelListener(this);

		getSite().getWorkbenchWindow().getSelectionService().removeSelectionListener(selectionListener);
		getSite().getWorkbenchWindow().getPartService().removePartListener(partListener);

		super.dispose();
	}

	@Override
	public void modelChanged(Object[] objects, String type, String property) {
		try {
			if (type.equals(XMLModelListener.ATTR_CHANGED) && property.equalsIgnoreCase(QueryDefinition.COMMENT))
				text.setText(getComments());
		} catch (Exception e) {
		}

	}

}
