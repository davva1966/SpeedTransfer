// {{CopyrightNotice}}

package com.ss.speedtransfer.export;

import org.eclipse.jface.resource.ImageDescriptor;

/**
 * The <code>QueryExporter</code> interface This is the interface for query exporters. Query exporters export the result of a query.
 */
public interface QueryExporter {

	/** The export to file property */
	public static final String EXPORT_TO_FILE = "EXPORT_TO_FILE";//$NON-NLS-1$

	/** The launch file property */
	public static final String LAUNCH = "LAUNCH";//$NON-NLS-1$

	/**
	 * Export the query result data.
	 * 
	 */
	public abstract void export();

	/**
	 * Get the name to use for this exporter.
	 * 
	 * @return the job name
	 */
	public abstract String getJobName();

	/**
	 * Get the task name to use on the progress bar for this exporter.
	 * 
	 * @param arg
	 *            argument to include in task name
	 * @return the task name
	 */
	public abstract String getTaskName(String arg);

	/**
	 * Set the name to use for this exporter.
	 * 
	 * @param jobName
	 *            the name to use for this exporter
	 */
	public abstract void setJobName(String jobName);

	/**
	 * Get the image descriptor to use for this exporter.
	 * 
	 * @return the job image descriptor
	 */
	public abstract ImageDescriptor getJobImage();

	/**
	 * Set the image descriptor to use for this exporter.
	 * 
	 * @param jobImage
	 *            the image to use for this exporter
	 */
	public abstract void setJobImage(ImageDescriptor jobImage);

}