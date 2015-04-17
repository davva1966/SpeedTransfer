// {{CopyrightNotice}}

package com.ss.speedtransfer.ui.wizard.qb;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import oracle.jdbc.OracleConnection;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Shell;
import org.postgresql.PGConnection;

import com.ibm.db2.jcc.DB2Connection;
import com.microsoft.sqlserver.jdbc.SQLServerConnection;
import com.ss.speedtransfer.model.QueryDefinition;
import com.ss.speedtransfer.util.ConnectionManager;
import com.ss.speedtransfer.util.SSUtil;
import com.ss.speedtransfer.util.StringHelper;
import com.ss.speedtransfer.util.StringMatcher;
import com.ss.speedtransfer.util.UIHelper;
import com.ss.speedtransfer.util.parser.sql.FromTableDef;
import com.ss.speedtransfer.util.parser.sql.SQLParserHelper;
import com.ss.speedtransfer.util.parser.sql.SelectColumn;


/**
 * The <code>SQLQueryData</code> class
 */
public class SQLQueryData {

	/** Connection * */
	protected Connection connection = null;

	protected String database = null;

	protected QueryDefinition queryDef = null;

	/** True if column values should be distinct */
	protected boolean distinct = false;

	/** Available tables */
	protected Map availableTables = new HashMap();

	/** Selected tables */
	protected List selectedTables = new ArrayList();

	/** Selected columns */
	protected List selectedColumns = new ArrayList();

	/** Join specifications */
	protected List joinSpecifications = new ArrayList();

	protected boolean cancelled = false;

	/**
	 * Filter condition containing String arrays of conditions. The arrays has 6 elements: [logical, LBrack, Value1, Condition, Value2, RBrack]
	 */
	protected List filterCondition = new ArrayList();

	/**
	 * Group by columns
	 */
	protected List groupByColumns = new ArrayList();

	/**
	 * Having condition containing String arrays of "having" conditions. The arrays has 8 elements: [logical, LBrack, Value1Func, Value1, Condition, Value2Func, Value2, RBrack]
	 */
	protected List havingCondition = new ArrayList();

	/**
	 * Sequence column condition containing String arrays of columns The arrays has 3 elements: [Func, Column, Sequence]
	 */
	protected List orderByColumns = new ArrayList();

	/** Cancel */
	public boolean cancel = false;

	protected StringMatcher sm = new StringMatcher(QueryDefinition.REP_VAR_START_MARKER, QueryDefinition.REP_VAR_STOP_MARKER);

	protected boolean loadTablesExecutionCompleted = false;
	protected String loadTablesError = null;

	protected static Map<String, Boolean> availableTablesLoaded = new HashMap<String, Boolean>();
	protected static Map<String, Map<String, SQLTable>> availableTablesMap = new HashMap<String, Map<String, SQLTable>>();

	public SQLQueryData(Connection connection, QueryDefinition queryDef) {
		this(connection, queryDef, true);

	}

	public SQLQueryData(Connection connection, QueryDefinition queryDef, boolean load) {
		super();
		this.connection = connection;
		this.database = queryDef.getDBConnection().getDatabase();
		this.queryDef = queryDef;
		if (connection != null && load)
			loadAvailableTables();
	}

	public QueryDefinition getQueryDefinition() {
		return queryDef;
	}

	public void refreshAvailableTables() {
		availableTables.clear();
		final String key = ConnectionManager.buildKey(queryDef.getDBConnection());
		if (availableTablesLoaded.containsKey(key)) {
			availableTablesLoaded.remove(key);
			availableTablesMap.remove(key);
		}
		runLoadTablesJob();

	}

	protected void loadAvailableTables() {
		runLoadTablesJob();

	}

	protected void runLoadTablesJob() {

		final String key = ConnectionManager.buildKey(queryDef.getDBConnection());
		if (availableTablesLoaded.containsKey(key) && availableTablesLoaded.get(key)) {
			availableTables = availableTablesMap.get(key);
			return;
		}

		loadTablesExecutionCompleted = false;
		loadTablesError = null;

		Shell shell = UIHelper.instance().getActiveShell();
		try {
			ProgressMonitorDialog pmd = new ProgressMonitorDialog(shell);
			pmd.run(true, true, new IRunnableWithProgress() {
				public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

					monitor.beginTask("Retrieving available tables... Press cancel to skip.", IProgressMonitor.UNKNOWN);

					try {
						Thread executionThread = new Thread(new Runnable() {
							public void run() {
								ResultSet rs = null;
								try {
									DatabaseMetaData md = connection.getMetaData();

									String[] types = new String[1];
									// Load tables
									types[0] = "TABLE";
									if (connection instanceof SQLServerConnection || connection instanceof OracleConnection || connection instanceof PGConnection || connection instanceof DB2Connection)
										rs = md.getTables(null, null, "%", types);
									else
										rs = md.getTables(null, database, "%", types);

									boolean aborted = loadTablesFromRS(rs, monitor, types[0]);

									// Load views
									if (!aborted) {
										types[0] = "VIEW";
										if (connection instanceof SQLServerConnection || connection instanceof OracleConnection || connection instanceof PGConnection || connection instanceof DB2Connection)
											rs = md.getTables(null, null, "%", types);
										else
											rs = md.getTables(null, database, "%", types);
										aborted = loadTablesFromRS(rs, monitor, types[0]);
									}
									// Load system tables
									if (!aborted) {
										types[0] = "SYSTEM TABLE";
										if (connection instanceof SQLServerConnection || connection instanceof OracleConnection || connection instanceof PGConnection || connection instanceof DB2Connection)
											rs = md.getTables(null, null, "%", types);
										else
											rs = md.getTables(null, database, "%", types);
										loadTablesFromRS(rs, monitor, types[0]);
									}

									if (!aborted) {
										availableTablesLoaded.put(key, true);
										availableTablesMap.put(key, availableTables);
									}

								} catch (Exception e) {
									loadTablesError = SSUtil.getMessage(e);
								} finally {
									try {
										if (rs != null)
											rs.close();
									} catch (Exception e2) {
										// TODO: handle exception
									}
								}
							}
						}, "Metadata table retrieval");
						executionThread.start();

						boolean running = true;
						while (running) {
							Thread.sleep(200);
							running = executionThread.isAlive() && monitor.isCanceled() == false;
						}

					} catch (Exception e) {

					}

					monitor.done();
					loadTablesExecutionCompleted = true;

				}
			});

		} catch (InvocationTargetException e) {
			UIHelper.instance().showErrorMsg("Error", SSUtil.getMessage(e));
		} catch (InterruptedException e) {
			MessageDialog.openInformation(shell, "Cancelled", SSUtil.getMessage(e));
		}

