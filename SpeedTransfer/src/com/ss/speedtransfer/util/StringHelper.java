package com.ss.speedtransfer.util;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import com.ss.speedtransfer.util.crypto.Base64;


/**
 * This class is a helper class for string operations
 */
public class StringHelper {

	/** Lead characters to denote a password string **/
	public static String PASSWORD_START = "#$&%0x";

	private StringHelper() {
		super();
	}

	/**
	 * Returns the string defined by parameter <code>string</code> capitalized (the first letter in uppercase).
	 * 
	 * @param string
	 *            the string to capitalize
	 * @return a capitalized string (the first letter in uppercase)
	 */
	public static String capitalizeString(String string) {
		if (string.length() == 0)
			return string;

		String firstLetter = string.substring(0, 1);
		String theRest = string.substring(1, string.length());
		return firstLetter.toUpperCase() + theRest;

	}

	/**
	 * Returns the string defined by parameter <code>string</code> decapitalized (the first letter in lowercase).
	 * 
	 * @param string
	 *            the string to decapitalize
	 * @return a decapitalized string (the first letter in lowercase)
	 */
	public static String decapitalizeString(String string) {
		if (string.length() == 0)
			return string;

		String firstLetter = string.substring(0, 1);
		String theRest = string.substring(1, string.length());
		return firstLetter.toLowerCase() + theRest;

	}

	/**
	 * Returns the strings defined by parameter <code>array</code> as a comma separated string.
	 * 
	 * @param array
	 *            array of strings to be separated
	 * @return a comma separated string
	 */
	public static String formatArrayToCommaSepareted(String[] array) {
		return formatArrayToSepareted(array, ",");
	}

	/**
	 * Returns the strings defined by parameter <code>array</code> as a separated string.
	 * 
	 * @param array
	 *            array of strings to be separated
	 * @param separator
	 *            separator string
	 * @return a separated string
	 */
	public static String formatArrayToSepareted(String[] array, String separator) {

		if (array == null || array.length == 0)
			return "";

		StringBuffer formattedString = new StringBuffer();

		for (int i = 0; i < array.length; i++) {
			if (i > 0)
				formattedString.append(separator);
			formattedString.append(array[i]);
		}

		return formattedString.toString();
	}

	/**
	 * Returns the integers defined by parameter <code>array</code> as a separated string.
	 * 
	 * @param array
	 *            array of integers to be separated
	 * @param separator
	 *            separator string
	 * @return a separated string
	 */
	public static String formatArrayToSepareted(Integer[] array, String separator) {

		if (array == null || array.length == 0)
			return "";

		StringBuffer formattedString = new StringBuffer();

		for (int i = 0; i < array.length; i++) {
			if (i > 0)
				formattedString.append(separator);
			formattedString.append(array[i]);
		}

		return formattedString.toString();
	}

	public static String[] formatCommaSeparetedToArray(String commaString) {
		return formatCommaSeparetedToArray(commaString, false);
	}

	public static String[] formatCommaSeparetedToArray(String commaString, boolean trimValues) {
		return formatSeparetedToArray(commaString, ",", trimValues);
	}

	/**
	 * Returns a Vector of strings from the comma separated string defined by parameter <code>commaString</code>.
	 * 
	 * @param commaString
	 *            a comma separated string
	 * @return a Vector of strings
	 */
	public static Vector formatCommaSeparetedToVector(String commaString) {
		return formatSeparetedToVector(commaString, ",");
	}

	/**
	 * Returns the properties defined by parameter <code>properties</code> as a separated string.
	 * 
	 * @param properties
	 *            to be separated
	 * @param separator
	 *            separator string
	 * @return a separated string
	 */
	public static String formatPropertyToSepareted(Properties properties, String separator) {

		StringBuffer formattedString = new StringBuffer();
		if (properties.size() == 0)
			return "";

		int count = 0;
		Enumeration enumerator = properties.keys();
		while (enumerator.hasMoreElements()) {
			Object key = enumerator.nextElement();
			Object value = properties.get(key);
			if (count > 0)
				formattedString.append(separator);
			formattedString.append(key);
			formattedString.append("=");
			formattedString.append(value);
			count++;
		}

		return formattedString.toString();
	}

	public static String[] formatSeparetedToArray(String string, String separator) {
		return formatSeparetedToArray(string, separator, false);

	}

