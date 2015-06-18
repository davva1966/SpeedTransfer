package com.ss.speedtransfer.ui.view;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.copy.command.CopyDataCommandHandler;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.freeze.CompositeFreezeLayer;
import org.eclipse.nebula.widgets.nattable.freeze.FreezeLayer;
import org.eclipse.nebula.widgets.nattable.freeze.config.DefaultFreezeGridBindings;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultCornerDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.CornerLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.GridLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.RowHeaderLayer;
import org.eclipse.nebula.widgets.nattable.hideshow.ColumnHideShowLayer;
import org.eclipse.nebula.widgets.nattable.layer.AbstractLayerTransform;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.reorder.ColumnReorderLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.summaryrow.SummaryRowLayer;
import org.eclipse.nebula.widgets.nattable.util.GCFactory;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.SubActionBars;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.part.ViewPart;

import com.ss.speedtransfer.model.QueryDefinition;
import com.ss.speedtransfer.ui.nattable.ExcelColumnHeadingDataProvider;
import com.ss.speedtransfer.ui.nattable.QueryDefinitionDataProvider;
import com.ss.speedtransfer.ui.nattable.QueryDefinitionResultTableMenuConfiguration;
import com.ss.speedtransfer.ui.nattable.ResizeAllColumnsCommand;
import com.ss.speedtransfer.ui.nattable.STDataLabelAccumulator;
import com.ss.speedtransfer.ui.nattable.STNatTableStyleConfiguration;
import com.ss.speedtransfer.ui.nattable.STSelectionLayerConfig;
import com.ss.speedtransfer.ui.nattable.STSummaryRowConfiguration;
import com.ss.speedtransfer.ui.nattable.STSummaryRowHeaderDataProvider;
import com.ss.speedtransfer.util.UIHelper;

public class QueryResultView extends ViewPart {

	public static final String ID = "com.ss.speedtransfer.queryResultView";
	protected QueryDefinition queryDefinition;
	protected QueryDefinitionDataProvider dataProvider = new QueryDefinitionDataProvider();

	protected Composite parent;
	protected Composite rowCountPanel;
	protected boolean showSummaryRow = false;

	protected ConfigRegistry configRegistry;
	protected BodyLayerStack bodyLayer;
	protected NatTable natTable;

	protected Hyperlink loadedText;

	public QueryResultView() {
	}

	public QueryDefinition getQueryDefinition() {
		return queryDefinition;
	}

	public void setQueryDefinition(QueryDefinition queryDefinition) {
		this.queryDefinition = queryDefinition;
		setPartName(queryDefinition.getName());
		setTitleToolTip(queryDefinition.getSQL());
		dataProvider.setQueryDefinition(queryDefinition);
		natTable.refresh();
		natTable.setVisible(true);
	}

	@Override
	public void createPartControl(Composite parent) {
		this.parent = parent;
		parent.setLayout(new GridLayout());
		createRowCountPanel();
		buildNatTable();
		natTable.setVisible(false);

	}

	protected void buildNatTable() {

		if (natTable != null)
			natTable.dispose();

		dataProvider.setViewToNotify(this);

		configRegistry = new ConfigRegistry();

		bodyLayer = new BodyLayerStack(dataProvider);

		IDataProvider colHeaderDataProvider = new ExcelColumnHeadingDataProvider(dataProvider);
		ColumnHeaderLayerStack columnHeaderLayer = new ColumnHeaderLayerStack(colHeaderDataProvider);

		IDataProvider rowHeaderDataProvider = new STSummaryRowHeaderDataProvider(dataProvider);
		RowHeaderLayerStack rowHeaderLayer = new RowHeaderLayerStack(rowHeaderDataProvider);

		DefaultCornerDataProvider cornerDataProvider = new DefaultCornerDataProvider(colHeaderDataProvider, rowHeaderDataProvider);
		CornerLayer cornerLayer = new CornerLayer(new DataLayer(cornerDataProvider), rowHeaderLayer, columnHeaderLayer);

		GridLayer gridLayer = new GridLayer(bodyLayer.getCompositeFreezeLayer(), columnHeaderLayer, rowHeaderLayer, cornerLayer);

		natTable = new NatTable(parent, gridLayer, false);

		// Configuration
		natTable.addConfiguration(new DefaultNatTableStyleConfiguration());
		natTable.addConfiguration(new STNatTableStyleConfiguration());
		natTable.addConfiguration(new QueryDefinitionResultTableMenuConfiguration(natTable, bodyLayer.getSelectionLayer(), bodyLayer.getCompositeFreezeLayer()));
		natTable.addConfiguration(new DefaultFreezeGridBindings());

		natTable.setConfigRegistry(configRegistry);

		natTable.configure();

		GridDataFactory.fillDefaults().grab(true, true).applyTo(natTable);

		parent.layout();
	}

