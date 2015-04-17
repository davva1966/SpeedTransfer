package com.ss.speedtransfer.util;

import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

public class SettingsManager {

	public static Preferences preferences = null;

	static {
		preferences = ConfigurationScope.INSTANCE.getNode("com.ss.speedtransfer.global.preferences");
	}

	public static void set(String name, String value) {
		set(null, name, value);
	}

	public static void set(String category, String name, String value) {

		Preferences prefs = preferences;
		if (category != null && category.trim().length() > 0)
			prefs = prefs.node(category);

		prefs.put(name, value);

		try {
			// Forces the application to save the preferences
			preferences.flush();
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}

	}

	public static String get(String name) {
		return get(null, name);
	}

	public static String get(String category, String name) {

		Preferences prefs = preferences;
		if (category != null && category.trim().length() > 0)
			prefs = prefs.node(category);

		return prefs.get(name, "");

	}

	public static void remove(String name) {
		remove(null, name);
	}

	public static void remove(String category, String name) {

		Preferences prefs = preferences;
		if (category != null && category.trim().length() > 0)
			prefs = prefs.node(category);

		prefs.remove(name);

		try {
			// Forces the application to save the preferences
			preferences.flush();
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}

	}

}
