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

import com.ss.speedtransfer.model.QueryDefinition;


public class DBConnectionResourceListener implements IResourceChangeListener {

	private static final IPath DOC_PATH = new Path("/");

	protected QueryDefinition queryDef;

	public DBConnectionResourceListener(QueryDefinition queryDef) {
		super();
		this.queryDef = queryDef;
		listenForResourceChanges();
	}

	public void dispose() {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		workspace.removeResourceChangeListener(this);
	}

	protected void listenForResourceChanges() {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		workspace.addResourceChangeListener(this);
	}

	@Override
	public void resourceChanged(IResourceChangeEvent event) {

		if (queryDef.getDBConnectionFile() == null || queryDef.getDBConnectionFile().trim().length() == 0)
			return;

		IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(queryDef.getDBConnectionFile().trim()));
		if (file.exists() == false) {
			queryDef.resetDBConnection();
			return;
		}

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
				// only interested in changed resources
				if (delta.getKind() == IResourceDelta.CHANGED && delta.getFlags() == IResourceDelta.CONTENT) {

					IResource resource = delta.getResource();
					// only interested in files with the "xml" extension
					if (resource.getType() == IResource.FILE && resource.getFileExtension().equalsIgnoreCase("xml")) {
						if (resource.getFullPath().toOSString().equalsIgnoreCase(queryDef.getDBConnectionFile().trim())) {
							queryDef.resetDBConnection();
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

	@Override
	protected void finalize() throws Throwable {
		try {
			dispose();
		} catch (Exception e) {
		}
		super.finalize();

	}

}
