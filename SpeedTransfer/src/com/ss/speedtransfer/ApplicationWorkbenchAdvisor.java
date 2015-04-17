package com.ss.speedtransfer;

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

import com.ss.speedtransfer.util.SSUtil;
import com.ss.speedtransfer.util.LicenseManager;
import com.ss.speedtransfer.util.StringHelper;
import com.ss.speedtransfer.util.UIHelper;


public class ApplicationWorkbenchAdvisor extends WorkbenchAdvisorHack {
	private static final String PERSPECTIVE_ID = "com.ss.speedtransfer.mainPerspective";

	@Override
	public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
		return new ApplicationWorkbenchWindowAdvisor(configurer);
	}

	@Override
	public String getInitialWindowPerspectiveId() {
		return PERSPECTIVE_ID;
	}

	public void postStartup() {
		if (checkLicense() == false)
			PlatformUI.getWorkbench().close();
	}

	protected boolean checkLicense() {

		if (LicenseManager.isValid() == false) {
			UIHelper.instance().showMessage("Invalid license", "License is invalid. Program will exit. Message: " + LicenseManager.getErrorMessage());
			return false;
		}

		if (LicenseManager.isTrial() && LicenseManager.isExpired()) {
			UIHelper.instance().showMessage(
					"license expired",
					"The program license has expired. Program will exit. Expiry date was: " + LicenseManager.getExpiryDate() + StringHelper.getNewLine() + StringHelper.getNewLine()
							+ LicenseManager.getExpiryMessage());
			return false;
		}

		return true;
	}

	@Override
	public void preStartup() {
		super.preStartup();

		SSUtil.determineExcelInstalled();

	}

}