		while (loadTablesExecutionCompleted == false) {
			try {
				Thread.sleep(200);
			} catch (Exception e) {
			}
		}

		if (loadTablesError != null) {
			UIHelper.instance().showErrorMsg("Error", loadTablesError);
		}

	}

	protected boolean loadTablesFromRS(ResultSet rs, IProgressMonitor monitor, String type) throws SQLException {
		boolean aborted = false;
		while (rs.next()) {
			// String tableType = rs.getString(4);
			String owner = rs.getString(1);
			String schemaName = rs.getString(2);
			String tableName = rs.getString(3);
			String remark = rs.getString(5);

			SQLTable table = new SQLTable();
			table.owner = owner;
			table.schema = schemaName;
			table.name = tableName;
			table.remark = remark;
			// table.type = tableType;
			table.type = type;

			availableTables.put(tableName, table);
			if (monitor.isCanceled()) {
				aborted = true;
				break;
			}
		}

		return aborted;
	}

	/**
	 * Get available tables
	 */
	public Map getAvailableTables() {
		return availableTables;
	}

	/**
	 * Get available table
	 */
	public SQLTable getAvailableTable(String tableName) {

		return (SQLTable) getAvailableTables().get(tableName);

	}

	/**
	 * Get available columns
	 */
	public Map getAvailableColumns() {
		Map columnMap = new HashMap();
		for (Iterator iter = selectedTables.iterator(); iter.hasNext();) {
			SQLTable table = (SQLTable) iter.next();
			for (Iterator iter1 = table.getAvailableColumns(connection).iterator(); iter1.hasNext();) {
				SQLColumn column = (SQLColumn) iter1.next();
				columnMap.put(column.getQualifiedName(), column);
			}
		}
		return columnMap;
	}

	public SQLColumn getAvailableColumn(String columnName) {
		if (columnName == null || columnName.trim().length() == 0)
			return null;

		for (Iterator iter = getAvailableColumns().values().iterator(); iter.hasNext();) {
			SQLColumn column = (SQLColumn) iter.next();
			if (column.name.trim().equalsIgnoreCase(columnName.trim()))
				return column;
		}

		return null;
	}

	/**
	 * Add a table
	 * 
	 * @param table
	 */
	public void addTable(SQLTable table) {
		selectedTables.add(table);
		if (connection != null)
			table.updateAvailableColumns(connection);

	}

	/**
	 * Add a column
	 * 
	 * @param column
	 */
	public void addColumn(SQLColumn column) {
		if (column.table == null || column.table.trim().length() == 0) {
			for (Iterator iter = getSelectedTables().iterator(); iter.hasNext();) {
				SQLTable table = (SQLTable) iter.next();
				if (table.getAvailableColumn(column.name) != null)
					column.table = table.getDisplayName();
			}
		}

		column.updateSQLString();
		selectedColumns.add(column);

	}

	/**
	 * Remove a table.
	 * 
	 * @param tableIdx
	 */
	public void removeTable(int tableIdx) {
		SQLTable removedTable = (SQLTable) selectedTables.get(tableIdx);
		if (removedTable == null)
			return;

		selectedTables.remove(tableIdx);
		cleanupAfterTableRemove(removedTable.name);

	}

	/**
	 * Remove tables.
	 * 
	 * @param tableIdx
	 */
	public void removeTables(int[] tableIdx) {
		List tablesToRemove = new ArrayList(tableIdx.length);
		for (int i = 0; i < tableIdx.length; i++) {
			if (selectedTables.size() > tableIdx[i])
				tablesToRemove.add(selectedTables.get(tableIdx[i]));
		}
		selectedTables.removeAll(tablesToRemove);

		for (Iterator iter = tablesToRemove.iterator(); iter.hasNext();) {
			SQLTable table = (SQLTable) iter.next();
			cleanupAfterTableRemove(table.name);
		}

	}

	/**
	 * Remove all selected tables.
	 */
	public void removeAllTables() {
		int[] tableIdx = new int[selectedTables.size()];
		for (int i = 0; i < tableIdx.length; i++)
			tableIdx[i] = i;

		removeTables(tableIdx);

	}

	/**
	 * Cleanup after a tble is removed
	 * 
	 * @param tableIdx
	 */
	protected void cleanupAfterTableRemove(String tableName) {

		// Remove selected columns for table
		List newSelectedColumns = new ArrayList();
		for (Iterator iter = selectedColumns.iterator(); iter.hasNext();) {
			SQLColumn column = (SQLColumn) iter.next();
			if (column.table.equalsIgnoreCase(tableName) == false)
				newSelectedColumns.add(column);
		}
		selectedColumns = newSelectedColumns;

		// Remove join specs for table
		List newJoinSpecs = new ArrayList();
		for (Iterator iter = joinSpecifications.iterator(); iter.hasNext();) {
			SQLJoinSpec join = (SQLJoinSpec) iter.next();
			if (join.mainTableName.equalsIgnoreCase(tableName) == false && join.joinedTableName.equalsIgnoreCase(tableName) == false)
				newJoinSpecs.add(join);
		}
		joinSpecifications = newJoinSpecs;

	}

	/**
	 * Remove a column
	 * 
	 * @param colIdx
	 */
	public void removeColumn(int colIdx) {
		selectedColumns.remove(colIdx);

	}

	/**
	 * Remove columns
	 * 
	 * @param colIdx
	 */
	public void removeColumns(int[] colIdx) {
		List columnsToRemove = new ArrayList(colIdx.length);
		for (int i = 0; i < colIdx.length; i++) {
			if (selectedColumns.size() > colIdx[i])
				columnsToRemove.add(selectedColumns.get(colIdx[i]));
		}
		selectedColumns.removeAll(columnsToRemove);

	}

	/**
	 * Remove all selected columns.
	 */
	public void removeAllColumns() {
		selectedColumns.clear();

	}

	/**
	 * Check if column values should be distinct
	 * 
	 * @return distinct
	 */
	public boolean isDistinct() {
		return distinct;
	}

	/**
	 * Set distinct flag
	 * 
	 * @param distinct
	 */
	public void setDistinct(boolean distinct) {
		this.distinct = distinct;
	}

	/**
	 * Get join specifications
	 * 
	 * @return join criteria
	 */
	public List getJoinSpecifications() {
		return joinSpecifications;
	}

	/**
	 * Add join specification
	 * 
	 * @param joinSpec
	 */
	public void addJoinSpecification(SQLJoinSpec joinSpec) {
		joinSpecifications.add(joinSpec);
	}

	/**
	 * Get selected tables
	 */
	public List getSelectedTables() {
		if (selectedTables == null)
			selectedTables = new ArrayList();
		return selectedTables;
	}

	/**
	 * Get selected table
	 */
	public SQLTable getSelectedTable(String tableName) {

		for (Iterator iter = getSelectedTables().iterator(); iter.hasNext();) {
			SQLTable table = (SQLTable) iter.next();
			if (table.getDisplayName().equalsIgnoreCase(tableName) || table.name.equalsIgnoreCase(tableName))
				return table;

		}

		return null;
	}

	/**
	 * Get filter condition clause
	 * 
	 * @return
	 */
	public List getFilterCondition() {
		return filterCondition;
	}

	/**
	 * Get having condition clause
	 * 
	 * @return
	 */
	public List getHavingCondition() {
		return havingCondition;
	}

	/**
	 * Set order by columns
	 * 
	 * @param orderByColumns
	 */
	public void setOrderByColumns(List orderByColumns) {
		if (orderByColumns == null)
			this.orderByColumns = new ArrayList();
		else
			this.orderByColumns = orderByColumns;
	}

	/**
	 * Set group by columns
	 * 
	 * @param groupByColumns
	 */
	public void setGroupByColumns(List groupByColumns) {
		if (groupByColumns == null)
			this.groupByColumns = new ArrayList();
		else
			this.groupByColumns = groupByColumns;
	}

	/**
	 * Add order by column
	 * 
	 * @param orderByColumn
	 */
	public void addOrderByColumn(String[] orderByColumn) {
		this.orderByColumns.add(orderByColumn);
	}

	/**
	 * Add group by column
	 * 
	 * @param groupColumn
	 */
	public void addGroupByColumn(String[] groupColumn) {
		this.groupByColumns.add(groupColumn);
	}

	/**
	 * Get order by columns
	 * 
	 * @return order by columns
	 */
	public List getOrderByColumns() {
		return orderByColumns;
	}

	/**
	 * Get group by clause
	 * 
	 * @return
	 */
	public List getGroupByColumns() {

		// Add mandatory columns
		boolean aggrUsed = false;
		boolean singleUsed = false;
		for (Iterator iter = getSelectedColumns().iterator(); iter.hasNext();) {
			SQLColumn col = (SQLColumn) iter.next();
			if (col.aggregateFunction != null && col.aggregateFunction.trim().length() > 0)
				aggrUsed = true;
			if (col.aggregateFunction == null || col.aggregateFunction.trim().length() == 0)
				singleUsed = true;
			if (aggrUsed && singleUsed)
				break;
		}

		if (aggrUsed && singleUsed)
			groupByColumns.addAll(getMissingGroupByColumns());

		return groupByColumns;
	}

	public List getMissingGroupByColumns() {

		List missingColumns = new ArrayList();
		List added = new ArrayList();

		nextCol: for (Iterator iter1 = getSelectedColumns().iterator(); iter1.hasNext();) {
			SQLColumn col = (SQLColumn) iter1.next();
			if (col.hasFunction == false) {
				// This column is mandatory, check if it has been used, if not,
				// add it.
				for (Iterator iter2 = groupByColumns.iterator(); iter2.hasNext();) {
					String[] groupCol = (String[]) iter2.next();
					String groupName = groupCol[0];
					String groupTable = null;
					int idx = groupCol[0].indexOf(".");
					if (idx > -1) {
						groupName = groupCol[0].substring(idx + 1);
						groupTable = groupCol[0].substring(0, idx);
					}
					if (col.name.equalsIgnoreCase(groupName)) {
						String tab = col.table;
						if (tab == null)
							tab = "";
						if (groupTable == null)
							groupTable = "";
						if (tab.trim().length() == 0 || groupTable.trim().length() == 0 || tab.equalsIgnoreCase(groupTable))
							continue nextCol;
					}
				}

				// Column was not found in group by, add it if not already
				// added.
				String[] arr = new String[1];
				arr[0] = col.table + "." + col.name;
				if (!added.contains(arr[0])) {
					added.add(arr[0]);
					missingColumns.add(arr);
				}
			}
		}

		return missingColumns;

	}

	public boolean columnMandatoryInGroupBy(String columnName) {

		// Add mandatory columns
		boolean aggrUsed = false;
		boolean singleUsed = false;
		for (Iterator iter = getSelectedColumns().iterator(); iter.hasNext();) {
			SQLColumn col = (SQLColumn) iter.next();
			if (col.aggregateFunction != null && col.aggregateFunction.trim().length() > 0)
				aggrUsed = true;
			if (col.aggregateFunction == null || col.aggregateFunction.trim().length() == 0)
				singleUsed = true;
			if (aggrUsed && singleUsed)
				break;
		}

		if (aggrUsed && singleUsed) {

			String groupName = columnName;
			String groupTable = null;
			int idx = columnName.indexOf(".");
			if (idx > -1) {
				groupName = columnName.substring(idx + 1);
				groupTable = columnName.substring(0, idx);
			}

			if (groupName.trim().length() == 0)
				return false;

			for (Iterator iter = getSelectedColumns().iterator(); iter.hasNext();) {
				SQLColumn col = (SQLColumn) iter.next();
				if (col.aggregateFunction == null || col.aggregateFunction.trim().length() == 0) {
					if (col.name.equalsIgnoreCase(groupName)) {
						if (groupTable != null && !col.table.equalsIgnoreCase(groupTable))
							continue;
						return true;
					}

				}
			}
		}

		return false;

	}

	/**
	 * Set filter condition
	 * 
	 * @param filterCondition
	 */
	public void setFilterConditon(List filterCondition) {
		if (filterCondition == null)
			this.filterCondition = new ArrayList();
		else
			this.filterCondition = filterCondition;
	}

	/**
	 * Set having condition
	 * 
	 * @param havingCondition
	 */
	public void setHavingConditon(List havingCondition) {
		if (havingCondition == null)
			this.havingCondition = new ArrayList();
		else
			this.havingCondition = havingCondition;
	}

	/**
	 * Add filter condition
	 * 
	 * @param filterCondition
	 */
	public void addFilterConditon(String[] filterCondition) {
		this.filterCondition.add(filterCondition);
	}

	/**
	 * Add having condition
	 * 
	 * @param havingCondition
	 */
	public void addHavingConditon(String[] havingCondition) {
		this.havingCondition.add(havingCondition);
	}

	/**
	 * Load sql data from sql string
	 * 
	 * @param sql
	 * @return error message or null
	 */
	public String loadQueryData(String sql) {
		if (sql == null || sql.trim().length() == 0)
			return null;

		String msg = null;
		try {
			// Parse the sql syntax
			SQLParserHelper parserHelper = new SQLParserHelper(sql);
			parserHelper.parse();

			// Update tables
			List tables = parserHelper.getTables();
			for (Iterator iter = tables.iterator(); iter.hasNext();) {
				FromTableDef tbl = (FromTableDef) iter.next();
				SQLTable newtbl = newTable(tbl);
//				if (newtbl.schema == null || newtbl.schema.trim().length() == 0)
//					newtbl.schema = database;
				addTable(newtbl);
			}

			// Update columns
			List cols = parserHelper.getColumns();
			for (Iterator iter = cols.iterator(); iter.hasNext();) {
				SelectColumn col = (SelectColumn) iter.next();
				SQLColumn newcol = null;
				if (col.glob_name != null && col.glob_name.indexOf(".*") != -1) {
					newcol = new SQLColumn();
					newcol.name = col.glob_name;
					newcol.sqlString = col.glob_name;
				} else if (col.glob_name == null) {
					newcol = new SQLColumn(col);
				}
				if (newcol != null) {
					addColumn(newcol);
				}
			}

			// Update group by columns
			setGroupByColumns(parserHelper.getGroupByColumns());

			// Update distinct flag
			setDistinct(parserHelper.getDistinct());

			// Update filter condition
			setFilterConditon(parserHelper.getFilterExpression());

			// Update having condition
			setHavingConditon(parserHelper.getHavingExpression());

			// Update join criteria
			List joinSpecs = parserHelper.getJoinCriterias(null);
			if (joinSpecs != null) {
				for (Iterator iter = joinSpecs.iterator(); iter.hasNext();) {
					List tempJoinSpec = (List) iter.next();
					SQLJoinSpec joinSpec = new SQLJoinSpec(tempJoinSpec);
					addJoinSpecification(joinSpec);
				}
			}

			// Update order by
			setOrderByColumns(parserHelper.getOrderByColumns());

		} catch (Throwable e) {
			msg = SSUtil.getMessage(e);
			if (msg == null)
				msg = e.toString();
		}
		return msg;
	}

	/**
	 * Get selected columns
	 * 
	 * @return selectedColumns
	 */
	public List getSelectedColumns() {
		if (selectedColumns == null)
			selectedColumns = new ArrayList();
		return selectedColumns;
	}

	/**
	 * Return sql string
	 * 
	 * @return sql string
	 */
	public String createSQL() {
		if (isCancel())
			return null;

		StringBuffer sql = new StringBuffer("select ");
		List columns = getSelectedColumns();
		if (isDistinct())
			sql.append("distinct ");

		if (columns == null || columns.isEmpty()) {
			sql.append("*");
		} else {
			for (Iterator iter = columns.iterator(); iter.hasNext();) {
				SQLColumn column = (SQLColumn) iter.next();
				maybeQualify(column);

				// Add space on either side of "/" division sign (To avoid clash
				// with the separator sign used
				// on some db's)
				String s = column.sqlString;
				s = s.replaceAll("/", " / ");
				int idx = s.indexOf("  ");
				while (idx > -1) {
					s = s.replaceAll("  ", " ");
					idx = s.indexOf("  ");
				}

				sql.append(s);
				if (column.alias != null && column.alias.trim().length() > 0)
					sql.append(" as " + "\"" + column.alias.trim() + "\"");
				// More columns to come
				if (iter.hasNext())
					sql.append(", ");
			}
		}

		// From clause
		sql.append(" from ");

		// Join
		List joinSpecifiations = getJoinSpecifications();
		if (joinSpecifiations != null && !joinSpecifiations.isEmpty()) {

			int idx = 0;
			for (Iterator iter = joinSpecifiations.iterator(); iter.hasNext();) {
				SQLJoinSpec joinSpec = (SQLJoinSpec) iter.next();

				// Main table
				if (idx == 0) {
					String tableName = joinSpec.mainTableName;
					SQLTable table1 = getSelectedTable(tableName);
					sql.append(table1.name);
					if (table1.alias != null && table1.alias.trim().length() > 0) {
						sql.append(" AS ");
						sql.append(table1.alias);
					}
				}
				sql.append(" ");
				// Join type
				sql.append(joinSpec.joinType);
				sql.append(" ");
				// Join table
				String tableName = joinSpec.joinedTableName;
				SQLTable table2 = getSelectedTable(tableName);
				sql.append(table2.name);

				if (table2.alias != null && table2.alias.trim().length() > 0) {
					sql.append(" as ");
					sql.append(table2.alias);
				}
				// Columns
				sql.append(" on ");
				String onString = buildFilterSQL(joinSpec.getExpressions());
				sql.append(onString);
				idx++;
			}
		} else {
			List tables = getSelectedTables();
			for (Iterator iter = tables.iterator(); iter.hasNext();) {
				SQLTable table = (SQLTable) iter.next();
				sql.append(table.name);

				if (table.alias != null && table.alias.trim().length() > 0) {
					sql.append(" as ");
					sql.append(table.alias);
				}
				if (iter.hasNext())
					sql.append(",");
				else
					sql.append(" ");
			}
		}

		// Where clause
		String whereString = buildFilterSQL(getFilterCondition());
		if (whereString != null && whereString.trim().length() > 0) {
			sql.append(" where ");
			sql.append(whereString.trim());
		}

		// Group by clause
		List groupCols = getGroupByColumns();
		if (groupCols != null && !groupCols.isEmpty()) {
			sql.append(" group by ");
			for (Iterator iter = groupCols.iterator(); iter.hasNext();) {
				String[] groupCol = (String[]) iter.next();
				sql.append(groupCol[0]);
				// More columns to come
				if (iter.hasNext())
					sql.append(",");
			}
		}

		// Having clause
		String havingString = buildHavingSQL(getHavingCondition());
		if (havingString != null && havingString.trim().length() > 0) {
			sql.append(" having ");
			sql.append(havingString.trim());
		}

		// Order by clause
		List orderByCols = getOrderByColumns();
		if (orderByCols != null && !orderByCols.isEmpty()) {
			sql.append(" order by ");
			for (Iterator iter = orderByCols.iterator(); iter.hasNext();) {
				String[] orderByCol = (String[]) iter.next();

				sql.append(buildColumnFunctionSQL(orderByCol[0], orderByCol[1]));

				if (orderByCol[2] != null && orderByCol[2].trim().length() > 0) {
					sql.append(" " + orderByCol[2].toLowerCase());
				}

				// More columns to come
				if (iter.hasNext())
					sql.append(", ");
			}
		}

		return sql.toString();
	}

	/**
	 * Get connection
	 * 
	 * @return IConnection
	 */
	public Connection getConnection() {
		return connection;
	}

	/**
	 * Set new connection
	 * 
	 * @param connection
	 */
	public void setConnection(Connection connection) {
		this.connection = connection;
	}

	public boolean isCancel() {
		return cancel;
	}

	public void setCancel(boolean cancel) {
		this.cancel = cancel;
	}

	protected boolean isColumnName(String value) {
		if (value == null || value.trim().length() == 0)
			return false;

		if (value.startsWith("'") || value.endsWith("'"))
			return false;

		if (value.indexOf(" - ") > -1)
			value = value.substring(0, value.indexOf(" - "));

		// Test if the value is a function of column names. Ie (COLA+COLB)
		String[] arr = value.split("[\\+\\-\\*\\/]");
		if (arr.length > 1) {
			for (int i = 0; i < arr.length; i++) {
				if (isColumnName(arr[i]))
					return true;

			}
		}

		for (Iterator iter = getAvailableColumns().keySet().iterator(); iter.hasNext();) {
			String colName = (String) iter.next();
			if (value.equalsIgnoreCase(colName) || value.substring(value.indexOf(".") + 1).equalsIgnoreCase(colName.substring(colName.indexOf(".") + 1)))
				return true;

		}

		return false;
	}

	protected void maybeQualify(SQLColumn column) {
		if (column.qualified || column.table == null || column.table.trim().length() == 0)
			return;

		for (Iterator iter1 = getSelectedTables().iterator(); iter1.hasNext();) {
			SQLTable tab = (SQLTable) iter1.next();
			if (tab.getDisplayName().equalsIgnoreCase(column.table))
				continue;
			for (Iterator iter2 = tab.getAvailableColumns().iterator(); iter2.hasNext();) {
				SQLColumn col = (SQLColumn) iter2.next();
				if (col.name.equalsIgnoreCase(column.name)) {
					column.qualified = true;
					column.updateSQLString();
					return;
				}

			}
		}

	}

	public String buildFilterSQL(List filterList) {

		StringBuffer sql = new StringBuffer();

		if (filterList != null && !filterList.isEmpty()) {
			int c;
			for (Iterator iter = filterList.iterator(); iter.hasNext();) {
				String[] expression = (String[]) iter.next();

				c = 0;
				String logical = expression[c++];
				String lPar = expression[c++];
				String value1 = expression[c++];
				String cond = expression[c++];
				String value2 = expression[c++];
				String rPar = expression[c++];

				String[] valueArr = StringHelper.formatSeparetedToArray(value1.trim(), ".");
				String tableName1 = "";
				String columnName1 = "";
				if (valueArr.length == 1) {
					columnName1 = valueArr[0];
				} else if (valueArr.length == 2) {
					tableName1 = valueArr[0];
					columnName1 = valueArr[1];
				} else if (valueArr.length == 3) {
					tableName1 = valueArr[1];
					columnName1 = valueArr[2];
				}

				valueArr = StringHelper.formatSeparetedToArray(value2.trim(), ".");
				String tableName2 = "";
				String columnName2 = "";
				if (valueArr.length == 1) {
					columnName2 = valueArr[0];
				} else if (valueArr.length == 2) {
					tableName2 = valueArr[0];
					columnName2 = valueArr[1];
				} else if (valueArr.length == 3) {
					tableName2 = valueArr[1];
					columnName2 = valueArr[2];
				}

				SQLColumn col1 = null;
				try {
					if (tableName1 != null && tableName1.trim().length() > 0)
						col1 = getAvailableTable(tableName1).getAvailableColumn(columnName1, connection);
					else
						col1 = getAvailableColumn(columnName1);
				} catch (Exception e) {
				}
				SQLColumn col2 = null;
				try {
					if (tableName2 != null && tableName2.trim().length() > 0)
						col2 = getAvailableTable(tableName2).getAvailableColumn(columnName2, connection);
					else
						col2 = getAvailableColumn(columnName2);
				} catch (Exception e) {
				}

				// Translate simplified conditions to SQL conditions
				if (cond.equalsIgnoreCase("begins")) {
					cond = "Like";
					value2 += "%";
				} else if (cond.equalsIgnoreCase("ends")) {
					cond = "Like";
					value2 = "%" + value2;
				} else if (cond.equalsIgnoreCase("contains")) {
					cond = "Like";
					value2 = "%" + value2 + "%";
				}

				if (logical != null && logical.trim().length() > 0) {
					sql.append(logical);
					sql.append(" ");
				}
				if (lPar != null && lPar.trim().length() > 0) {
					sql.append(lPar);
				}

				if (isColumnName(value1)) {
					sql.append(value1);
				} else {
					if (col2 != null) {
						if (col2.isNumeric()) {
							sql.append(value1);
						} else {
							// String value
							sql.append("'");
							sql.append(value1);
							sql.append("'");
						}
					} else {
						try {
							// number value
							Integer.parseInt(value1);
							sql.append(value1);
						} catch (NumberFormatException e) {
							// String value
							sql.append("'");
							sql.append(value1);
							sql.append("'");
						}
					}

				}

				sql.append(" ");
				sql.append(cond);
				sql.append(" ");

				if (isColumnName(value2)) {
					sql.append(value2);
				} else {
					if (col1 != null) {
						if (col1.isNumeric()) {
							// number value
							sql.append(value2);
						} else {
							// String value
							sql.append("'");
							sql.append(value2);
							sql.append("'");
						}
					} else {
						try {
							// number value
							Integer.parseInt(value2);
							sql.append(value2);
						} catch (NumberFormatException e) {
							// String value
							sql.append("'");
							sql.append(value2);
							sql.append("'");
						}
					}
				}

				if (rPar != null && rPar.trim().length() > 0) {
					sql.append(rPar);
				}
				if (iter.hasNext())
					sql.append(" ");

			}
		}

		return sql.toString();
	}

	public String buildHavingSQL(List havingList) {

		StringBuffer sql = new StringBuffer();

		if (havingList != null && !havingList.isEmpty()) {

			int c;
			for (Iterator iter = havingList.iterator(); iter.hasNext();) {
				String[] expression = (String[]) iter.next();

				c = 0;
				String logical = expression[c++];
				String lPar = expression[c++];
				String funcValue1 = expression[c++];
				if (funcValue1 != null)
					funcValue1 = funcValue1.toLowerCase();
				String value1 = expression[c++];
				String cond = expression[c++];
				String funcValue2 = expression[c++];
				if (funcValue2 != null)
					funcValue2 = funcValue2.toLowerCase();
				String value2 = expression[c++];
				String rPar = expression[c++];

				if (logical != null && logical.trim().length() > 0) {
					sql.append(logical);
					sql.append(" ");
				}
				if (lPar != null && lPar.trim().length() > 0) {
					sql.append(lPar);
				}

				sql.append(buildColumnFunctionSQL(funcValue1, value1));

				sql.append(" ");

				// Translate simplified conditions to SQL conditions
				if (cond.equalsIgnoreCase("begins")) {
					cond = "Like";
					value2 += "%";
				} else if (cond.equalsIgnoreCase("ends")) {
					cond = "Like";
					value2 = "%" + value2;
				} else if (cond.equalsIgnoreCase("contains")) {
					cond = "Like";
					value2 = "%" + value2 + "%";
				}

				sql.append(cond);
				sql.append(" ");

				sql.append(buildColumnFunctionSQL(funcValue2, value2));

				if (rPar != null && rPar.trim().length() > 0) {
					sql.append(rPar);
				}
				if (iter.hasNext())
					sql.append(" ");

			}
		}

		return sql.toString();
	}

	public void updateTableName(String oldName, String newName) {

		// Update available columns for table
		SQLTable table = getSelectedTable(newName);
		for (Iterator iter = table.getAvailableColumns().iterator(); iter.hasNext();) {
			SQLColumn column = (SQLColumn) iter.next();
			column.table = newName;
		}

		String oldValue = oldName + ".";
		String newValue = newName + ".";

		// Update selected columns
		for (Iterator iter = getSelectedColumns().iterator(); iter.hasNext();) {
			SQLColumn column = (SQLColumn) iter.next();
			if (column.table.equalsIgnoreCase(oldName))
				column.table = newName;
			column.sqlString = StringHelper.scanAndReplace(column.sqlString, oldValue, newValue, false);
		}

		// Update selections
		for (Iterator iter = getFilterCondition().iterator(); iter.hasNext();) {
			String[] filterPart = (String[]) iter.next();
			for (int i = 0; i < filterPart.length; i++)
				if (filterPart[i] != null && isColumnName(filterPart[i]))
					filterPart[i] = StringHelper.scanAndReplace(filterPart[i], oldValue, newValue, false);
		}

		// Update sequence
		for (Iterator iter = getOrderByColumns().iterator(); iter.hasNext();) {
			String[] seqPart = (String[]) iter.next();
			for (int i = 0; i < seqPart.length; i++)
				if (seqPart[i] != null && isColumnName(seqPart[i]))
					seqPart[i] = StringHelper.scanAndReplace(seqPart[i], oldValue, newValue, false);
		}

		// Update join specifications
		for (Iterator iter = getJoinSpecifications().iterator(); iter.hasNext();) {
			SQLJoinSpec joinSpec = (SQLJoinSpec) iter.next();
			if (joinSpec.mainTableName.equalsIgnoreCase(oldName))
				joinSpec.mainTableName = newName;
			if (joinSpec.joinedTableName.equalsIgnoreCase(oldName))
				joinSpec.joinedTableName = newName;
			for (Iterator iterator = joinSpec.getExpressions().iterator(); iterator.hasNext();) {
				String[] expPart = (String[]) iterator.next();
				for (int i = 0; i < expPart.length; i++)
					if (expPart[i] != null && isColumnName(expPart[i]))
						expPart[i] = StringHelper.scanAndReplace(expPart[i], oldValue, newValue, false);

			}
		}

		// Update group by
		for (Iterator iter = getGroupByColumns().iterator(); iter.hasNext();) {
			String[] groupingPart = (String[]) iter.next();
			for (int i = 0; i < groupingPart.length; i++)
				if (groupingPart[i] != null && isColumnName(groupingPart[i]))
					groupingPart[i] = StringHelper.scanAndReplace(groupingPart[i], oldValue, newValue, false);
		}

		// Update having clause
		for (Iterator iter = getHavingCondition().iterator(); iter.hasNext();) {
			String[] havingPart = (String[]) iter.next();
			for (int i = 0; i < havingPart.length; i++)
				if (havingPart[i] != null && isColumnName(havingPart[i]))
					havingPart[i] = StringHelper.scanAndReplace(havingPart[i], oldValue, newValue, false);
		}

	}

	public SQLTable newTable() {
		return new SQLTable();
	}

	public SQLTable newTable(FromTableDef tableDef) {
		return new SQLTable(ConnectionManager.getSchemaSep(queryDef.getDBConnection()), tableDef);
	}

	public SQLTable newTable(SQLTable table) {
		return new SQLTable(table);
	}

	public String buildColumnFunctionSQL(String function, String columnName) {
		return buildColumnFunctionSQL(function, columnName, true);
	}

	public String buildColumnFunctionSQL(String function, String columnName, boolean addQuote) {

		StringBuffer sql = new StringBuffer();

		if (function != null && function.trim().length() > 0) {
			sql.append(function.toLowerCase());
			sql.append("(");
		}

		if (columnName == null)
			columnName = "";

		if (isColumnName(columnName) || isReplacementVar(columnName)) {
			sql.append(columnName);
		} else {
			try {
				// number value
				Integer.parseInt(columnName);
				sql.append(columnName);
			} catch (NumberFormatException e) {
				// String value
				boolean quoted = false;
				if (columnName.startsWith("'") && columnName.endsWith("'"))
					quoted = true;
				if (addQuote && !quoted)
					sql.append("'");
				sql.append(columnName);
				if (addQuote && !quoted)
					sql.append("'");

			}
		}

		if (function != null && function.trim().length() > 0) {
			sql.append(")");
		}

		return sql.toString();

	}

	public String[] splitColumnFunctionSQL(String function) {

		String[] arr = new String[2];
		arr[0] = "";
		arr[1] = function;

		// Function on column?
		if (function != null && function.trim().length() > 0) {
			for (int j = 0; j < SQLParserHelper.FUNCTIONS.length; j++) {
				String func = SQLParserHelper.FUNCTIONS[j] + "(";
				if (function.toUpperCase().indexOf(func) > -1) {
					arr[0] = SQLParserHelper.FUNCTIONS[j];

					// Extract column name
					int from = function.indexOf("(");
					int to = function.lastIndexOf(")");
					if (from > -1 && to > -1) {
						String name = function.substring(from + 1, to).trim();
						name = name.replace(" ", "");
						arr[1] = name;
					}
					break;

				}
			}
		}

		return arr;

	}

	public boolean isGrouped() {

		if (!groupByColumns.isEmpty())
			return true;

		boolean aggrUsed = false;
		boolean singleUsed = false;
		for (Iterator iter = getSelectedColumns().iterator(); iter.hasNext();) {
			SQLColumn col = (SQLColumn) iter.next();
			if (col.aggregateFunction != null && col.aggregateFunction.trim().length() > 0)
				aggrUsed = true;
			if (col.aggregateFunction == null || col.aggregateFunction.trim().length() == 0)
				singleUsed = true;
			if (aggrUsed && singleUsed)
				break;
		}

		if (aggrUsed == true && singleUsed == false)
			return true;

		return false;

	}

	public void searchColumn(Combo combo) {

		SQLAvailableColumnsSearchDialog dialog = new SQLAvailableColumnsSearchDialog(combo.getShell(), this);
		if (dialog.open()) {
			if (dialog.getSelectedColumn() != null)
				combo.setText(dialog.getSelectedColumn().getQualifiedName());
		}

	}

	public void sampleColumnContents(Combo combo) {

		String columnName = combo.getText();

		if (columnName.indexOf(" - ") > -1)
			columnName = columnName.substring(0, columnName.indexOf(" - "));
		if (columnName.indexOf(".") > -1)
			columnName = columnName.substring(columnName.indexOf(".") + 1);

		// Find column
		SQLColumn column = null;
		SQLTable table = null;
		for (Iterator iter = getSelectedTables().iterator(); iter.hasNext();) {
			table = (SQLTable) iter.next();
			SQLColumn tempCol = table.getAvailableColumn(columnName);
			if (tempCol != null) {
				column = tempCol;
				break;
			}
		}

		if (column != null) {
			SQLTableContentsSampleDialog dialog = new SQLTableContentsSampleDialog(combo.getShell(), this, table, column);
			dialog.open();
		}

	}

	public boolean isCancelled() {
		return cancelled;
	}

	public boolean isReplacementVar(String value) {
		return sm.isReplacementVar(value);
	}
}
