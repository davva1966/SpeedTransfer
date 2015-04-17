// {{CopyrightNotice}}

package com.ss.speedtransfer.util.parser.sql;

/**
 * A TObject is a strongly typed object in a database engine. A TObject must maintain type information (eg. STRING, NUMBER, etc) along with the object value being represented itself.
 */
public final class TObject implements java.io.Serializable {

	/**
	 * The type of this object.
	 */
	private TType type;

	/**
	 * The Java representation of the object.
	 */
	private Object ob;

	/**
	 * Constructs the TObject as the given type.
	 */
	public TObject(TType type, Object ob) {
		this.type = type;
		if (ob instanceof String) {
			this.ob = StringObject.fromString((String) ob);
		} else {
			this.ob = ob;
		}
	}

	/**
	 * Returns the type of this object.
	 */
	public TType getTType() {
		return type;
	}

	/**
	 * Returns true if the object is null. Note that we must still be able to determine type information for an object that is NULL.
	 */
	public boolean isNull() {
		return (getObject() == null);
	}

	/**
	 * Returns a java.lang.Object that is the data behind this object.
	 */
	public Object getObject() {
		return ob;
	}

	/**
	 * Returns the Boolean of this object if this object is a boolean type. If the object is not a boolean type or is NULL then a null object is returned. This method must not be used to cast from a
	 * type to a boolean.
	 */
	public Boolean toBoolean() {
		if (getTType() instanceof TBooleanType) {
			return (Boolean) getObject();
		}
		return null;
	}

	/**
	 * Returns the String of this object if this object is a string type. If the object is not a string type or is NULL then a null object is returned. This method must not be used to cast from a type
	 * to a string.
	 */
	public String toStringValue() {
		if (getTType() instanceof TStringType) {
			return getObject().toString();
		}
		return null;
	}

	public static final TObject BOOLEAN_TRUE = new TObject(TType.BOOLEAN_TYPE, Boolean.TRUE);

	public static final TObject BOOLEAN_FALSE = new TObject(TType.BOOLEAN_TYPE, Boolean.FALSE);

	public static final TObject BOOLEAN_NULL = new TObject(TType.BOOLEAN_TYPE, null);

	public static final TObject NULL_OBJECT = new TObject(TType.NULL_TYPE, null);

	/**
	 * Returns a TObject of boolean type that is either true or false.
	 */
	public static TObject booleanVal(boolean b) {
		if (b) {
			return BOOLEAN_TRUE;
		}
		return BOOLEAN_FALSE;
	}

	/**
	 * Returns a TObject of numeric type that represents the given int value.
	 */
	public static TObject intVal(int val) {
		return bigNumberVal(BigNumber.fromLong(val));
	}

	/**
	 * Returns a TObject of numeric type that represents the given long value.
	 */
	public static TObject longVal(long val) {
		return bigNumberVal(BigNumber.fromLong(val));
	}

	/**
	 * Returns a TObject of numeric type that represents the given double value.
	 */
	public static TObject doubleVal(double val) {
		return bigNumberVal(BigNumber.fromDouble(val));
	}

	/**
	 * Returns a TObject of numeric type that represents the given BigNumber value.
	 */
	public static TObject bigNumberVal(BigNumber val) {
		return new TObject(TType.NUMERIC_TYPE, val);
	}

	/**
	 * Returns a TObject of VARCHAR type that represents the given StringObject value.
	 */
	public static TObject stringVal(StringObject str) {
		return new TObject(TType.STRING_TYPE, str);
	}

	/**
	 * Returns a TObject of VARCHAR type that represents the given String value.
	 */
	public static TObject stringVal(String str) {
		return new TObject(TType.STRING_TYPE, StringObject.fromString(str));
	}

	/**
	 * Returns a TObject of DATE type that represents the given time value.
	 */
	public static TObject dateVal(java.util.Date d) {
		return new TObject(TType.DATE_TYPE, d);
	}

	/**
	 * Returns a TObject of NULL type that represents a null value.
	 */
	public static TObject nullVal() {
		return NULL_OBJECT;
	}

	/**
	 * Equality test. This will throw an exception if it is used. The reason for this is because it's not clear what we would be testing the equality of with this method. Equality of the object + the
	 * type or equality of the objects only?
	 */
	public boolean equals(Object ob) {
		throw new Error("equals method should not be used.");
	}

	public String toString() {
		if (isNull()) {
			return "NULL";
		} else {
			return getObject().toString();
		}
	}

	public String getValue() {
		if (isNull()) {
			return null;
		} else {
			return getObject().toString();
		}
	}
}
