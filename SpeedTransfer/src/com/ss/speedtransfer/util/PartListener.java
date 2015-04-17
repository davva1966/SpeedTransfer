package com.ss.speedtransfer.util;

import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PlatformUI;

import com.ss.speedtransfer.ui.editor.dbcon.DBConnectionEditor;
import com.ss.speedtransfer.ui.editor.querydef.QueryDefinitonEditor;
import com.ss.speedtransfer.ui.editor.querydef.SQLScratchPadEditor;
import com.ss.speedtransfer.ui.view.CommentsView;
import com.ss.speedtransfer.ui.view.QueryExcelResultView;
import com.ss.speedtransfer.ui.view.QueryResultView;
import com.ss.speedtransfer.ui.view.StartView;


public class PartListener implements IPartListener2 {

	private static PartListener instance = null;

	protected boolean started = false;

	static {
		instance = new PartListener();
	}

	public static PartListener instance() {
		return instance;
	}

	public synchronized void start() {
		if (started)
			return;

		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		page.addPartListener(this);

		started = true;

	}

	@Override
	public void partActivated(IWorkbenchPartReference partRef) {
		if (partRef.getPart(false) instanceof SQLScratchPadEditor) {
			((SQLScratchPadEditor) partRef.getPart(false)).partActivated();
		}
		if (partRef.getPart(false) instanceof QueryDefinitonEditor) {
			((QueryDefinitonEditor) partRef.getPart(false)).partActivated();
		}

	}

	@Override
	public void partBroughtToTop(IWorkbenchPartReference partRef) {

	}

	@Override
	public void partClosed(IWorkbenchPartReference partRef) {
		maybeShowStartView(partRef.getPage());

	}

	@Override
	public void partDeactivated(IWorkbenchPartReference partRef) {

	}

	@Override
	public void partOpened(IWorkbenchPartReference partRef) {

	}

	@Override
	public void partHidden(IWorkbenchPartReference partRef) {

	}

	@Override
	public void partVisible(IWorkbenchPartReference partRef) {
		if (partRef.getId().equalsIgnoreCase(QueryResultView.ID)) {
			hideStartView(partRef);
		}
		if (partRef.getId().equalsIgnoreCase(QueryExcelResultView.ID)) {
			hideStartView(partRef);
		}
		if (partRef.getId().equalsIgnoreCase(QueryDefinitonEditor.ID)) {
			hideStartView(partRef);
		}
		if (partRef.getId().equalsIgnoreCase(SQLScratchPadEditor.ID)) {
			hideStartView(partRef);
		}
		if (partRef.getId().equalsIgnoreCase(DBConnectionEditor.ID)) {
			hideStartView(partRef);
		}
		if (partRef.getId().equalsIgnoreCase(CommentsView.ID)) {
			hideStartView(partRef);
		}

	}

	@Override
	public void partInputChanged(IWorkbenchPartReference partRef) {

	}

	protected void hideStartView(IWorkbenchPartReference partRef) {
		IViewReference viewRef = partRef.getPage().findViewReference(StartView.ID);
		if (viewRef != null)
			partRef.getPage().hideView(viewRef);
	}

	public void maybeShowStartView(IWorkbenchPage page) {
		try {
			boolean editorsOpen = true;
			boolean viewsOpen = false;
			if (page.getEditorReferences().length == 0)
				editorsOpen = false;
			IViewReference[] viewRefs = page.getViewReferences();
			for (IViewReference viewRef : viewRefs) {
				if (viewRef.getId().equalsIgnoreCase(QueryResultView.ID) || viewRef.getId().equalsIgnoreCase(QueryExcelResultView.ID) || viewRef.getId().equalsIgnoreCase(CommentsView.ID)) {
					viewsOpen = true;
					break;
				}

			}

			if (editorsOpen == false)
				page.setEditorAreaVisible(false);

			if (viewsOpen == false && editorsOpen == false)
				page.showView(StartView.ID, null, IWorkbenchPage.VIEW_VISIBLE);

		} catch (Exception e) {
		}

	}

}
