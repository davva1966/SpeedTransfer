package com.ss.speedtransfer;

import java.net.URL;

import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

import com.ss.speedtransfer.util.EnvironmentHelper;
import com.ss.speedtransfer.util.PickWorkspaceDialog;


/**
 * This class controls all aspects of the application's execution
 */
public class Application implements IApplication {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.equinox.app.IApplication#start(org.eclipse.equinox.app.
	 * IApplicationContext)
	 */
	public Object start(IApplicationContext context) {
		Display display = PlatformUI.createDisplay();
		try {

			// fetch the Location that we will be modifying
			Location instanceLoc = Platform.getInstanceLocation();

			// get what the user last said about remembering the workspace
			// location
			boolean remember = PickWorkspaceDialog.isRememberWorkspace() || PickWorkspaceDialog.isRestarting();

			// Reset restarting flag
			PickWorkspaceDialog.setIsRestarting(false);

			// get the last used workspace location
			String lastUsedWs = PickWorkspaceDialog.getLastSetWorkspaceDirectory();

			// if we have a "remember" but no last used workspace, it's not much
			// to remember
			if (remember && (lastUsedWs == null || lastUsedWs.length() == 0)) {
				remember = false;
			}

			// check to ensure the workspace location is still OK
			if (remember) {
				// if there's any problem whatsoever with the workspace, force a
				// dialog which in its turn will tell them what's bad
				String ret = PickWorkspaceDialog.checkWorkspaceDirectory(Display.getDefault().getActiveShell(), lastUsedWs, false, false);
				if (ret != null) {
					remember = false;
				}

			}

			// if we don't remember the workspace, show the dialog
			if (!remember) {
				PickWorkspaceDialog pwd = new PickWorkspaceDialog(false, null);
				int pick = pwd.open();

				// if the user cancelled, we can't do anything as we need a
				// workspace, so in this case, we tell them and exit
				if (pick == Window.CANCEL) {
					if (pwd.getSelectedWorkspaceLocation() == null) {
						MessageDialog.openError(display.getActiveShell(), "Error", "The application can not start without a workspace root and will now exit.");
						try {
							PlatformUI.getWorkbench().close();
						} catch (Exception err) {

						}
						System.exit(0);
						return IApplication.EXIT_OK;
					}
				} else {
					// tell Eclipse what the selected location was and continue
					instanceLoc.set(new URL("file", null, pwd.getSelectedWorkspaceLocation()), false);
				}
			} else {
				// set the last used location and continue
				instanceLoc.set(new URL("file", null, lastUsedWs), false);
			}

			int returnCode = PlatformUI.createAndRunWorkbench(display, new ApplicationWorkbenchAdvisor());
			if (returnCode == PlatformUI.RETURN_RESTART) {
				return IApplication.EXIT_RESTART;
			}
			return IApplication.EXIT_OK;
		} catch (Exception err) {
			System.out.println(err.toString());

		} finally {
			display.dispose();
		}

		return IApplication.EXIT_OK;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.equinox.app.IApplication#stop()
	 */
	public void stop() {
//		if (!PlatformUI.isWorkbenchRunning())
//			return;
		if (EnvironmentHelper.isExecutableEnvironment())
			return;
		final IWorkbench workbench = PlatformUI.getWorkbench();
		final Display display = workbench.getDisplay();
		display.syncExec(new Runnable() {
			public void run() {
				if (!display.isDisposed())
					workbench.close();
			}
		});
	}
}