	public static String[] formatSeparetedToArray(String string, String separator, boolean trimValues) {

		StringTokenizer tokenizer = new StringTokenizer(string, separator);

		Vector elements = new Vector();

		while (tokenizer.more())
			elements.add(tokenizer.next());

		String[] stringArray = new String[elements.size()];
		for (int i = 0; i < stringArray.length; i++)
			if (trimValues)
				stringArray[i] = ((String) elements.get(i)).trim();
			else
				stringArray[i] = (String) elements.get(i);

		return stringArray;
	}

	/**
	 * Returns a Vector of strings from the separated string defined by parameter <code>string</code>.
	 * 
	 * @param string
	 *            the separarated string
	 * @param separator
	 *            the string used to separate values
	 * @return a Vector of strings
	 */
	public static Vector<String> formatSeparetedToVector(String string, String separator) {
		Vector<String> vector = new Vector<String>();
		if (string.length() <= 0)
			return vector;

		String[] stringArray = formatSeparetedToArray(string, separator);

		if (stringArray.length <= 0)
			return vector;

		for (int i = 0; i < stringArray.length; i++)
			vector.add(stringArray[i]);

		return vector;
	}

	/**
	 * Returns a List of integers from the separated string defined by parameter <code>string</code>.
	 * 
	 * @param string
	 *            the separarated string, accepts null
	 * @param separator
	 *            the string used to separate values
	 * @return a List of integers
	 */
	public static List<Integer> formatSeparatedToIntegerList(String string, String separator) {
		List<Integer> list = new ArrayList<Integer>();
		if (string == null || string.length() <= 0)
			return list;

		String[] stringArray = formatSeparetedToArray(string, separator);

		if (stringArray.length <= 0)
			return list;

		for (int i = 0; i < stringArray.length; i++)
			list.add(Integer.parseInt(stringArray[i]));

		return list;
	}

	/**
	 * Returns a List of strings from the separated string defined by parameter <code>string</code>.
	 * 
	 * @param string
	 *            the separarated string
	 * @param separator
	 *            the string used to separate values
	 * @return a List of strings
	 */
	public static List<String> formatSeparatedToStringList(String string, String separator) {
		List<String> list = new ArrayList<String>();
		if (string.length() <= 0)
			return list;

		String[] stringArray = formatSeparetedToArray(string, separator);

		if (stringArray.length <= 0)
			return list;

		for (int i = 0; i < stringArray.length; i++)
			list.add(stringArray[i]);

		return list;
	}

	/**
	 * Returns a Properties containing the keys and values in the separatedd string defined by parameter <code>string</code>.
	 * 
	 * @param string
	 *            the separarated string
	 * @param separator
	 *            the string used to separate key/value pairs
	 * @return a Properties with string key/value pairs
	 */
	public static Properties formatSeparetedToProperty(String string, String separator) {
		Properties props = new Properties();
		if (string.length() <= 0)
			return props;

		String[] stringArray = formatSeparetedToArray(string, separator);

		if (stringArray.length <= 0)
			return props;

		for (int i = 0; i < stringArray.length; i++) {
			int idx = stringArray[i].indexOf('=');
			if (idx > -1) {
				String key = stringArray[i].substring(0, idx);
				String value = "";
				try {
					value = stringArray[i].substring(idx + 1);
				} catch (Exception e) {
				}
				props.put(key, value);
			}
		}

		return props;
	}

	/**
	 * Replace all occurences of the specified substring in the specified string with the the new substring. The searcg is case sensitive.
	 * 
	 * @param string
	 *            the string to perform the replace in
	 * @param oldString
	 *            the substring to replace
	 * @param newString
	 *            the string to replace the old substring with
	 * @return a new string with all occurences of the specified substring replaced by the new substrring.
	 */
	public static String scanAndReplace(String string, String oldString, String newString) {
		return scanAndReplace(string, oldString, newString, true);
	}

