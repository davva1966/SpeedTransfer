package com.ss.speedtransfer.ui.view;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.List;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IElementComparer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.part.ViewPart;

import com.ss.speedtransfer.model.QueryDefinition;
import com.ss.speedtransfer.model.QueryResultColumnLabelProvider;
import com.ss.speedtransfer.model.QueryResultLazyContentProvider;
import com.ss.speedtransfer.util.UIHelper;


public class QueryResultView extends ViewPart {
	public static final String ID = "com.ss.speedtransfer.queryResultView";

	private TableViewer viewer;

	protected class TextEditingSupport extends EditingSupport {

		private final TableViewer viewer;
		private final int idx;

		public TextEditingSupport(TableViewer viewer, int idx) {
			super(viewer);
			this.viewer = viewer;
			this.idx = idx;
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			return new TextCellEditor(viewer.getTable());
		}

		@Override
		protected boolean canEdit(Object element) {
			return true;
		}

		@Override
		protected Object getValue(Object element) {
			return ((List<String>) element).get(idx);
		}

		@Override
		protected void setValue(Object element, Object value) {

		}
	}

	public void createPartControl(Composite parent) {
		GridLayout layout = new GridLayout(2, false);
		parent.setLayout(layout);
		createViewer(parent);
	}

	private void createViewer(Composite parent) {
		viewer = new TableViewer(parent, SWT.VIRTUAL | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);

		final Table table = viewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		viewer.setContentProvider(new QueryResultLazyContentProvider(this));
		viewer.setComparer(new IElementComparer() {

			@Override
			public int hashCode(Object element) {
				return element.hashCode();
			}

			@Override
			public boolean equals(Object a, Object b) {
				try {
					return ((List<String>) a).get(0).equals(((List<String>) b).get(0));
				} catch (Exception e) {
				}
				return a.equals(b);
			}
		});

		getSite().setSelectionProvider(viewer);

		// Layout the viewer
		GridData gridData = new GridData();
		gridData.verticalAlignment = GridData.FILL;
		gridData.horizontalSpan = 2;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		viewer.getControl().setLayoutData(gridData);
		
	}

	public TableViewer getViewer() {
		return viewer;
	}

	public void inputChanged(ResultSet result, List<String[]> columnProperties, QueryDefinition queryDef) {

		setPartName(queryDef.getName());
		setTitleToolTip(queryDef.getSQL());

		TableColumn[] columns = viewer.getTable().getColumns();
		for (TableColumn col : columns)
			col.dispose();
		createColumns(result, columnProperties);
	}

	// This will create the columns for the table
	private void createColumns(ResultSet result, List<String[]> columnProperties) {

		try {
			ResultSetMetaData rsmd = result.getMetaData();

			// Create row column
			TableViewerColumn rowColumn = new TableViewerColumn(viewer, SWT.NONE);
			TableColumn column = rowColumn.getColumn();
			column.setText("Row");
			column.setWidth(50);
			rowColumn.setLabelProvider(new QueryResultColumnLabelProvider(0));

			for (int i = 1; i <= rsmd.getColumnCount(); i++) {
				TableViewerColumn col = createTableViewerColumn(columnProperties.get(i - 1));
				col.setLabelProvider(new QueryResultColumnLabelProvider(i));
				col.setEditingSupport(new TextEditingSupport(viewer, i));
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private TableViewerColumn createTableViewerColumn(String[] columnProperties) {
		final TableViewerColumn viewerColumn = new TableViewerColumn(viewer, SWT.NONE);
		final TableColumn column = viewerColumn.getColumn();
		column.setText(columnProperties[4]);
		int width = Integer.parseInt(columnProperties[5]);
		if (width < 10)
			width = width * 12;
		else if (width < 20)
			width = width * 8;
		else
			width = width * 6;
		if (width > 200)
			width = 200;
		column.setWidth(width);
		column.setAlignment(SWT.RIGHT);
		if (columnProperties[6].equalsIgnoreCase("left"))
			column.setAlignment(SWT.LEFT);
		else if (columnProperties[6].equalsIgnoreCase("center"))
			column.setAlignment(SWT.CENTER);
		column.setResizable(true);
		column.setMoveable(true);
		column.addListener(SWT.DRAG, new Listener() {
			public void handleEvent(Event e) {
				System.out.println("Drag " + e.widget);
			}
		});

		return viewerColumn;

	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	public void autoSizeColumns() {
		BusyIndicator.showWhile(UIHelper.instance().getDisplay(), new Runnable() {
			public void run() {
				for (TableColumn tc : getViewer().getTable().getColumns())
					tc.pack();
			}
		});
	}

}
