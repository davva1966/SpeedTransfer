package com.ss.speedtransfer.util;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.ss.speedtransfer.model.DBConnection;
import com.ss.speedtransfer.model.QueryDefinition;


public class ConnectionManager {

	public static final String MYSQL = "mysql";
	public static final String ISERIES = "iseries";
	public static final String AS400 = "as400";
	public static final String SQLSERVER = "sqlserver";
	public static final String ORACLE = "oracle";
	public static final String POSTGRESQL = "postgresql";
	public static final String DB2 = "db2";
	public static final String CSV = "csv";

	public static Map<String, String> schemaSepMap = new HashMap<String, String>();

	@SuppressWarnings("serial")
	public static class LogonCancelledException extends Exception {
		public LogonCancelledException(String msg) {
			super(msg);
		}

	}

	static {
		try {
			DriverManager.registerDriver(new com.mysql.jdbc.Driver());
		} catch (SQLException e) {
		}
		try {
			DriverManager.registerDriver(new com.ibm.as400.access.AS400JDBCDriver());
		} catch (SQLException e) {
		}
		try {
			DriverManager.registerDriver(new com.microsoft.sqlserver.jdbc.SQLServerDriver());
		} catch (SQLException e) {
		}
		try {
			DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
		} catch (SQLException e) {
		}
		try {
			DriverManager.registerDriver(new org.postgresql.Driver());
		} catch (SQLException e) {
		}
		try {
			DriverManager.registerDriver(new com.ibm.db2.jcc.DB2Driver());
		} catch (SQLException e) {
		}
		try {
			DriverManager.registerDriver(new org.relique.jdbc.csv.CsvDriver());
		} catch (SQLException e) {
		}

	}

	public static Connection getConnection(QueryDefinition queryDef) throws Exception {
		if (queryDef != null) {
			DBConnection dbCon = null;

			String prop = System.getProperty("com.ss.speedtransfer.executeOnDefaultCon");
			if (prop != null && prop.trim().equalsIgnoreCase("true")) {
				if (DefaultDBManager.instance().hasDefaultConnection())
					dbCon = DefaultDBManager.instance().getDefaultConnection();
			}

			if (dbCon == null)
				dbCon = queryDef.getDBConnection();

			if (dbCon != null)
				return getConnection(dbCon);
			else
				throw new Exception("No database connection found");
		}

		return null;

	}

	public static Connection getConnection(DBConnection dbCon) throws Exception {
		Connection con = getConnection(buildURL(dbCon), buildProperties(dbCon));
		dbCon.setPromptComplete(true);
		return con;

	}

	public static Connection getConnection(String url) throws SQLException {
		return DriverManager.getConnection(url);

	}

	public static Connection getConnection(String url, String user, String password) throws SQLException {
		return DriverManager.getConnection(url, user, password);

	}

	public static Connection getConnection(String url, Properties props) throws SQLException {
		return DriverManager.getConnection(url, props);

	}

	public static Driver getDriver(String conType) throws SQLException {
		return DriverManager.getDriver(getBaseUrl(conType));

	}

	public static String getBaseUrl(String conType) {
		if (conType.equalsIgnoreCase(MYSQL))
			return "jdbc:mysql://";
		else if (conType.equalsIgnoreCase(ISERIES) || conType.equalsIgnoreCase(AS400))
			return "jdbc:as400://";
		else if (conType.equalsIgnoreCase(SQLSERVER))
			return "jdbc:sqlserver://";
		else if (conType.equalsIgnoreCase(ORACLE))
			return "jdbc:oracle:thin:@//";
		else if (conType.equalsIgnoreCase(POSTGRESQL))
			return "jdbc:postgresql://";
		else if (conType.equalsIgnoreCase(DB2))
			return "jdbc:db2://";
		else if (conType.equalsIgnoreCase(CSV))
			return "jdbc:relique:csv:";

		return null;

	}

	public static String buildURL(DBConnection dbCon) throws Exception {

		String base = getBaseUrl(dbCon.getConnectionType());
		String host = dbCon.getHost();
		String port = dbCon.getPort();
		String db = dbCon.getDatabase();

		StringBuilder sb = new StringBuilder();
		sb.append(base);
		sb.append(host);

		if (dbCon.getConnectionType().equalsIgnoreCase(CSV))
			return sb.toString();

		if (port != null && port.trim().length() > 0 && port.trim().equals("0") == false) {
			sb.append(":");
			sb.append(port);
		}

		if (dbCon.getConnectionType().equalsIgnoreCase(SQLSERVER) == false) {
			sb.append("/");
			sb.append(db);
		}

		return sb.toString();

	}

	public static Properties buildProperties(DBConnection dbCon) throws Exception {
		Properties props = new Properties();
		Map<String, String> propertyMap = dbCon.getConnectionProperties();

		if (dbCon.getConnectionType().equalsIgnoreCase(SQLSERVER))
			props.put("databaseName", dbCon.getDatabase());

		for (String propName : propertyMap.keySet()) {
			if (propName.length() > 0)
				props.put(propName, propertyMap.get(propName));
		}

		boolean prompt = dbCon.getPrompt();

		if (prompt) {
			if (logonPrompt(dbCon) == false)
				throw new Exception("Logon cancelled");
		}

		if (dbCon.getConnectionType().equalsIgnoreCase(CSV) == false) {
			props.put("user", dbCon.getUser());
			props.put("password", dbCon.getPassword());
		}

		applyDefaults(dbCon, props);

		return props;

	}

