// {{CopyrightNotice}}

package com.ss.speedtransfer.util;

import java.io.IOException;
import java.io.OutputStream;

/**
 * This class implements a generation stream
 */
public class GenerationStreamImpl implements GenerationStream {

	/** This is the current indentation level */
	protected int indent = 0;

	/** Represents the number of lines written */
	protected int lineCount = 0;

	/** This is the current output stream */
	protected OutputStream out = null;

	/** This is the current line mapping output stream */
	protected OutputStream lineMapOut = null;

	/** This current line number */
	protected int lineNumber = 1;

	/** This current temporary line number */
	protected int tempLineNumber = 0;

	/** The line separator character */
	protected static String lineseparator = System.getProperty("line.separator");

	/** The last data printed */
	protected String lastData = null;

	/** Flag indicating if anything has been printed since last reset */
	protected boolean dirty = false;

	/**
	 * Initializes a newly created <code>GenerationStreamImpl</code>
	 */
	public GenerationStreamImpl() {
	}

	/**
	 * Check if anything has been printed since last reset
	 * 
	 * @return true if anything has been printed since last reset
	 */
	public boolean isDirty() {
		return dirty;
	}

	/**
	 * Reset anything has been printed since flag
	 */
	public void resetDirty() {
		dirty = false;
	}

	/**
	 * Check if the last data is linefeed
	 * 
	 * @return true if the last data printed is a linefeed
	 */
	public boolean isLastLF() {
		if (lastData != null && lastData.equals(lineseparator))
			return true;
		return false;
	}

	/**
	 * Return the active file name if the source is splitted
	 * 
	 * @return the name of the active source file
	 */
	public String getActiveFileName() {
		return null;
	}

	/**
	 * Close the stream
	 */
	public void close() {

		try {
			if (out != null) {
				out.close();
				out = null;
			}
			if (lineMapOut != null) {
				lineMapOut.close();
				lineMapOut = null;
			}

		} catch (IOException e) {

		}

	}

	/**
	 * Sets the output stream
	 * 
	 * @param out
	 *            - the stream the output is written to
	 */
	public void setOutputStream(OutputStream out) {
		this.out = out;
	}

	/**
	 * Return the output stream
	 * 
	 * @return the current OutputStream or null if not set
	 */
	public OutputStream getOutputStream() {
		return out;
	}

	/**
	 * Sets the line mapping output stream
	 * 
	 * @param lineMapOut
	 *            - the stream the line map is written to
	 */
	public void setLineMapOutputStream(OutputStream lineMapOut) {
		this.lineMapOut = lineMapOut;
	}

	/**
	 * Return the output stream
	 * 
	 * @return the current line map OutputStream or null if not set
	 */
	public OutputStream getLineMapOutputStream() {
		return lineMapOut;
	}

	/**
	 * Print a line map
	 * 
	 * @param lineInFromSource
	 *            - the line in the read source the line in this source is known
	 */
	public void printLineMap(int lineInFromSource) {

		printLineMap(lineInFromSource, lineNumber);
	}

	/**
	 * Print a line map
	 * 
	 * @param lineInFromSource
	 *            - the line in the read source the line in this source is known
	 * @param lineInThisSource
	 *            - the line in the this source
	 */
	protected void printLineMap(int lineInFromSource, int lineInThisSource) {

		if (lineMapOut == null)
			return;

		try {
			String data = lineInFromSource + "=" + lineInThisSource + lineseparator;
			lineMapOut.write(data.getBytes());

		} catch (Exception e) {
			logError("Failed to print line mapping:" + e);
		}
	}

	/**
	 * Set the indentation level
	 * 
	 * @param indent
	 *            - the indentation level
	 */
	public void setIndent(int indent) {
		this.indent = indent;
	}

	/**
	 * Return the current indentation level
	 */
	public int getIndent() {
		return indent;
	}

	/**
	 * Return the number of lines written
	 */
	public int getLineCount() {
		return lineCount;
	}

	/**
	 * Increment the indentation by one
	 */
	public void incIndent() {
		indent++;
	}

	/**
	 * Decrement the indentation by one
	 */
	public void decIndent() {
		indent--;
	}

	/**
	 * This method prints the number of tabs the indent instance represents
	 */
	protected void printIndent() {

		if (out == null) {
			logError("Failed to print because stream is null");
			return;
		}

		try {
			int tab = '\t';
			for (int i = 0; i < indent; i++)
				out.write(tab);

			dirty = true;
		} catch (Exception e) {
			logError("Failed to print indent:" + e);
		}
	}

	/**
	 * Print the passed data to the stream
	 * 
	 * @param data
	 *            - the content to print
	 */
	public void print(String data) {

		printInternal(data);

	}

	/**
	 * First print the current indentation then print the passed data to the stream
	 * 
	 * @param data
	 *            - the content to print
	 */
	public void iprint(String data) {

		iprintInternal(data);

	}

	/**
	 * First print the current indentation then print the passed data to the stream and a linefeed
	 * 
	 * @param data
	 *            - the content to print
	 */
	public void iprintln(String data) {

		iprintlnInternal(data);
	}

