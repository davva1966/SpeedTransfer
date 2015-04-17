package com.ss.speedtransfer.util;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.StatusLineContributionItem;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PlatformUI;

import com.ss.speedtransfer.model.DBConnection;


public class DefaultDBManager implements IResourceChangeListener {

	public static final String DEFAULT_DB_SETTING = "default.db.connection";
	public static final String DECORATOR_ID = "com.ss.speedtransfer.defaultDBDecorator";
	public static final String STATUS_LINE_ID = "com.ss.speedtransfer.defaultDBStatus";

	private static final IPath DOC_PATH = new Path("/");

	private static DefaultDBManager instance;

	static {
		instance = new DefaultDBManager();
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		workspace.addResourceChangeListener(instance);
	}

	protected DefaultDBManager() {
		super();
	}

	public static DefaultDBManager instance() {
		return instance;
	}

	public boolean isDefault(IFile file) {
		return file.getFullPath().toOSString().equalsIgnoreCase(SettingsManager.get(DEFAULT_DB_SETTING));
	}

	public void makeDefault(final IFile file) {
		SettingsManager.set(DEFAULT_DB_SETTING, file.getFullPath().toOSString());

		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				updateStatusLine(file.getFullPath().toOSString());
				PlatformUI.getWorkbench().getDecoratorManager().update(DECORATOR_ID);
			}
		});

	}

	public void removeDefault() {
		SettingsManager.remove(DEFAULT_DB_SETTING);

		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				updateStatusLine("");
				PlatformUI.getWorkbench().getDecoratorManager().update(DECORATOR_ID);
			}
		});

	}

	public boolean hasDefaultConnection() {
		String defaultFile = SettingsManager.get(DEFAULT_DB_SETTING);
		return defaultFile != null && defaultFile.trim().length() > 0;

	}

	public DBConnection getDefaultConnection() {
		if (hasDefaultConnection() == false)
			return null;

		String defaultFile = SettingsManager.get(DEFAULT_DB_SETTING);
		try {
			IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(defaultFile));
			return new DBConnection(file);
		} catch (Exception e) {
			return null;
		}

	}

	protected void updateStatusLine(final String defaultDB) {

		IWorkbenchPartSite site = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart().getSite();
		if (site instanceof IViewSite) {
			IContributionItem item = ((IViewSite) site).getActionBars().getStatusLineManager().find(STATUS_LINE_ID);
			if (item != null) {
				if (defaultDB.trim().length() > 0)
					((StatusLineContributionItem) item).setText("Default Connection: " + defaultDB);
				else
					((StatusLineContributionItem) item).setText("No Default Connection");
			}
		}

	}

	@Override
	public void resourceChanged(IResourceChangeEvent event) {

		if (hasDefaultConnection() == false)
			return;

		// we are only interested in POST_CHANGE events
		if (event.getType() != IResourceChangeEvent.POST_CHANGE)
			return;
		IResourceDelta rootDelta = event.getDelta();
		// get the delta, if any, for the root directory
		IResourceDelta docDelta = rootDelta.findMember(DOC_PATH);
		if (docDelta == null)
			return;

		IResourceDeltaVisitor visitor = new IResourceDeltaVisitor() {

			public boolean visit(IResourceDelta delta) {
				// only interested in deletions
				if (delta.getKind() == IResourceDelta.REMOVED) {
					IResource resource = delta.getResource();
					if (resource.getType() == IResource.FILE && resource.getFileExtension().equalsIgnoreCase("xml")) {
						if (resource.getFullPath().toOSString().equalsIgnoreCase(SettingsManager.get(DEFAULT_DB_SETTING))) {
							removeDefault();
							return false;
						}
					}

				}
				return true;
			}
		};
		try {
			docDelta.accept(visitor);
		} catch (CoreException e) {
		}

	}

}
