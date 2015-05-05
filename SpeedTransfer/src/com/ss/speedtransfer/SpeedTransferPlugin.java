package com.ss.speedtransfer;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.FormColors;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.ss.speedtransfer.util.EnvironmentHelper;

/**
 * The activator class controls the plug-in life cycle
 */
public class SpeedTransferPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "speedtransfer"; //$NON-NLS-1$

	// The shared instance
	private static SpeedTransferPlugin plugin;

	private FormColors formColors;

	/**
	 * The constructor
	 */
	public SpeedTransferPlugin() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
	 * )
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;

		// Create top project if none exists
		// IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		// if (root.getProjects().length == 0) {
		// IProgressMonitor progressMonitor = new NullProgressMonitor();
		// IProject project = root.getProject("Query Definitions");
		// try {
		// project.create(progressMonitor);
		// project.open(progressMonitor);
		// project.refreshLocal(IResource.DEPTH_ZERO, null);
		// } catch (Exception e) {
		// }
		// }

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
	 * )
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static SpeedTransferPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in
	 * relative path
	 * 
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {

		if (path.contains("/") == false)
			path = "resources/images/" + path;

		ImageDescriptor desc = null;
		if (EnvironmentHelper.isExecutableEnvironment())
			desc = getImageDescriptorFromResource(path);
		else
			desc = getImageDescriptorFromPlugin(path);

		return desc;
	}

	protected static ImageDescriptor getImageDescriptorFromPlugin(String path) {

		ImageDescriptor desc = null;
		if (path.indexOf('.') != -1)
			desc = AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, path);
		else
			desc = AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, path + ".gif");
		if (desc == null)
			desc = AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, path + ".jpg");

		return desc;
	}

	protected static ImageDescriptor getImageDescriptorFromResource(String path) {

		if (path.startsWith("/") == false)
			path = "/" + path;

		ImageDescriptor desc = null;
		if (path.indexOf('.') != -1)
			desc = ImageDescriptor.createFromFile(SpeedTransferPlugin.class, path);
		else
			desc = ImageDescriptor.createFromFile(SpeedTransferPlugin.class, path + ".gif");
		if (desc == null)
			desc = ImageDescriptor.createFromFile(SpeedTransferPlugin.class, path + ".jpg");

		return desc;
	}

	public static IDialogSettings getDialogSettingsFor(String id) {
		// Get main dialog settings from plug-in
		IDialogSettings dSettings = getDefault().getDialogSettings();

		// Return main dialog settings if no object passed
		if (id == null)
			return dSettings;

		// Get or create section for passed object's class name
		IDialogSettings dSection = dSettings.getSection(id);

		if (dSection == null)
			dSection = dSettings.addNewSection(id);

		return dSection;
	}

	public FormColors getFormColors(Display display) {
		if (formColors == null) {
			formColors = new FormColors(display);
			formColors.markShared();
		}
		return formColors;
	}

}
