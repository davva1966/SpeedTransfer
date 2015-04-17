// {{CopyrightNotice}}

package com.ss.speedtransfer.util.parser.sql;

import java.util.List;

/**
 * The <code>TType</code> class
 */
public abstract class TType {

	/**
	 * The type as an SQL identifier from com.mckoi.database.global.SQLTypes.
	 */
	private int sql_type;

	/**
	 * Constructs the type object.
	 */
	protected TType(int sql_type) {
		this.sql_type = sql_type;
	}

	/**
	 * Returns the SQL type of this.
	 */
	public int getSQLType() {
		return sql_type;
	}

	/**
	 * Returns this TType as a fully parsable declared SQL type. For example, if this represents a string we might return "VARCHAR(30) COLLATE 'jpJP'" This method is used for debugging and display
	 * purposes only and we would not expect to actually feed this back into an SQL parser.
	 */
	public String asSQLString() {
		return DataTableColumnDef.sqlTypeToString(getSQLType());
	}

	/**
	 * Returns the value of a string that is quoted. For example, 'test' becomes test.
	 */
	private static String parseQuotedString(String str) {
		if (str.startsWith("'") && str.endsWith("'")) {
			return str.substring(1, str.length() - 1);
		} else {
			throw new RuntimeException("String is not quoted: " + str);
		}
	}

	/**
	 * Encodes a TType into a string which is a useful way to serialize a TType. The encoded string should be understandable when read.
	 */
	public static String asEncodedString(TType type) {
		StringBuffer buf = new StringBuffer();
		if (type instanceof TBooleanType) {
			buf.append("BOOLEAN(");
			buf.append(type.getSQLType());
			buf.append(')');
		} else if (type instanceof TStringType) {
			TStringType str_type = (TStringType) type;
			buf.append("STRING(");
			buf.append(type.getSQLType());
			buf.append(',');
			buf.append(str_type.getMaximumSize());
			buf.append(",'");
			buf.append(str_type.getLocaleString());
			buf.append("',");
			buf.append(str_type.getStrength());
			buf.append(',');
			buf.append(str_type.getDecomposition());
			buf.append(')');
		} else if (type instanceof TNumericType) {
			TNumericType num_type = (TNumericType) type;
			buf.append("NUMERIC(");
			buf.append(type.getSQLType());
			buf.append(',');
			buf.append(num_type.getSize());
			buf.append(',');
			buf.append(num_type.getScale());
			buf.append(')');
		} else if (type instanceof TBinaryType) {
			TBinaryType bin_type = (TBinaryType) type;
			buf.append("BINARY(");
			buf.append(type.getSQLType());
			buf.append(',');
			buf.append(bin_type.getMaximumSize());
			buf.append(')');
		} else if (type instanceof TDateType) {
			buf.append("DATE(");
			buf.append(type.getSQLType());
			buf.append(')');
		} else if (type instanceof TNullType) {
			buf.append("NULL(");
			buf.append(type.getSQLType());
			buf.append(')');
		} else if (type instanceof TJavaObjectType) {
			buf.append("JAVAOBJECT(");
			buf.append(type.getSQLType());
			buf.append(",'");
			buf.append(((TJavaObjectType) type).getJavaClassTypeString());
			buf.append("')");
		} else {
			throw new RuntimeException("Can not encode type: " + type);
		}
		return new String(buf);
	}

