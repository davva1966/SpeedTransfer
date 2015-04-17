// {{CopyrightNotice}}

package com.ss.speedtransfer.util;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.Stack;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

import com.ss.speedtransfer.SpeedTransferPlugin;


public class ResourceBundles {

	protected Map<String, Properties> bundles = null;

	private static ResourceBundles rBundles = null;

	public static ResourceBundles instance() {
		if (rBundles == null) {
			rBundles = new ResourceBundles();
		}
		return rBundles;
	}

	public void loadBundle(String bundleName, Locale locale) {
		String bundlePath = "resources/translations/";
		Bundle bundle = Platform.getBundle(SpeedTransferPlugin.PLUGIN_ID);

		String country = locale.getCountry();
		String language = locale.getLanguage();

		if (!bundlePath.endsWith(File.separator))
			bundlePath += File.separator;

		String extension = ".properties";

		InputStream in = null;
		Path path = null;
		URL fileURL = null;
		boolean found = false;

		try {

			path = new Path(bundlePath + bundleName + extension);
			fileURL = FileLocator.find(bundle, path, null);
			try {
				in = fileURL.openStream();
				loadResourceBundle(in, bundleName, bundleName);
				in.close();
				found = true;
			} catch (Exception e) {
			}

			if (language != null && language.trim().length() > 0) {
				path = new Path(bundlePath + language + File.separator + bundleName + extension);
				fileURL = FileLocator.find(bundle, path, null);
				try {
					in = fileURL.openStream();
					loadResourceBundle(in, bundleName, bundleName + "_" + language);
					in.close();
					found = true;
				} catch (Exception e) {
				}

			}

			if (country != null && country.trim().length() > 0) {
				String pathname = bundlePath + language + "_" + country + File.separator + bundleName + extension;
				path = new Path(pathname);
				fileURL = FileLocator.find(bundle, path, null);
				try {
					in = fileURL.openStream();
					loadResourceBundle(in, bundleName, bundleName + "_" + locale.toString());
					in.close();
					found = true;
				} catch (Exception e) {
				}

			}

			if (found == false)
				throw new MissingResourceException("Cannot find resource bundle: " + bundleName + " in path: " + bundlePath, "", "");

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (in != null)
					in.close();
			} catch (Exception f) {
			}
		}

	}

	/**
	 * Loads bundles if found. Loading: 1.bundlename_language_country 2.bundlename_language 3.bundlename
	 * 
	 * @param bundleName
	 * @param locale
	 */
	private void loadResourceBundle(InputStream stream, String bundleName, String keyName) {

		Properties prop = null;

		try {

			if (bundles == null)
				bundles = new HashMap<String, Properties>();

			prop = new Properties();
			prop.load(stream);
			bundles.put(keyName.toLowerCase(), prop);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getTranslatedString(String text, String bundleName, Locale locale) throws MissingResourceException {

		String translated = null;
		if (bundleName != null && bundleName.length() > 0) {
			if (locale == null)
				locale = Locale.getDefault();

			Stack<Properties> bundles = getBundles(bundleName, locale);
			Properties props = null;

			while (!bundles.empty()) {
				props = bundles.pop();
				translated = props.getProperty(text);
				if (translated != null) {
					return translated;
				}
			}

		} else {
			throw new MissingResourceException("No bundle found", bundleName, "");
		}
		// if no translation found return null
		return text;
	}

	/**
	 * returns a stack o bundles based on the search order 1. bundlename_language_country 2. bundlename_language 3. bundlename
	 * 
	 * @param bundleName
	 * @param locale
	 * @return
	 * @throws MissingResourceException
	 */
	private Stack<Properties> getBundles(String bundleName, Locale locale) throws MissingResourceException {
		if (bundles == null) {
			bundles = new HashMap<String, Properties>();
		}
		// throw new MissingResourceException("No language bundles loaded",
		// bundleName,
		// locale.getLanguage()+"_"+locale.getCountry());

		Properties props = null;
		String key = null;
		// load bundle if not found
		if (bundles.get(bundleName.toLowerCase()) == null)
			loadBundle(bundleName, locale);

		Stack<Properties> properties = new Stack<Properties>();

		props = bundles.get(bundleName.toLowerCase());
		if (props != null) {
			properties.push(props);
		}

		key = bundleName + "_" + locale.getLanguage();
		props = bundles.get(key.toLowerCase());
		if (props != null)
			properties.push(props);

		if (locale.getCountry().length() != 0)
			key = bundleName + "_" + locale.getLanguage() + "_" + locale.getCountry();
		else
			key = bundleName + "_" + locale.getLanguage();

		props = bundles.get(key.toLowerCase());
		if (props != null)
			properties.push(props);

		if (properties.isEmpty())
			throw new MissingResourceException("No bundle found", bundleName, "");
		return properties;
	}

}
