// {{CopyrightNotice}}

package com.ss.speedtransfer.util;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import oracle.jdbc.OracleConnection;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.relique.jdbc.csv.CsvConnection;

import com.ibm.db2.jcc.DB2Connection;
import com.mysql.jdbc.MySQLConnection;

public class SQLHelper {

	public static List<String[]> getColumnProperties(Connection con, ResultSet rs, String headingType) {
		return getColumnProperties(con, rs, headingType, null);

	}

	public static List<String[]> getColumnProperties(Connection con, ResultSet rs, String headingType, IProgressMonitor monitor) {

		SubMonitor progress = null;
		List<String[]> props = new ArrayList<String[]>();

		if (headingType == null || headingType.trim().length() == 0)
			headingType = "name";

		try {
			ResultSetMetaData rsmd = rs.getMetaData();
			DatabaseMetaData md = con.getMetaData();

			if (monitor != null) {
				progress = SubMonitor.convert(monitor, rsmd.getColumnCount());
				progress.beginTask("Retrieving column information from database...", rsmd.getColumnCount());
			}

			for (int i = 1; i <= rsmd.getColumnCount(); i++) {
				String column = rsmd.getColumnName(i);
				String label = rsmd.getColumnLabel(i);
				String table = rsmd.getTableName(i);
				String schema = rsmd.getSchemaName(i);
				String catalog = rsmd.getCatalogName(i);

				if (con instanceof MySQLConnection || con instanceof DB2Connection) {
					if (label != null && label.trim().length() > 0)
						column = label;

				}

				String remark = "";
				ResultSet columnrs = null;
				if (headingType.equalsIgnoreCase("name") == false) {
					try {
						columnrs = md.getColumns(catalog, schema, table, column);
						if (columnrs.next()) {
							remark = columnrs.getString(12);
						} else {
							columnrs = md.getColumns(null, schema, table, column);
							if (columnrs.next())
								remark = columnrs.getString(12);
						}
						if (remark == null || remark.trim().length() == 0)
							remark = column;
					} catch (Exception e) {
					} finally {
						try {
							if (columnrs != null)
								columnrs.close();
						} catch (Exception e2) {
						}
					}
				}

				String heading;
				if (headingType.equalsIgnoreCase("name") || (remark == null || remark.trim().length() == 0)) {
					heading = column;
				} else {
					if (headingType.equalsIgnoreCase("both"))
						heading = column + " (" + remark + ")";
					else
						heading = remark;

				}

				int type = rsmd.getColumnType(i);
				int size = rsmd.getColumnDisplaySize(i);
				int columnSize = size;
				if (columnSize < heading.length())
					columnSize = heading.length();

				// Find alignment
				String alignment = "right";
				switch (type) {
				case Types.BOOLEAN:
					alignment = "center";
					break;
				case Types.DATE:
					alignment = "center";
					break;
				case Types.TIME:
					alignment = "center";
					break;
				case Types.TIMESTAMP:
					alignment = "center";
					break;
				case Types.CHAR:
					alignment = "left";
					break;
				case Types.LONGVARCHAR:
					alignment = "left";
					break;
				case Types.VARCHAR:
					alignment = "left";
					break;

				default:
					break;
				}

				if (alignment.equals("left") && size == 1)
					alignment = "center";

				String[] arr = new String[8];
				arr[0] = column;
				arr[1] = table;
				arr[2] = schema;
				arr[3] = label;
				arr[4] = heading;
				arr[5] = Integer.toString(columnSize);
				arr[6] = alignment;
				arr[7] = Integer.toString(type);

				props.add(arr);

				if (progress != null) {
					if (progress.isCanceled())
						return props;
					progress.worked(1);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (progress != null)
				progress.done();
		}

		return props;

	}

	public static int getSQLRowCount(String sql, Connection con) throws SQLException {

		Statement stmt = null;

		try {
			if (con instanceof CsvConnection) {
				stmt = con.createStatement();
				int count = 0;
				ResultSet rs = stmt.executeQuery(sql);
				while (rs.next())
					count++;
				return count;
			}

			sql = sql.trim();
			if (sql.endsWith(";"))
				sql = sql.substring(0, sql.length() - 1);

			String countSQL = "SELECT COUNT(1) FROM (" + sql + ") as temp";
			if (con instanceof OracleConnection)
				countSQL = "SELECT COUNT(1) FROM (" + sql + ")";

			stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(countSQL);
			if (rs.next())
				return rs.getInt(1);

		} catch (SQLException e) {
			throw e;
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (Exception e2) {
			}
		}

		return 0;

	}

	public static boolean hasMultipleStatements(String sql) {
		if (sql == null || sql.trim().length() == 0)
			return false;
		return getStatements(sql).size() > 1;
	}

	public static List<String> getStatements(String sql) {
		String[] arr = sql.split(";");
		List<String> list = Arrays.asList(arr);

		List<String> newList = new ArrayList<String>();
		for (String s : list) {
			if (s.trim().length() > 0)
				newList.add(s);
		}

		return newList;
	}

	public static String getAllButLastSQLStatement(String sql) {
		if (sql == null || sql.trim().length() == 0)
			return null;

		List<String> statements = getStatements(sql);

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < statements.size() - 1; i++) {
			sb.append(statements.get(i));
			sb.append("; ");
		}
		return sb.toString();
	}

	public static String getLastSQLStatement(String sql) {
		if (sql == null || sql.trim().length() == 0)
			return null;

		List<String> statements = getStatements(sql);
		return statements.get(statements.size() - 1);

	}

	public static boolean isQuery(String sql) {
		if (sql == null || sql.trim().length() == 0)
			return false;
		return getLastSQLStatement(sql).trim().toLowerCase().startsWith("select");
	}

	public static boolean isSingleStatementQuery(String sql) {
		if (sql == null || sql.trim().length() == 0)
			return false;
		return hasMultipleStatements(sql) == false && sql.trim().toLowerCase().startsWith("select");
	}

	public static String getSQLType(String sql) {
		sql = sql.trim().toLowerCase();
		int idx = sql.indexOf(" ");
		return sql.substring(0, idx);
	}

	public static boolean producesResult(String sql) {
		if (hasMultipleStatements(sql) == false) {
			String type = getSQLType(sql).toLowerCase();
			return type.equals("update") || type.equals("delete") || type.equals("insert");
		}

		return false;

	}

}