	/**
	 * Given an array of TType, returns a String that that is the encoded form of the array and that can be later decoded back into an array of TType. Useful for serializing a list of TType
	 * information.
	 */
	public static String asEncodedString(TType[] types) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < types.length; ++i) {
			buf.append(asEncodedString(types[i]));
			if (i < types.length - 1) {
				buf.append("!|");
			}
		}
		return new String(buf);
	}

	/**
	 * Decodes a String that has been encoded with the 'asEncodedString' method and returns a TType that represented the type.
	 */
	public static TType decodeString(String encoded_str) {
		int param_s = encoded_str.indexOf('(');
		int param_e = encoded_str.lastIndexOf(')');
		String params = encoded_str.substring(param_s + 1, param_e);
		List param_list = StringUtil.explode(params, ",");
		int sql_type = Integer.parseInt((String) param_list.get(0));

		if (encoded_str.startsWith("BOOLEAN(")) {
			return new TBooleanType(sql_type);
		} else if (encoded_str.startsWith("STRING(")) {
			int size = Integer.parseInt((String) param_list.get(1));
			String locale_str = parseQuotedString((String) param_list.get(2));
			if (locale_str.length() == 0) {
				locale_str = null;
			}
			int strength = Integer.parseInt((String) param_list.get(3));
			int decomposition = Integer.parseInt((String) param_list.get(4));
			return new TStringType(sql_type, size, locale_str, strength, decomposition);
		} else if (encoded_str.startsWith("NUMERIC(")) {
			int size = Integer.parseInt((String) param_list.get(1));
			int scale = Integer.parseInt((String) param_list.get(2));
			return new TNumericType(sql_type, size, scale);
		} else if (encoded_str.startsWith("BINARY(")) {
			int size = Integer.parseInt((String) param_list.get(1));
			return new TBinaryType(sql_type, size);
		} else if (encoded_str.startsWith("DATE(")) {
			return new TDateType(sql_type);
		} else if (encoded_str.startsWith("NULL(")) {
			return new TNullType();
		} else if (encoded_str.startsWith("JAVAOBJECT(")) {
			String class_str = parseQuotedString((String) param_list.get(1));
			return new TJavaObjectType(class_str);
		} else {
			throw new RuntimeException("Can not parse encoded string: " + encoded_str);
		}
	}

	/**
	 * Returns a TBinaryType constrained for the given class.
	 */
	public static TType javaObjectType(String class_name) {
		return new TJavaObjectType(class_name);
	}

	/**
	 * Returns a TStringType object of the given size and locale information. If locale is null then collation is lexicographical.
	 */
	public static TType stringType(int sql_type, int size, String locale, int strength, int decomposition) {

		return new TStringType(sql_type, size, locale, strength, decomposition);
	}

	/**
	 * Returns a TNumericType object of the given size and scale.
	 */
	public static TType numericType(int sql_type, int size, int scale) {
		return new TNumericType(sql_type, size, scale);
	}

	/**
	 * Returns a TBooleanType object.
	 */
	public static TType booleanType(int sql_type) {
		return new TBooleanType(sql_type);
	}

	/**
	 * Returns a TDateType object.
	 */
	public static TType dateType(int sql_type) {
		return new TDateType(sql_type);
	}

	/**
	 * Returns a TBinaryType object.
	 */
	public static TType binaryType(int sql_type, int size) {
		return new TBinaryType(sql_type, size);
	}

	// -----

	/**
	 * Assuming that the two types are numeric types, this will return the 'widest' of the two types. For example, an INTEGER is a wider type than a SHORT, and a FLOAT is wider than an INTEGER.
	 * <p>
	 * Code by Jim McBeath.
	 */
	public static TType getWidestType(TType t1, TType t2) {
		int t1SQLType = t1.getSQLType();
		int t2SQLType = t2.getSQLType();
		if (t1SQLType == SQLTypes.DECIMAL) {
			return t1;
		}
		if (t2SQLType == SQLTypes.DECIMAL) {
			return t2;
		}
		if (t1SQLType == SQLTypes.NUMERIC) {
			return t1;
		}
		if (t2SQLType == SQLTypes.NUMERIC) {
			return t2;
		}

		if (t1SQLType == SQLTypes.BIT) {
			return t2; // It can't be any smaller than a BIT
		}
		if (t2SQLType == SQLTypes.BIT) {
			return t1;
		}

		int t1IntSize = getIntSize(t1SQLType);
		int t2IntSize = getIntSize(t2SQLType);
		if (t1IntSize > 0 && t2IntSize > 0) {
			// Both are int types, use the largest size
			return (t1IntSize > t2IntSize) ? t1 : t2;
		}

		int t1FloatSize = getFloatSize(t1SQLType);
		int t2FloatSize = getFloatSize(t2SQLType);
		if (t1FloatSize > 0 && t2FloatSize > 0) {
			// Both are floating types, use the largest size
			return (t1FloatSize > t2FloatSize) ? t1 : t2;
		}

		if (t1FloatSize > t2IntSize) {
			return t1;
		}
		if (t2FloatSize > t1IntSize) {
			return t2;
		}
		if (t1IntSize >= t2FloatSize || t2IntSize >= t1FloatSize) {
			// Must be a long (8 bytes) and a real (4 bytes), widen to a double
			return new TNumericType(SQLTypes.DOUBLE, 8, -1);
		}
		// NOTREACHED - can't get here, the last three if statements cover
		// all possibilities.
		throw new Error("Widest type error.");
	}

	/**
	 * Get the number of bytes used by an integer type.
	 * <p>
	 * Code by Jim McBeath.
	 * 
	 * @param sqlType
	 *            The SQL type.
	 * @return The number of bytes required for data of that type, or 0 if not an int type.
	 */
	private static int getIntSize(int sqlType) {
		switch (sqlType) {
		case SQLTypes.TINYINT:
			return 1;
		case SQLTypes.SMALLINT:
			return 2;
		case SQLTypes.INTEGER:
			return 4;
		case SQLTypes.BIGINT:
			return 8;
		default:
			return 0;
		}
	}

	/**
	 * Get the number of bytes used by a floating type.
	 * <p>
	 * Code by Jim McBeath.
	 * 
	 * @param sqlType
	 *            The SQL type.
	 * @return The number of bytes required for data of that type, or 0 if not an int type.
	 */
	private static int getFloatSize(int sqlType) {
		switch (sqlType) {
		default:
			return 0;
		case SQLTypes.REAL:
			return 4;
		case SQLTypes.FLOAT:
		case SQLTypes.DOUBLE:
			return 8;
		}
	}

	// ------ Useful convenience statics ------

	/**
	 * A default boolean (SQL BIT) type.
	 */
	public static final TBooleanType BOOLEAN_TYPE = new TBooleanType(SQLTypes.BIT);

	/**
	 * A default string (SQL VARCHAR) type of unlimited maximum size and null locale.
	 */
	public static final TStringType STRING_TYPE = new TStringType(SQLTypes.VARCHAR, -1, (String) null);

	/**
	 * A default numeric (SQL NUMERIC) type of unlimited size and scale.
	 */
	public static final TNumericType NUMERIC_TYPE = new TNumericType(SQLTypes.NUMERIC, -1, -1);

	/**
	 * A default date (SQL TIMESTAMP) type.
	 */
	public static final TDateType DATE_TYPE = new TDateType(SQLTypes.TIMESTAMP);

	/**
	 * A default binary (SQL BLOB) type of unlimited maximum size.
	 */
	public static final TBinaryType BINARY_TYPE = new TBinaryType(SQLTypes.BLOB, -1);

	/**
	 * A default NULL type.
	 */
	public static final TNullType NULL_TYPE = new TNullType();

	/**
	 * A type that represents a query plan (sub-select).
	 */
	public static final TQueryPlanType QUERY_PLAN_TYPE = new TQueryPlanType();

	/**
	 * A type that represents an array.
	 */
	public static final TArrayType ARRAY_TYPE = new TArrayType();

}