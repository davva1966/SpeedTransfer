package com.ss.speedtransfer.util;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ExpiryDateGenerator {

//	public static String generate(int year, int month, int day) {
//		return generate(year, month, day, true);
//
//	}

	public static String generate(Date date, boolean encrypt) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return generate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), encrypt);

	}

	protected static String generate(int year, int month, int day, boolean encrypt) {

		try {
			Calendar cal = Calendar.getInstance();

			// Month is zero based (0=January)
			cal.set(year, month, day);

			Date expDate = cal.getTime();
			String expDateStr = DateFormat.getDateInstance(DateFormat.SHORT, Locale.US).format(expDate);
			if (encrypt)
				return StringHelper.encodePassword(expDateStr);

			return expDateStr;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;

	}

}
