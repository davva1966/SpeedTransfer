// {{CopyrightNotice}}

package com.ss.speedtransfer.ui.wizard.qb;

import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;

import com.ss.speedtransfer.util.UIHelper;


/**
 * The <code>SQLQueryBuilderWizard</code> class
 */
public class SQLQueryBuilderWizard extends Wizard {

	/** Query Data */
	protected SQLQueryData queryData = null;

	public SQLQueryBuilderWizard() {
		super();
	}

	/**
	 * Initializes a newly created <code>SQLQueryBuilderWizard</code>
	 * 
	 * @param queryData
	 */
	public SQLQueryBuilderWizard(SQLQueryData queryData) {
		this.queryData = queryData;
		setWindowTitle("SQL Query Builder");
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		setWindowTitle("SQL Query Builder");
	}

	/**
	 * Add wizard pages
	 */
	public void addPages() {
		addPage(new TableSelectionPage());
		addPage(new ColumnSelectionPage());
		addPage(new DataFilterPage());
		addPage(new GroupingSelectionPage());
		addPage(new HavingSelectionPage());
		addPage(new SequenceSelectionPage());
		addPage(new TableJoinPage());
	}

	/**
	 * Finish
	 */
	public boolean performFinish() {
		return true;
	}

	/**
	 * Cancel
	 */
	public boolean performCancel() {
		queryData.setCancel(true);
		return true;
	}

	/**
	 * Get query data
	 * 
	 * @return queryData
	 */
	public SQLQueryData getQueryData() {
		return queryData;
	}

	@Override
	public IWizardPage getNextPage(IWizardPage page) {

		IWizardPage nextPage = super.getNextPage(page);
		if (nextPage instanceof TableJoinPage) {
			if (getQueryData().getSelectedTables().size() == 1) {
				nextPage = getNextPage(nextPage);
			}
		}
		if (nextPage instanceof HavingSelectionPage) {
			if (getQueryData().isGrouped() == false) {
				nextPage = getNextPage(nextPage);
			}
		}

		return nextPage;
	}

	/**
	 * The <code>ColumnSelectionPage02</code> class
	 */
	abstract class QBWizardPage extends WizardPage {

		public SQLQBComposite pageControl;

		public QBWizardPage(String pageName, String title, ImageDescriptor image) {
			super(pageName, title, image);
		}

	}

	/**
	 * The <code>DataFilterPage03</code> class
	 */
	class DataFilterPage extends QBWizardPage {

		public DataFilterPage() {
			super("DataFilter", "Data Filter", null);
			setDescription("Specify the selection criteria to use when selecting rows to include in the query");

		}

		public void createControl(Composite parent) {
			pageControl = getDataFilterComp(this, parent, getQueryData());
			setControl(pageControl);
		}

	}

	/**
	 * The <code>TableJoinPage04</code> class
	 */
	class TableJoinPage extends QBWizardPage {

		public TableJoinPage() {
			super("TableJoin", "Table Join", null);
			setDescription("Specify how the tables should be joined together");
		}

		/**
		 * Create control
		 */
		public void createControl(Composite parent) {
			pageControl = getTableJoinComp(this, parent, getQueryData());
			setControl(pageControl);
		}

		@Override
		public boolean canFlipToNextPage() {
			return false;
		}

		@Override
		public boolean isPageComplete() {
			return true;
		}

	}

	/**
	 * The <code>TableSelectionPage01</code> class
	 */
	class TableSelectionPage extends QBWizardPage {

		public TableSelectionPage() {
			super("TableSelect", "Table Selection", null);
			setDescription("Select one or more tables to be included in the query");
		}

		public void createControl(Composite parent) {
			pageControl = getTableSelectionComp(this, parent, getQueryData());
			setControl(pageControl);

		}

	}

	/**
	 * The <code>ColumnSelectionPage02</code> class
	 */
	class ColumnSelectionPage extends QBWizardPage {

		public ColumnSelectionPage() {
			super("ColumnSelection", "Column Selection", null);
			setDescription("Select the columns to be displayed when this query is executed");
		}

		public void createControl(Composite parent) {
			pageControl = getColumnSelectionComp(this, parent, getQueryData());
			setControl(pageControl);
		}

		@Override
		public boolean canFlipToNextPage() {
			return true;
		}

	}

	/**
	 * The <code>GroupingSelectionPage</code> class
	 */
	class GroupingSelectionPage extends QBWizardPage {

