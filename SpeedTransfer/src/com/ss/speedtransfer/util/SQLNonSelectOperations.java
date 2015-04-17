package com.ss.speedtransfer.util;

import java.util.ArrayList;
import java.util.List;

public class SQLNonSelectOperations {

	public static String DROP = "DROP ";
	public static String SHOW = "SHOW ";
	public static String ALTER = "ALTER ";
	public static String UPDATE = "UPDATE ";
	public static String CREATE = "CREATE ";
	public static String DELETE = "DELETE ";
	public static String INSERT = "INSERT ";
	public static String COMMIT = "COMMIT ";
	public static String COMMENT = "COMMENT ";
	public static String COMPACT = "COMPACT ";
	public static String EXPLAIN = "EXPLAIN ";
	public static String ROLLBACK = "ROLLBACK ";
	public static String OPTIMIZE = "OPTIMIZE ";
	public static String DESCRIBE = "DESCRIBE ";
	public static String SHUTDOWN = "SHUTDOWN ";
	public static String PREPARE = "PREPARE ";
	public static String CALL = "CALL ";
	public static String INTO = "INTO ";
	public static String LOCK = "LOCK ";
	public static String GRANT = "GRANT ";
	public static String UNLOCK = "UNLOCK ";
	public static String ACTION = "ACTION ";
	public static String GROUPS = "GROUPS ";
	public static String REVOKE = "REVOKE ";

	protected static List<String> operations = null;

	public static String getOperation(String sql) {

		String[] statements = sql.split(";");
		for (String stmt : statements) {
			String cleanStmt = stmt.trim();
			cleanStmt.replace("\r", "");
			cleanStmt.replace("\t", "");
			cleanStmt.replace("\n", "");
			for (String oper : getOperations()) {
				if (cleanStmt.trim().startsWith(oper))
					return oper;
				if (cleanStmt.trim().contains(" " + oper))
					return oper;
			}

		}

		return null;

	}

	protected static List<String> getOperations() {
		if (operations == null) {
			operations = new ArrayList<String>();
			operations.add(DROP);
			operations.add(SHOW);
			operations.add(ALTER);
			operations.add(UPDATE);
			operations.add(CREATE);
			operations.add(DELETE);
			operations.add(INSERT);
			operations.add(COMMIT);
			operations.add(COMMENT);
			operations.add(COMPACT);
			operations.add(EXPLAIN);
			operations.add(ROLLBACK);
			operations.add(OPTIMIZE);
			operations.add(DESCRIBE);
			operations.add(SHUTDOWN);
			operations.add(PREPARE);
			operations.add(CALL);
			operations.add(INTO);
			operations.add(LOCK);
			operations.add(GRANT);
			operations.add(UNLOCK);
			operations.add(ACTION);
			operations.add(GROUPS);
			operations.add(REVOKE);
		}

		return operations;
	}
}
