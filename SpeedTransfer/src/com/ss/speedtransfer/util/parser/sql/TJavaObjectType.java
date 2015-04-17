// {{CopyrightNotice}}

package com.ss.speedtransfer.util.parser.sql;

/**
 * The <code>TJavaObjectType</code> class
 */
public class TJavaObjectType extends TType {

	/**
	 * The type of class this is contrained to or null if it is not constrained to a java class.
	 */
	private String class_type;

	/**
	 * Constructs the type.
	 */
	public TJavaObjectType(String class_type) {
		super(SQLTypes.JAVA_OBJECT);
		this.class_type = class_type;
	}

	/**
	 * Returns the java class type of this type. For example, "java.net.URL" if this type is constrained to a java.net.URL object.
	 */
	public String getJavaClassTypeString() {
		return class_type;
	}

}