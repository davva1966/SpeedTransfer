//{{CopyrightNotice}}

package com.ss.speedtransfer.util;

/**
 * The <code>GenerationStream</code> class
 * 
 * @author Administrator
 */
public interface BackupGenerationStream extends GenerationStream {

	/**
	 * Set to true if stream should backup the data written
	 * 
	 * @param value
	 *            - true if data should be placed in a in-memory buffer
	 */
	public void backup(boolean value);

	/**
	 * Write the data in the backup buffer to the stream The buffer is empty after this method
	 */
	public void flush();

	/**
	 * Clear the data in the backup buffer The buffer is empty after this method
	 */
	public void clear();

	/**
	 * Push a new backup stream on the stack
	 */
	public void push();

	/**
	 * Remove and writes the last stack stream
	 */
	public void pop(boolean flush);

	/**
	 * Return the data in the buffer which is the data written since the last flush or when the backup flag was set to true
	 * 
	 * @return the buffer as a String
	 */
	public String getBuffer();

	/**
	 * Return the current byte index of the buffer
	 * 
	 * @return the buffer index or null
	 */
	public int getCurrentPos();

	/**
	 * Print the passed data to the stream at the specified index
	 * 
	 * @param index
	 *            - the index where to print
	 * @param data
	 *            - the content to print
	 */
	public void print(int index, String data);

}
