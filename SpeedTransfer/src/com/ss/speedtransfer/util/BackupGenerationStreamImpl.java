// {{CopyrightNotice}}

package com.ss.speedtransfer.util;

import java.io.OutputStream;
import java.util.Stack;

/**
 * This class implements a backupable generation stream
 */
public class BackupGenerationStreamImpl extends GenerationStreamImpl implements BackupGenerationStream {

	/** This is the original output stream */
	protected OutputStream origout = null;

	/** The backup stack */
	protected Stack<OutputStream> stack = new Stack<OutputStream>();
	/** The backup stack */
	protected Stack<Integer> stackBufferIndex = new Stack<Integer>();

	/** This is the backup output stream */
	protected RandomByteArrayOutputStream backupout = null;

	/** The current buffer index */
	protected int bufferIndex = 0;

	/**
	 * Close the stream
	 */
	public void close() {
		super.close();
		clear();
	}

	/**
	 * Initializes a newly created <code>BackupGenerationStreamImpl</code>
	 */
	public BackupGenerationStreamImpl() {
		backup(true);
	}

	/**
	 * Initializes a newly created <code>BackupGenerationStreamImpl</code>
	 * 
	 * @param backup
	 *            - true if data should be placed in a in-memory buffer
	 */
	public BackupGenerationStreamImpl(boolean backup) {
		backup(backup);
	}

	/**
	 * Push a new backup stream on the stack
	 */
	public void push() {

		RandomByteArrayOutputStream newout = new RandomByteArrayOutputStream(4096);

		if (backupout != null) {
			stack.push(backupout);
			stackBufferIndex.push(bufferIndex);
		} else if (out != null) {
			stack.push(out);

			origout = out;
		}

		backupout = newout;
		out = newout;
	}

	/**
	 * Remove and writes the last stack stream
	 */
	public void pop(boolean flush) {
		if (stack.size() == 0)
			return;

		byte[] bytes = null;
		if (flush) {
			try {

				bytes = backupout.toByteArray();

			} catch (Exception e) {
				// IBSEnvironmentLog.getLogger().error("Failed to pop read:"+e,e);
			}
		}
		OutputStream o = stack.pop();
		if (o instanceof RandomByteArrayOutputStream) {
			backupout = (RandomByteArrayOutputStream) o;
			out = backupout;
			bufferIndex = stackBufferIndex.pop();
		} else {
			out = origout;
			backupout = null;
			origout = null;
			bufferIndex = 0;
		}

		if (flush) {
			try {
				out.write(bytes);
			} catch (Exception e) {
				// IBSEnvironmentLog.getLogger().error("Failed to pop write:"+e,e);
			}
		}
	}

	/**
	 * Sets the output stream
	 * 
	 * @param out
	 *            - the stream the output is written to
	 */
	public void setOutputStream(OutputStream out) {
		super.setOutputStream(out);
		this.origout = out;
	}

	/**
	 * This method prints the number of tabs the indent instance represents
	 */
	protected void printIndent() {
		super.printIndent();

		bufferIndex += indent;
	}

	/**
	 * Print the passed data to the stream
	 * 
	 * @param data
	 *            - the content to print
	 */
	protected void printInternal(String data) {
		super.printInternal(data);

		bufferIndex += data.length();
	}

	/**
	 * Print the passed data to the stream
	 * 
	 * @param data
	 *            - the content to print
	 */
	protected void printInternal(char ch) {
		super.printInternal(ch);
		bufferIndex++;
	}

	/**
	 * Print the passed data to the stream
	 * 
	 * @param data
	 *            - the content to print
	 */
	protected void printInternal(int ch) {
		super.printInternal(ch);
		bufferIndex++;
	}

	/**
	 * Return the current byte index of the buffer
	 * 
	 * @return the buffer index or null
	 */
	public int getCurrentPos() {
		return bufferIndex;
	}

	/**
	 * Set to true if stream should backup the data written
	 * 
	 * @param value
	 *            - true if data should be placed in a in-memory buffer
	 */
	public void backup(boolean value) {
		if (value) {
			origout = out;

			if (backupout == null)
				backupout = new RandomByteArrayOutputStream(4096);

			out = backupout;
			bufferIndex = 0;
		} else {
			backupout = null;
			if (origout != null)
				out = origout;
			origout = null;
			bufferIndex = 0;
		}
	}

	/**
	 * Write the data in the backup buffer to the stream The buffer is empty after
	 */
	public void flush() {
		if (origout == null)
			return;
		if (backupout == null)
			return;

		if (out == null) {
			logError("Failed to print because stream is null");
			return;
		}

		try {

			byte[] bytes = backupout.toByteArray();

			origout.write(bytes);

		} catch (Exception e) {
			// IBSEnvironmentLog.getLogger().error("Failed to flush:"+e,e);
		}

		backupout.reset();
		bufferIndex = 0;
	}

	/**
	 * Clear the data in the backup buffer The buffer is empty after this method
	 */
	public void clear() {
		if (backupout == null)
			return;
		backupout.reset();
		bufferIndex = 0;

	}

	/**
	 * Return the data in the buffer which is the data written since the last flush or when the backup flag was set to true
	 * 
	 * @return the buffer as a String
	 */
	public String getBuffer() {
		if (backupout == null)
			return null;
		return backupout.toString();
	}

	/**
	 * Return the data in the buffer which is the data written since the last flush or when the backup flag was set to true
	 * 
	 * @return the buffer as a String
	 */
	public String toString() {
		return getBuffer();
	}

	/**
	 * Print the passed data to the stream at the specified index
	 * 
	 * @param index
	 *            - the index where to print
	 * @param data
	 *            - the content to print
	 */
	public void print(int index, String data) {
		if (backupout == null)
			return;

		backupout.insert(index, data.getBytes());
	}
}