	/**
	 * Replace all occurences of the specified substring in the specified string with the the new substring.
	 * 
	 * @param string
	 *            the string to perform the replace in
	 * @param oldString
	 *            the substring to replace
	 * @param newString
	 *            the string to replace the old substring with
	 * @param caseSensitive
	 *            indicates if the search is case sensitive or not
	 * @return a new string with all occurences of the specified substring replaced by the new substrring.
	 */
	public static String scanAndReplace(String string, String oldString, String newString, boolean caseSensitive) {

		if (string == null)
			return null;

		int len = string.length();

		if (len == 0)
			return string;

		String tempOldString = oldString;
		String tempString = string;

		if (!caseSensitive) {
			tempOldString = tempOldString.toUpperCase();
			tempString = tempString.toUpperCase();
		}

		int pos = 0;
		int index = tempString.indexOf(tempOldString);
		if (index == -1)
			return string;

		StringBuffer buffer = new StringBuffer(len);

		while (index != -1 && pos < len) {

			buffer.append(string.substring(pos, index));
			buffer.append(newString);

			pos = index + tempOldString.length();

			index = tempString.indexOf(tempOldString, pos);
			if (index == -1)
				buffer.append(string.substring(pos));
		}

		return buffer.toString();

	}

	/**
	 * Returns an string padded with blanks to the length specifed by parameter <code>length</code>, if the passed length is less than or equal to zero, a string with length 0 (zero) is returned.
	 * 
	 * @param length
	 *            the length of the string
	 * @return a string padded with blanks to the specified length
	 */
	public static String stringWithLength(int length) {

		if (length <= 0)
			return "";

		String string = new String();
		for (int i = 0; i < length; i++) {
			string += " ";
		}

		return string;
	}

