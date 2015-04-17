// {{CopyrightNotice}}

package com.ss.speedtransfer.util.parser.sql;

/**
 * The <code>TNumericType</code> class
 */
public final class TNumericType extends TType {

	/**
	 * The size of the number.
	 */
	private int size;

	/**
	 * The scale of the number.
	 */
	private int scale;

	/**
	 * Constructs a type with the given sql_type value, the size, and the scale of the number. Note that the 'sql_type' MUST be a numeric SQL type (FLOAT, INTEGER, DOUBLE, etc).
	 */
	public TNumericType(int sql_type, int size, int scale) {
		super(sql_type);
		this.size = size;
		this.scale = scale;
	}

	/**
	 * Returns the size of the number (-1 is don't care).
	 */
	public int getSize() {
		return size;
	}

	/**
	 * Returns the scale of the number (-1 is don't care).
	 */
	public int getScale() {
		return scale;
	}

	// ---------- Implemented from TType ----------

	public boolean comparableTypes(TType type) {
		return (type instanceof TNumericType || type instanceof TBooleanType);
	}

}