		public GroupingSelectionPage() {
			super("GroupingSelection", "Grouping Selection", null);
			setDescription("Select the columns used to group the result of the query");
		}

		public void createControl(Composite parent) {
			pageControl = getGroupingSelectionComp(this, parent, getQueryData());
			setControl(pageControl);
		}

		@Override
		public boolean canFlipToNextPage() {
			return true;
		}

		@Override
		public IWizardPage getNextPage() {
			if (validate())
				return super.getNextPage();
			else
				return this;
		}

		protected boolean validate() {
			if (pageControl.queryData.getGroupByColumns() != null && pageControl.queryData.getGroupByColumns().size() > 0) {
				List missingColumns = pageControl.queryData.getMissingGroupByColumns();
				if (missingColumns != null && missingColumns.size() > 0) {
					StringBuilder missingColumnsString = new StringBuilder();
					for (Iterator iter = missingColumns.iterator(); iter.hasNext();) {
						String[] missingCol = (String[]) iter.next();
						if (missingColumnsString.length() > 0)
							missingColumnsString.append(", ");
						missingColumnsString.append(missingCol[0]);
					}
					int response = UIHelper.instance().showErrorMsg(getShell(), "Information",
							"The following columns from the query select clause does not have a function and are therefore mandatory in the \"group by\" clause," + missingColumnsString.toString() + " Do want to automatically add them?", SWT.ICON_INFORMATION | SWT.YES | SWT.NO | SWT.ON_TOP, null);

					if (response == SWT.YES) {
						for (Iterator iter = missingColumns.iterator(); iter.hasNext();) {
							String[] missingCol = (String[]) iter.next();
							pageControl.queryData.addGroupByColumn(missingCol);
							((SQLGroupingSelectionComp) getControl()).fillTable();
						}
					}

					return false;
				}
			}

			return true;
		}

	}

	/**
	 * The <code>HavingSelectionPage</code> class
	 */
	class HavingSelectionPage extends QBWizardPage {

		public HavingSelectionPage() {
			super("HavingSelection", "Aggregated values selection", null);
			setDescription("Specify selections on aggregated values");
		}

		public void createControl(Composite parent) {
			pageControl = getHavingSelectionComp(this, parent, getQueryData());
			setControl(pageControl);
		}

		@Override
		public boolean canFlipToNextPage() {
			return true;
		}

	}

	/**
	 * The <code>SequenceSelectionPage</code> class
	 */
	class SequenceSelectionPage extends QBWizardPage {

		public SequenceSelectionPage() {
			super("SequenceSelection", "Sequence Selection", null);
			setDescription("Select the columns to be used to order the result when this query is executed");
		}

		public void createControl(Composite parent) {
			pageControl = getSequenceSelectionComp(this, parent, getQueryData());
			setControl(pageControl);
		}

		@Override
		public boolean canFlipToNextPage() {
			if (getQueryData().getSelectedTables().size() > 1)
				return true;
			return false;
		}

	}

	@Override
	public boolean canFinish() {
		IWizardPage startingPage = getStartingPage();
		if (!startingPage.isPageComplete()) {
			return false;
		}
		return true;
	}

	protected SQLTableSelectionComp getTableSelectionComp(WizardPage page, Composite comp, SQLQueryData queryData) {
		return new SQLTableSelectionComp(page, comp, queryData);
	}

	protected SQLColumnSelectionComp getColumnSelectionComp(WizardPage page, Composite comp, SQLQueryData queryData) {
		return new SQLColumnSelectionComp(page, comp, queryData);
	}

	protected SQLSequenceSelectionComp getSequenceSelectionComp(WizardPage page, Composite comp, SQLQueryData queryData) {
		return new SQLSequenceSelectionComp(page, comp, queryData);
	}

	protected SQLGroupingSelectionComp getGroupingSelectionComp(WizardPage page, Composite comp, SQLQueryData queryData) {
		return new SQLGroupingSelectionComp(page, comp, queryData);
	}

	protected SQLAggregateFilterComp getHavingSelectionComp(WizardPage page, Composite comp, SQLQueryData queryData) {
		return new SQLAggregateFilterComp(page, comp, queryData);
	}

	protected SQLDataFilterComp getDataFilterComp(WizardPage page, Composite comp, SQLQueryData queryData) {
		return new SQLDataFilterComp(page, comp, queryData);
	}

	protected SQLTableJoinComp getTableJoinComp(WizardPage page, Composite comp, SQLQueryData queryData) {
		return new SQLTableJoinComp(page, comp, queryData);
	}

}
