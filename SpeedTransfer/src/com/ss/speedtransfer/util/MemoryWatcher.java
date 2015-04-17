package com.ss.speedtransfer.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;

public class MemoryWatcher {

	protected static int warningCount = 0;
	protected int response = 0;

	public static boolean checkMemory() {
		MemoryWatcher mw = new MemoryWatcher();
		return mw.runMemoryCheck();

	}

	public boolean runMemoryCheck() throws OutOfMemoryError {

		long availableMemory = getAvailableMemory();
		if (availableMemory < 4000) {
			Runtime.getRuntime().gc();
			resetWarningCount();
			throw new OutOfMemoryError();
		}

		if (availableMemory > 32000) {
			warningCount = 0;
			return true;
		} else {
			// Run garbage collector and try again
			Runtime.getRuntime().gc();
			availableMemory = getAvailableMemory();
			if (availableMemory > 32000) {
				resetWarningCount();
				return true;
			}

		}

		boolean showWarning = false;
		if (warningCount == 0 && availableMemory < 32000)
			showWarning = true;
		else if (warningCount == 1 && availableMemory < 16000)
			showWarning = true;
		else if (warningCount == 2 && availableMemory < 8000)
			showWarning = true;
		else if (warningCount == 3 && availableMemory < 5000)
			showWarning = true;

		if (showWarning) {
			incrementWarningCount();

			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					response = UIHelper.instance.showMessage("Low memory warning", "The application is running low on memory. (" + getAvailableMemory() + " KB of memory remaining)." + StringHelper.getNewLine() + StringHelper.getNewLine() + "Would like to cancel the current operation?",
							SWT.ICON_WARNING | SWT.YES | SWT.NO | SWT.ON_TOP, null);
				}
			});
			if (response == SWT.YES)
				return false;

		}

		return true;

	}

	public long getAvailableMemory() {
		Runtime runtime = Runtime.getRuntime();

		final long maxMemory = runtime.maxMemory();
		final long allocatedMemory = runtime.totalMemory();
		final long freeMemory = runtime.freeMemory();

		final long availableMemory = (freeMemory + (maxMemory - allocatedMemory)) / 1024;

		return availableMemory;

	}

	protected static synchronized void incrementWarningCount() {
		warningCount++;
	}

	protected static synchronized void resetWarningCount() {
		warningCount = 0;
	}
}
