package com.ss.speedtransfer.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;

import org.eclipse.core.runtime.Platform;

public class LicenseManager {

	public static final String VERSION = "version";
	public static final String EXP_DATE = "expdate";
	public static final String EXP_MSG = "expmessage";
	public static final String LICENSE = "license";
	public static final String SELECT_ONLY = "selectonly";

	private static Properties properties;
	private static boolean isValid = false;
	private static boolean isTrial = true;
	private static boolean isExpired = false;
	private static String expiryDate;
	private static int daysRemaining = 0;
	private static String errorMessage;

	static {
		load();
	}

	public static String getVersion() {
		if (properties != null)
			return properties.getProperty(VERSION, "browser");

		return "";
	}

	public static String getLicense() {
		if (properties != null)
			return properties.getProperty(LICENSE, "None");
		return "";
	}

	public static String getExpiryMessage() {
		if (properties != null)
			return properties.getProperty(EXP_MSG, "Trial period ended");
		return "";
	}

	public static boolean isValid() {
		return isValid;
	}

	public static boolean isTrial() {
		return isTrial;
	}

	public static boolean isExpired() {
		return isExpired;
	}

	public static boolean isSelectOnly() {
		String s = properties.getProperty(SELECT_ONLY, "false");
		if (s.trim().equalsIgnoreCase("true"))
			return true;

		return false;
	}

	public static int getDaysRemaining() {
		return daysRemaining;
	}

	public static String getExpiryDate() {
		return expiryDate;
	}

	public static String getErrorMessage() {
		return errorMessage;
	}

	public static boolean isStudioVersion() {
		return getVersion().equalsIgnoreCase("studio");

	}

	public static boolean isBrowserVersion() {
		return isStudioVersion() == false;

	}

	protected static String encrypt(String text) throws IOException {
		return StringHelper.encodePassword(text);

	}

	protected static String decode(String text) throws IOException {
		return StringHelper.decodePassword(text);

	}

	private static void load() {

		FileInputStream fis = null;
		ObjectInputStream ois = null;

		try {
			String installPath = Platform.getInstallLocation().getURL().getPath();
			if (installPath.endsWith(File.separator) == false)
				installPath = installPath + File.separator;
			String licenseFilepath = installPath + "License";
			File licenseFile = new File(licenseFilepath);

			if (licenseFile.exists() == false) {
				errorMessage = "No license file found";
				return;
			}

			fis = new FileInputStream(licenseFile);
			ois = new ObjectInputStream(fis);
			Properties props = (Properties) ois.readObject();
			properties = decryptProperties(props);

			if (properties.containsKey(EXP_DATE) == false)
				setTrialPeriod(licenseFile);

			checkExpired();

			System.setProperty("com.ss.speedtransfer.version", getVersion());
			System.setProperty("com.ss.speedtransfer.selectonly", Boolean.toString(isSelectOnly()));

			isValid = true;

		} catch (Exception e) {
			errorMessage = SSUtil.getMessage(e);
			isValid = false;
		} finally {
			try {
				if (fis != null)
					fis.close();
			} catch (Exception e2) {
			}
			try {
				if (ois != null)
					ois.close();
			} catch (Exception e2) {
			}
		}

	}

	private static Properties encryptProperties(Properties props) throws IOException {
		Properties newProps = new Properties();
		for (Object key : props.keySet()) {
			newProps.put(encrypt((String) key), encrypt(props.getProperty((String) key)));
		}

		return newProps;

	}

	private static Properties decryptProperties(Properties props) throws IOException {
		Properties newProps = new Properties();
		for (Object key : props.keySet()) {
			newProps.put(decode((String) key), decode(props.getProperty((String) key)));
		}

		return newProps;

	}

	private static void setTrialPeriod(File licenseFile) throws Exception {
		FileOutputStream fos = null;
		ObjectOutputStream oos = null;
		try {
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DAY_OF_MONTH, 30);
			int year = cal.get(Calendar.YEAR);
			int month = cal.get(Calendar.MONTH);
			int day = cal.get(Calendar.DAY_OF_MONTH);
			String dateString = ExpiryDateGenerator.generate(year, month + 1, day, false);
			properties.put(EXP_DATE, dateString);

			fos = new FileOutputStream(licenseFile);
			oos = new ObjectOutputStream(fos);
			oos.writeObject(encryptProperties(properties));

		} finally {
			try {
				if (fos != null)
					fos.close();
			} catch (Exception e2) {
			}
			try {
				if (oos != null)
					oos.close();
			} catch (Exception e2) {
			}
		}

	}

	private static void checkExpired() throws ParseException {

		String dateString = properties.getProperty(EXP_DATE);
		if (dateString.trim().equalsIgnoreCase("notused")) {
			isExpired = false;
			isTrial = false;
			return;
		} else {
			isTrial = true;
		}

		Date expDate = DateFormat.getDateInstance(DateFormat.SHORT, Locale.US).parse(dateString);
		Date today = new Date(System.currentTimeMillis());

		expiryDate = DateFormat.getInstance().format(expDate);

		if (today.compareTo(expDate) > 0) {
			isExpired = true;
			daysRemaining = 0;
			return;
		} else {
			isExpired = false;
			daysRemaining = daysBetween(today, expDate);
		}

	}

	protected static int daysBetween(Date d1, Date d2) {
		return (int) ((d2.getTime() - d1.getTime()) / (1000 * 60 * 60 * 24));
	}

}
