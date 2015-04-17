// {{CopyrightNotice}}

package com.ss.speedtransfer.ui.wizard.qb;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import oracle.jdbc.OracleConnection;

import org.postgresql.PGConnection;

import com.ibm.db2.jcc.DB2Connection;
import com.microsoft.sqlserver.jdbc.SQLServerConnection;
import com.ss.speedtransfer.util.SSUtil;
import com.ss.speedtransfer.util.UIHelper;
import com.ss.speedtransfer.util.parser.sql.FromTableDef;


/**
 * The <code>SQLTable</code> class
 */
public class SQLTable extends Object {

	/** Table owner */
	public String owner = "";

	/** Table name */
	public String name = "";

	/** Table schema */
	public String schema = "";

	/** Table qualified */
	public boolean qualified = false;

	/** The alias of this table */
	public String alias = "";

	/** The table description */
	public String remark = "";

	/** The table type */
	public String type = "";

	/** Schema separator */
	public String schemaSeparator = ".";

	/** Table sql string */
	public String sqlString = "";

	/** Available columns in this table */
	protected List availableColumns = new ArrayList();

	/** Table reference. USed during edit of table */
	public SQLTable tableRef = null;

	protected boolean cancelled = false;

	protected Map<String, String> labelMap = null;

	public SQLTable() {
		super();
	}

	public SQLTable(SQLTable table) {
		super();
		owner = table.owner;
		name = table.name;
		schema = table.schema;
		qualified = table.qualified;
		alias = table.alias;
		remark = table.remark;
		type = table.type;
		schemaSeparator = table.schemaSeparator;
		sqlString = table.sqlString;
		availableColumns = table.getAvailableColumns();
		tableRef = table;

	}

	public SQLTable(String schemaSeparator) {
		super();
		this.schemaSeparator = schemaSeparator;

	}

	public SQLTable(FromTableDef table) {
		this(null, table);

	}

	public SQLTable(String schemaSeparator, FromTableDef table) {
		this(schemaSeparator);
		if (table != null)
			parseTableDef(table);
	}

	public String getQualifiedName() {
		StringBuffer qualName = new StringBuffer();
		if (schema != null && schema.trim().length() > 0) {
			qualName.append(schema);
			qualName.append(schemaSeparator);
		}

		qualName.append(name);

		return qualName.toString();

	}

	public String getDisplayName() {
		if (alias != null && alias.trim().length() > 0)
			return alias;
		else
			return name;

	}

	public SQLColumn getAvailableColumn(String columnName) {
		return getAvailableColumn(columnName, null);

	}

	public SQLColumn getAvailableColumn(String columnName, Connection connection) {
		if (columnName == null || columnName.trim().length() == 0)
			return null;

		for (Iterator iter = getAvailableColumns(connection).iterator(); iter.hasNext();) {
			SQLColumn column = (SQLColumn) iter.next();
			if (column.name.trim().equalsIgnoreCase(columnName.trim()))
				return column;
		}

		return null;
	}

	public List getAvailableColumns() {
		return getAvailableColumns(null);
	}

	public List getAvailableColumns(Connection connection) {
		if ((availableColumns == null || availableColumns.size() == 0))
			// if((availableColumns == null || availableColumns.size() == 0) &&
			// connection != null)
			updateAvailableColumns(connection);
		return availableColumns;
	}

	public void updateAvailableColumns(Connection connection) {

		availableColumns.clear();
		try {

			DatabaseMetaData md = connection.getMetaData();

			if (schema != null && schema.trim().length() == 0)
				schema = null;

			ResultSet rs;
			if (connection instanceof SQLServerConnection || connection instanceof OracleConnection || connection instanceof PGConnection) {
				rs = md.getColumns(schema, null, name, null);
			}else{
				if (connection instanceof DB2Connection)
					rs = md.getColumns(null, schema.toUpperCase(), name.toUpperCase(), null);
				else
					rs = md.getColumns(null, schema, name, null);
			}


			while (rs.next()) {
				String columnName = rs.getString(4);
				String remark = rs.getString(12);
				String dataType = rs.getString(5);
				String dataTypeName = rs.getString(6);
				String length = rs.getString(7);
				String decimals = rs.getString(9);

				if (remark == null) {
					remark = getLabelMap(connection).get(columnName);
				}

				SQLColumn column = new SQLColumn();
				if (alias != null && alias.trim().length() > 0)
					column.table = alias;
				else
					column.table = name;
				column.name = columnName;
				column.remark = remark;
				column.dataType = dataType;
				column.dataTypeName = dataTypeName;
				column.length = length;
				column.decimals = decimals;

				availableColumns.add(column);
			}

		} catch (Exception e) {
			UIHelper.instance().showErrorMsg("Error", "Load columns for table " + name + " failed! Error: " + SSUtil.getMessage(e));
		}

	}

	protected void parseTableDef(FromTableDef table) {
		if (table.getAlias() != null && table.getAlias().trim().length() > 0)
			alias = table.getAlias();

		name = table.getName();
		if (schemaSeparator != null && schemaSeparator.trim().length() > 0) {
			name = name.replace(".", schemaSeparator);
			name = name.replace("/", schemaSeparator);

			int idx = name.indexOf(schemaSeparator);
			if (idx > -1) {
				schema = name.substring(0, idx);
				name = name.substring(idx + 1);
				qualified = true;

			}
		}

		sqlString = name;

	}

	public List getSampleData(Connection connection, int numberOfRows, boolean distinctValues, String colName) {

		List data = new ArrayList(numberOfRows);
		Statement stmt = null;

		int size = 1;
		if (colName == null || colName.trim().length() == 0)
			size = getAvailableColumns(connection).size();

		try {
			stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(getSampleSQL(distinctValues, colName));
			int counter = 0;
			while (rs.next() && counter < numberOfRows) {
				String[] dataArr = new String[size];
				for (int i = 0; i < dataArr.length; i++) {
					dataArr[i] = rs.getString(i + 1);
				}
				data.add(dataArr);
				counter++;
			}

		} catch (Exception e) {
			UIHelper.instance().showErrorMsg("Error", "Load of table sample data for table " + name + " failed! Error: " + SSUtil.getMessage(e));
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (Exception e) {
			}
		}

		return data;

	}

	protected String getSampleSQL(boolean distinctValues, String colName) {

		StringBuffer sql = new StringBuffer();

		sql.append("select ");
		if (distinctValues)
			sql.append("distinct ");
		if (colName == null || colName.trim().length() == 0)
			sql.append("* ");
		else
			sql.append(colName + " ");
		sql.append("from ");
		if (schema != null && schema.trim().length() > 0) {
			sql.append(schema.trim());
			sql.append(schemaSeparator);
		}
		sql.append(name);

		return sql.toString();

	}

	public boolean isCancelled() {
		return cancelled;
	}

	public String getSchema() {
		if (schema == null)
			return "";

		return schema;
	}

	protected Map<String, String> getLabelMap(Connection connection) {

		if (labelMap == null) {
			labelMap = new HashMap<String, String>();
			Statement stmt = null;
			try {
				stmt = connection.createStatement();
				ResultSet rs = stmt.executeQuery("select * from " + name + " where 1=2");
				ResultSetMetaData md = rs.getMetaData();
				for (int i = 1; i <= md.getColumnCount(); i++) {
					labelMap.put(md.getColumnName(i), md.getColumnLabel(i));
				}

			} catch (Exception e) {
			} finally {
				try {
					if (stmt != null)
						stmt.close();
				} catch (Exception e) {
				}
			}
		}
		return labelMap;

	}
}