	/**
	 * Prepend a timestamp to the string defined by parameter <code>string</code>
	 * 
	 * @param string
	 *            the string
	 * @return the string data with a prepended timestamp
	 */
	public static String timestamp(String string) {
		String formattedString = DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime()) + "    " + string;
		return formattedString;
	}

	/**
	 * Return new line
	 */
	public static String getNewLine() {

		return System.getProperty("line.separator");
	}

	/**
	 * Return new line
	 */
	public static String trimLeadingZeros(String value) {

		if (value == null)
			return value;
		int length = value.length();
		int leadingZeros = 0;
		for (int i = 0; i < length; i++) {
			char ch = value.charAt(i);
			if (i == leadingZeros && ch == '0')
				leadingZeros++;
			if (ch < '0' || ch > '9') {
				leadingZeros = 0;
				break;
			}
		}

		if (leadingZeros > 0) {
			if (leadingZeros >= length)
				leadingZeros--;
			return value.substring(leadingZeros);
		}

		return value;
	}

	/**
	 * Return new line
	 */
	public static String trimRight(String value) {
		if (value == null)
			return null;
		int length = value.length();
		int index = length - 1;
		for (; index >= 0; index--) {
			char ch = value.charAt(index);
			if (ch != ' ') {
				break;
			}
		}

		if (index != length - 1)
			return value.substring(0, index + 1);

		return value;
	}

	public static boolean hasRPGQuotes(String value) {

		if (value == null || value.length() < 2) {
			return false;
		}

		if (value.charAt(0) == '\'' && value.charAt(value.length() - 1) == '\'')
			return true;
		return false;
	}

	public static String removeRPGQuotes(String value) {

		if (value == null || value.length() < 2) {
			return value;
		}

		if (value.charAt(0) == '\'' && value.charAt(value.length() - 1) == '\'') {
			String s = value.substring(1, value.length() - 1);
			int index = s.indexOf("''");
			if (index != -1) {
				StringBuffer buffer = new StringBuffer();
				int matchedLength = s.length();
				for (int i = 0; i < matchedLength; i++) {

					char c = s.charAt(i);

					if (c == '\'' && i + 1 < matchedLength) {
						char c2 = s.charAt(i + 1);
						if (c2 == '\'')
							i++;
					}

					buffer.append(c);
				}
				return buffer.toString();
			}
			return s;
		}
		return value;
	}

	public static String convertToJavaString(String value) {

		if (value == null) {
			return value;
		}
		if (value.indexOf('\"') == -1 && value.indexOf('\\') == -1)
			return value;

		int length = value.length();

		StringBuffer buffer = new StringBuffer();

		try {
			int len = (value != null) ? value.length() : 0;
			for (int i = 0; i < len; i++) {
				char ch = value.charAt(i);
				switch (ch) {
				case '\"': {
					buffer.append("\\\"");
					break;
				}
				case '\\': {
					buffer.append("\\\\");
					break;
				}
				default: {
					buffer.append(ch);
				}
				}
			}
		} catch (Exception e) {

		}

		return buffer.toString();
	}

	public static String replaceRPGQuotes(String value) {

		if (value == null || value.length() < 2) {
			return value;
		}

		if (hasRPGQuotes(value)) {
			return "\"" + removeRPGQuotes(value) + "\"";

		}

		return value;
	}

	public static boolean hasQuotes(String value) {

		if (value == null || value.length() < 2) {
			return false;
		}

		if (value.charAt(0) == '\"' && value.charAt(value.length() - 1) == '\"')
			return true;
		return false;
	}

	/**
	 * Insert the method's description here. Creation date: (8/29/2001 7:41:40 AM)
	 */
	public synchronized static String remove(String string, char ch1, char ch2) {

		if (string == null)
			return null;

		int len = string.length();
		int chlen = 1;

		if (len == 0)
			return string;

		int p1 = 0;
		int p2 = 0;

		char[] array = string.toCharArray();

		for (long i = 0; i < len && p2 < len; i++) {
			if (array[p2] == ch1 || array[p2] == ch2) {
				p2 += chlen;
			} else {
				array[p1] = array[p2];
				p2++;
				p1++;

			}
		}
		if (p1 == len)
			return string;

		return new String(array, 0, p1);
	}

	/**
	 * Normalize a string to XML
	 */
	public static String normalizeXML(String s) {
		return normalizeXML(s, true);
	}

	/**
	 * Normalize a string to XML Carriage Return is removed if removeCR is true
	 */
	public static String normalizeXML(String s, boolean removeCR) {
		StringBuffer str = new StringBuffer();

		try {
			int len = (s != null) ? s.length() : 0;
			for (int i = 0; i < len; i++) {
				char ch = s.charAt(i);
				switch (ch) {
				case '<': {
					str.append("&lt;");
					break;
				}
				case '>': {
					str.append("&gt;");
					break;
				}
				case '&': {
					str.append("&amp;");
					break;
				}
				case '"': {
					str.append("&quot;");
					break;
				}
				case '\0': {
					str.append(" ");
					break;
				}
				case '\r': {
					if (removeCR)
						str.append("");
					else
						str.append("&#13;");
					break;
				}

				case '\n': {
					str.append("&#10;");
					break;
				}
				case '\t': {
					str.append("&#09;");
					break;
				}

				default: {
					str.append(ch);
				}
				}
			}
		} catch (Exception e) {

		}

		return str.toString();

	}

	/**
	 * Normalize a string to Java
	 */
	public static String normalizeJava(String s) {
		StringBuffer str = new StringBuffer();

		try {
			int len = (s != null) ? s.length() : 0;
			for (int i = 0; i < len; i++) {
				char ch = s.charAt(i);
				switch (ch) {
				case '\"': {
					str.append("\\\"");
					break;
				}
				case '\\': {
					str.append("\\\\");
					break;
				}
				default: {
					str.append(ch);
				}
				}
			}
		} catch (Exception e) {

		}

		return (str.toString().trim());
	}

	/**
	 * Denormalize a string from Java
	 */
	public static String denormalizeJava(String s) {
		StringBuffer str = new StringBuffer();

		try {
			int len = (s != null) ? s.length() : 0;
			for (int i = 0; i < len; i++) {
				char ch = s.charAt(i);
				switch (ch) {
				case '\\': {
					i++;
					ch = s.charAt(i);
					if (ch == '\\') {
						str.append(ch);
					} else {
						str.append('\\');
						str.append(ch);
					}
					break;
				}

				default: {
					str.append(ch);
				}
				}
			}
		} catch (Exception e) {

		}

		return (str.toString().trim());
	}

	/**
	 * Check if string contains digits only
	 * 
	 * @param string
	 *            the string to check
	 * @return True if string contains digits only
	 */
	public static boolean isDigits(String string) {
		if (string == null || string.length() == 0)
			return false;

		int len = string.length();
		for (int i = 0; i < len; i++) {
			if (!Character.isDigit(string.charAt(i)))
				return false;
		}
		return true;
	}

	/**
	 * Replace all occurences of the specified substring in the specified string with the new substring.
	 * 
	 * @param string
	 *            the string to perform the replace in
	 * @param oldString
	 *            the substring to replace
	 * @param newString
	 *            the string to replace the old substring with
	 * @return a new string with all occurences of the specified substring replaced by the new substrring.
	 */
	public static String replace(String string, String oldString, String newString) {

		StringBuffer resultString = new StringBuffer("");
		int endIdx = string.indexOf(oldString);
		if (endIdx == -1)
			return string;
		int startIdx = 0;

		while (endIdx != -1) {
			resultString.append(string.substring(startIdx, endIdx));
			resultString.append(newString);
			startIdx = endIdx + oldString.length();
			endIdx = string.indexOf(oldString, startIdx);
		}

		resultString.append(string.substring(startIdx));
		return resultString.toString();
	}

	/**
	 * Create a string from the specified byte array.
	 * 
	 * @param bytes
	 *            the byte array to create the dtring from
	 * @param from
	 *            the start position in the byte array (inclusive)
	 * @param length
	 *            the number of bytes to use from the byte array
	 * @return a new string created from the bytes in the byte array
	 */
	public static String createString(byte[] bytes, int from, int length) {

		if (bytes == null)
			return "";

		if (from >= bytes.length)
			return "";

		if (from + length >= bytes.length)
			length = bytes.length - from;

		if (length < 1)
			return "";

		byte[] newArr = new byte[length];
		System.arraycopy(bytes, from, newArr, 0, length);

		return new String(newArr);

	}

	/**
	 * Right align the data in the specified string
	 * 
	 * @param value
	 *            the date to align
	 * @param length
	 *            the length of the returned string. If the length is greater than the length of the data the returned string will be padded with blanks. If the length is horter than the length of the
	 *            data, the data is truncated.
	 * @return a new string with the specified length with the data right aligned.
	 */
	public static String rightAlign(String value, int length) {

		value = value.trim();
		StringBuilder sbOrg = new StringBuilder(value);
		StringBuilder sbRight = new StringBuilder(stringWithLength(length));

		int idx = length - 1;
		for (int i = sbOrg.length() - 1; i >= 0; i--) {
			if (idx < 0)
				break;
			char ch = sbOrg.charAt(i);
			sbRight.setCharAt(idx, ch);
			idx--;
		}

		return sbRight.toString();

	}

	/**
	 * Verify if the passed string is a password string. A password string starts with a prefix
	 * 
	 * @param str
	 * @return
	 */
	static public boolean isPasswordString(String str) {
		return str != null && str.startsWith(PASSWORD_START);
	}

	static public String[] findSubstrings(String str, String prefix, String suffix) {
		int cursor = 0;
		int prefixLength = prefix.length();
		int suffixLength = suffix.length();
		Vector<String> subs = new Vector<String>();
		while (cursor < str.length() - 1) {
			int firstIndex = str.indexOf(prefix, cursor);
			int secondIndex = -1;
			if (firstIndex > -1) {
				cursor = firstIndex + prefixLength;
				secondIndex = str.indexOf(suffix, cursor);
				if (secondIndex > -1) {
					subs.add(str.substring(cursor, secondIndex));
					cursor = secondIndex + suffixLength;
				} else {
					cursor = str.length();
				}
			} else {
				cursor = str.length();
			}
		}

		int size = subs.size();
		String[] substrings = new String[subs.size()];

		for (int i = 0; i < size; i++) {
			substrings[i] = subs.elementAt(i);
		}
		return substrings;
	}

	public static String wordWrapString(String s, int rowLengthHint) {

		return wordWrapString(s, rowLengthHint, 0);

	}

	public static String wordWrapString(String s, int rowLengthHint, int indent) {

		StringBuilder sb = new StringBuilder();

		int start = 0;
		int end = s.indexOf(" ", rowLengthHint);

		while (end > -1 && end < s.length()) {
			sb.append(s.substring(start, end).trim());
			sb.append(getNewLine());
			if (indent > 0)
				sb.append(StringHelper.stringWithLength(indent));
			start = end;
			end = s.indexOf(" ", end + rowLengthHint);
		}
		sb.append(s.substring(start).trim());

		return sb.toString();

	}

	static public String encodePassword(String password) throws IOException {
		String encoded = null;
		if (password != null && password.length() > 0) {
			encoded = Base64.coding(password, Base64.ENCRYPT);
			encoded = PASSWORD_START + encoded;
		}

		return encoded;
	}

	static public String decodePassword(String str) throws IOException {
		String decoded = null;
		if (isPasswordString(str)) {
			decoded = Base64.coding(str.substring(PASSWORD_START.length()), Base64.DECRYPT);
		} else {
			decoded = Base64.coding(str, Base64.DECRYPT);
		}
		return decoded;
	}
}
