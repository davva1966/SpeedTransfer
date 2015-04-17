// {{CopyrightNotice}}

package com.ss.speedtransfer.util.parser.sql;

import java.text.Collator;
import java.util.Locale;

/**
 * The <code>TStringType</code> class
 */
public final class TStringType extends TType {

	/**
	 * The maximum allowed size for the string.
	 */
	private int max_size;

	/**
	 * The locale of the string.
	 */
	private Locale locale;

	/**
	 * The strength of the collator for this string (as defined in java.text.Collator).
	 */
	private int strength;

	/**
	 * The decomposition mode of the collator for this string type (as defined in java.text.Collator).
	 */
	private int decomposition;

	/**
	 * The Collator object for this type, created when we first compare objects.
	 */
	private transient Collator collator;

	/**
	 * Constructs a type with the given sql_type value, the maximum size, and the locale of the string. Note that the 'sql_type' MUST be a string SQL type.
	 * <p>
	 * Note that a string type may be constructed with a NULL locale which means strings are compared lexicographically.
	 */
	public TStringType(int sql_type, int max_size, Locale locale, int strength, int decomposition) {
		super(sql_type);
		this.max_size = max_size;
		this.strength = strength;
		this.decomposition = decomposition;
		this.locale = locale;
	}

	/**
	 * Constructs a type with the given sql_type value, the maximum size, and the locale of the string. Note that the 'sql_type' MUST be a string SQL type.
	 * <p>
	 * Note that a string type may be constructed with a NULL locale which means strings are compared lexicographically. The string locale is formated as [2 char language][2 char country][rest is
	 * variant]. For example, US english would be 'enUS', French would be 'fr' and Germany would be 'deDE'.
	 */
	public TStringType(int sql_type, int max_size, String locale_str, int strength, int decomposition) {
		super(sql_type);
		this.max_size = max_size;
		this.strength = strength;
		this.decomposition = decomposition;

		if (locale_str != null && locale_str.length() >= 2) {
			String language = locale_str.substring(0, 2);
			String country = "";
			String variant = "";
			if (locale_str.length() > 2) {
				country = locale_str.substring(2, 4);
				if (locale_str.length() > 4) {
					variant = locale_str.substring(4);
				}
			}
			locale = new Locale(language, country, variant);
		}

	}

	/**
	 * Constructor without strength and decomposition that sets to default levels.
	 */
	public TStringType(int sql_type, int max_size, String locale_str) {
		this(sql_type, max_size, locale_str, -1, -1);
	}

	/**
	 * Returns the maximum size of the string (-1 is don't care).
	 */
	public int getMaximumSize() {
		return max_size;
	}

	/**
	 * Returns the strength of this string type as defined in java.text.Collator.
	 */
	public int getStrength() {
		return strength;
	}

	/**
	 * Returns the decomposition of this string type as defined in java.text.Collator.
	 */
	public int getDecomposition() {
		return decomposition;
	}

	/**
	 * Returns the locale of the string.
	 */
	public Locale getLocale() {
		return locale;
	}

	/**
	 * Returns the locale information as a formatted string.
	 * <p>
	 * Note that a string type may be constructed with a NULL locale which means strings are compared lexicographically. The string locale is formated as [2 char language][2 char country][rest is
	 * variant]. For example, US english would be 'enUS', French would be 'fr' and Germany would be 'deDE'.
	 */
	public String getLocaleString() {
		if (locale == null) {
			return "";
		} else {
			StringBuffer locale_str = new StringBuffer();
			locale_str.append(locale.getLanguage());
			locale_str.append(locale.getCountry());
			locale_str.append(locale.getVariant());
			return new String(locale_str);
		}
	}
}