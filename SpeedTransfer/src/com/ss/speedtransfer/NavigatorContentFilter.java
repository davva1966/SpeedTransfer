package com.ss.speedtransfer;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

public class NavigatorContentFilter extends ViewerFilter {

	public NavigatorContentFilter() {
	}

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {

		if (element instanceof IProject)
			return true;

		if (element instanceof IFolder)
			return true;

		if (element instanceof IFile) {
			if (((IFile) element).getName().toLowerCase().endsWith("xml"))
				return true;
		}

		return false;
	}

}
