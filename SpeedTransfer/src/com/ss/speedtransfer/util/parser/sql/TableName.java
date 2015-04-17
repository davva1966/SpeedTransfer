// {{CopyrightNotice}}

package com.ss.speedtransfer.util.parser.sql;

/**
 * The <code>TableName</code> class
 */
public final class TableName {

	/**
	 * The constant 'schema_name' that defines a schema that is unknown.
	 */
	private static final String UNKNOWN_SCHEMA_NAME = "##UNKNOWN_SCHEMA##";

	/**
	 * The name of the schema of the table. This value can be 'null' which means the schema is currently unknown.
	 */
	private final String schema_name;

	/**
	 * The name of the table.
	 */
	private final String table_name;

	/**
	 * Constructs the name.
	 */
	public TableName(String schema_name, String table_name) {
		if (table_name == null) {
			throw new NullPointerException("'name' can not be null.");
		}
		if (schema_name == null) {
			schema_name = UNKNOWN_SCHEMA_NAME;
		}

		this.schema_name = schema_name;
		this.table_name = table_name;
	}

	public TableName(String table_name) {
		this(UNKNOWN_SCHEMA_NAME, table_name);
	}

	/**
	 * Returns the schema name or null if the schema name is unknown.
	 */
	public String getSchema() {
		if (schema_name.equals(UNKNOWN_SCHEMA_NAME)) {
			return null;
		} else {
			return schema_name;
		}
	}

	/**
	 * Returns the table name.
	 */
	public String getName() {
		return table_name;
	}

	/**
	 * Resolves a schema reference in a table name. If the schema in this table is 'null' (which means the schema is unknown) then it is set to the given schema argument.
	 */
	public TableName resolveSchema(String scheman) {
		if (schema_name.equals(UNKNOWN_SCHEMA_NAME)) {
			return new TableName(scheman, getName());
		}
		return this;
	}

	/**
	 * Resolves a [schema name].[table name] type syntax to a TableName object. Uses 'schemav' only if there is no schema name explicitely specified.
	 */
	public static TableName resolve(String schemav, String namev) {
		int i = namev.indexOf('.');
		if (i == -1) {
			return new TableName(schemav, namev);
		} else {
			return new TableName(namev.substring(0, i), namev.substring(i + 1));
		}
	}

	/**
	 * Resolves a [schema name].[table name] type syntax to a TableName object.
	 */
	public static TableName resolve(String namev) {
		return resolve(UNKNOWN_SCHEMA_NAME, namev);
	}

	// ----

	/**
	 * To string.
	 */
	public String toString() {
		if (getSchema() != null) {
			return getSchema() + "." + getName();
		}
		return getName();
	}

	/**
	 * Equality.
	 */
	public boolean equals(Object ob) {
		TableName tn = (TableName) ob;
		return tn.schema_name.equals(schema_name) && tn.table_name.equals(table_name);
	}

	/**
	 * Equality but ignore the case.
	 */
	public boolean equalsIgnoreCase(TableName tn) {
		return tn.schema_name.equalsIgnoreCase(schema_name) && tn.table_name.equalsIgnoreCase(table_name);
	}

	/**
	 * Comparable.
	 */
	public int compareTo(Object ob) {
		TableName tn = (TableName) ob;
		int v = schema_name.compareTo(tn.schema_name);
		if (v == 0) {
			return table_name.compareTo(tn.table_name);
		}
		return v;
	}

	/**
	 * Hash code.
	 */
	public int hashCode() {
		return schema_name.hashCode() + table_name.hashCode();
	}

}