package com.ss.speedtransfer.util;

public class EnvironmentHelper {

	public static final int STUDIO = 1;
	public static final int BROWSER = 2;
	public static final int EXECUTABLE = 3;

	public static boolean isExecutableEnvironment() {
		String executionEnvironment = System.getProperty("com.ss.speedtransfer.executionenvironment", "1");
		if (executionEnvironment == null || executionEnvironment.trim().length() == 0)
			return false;

		try {
			int env = Integer.parseInt(executionEnvironment);
			return env == EXECUTABLE;
		} catch (Exception e) {
		}

		return false;

	}

}