	protected void createRowCountPanel() {

		if (rowCountPanel != null)
			rowCountPanel.dispose();

		rowCountPanel = new Composite(parent, SWT.NONE);
		rowCountPanel.setLayout(new GridLayout());
		rowCountPanel.setVisible(false);

		loadedText = new Hyperlink(rowCountPanel, SWT.WRAP);
		loadedText.setText("");
		loadedText.setUnderlined(true);

		loadedText.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_DARK_BLUE));
		loadedText.addHyperlinkListener(new IHyperlinkListener() {
			@Override
			public void linkEntered(org.eclipse.ui.forms.events.HyperlinkEvent e) {
			}

			@Override
			public void linkExited(org.eclipse.ui.forms.events.HyperlinkEvent e) {
			}

			@Override
			public void linkActivated(org.eclipse.ui.forms.events.HyperlinkEvent e) {
				dataProvider.getAll();
			}
		});

		GridDataFactory.fillDefaults().grab(true, false).applyTo(loadedText);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(rowCountPanel);
	}

	@Override
	public void setFocus() {

	}

	public void autoSizeColumns() {
		BusyIndicator.showWhile(UIHelper.instance().getDisplay(), new Runnable() {
			public void run() {
				ILayerCommand command = new ResizeAllColumnsCommand(natTable, dataProvider.getColumnCount(), 1, configRegistry, new GCFactory(natTable));
				natTable.doCommand(command);
			}
		});
	}

	public void toggleSummaryRow() {
		BusyIndicator.showWhile(UIHelper.instance().getDisplay(), new Runnable() {
			public void run() {
				if (showSummaryRow) {
					showSummaryRow = false;
				} else {
					showSummaryRow = true;
				}
				buildNatTable();
				parent.layout();
				natTable.refresh();
			}
		});
	}

	public void dataLoadCompleted() {
		if (dataProvider.getRowsLoaded() < dataProvider.getTotalRows()) {
			StringBuilder sb = new StringBuilder();
			sb.append(dataProvider.getRowsLoaded());
			sb.append(" out of ");
			sb.append(dataProvider.getTotalRows());
			int remainingRows = dataProvider.getTotalRows() - dataProvider.getRowsLoaded();
			sb.append(" rows loaded. Click here to load remaining " + remainingRows + " rows");

			loadedText.setText(sb.toString());
			//loadedText.setFocus();
			rowCountPanel.setVisible(true);
		} else {
			rowCountPanel.dispose();
			parent.layout();
		}
		bodyLayer.getSummaryLayer().clearCache();
		parent.layout();

	}

	public class BodyLayerStack extends AbstractLayerTransform {

		private SummaryRowLayer summaryRowLayer;
		private SelectionLayer selectionLayer;
		private CompositeFreezeLayer compositeFreezeLayer;

		public BodyLayerStack(QueryDefinitionDataProvider dataProvider) {

			DataLayer bodyDataLayer = new DataLayer(dataProvider);
			bodyDataLayer.setConfigLabelAccumulator(new STDataLabelAccumulator(dataProvider));

			ColumnReorderLayer columnReorderLayer;
			if (showSummaryRow) {
				summaryRowLayer = new SummaryRowLayer(bodyDataLayer, configRegistry, false);
				summaryRowLayer.addConfiguration(new STSummaryRowConfiguration(dataProvider));
				columnReorderLayer = new ColumnReorderLayer(summaryRowLayer);
			} else {
				columnReorderLayer = new ColumnReorderLayer(bodyDataLayer);
			}
			ColumnHideShowLayer columnHideShowLayer = new ColumnHideShowLayer(columnReorderLayer);

			selectionLayer = new SelectionLayer(columnHideShowLayer, false);
			selectionLayer.addConfiguration(new STSelectionLayerConfig());

			// Add handler to copy formatted data
			CopyDataCommandHandler copyDataCommandHandler = new CopyDataCommandHandler(selectionLayer);
			copyDataCommandHandler.setCopyFormattedText(true);
			selectionLayer.registerCommandHandler(copyDataCommandHandler);

			ViewportLayer viewportLayer = new ViewportLayer(selectionLayer);
			FreezeLayer freezeLayer = new FreezeLayer(selectionLayer);
			compositeFreezeLayer = new CompositeFreezeLayer(freezeLayer, viewportLayer, selectionLayer);

			setUnderlyingLayer(compositeFreezeLayer);
		}

		public SummaryRowLayer getSummaryLayer() {
			return summaryRowLayer;
		}

		public SelectionLayer getSelectionLayer() {
			return selectionLayer;
		}

		public CompositeFreezeLayer getCompositeFreezeLayer() {
			return compositeFreezeLayer;
		}
	}

	public class ColumnHeaderLayerStack extends AbstractLayerTransform {

		public ColumnHeaderLayerStack(IDataProvider dataProvider) {
			DataLayer dataLayer = new DataLayer(dataProvider);
			ColumnHeaderLayer colHeaderLayer = new ColumnHeaderLayer(dataLayer, bodyLayer.getCompositeFreezeLayer(), bodyLayer.getSelectionLayer());
			setUnderlyingLayer(colHeaderLayer);
		}
	}

	public class RowHeaderLayerStack extends AbstractLayerTransform {

		public RowHeaderLayerStack(IDataProvider dataProvider) {
			DataLayer dataLayer = new DataLayer(dataProvider, 50, 20);
			RowHeaderLayer rowHeaderLayer = new RowHeaderLayer(dataLayer, bodyLayer.getCompositeFreezeLayer(), bodyLayer.getSelectionLayer());
			setUnderlyingLayer(rowHeaderLayer);
		}
	}

	@Override
	public void dispose() {
		try {
			final IActionBars actionBars = getViewSite().getActionBars();
			if (actionBars instanceof SubActionBars)
				((SubActionBars) actionBars).dispose();
		} catch (Exception e) {
		}

		super.dispose();
	}

}
