package com.ss.speedtransfer.ui.view;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.ole.win32.OLE;
import org.eclipse.swt.ole.win32.OleClientSite;
import org.eclipse.swt.ole.win32.OleFrame;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import com.ss.speedtransfer.util.SSUtil;
import com.ss.speedtransfer.util.UIHelper;


public class QueryExcelResultView extends ViewPart {

	public static final String ID = "com.ss.speedtransfer.queryExcelResultView";

	private OleFrame frame;
	private OleClientSite site;
	protected File file;

	public QueryExcelResultView() {
	}

	public void setFileName(String fileName) {
		try {
			this.file = new File(fileName);
			site = new OleClientSite(frame, SWT.NONE, file);
			site.doVerb(OLE.OLEIVERB_INPLACEACTIVATE);
		} catch (Exception e) {
			UIHelper.instance().showErrorMsg("Error", "Unable to open excel activeX control. Error: " + SSUtil.getMessage(e));
			return;
		}

	}

	public void setName(String name) {
		setPartName(name);

	}

	public void setTooltip(String text) {
		setTitleToolTip(text);

	}

	@Override
	public void createPartControl(Composite parent) {
		try {
			frame = new OleFrame(parent, SWT.NONE);
			// File file = new File(fileName);
			// site = new OleClientSite(frame, SWT.NONE, file);
			// site.doVerb(OLE.OLEIVERB_INPLACEACTIVATE);
		} catch (SWTError e) {
			UIHelper.instance().showErrorMsg("Error", "Unable to open excel activeX control. Error: " + SSUtil.getMessage(e));
			return;
		}
	}

	@Override
	public void setFocus() {
		frame.setFocus();

	}

	@Override
	public void dispose() {
		if (site != null)
			site.dispose();
		if (frame != null)
			frame.dispose();
		super.dispose();
		if (file != null) {
			try {
				file.delete();
			} catch (Exception e) {
			}
		}
	}

}