	/**
	 * Print the passed data to the stream and a linefeed
	 * 
	 * @param data
	 *            - the content to print
	 */
	public void println(String data) {

		printlnInternal(data);
	}

	/**
	 * Print the passed data to the stream and a linefeed
	 */
	public void println() {

		printlnInternal("");
	}

	/**
	 * Writes the data to the writer and append a new line character The data is padded with the pad character if data is less than length
	 * 
	 * @param data
	 *            - String to be written.
	 * @param pad
	 *            - the padding character
	 * @param length
	 *            the total length of the string to write
	 */
	public void print(String data, char pad, int length) {

		int len = data.length();
		if (len > length) {
			print(data.substring(0, length));

		} else if (len == length) {
			print(data);
		} else {
			print(data);
			print(pad, length - len);
		}
	}

	/**
	 * Writes the char to the stream
	 * 
	 * @param ch
	 *            - Character to be written.
	 */
	public void print(char ch) {
		printInternal(ch);
	}

	/**
	 * Writes the int to the stream
	 * 
	 * @param ch
	 *            - Character to be written.
	 */
	public void print(int ch) {
		printInternal(ch);
	}

	/**
	 * Writes the data right adjusted to the writer and append a new line character The data is padded with the pad character if data is less than length
	 * 
	 * @param data
	 *            - String to be written.
	 * @param pad
	 *            - the padding character
	 * @param length
	 *            the total length of the string to write
	 */
	public void printRight(String data, char pad, int length) {

		int len = 0;
		if (data != null)
			len = data.length();
		if (len > length) {
			print(data.substring(0, length));

		} else if (len == length) {
			print(data);
		} else {
			print(pad, length - len);
			print(data);
		}
	}

	/**
	 * Writes the char to the stream count times
	 * 
	 * @param ch
	 *            - Character to be written.
	 * @param count
	 *            - the number of times the data should be written
	 */
	public void print(char ch, int count) {
		for (int i = 0; i < count; i++)
			printInternal(ch);
	}

	/**
	 * Print the passed data to the stream
	 * 
	 * @param data
	 *            - the content to print
	 */
	protected void printInternal(String data) {

		if (out == null) {
			logError("Failed to print because stream is null");
			return;
		}

		try {
			if (data == null)
				return;

			lastData = data;

			byte[] bytes = data.getBytes();
			out.write(bytes);

			for (int i = 0; i < bytes.length; i++) {
				byte bt = bytes[i];
				if (bt == '\n') {
					lineNumber++;
					tempLineNumber++;
				}
			}

			dirty = true;

		} catch (Exception e) {
			// IBSEnvironmentLog.getLogger().error("Failed to print:"+e,e);
		}
	}

	/**
	 * Print the passed data to the stream
	 * 
	 * @param data
	 *            - the content to print
	 */
	protected void printInternal(char ch) {

		if (out == null) {
			logError("Failed to print because stream is null");
			return;
		}

		try {
			out.write(ch);

			if (ch == '\n') {
				lineNumber++;
				tempLineNumber++;
			}

			dirty = true;

		} catch (Exception e) {
			// IBSEnvironmentLog.getLogger().error("Failed to print:"+e,e);
		}
	}

	/**
	 * Print the passed data to the stream
	 * 
	 * @param data
	 *            - the content to print
	 */
	protected void printInternal(int ch) {

		if (out == null) {
			logError("Failed to print because stream is null");
			return;
		}

		try {
			out.write(ch);

			if (ch == '\n') {
				lineNumber++;
				tempLineNumber++;
			}

			dirty = true;

		} catch (Exception e) {
			// IBSEnvironmentLog.getLogger().error("Failed to print:"+e,e);
		}
	}

	/**
	 * First print the current indentation then print the passed data to the stream
	 * 
	 * @param data
	 *            - the content to print
	 */
	protected void iprintInternal(String data) {

		if (out == null) {
			logError("Failed to print because stream is null");
			return;
		}

		printIndent();
		printInternal(data);
	}

	/**
	 * First print the current indentation then print the passed data to the stream and a linefeed
	 * 
	 * @param data
	 *            - the content to print
	 */
	protected void iprintlnInternal(String data) {

		if (out == null) {
			logError("Failed to print because stream is null");
			return;
		}

		printIndent();
		printInternal(data);
		printInternal(lineseparator);
	}

	/**
	 * Print the passed data to the stream and a linefeed
	 * 
	 * @param data
	 *            - the content to print
	 */
	protected void printlnInternal(String data) {

		if (out == null) {
			logError("Failed to print because stream is null");
			return;
		}

		printInternal(data);
		printInternal(lineseparator);
	}

	/**
	 * Print data in error mode
	 * 
	 * @param data
	 *            - the content to log
	 */
	public void logError(String data) {
		// IBSEnvironmentLog.getLogger().error(data);
	}

	/**
	 * Reset the temporary line counter
	 */
	public void resetTempLineCounter() {
		tempLineNumber = 0;
	}

	/**
	 * Return the temporary line counter
	 */
	public int getTempLineCounter() {
		return tempLineNumber;
	}

	/**
	 * Return the temporary line counter
	 */
	public int getLine() {
		return lineNumber;
	}
}
