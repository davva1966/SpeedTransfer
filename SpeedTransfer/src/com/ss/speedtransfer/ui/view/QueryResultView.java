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
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
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
	protected boolean showSummaryRow = false;

	protected ConfigRegistry configRegistry;
	protected BodyLayerStack bodyLayer;
	protected NatTable natTable;

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
		buildNatTable();
		natTable.setVisible(false);

	}

	protected void buildNatTable() {

		if (natTable != null)
			natTable.dispose();

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

	@Override
	public void setFocus() {
		// viewer.getControl().setFocus();
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
				natTable.refresh();
			}
		});
	}

	public class BodyLayerStack extends AbstractLayerTransform {

		private SelectionLayer selectionLayer;
		private CompositeFreezeLayer compositeFreezeLayer;

		public BodyLayerStack(QueryDefinitionDataProvider dataProvider) {
			DataLayer bodyDataLayer = new DataLayer(dataProvider);
			bodyDataLayer.setConfigLabelAccumulator(new STDataLabelAccumulator(dataProvider));

			ColumnReorderLayer columnReorderLayer;
			if (showSummaryRow) {
				SummaryRowLayer summaryRowLayer = new SummaryRowLayer(bodyDataLayer, configRegistry, false);
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

}
