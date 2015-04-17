package com.ss.speedtransfer;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;

import com.ss.speedtransfer.util.DefaultDBManager;
import com.ss.speedtransfer.util.UIHelper;


public class DefaultDBDecorator implements ILightweightLabelDecorator {

	private static final ImageDescriptor IMAGE;

	static {
		IMAGE = UIHelper.instance().getImageDescriptor("check.gif");
	}

	@Override
	public void decorate(Object element, IDecoration decoration) {
		if (element instanceof IFile == false)
			return;

		IFile file = (IFile) element;
		if (DefaultDBManager.instance().isDefault(file))
			decoration.addOverlay(IMAGE);

	}

	@Override
	public void addListener(ILabelProviderListener listener) {

	}

	@Override
	public void dispose() {

	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {

	}

}
