package com.ss.speedtransfer.util;

import java.util.Vector;

public class StringTokenizer {

	protected Vector values = new Vector();
	protected String string = null;
	protected String token = null;
	protected int index = 0;

	public StringTokenizer(String string) {
		this(string, " \n");
	}

	public StringTokenizer(String string, String token) {
		this.string = string;
		this.token = token;

		parse();
	}

	public String getAt(int index) {
		if (values.size() > index)
			return (String) values.elementAt(index);
		return "";
	}

	/****************************************************************************
	 * Return count
	 *****************************************************************************/
	public int getCount() {

		return values.size();
	}

	/****************************************************************************
	 * More
	 *****************************************************************************/
	public boolean more() {
		if (values.size() > index)
			return true;
		return false;
	}

	/****************************************************************************
	 * Next
	 *****************************************************************************/
	public String next() {

		if (values.size() > index) {
			String value = (String) values.elementAt(index);
			index++;
			return value;
		}
		return "";
	}

	/****************************************************************************
	 * Parse
	 ****************************************************************************/
	protected void parse() {

		String value = "";
		String tempString = string;
		String tempToken = token;
		int pos = 0;

		int index = tempString.indexOf(tempToken);
		if (index == -1 && tempString.length() > 0) {
			values.addElement(tempString);
			return;
		}

		while (pos < tempString.length()) {
			if (index == -1) {
				value = tempString.substring(pos, tempString.length());
				values.addElement(value);
				break;
			} else
				value = tempString.substring(pos, pos + index);

			values.addElement(value);
			pos += index + tempToken.length();

			index = tempString.substring(pos, tempString.length()).indexOf(tempToken);
		}

		if (tempString.endsWith(tempToken))
			values.addElement("");

	}
}