	protected static void applyDefaults(DBConnection dbCon, Properties props) {

		if (dbCon.getConnectionType().equalsIgnoreCase(ISERIES)) {
			if (props.contains("prompt") == false)
				props.put("prompt", "false");
			if (props.contains("extended metadata") == false)
				props.put("extended metadata", "true");
			if (props.contains("metadata source") == false)
				props.put("metadata source", "0");
		}

	}

	public static List<String[]> getDriverPropertiesInfo(String conType) throws SQLException {

		List<String[]> propertyInfoList = new ArrayList<String[]>();
		Driver driver = getDriver(conType);

		Properties info = new Properties();
		DriverPropertyInfo[] attributes = driver.getPropertyInfo(getBaseUrl(conType), info);

		for (int i = 0; i < attributes.length; i++) {

			String[] propertyInfo = new String[4];

			String name = attributes[i].name;
			String[] choices = attributes[i].choices;
			boolean required = attributes[i].required;
			String description = attributes[i].description;

			StringBuilder sb = new StringBuilder();
			if (choices != null) {
				for (int j = 0; j < choices.length; j++) {
					sb.append(choices[j]);
					if (j < choices.length - 1)
						sb.append("| ");
				}
			}

			propertyInfo[0] = name;
			propertyInfo[1] = description;
			propertyInfo[2] = sb.toString();
			propertyInfo[3] = Boolean.toString(required);

			propertyInfoList.add(propertyInfo);

		}

		Collections.sort(propertyInfoList, new Comparator() {
			public int compare(Object o1, Object o2) {
				return ((String[]) o1)[0].compareToIgnoreCase(((String[]) o2)[0]);
			}
		});
		return propertyInfoList;

	}

	public static boolean logonPrompt(DBConnection dbCon) throws LogonCancelledException {

		if (dbCon.getConnectionType().equalsIgnoreCase(CSV))
			return true;

		dbCon.reset();
		DatabaseLogonPromptDialog dialog = new DatabaseLogonPromptDialog(UIHelper.instance().getActiveShell(), dbCon.toString(), dbCon.getUser(), dbCon.getPassword());
		dialog.open();
		if (dialog.isCancelled())
			throw new LogonCancelledException("Logon cancelled");

		dbCon.setUser(dialog.getUser());
		dbCon.setPassword(dialog.getPassword());

		return true;

	}

	public static boolean checkConnection(QueryDefinition queryDef) throws Exception {

		Connection con = null;
		try {
			con = ConnectionManager.getConnection(queryDef);
		} finally {
			try {
				if (con != null && con.isClosed() == false)
					con.close();
			} catch (Exception e2) {
			}
		}

		return true;
	}

	public static String buildKey(DBConnection dbCon) {
		String base = getBaseUrl(dbCon.getConnectionType());
		String host = dbCon.getHost();
		String port = dbCon.getPort();
		String db = dbCon.getDatabase();

		StringBuilder sb = new StringBuilder();
		sb.append(base);
		sb.append(host);
		if (port != null && port.trim().length() > 0 && port.trim().equals("0") == false) {
			sb.append(":");
			sb.append(port);
		}
		sb.append("/");
		sb.append(db);

		return sb.toString();
	}

	public static String getSchemaSep(DBConnection dbCon) {
		if (schemaSepMap.containsKey(dbCon.getConnectionType()) == false) {
			Connection con = null;
			try {
				con = getConnection(dbCon);
				String sep = con.getMetaData().getCatalogSeparator();
				if (sep == null || sep.trim().length() == 0)
					sep = ".";
				schemaSepMap.put(dbCon.getConnectionType(), sep);
			} catch (Exception e) {
			} finally {
				try {
					if (con != null)
						con.close();
				} catch (Exception e2) {
				}
			}
		}

		return schemaSepMap.get(dbCon.getConnectionType());

	}

	public static String buildConnectionInfoString(DBConnection dbCon) throws Exception {

		StringBuilder sb = new StringBuilder();
		sb.append("Connection string:");
		sb.append(StringHelper.getNewLine());
		sb.append(buildURL(dbCon));

		Properties props = buildProperties(dbCon);
		if (props.size() > 0) {
			sb.append(StringHelper.getNewLine());
			sb.append(StringHelper.getNewLine());

			sb.append("Connection properties:");
			sb.append(StringHelper.getNewLine());
			for (Object propName : props.keySet()) {
				sb.append(propName);
				sb.append("=");
				if (((String) propName).toLowerCase().contains("password"))
					sb.append("######");
				else
					sb.append(props.get(propName));
				sb.append(StringHelper.getNewLine());
			}

		}

		return sb.toString();

	}
}
