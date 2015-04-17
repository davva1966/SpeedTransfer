// {{CopyrightNotice}}

package com.ss.speedtransfer.util.parser.sql;

import java.io.Reader;
import java.io.StringReader;

/**
 * The <code>StringObject</code> class
 */
public class StringObject {

	/**
	 * The java.lang.String object.
	 */
	private String str;

	/**
	 * Constructs the object.
	 */
	private StringObject(String str) {
		this.str = str;
	}

	/**
	 * Returns the length of the string.
	 */
	public int length() {
		return str.length();
	}

	/**
	 * Returns a Reader that can read from the string.
	 */
	public Reader getReader() {
		return new StringReader(str);
	}

	/**
	 * Returns this object as a java.lang.String object (easy!)
	 */
	public String toString() {
		return str;
	}

	/**
	 * Static method that returns a StringObject from the given java.lang.String.
	 */
	public static StringObject fromString(String str) {
		if (str != null) {
			return new StringObject(str);
		}
		return null;
	}

}
