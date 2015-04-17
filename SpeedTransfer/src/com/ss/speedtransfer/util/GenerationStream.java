//{{CopyrightNotice}}

package com.ss.speedtransfer.util;

/**
 * The <code>GenerationStream</code> class
 * 
 * @author Administrator
 */
public interface GenerationStream {

	/**
	 * Return the active file name if the source is splitted
	 * 
	 * @return the name of the active source file
	 */
	public String getActiveFileName();

	/**
	 * Check if anything has been printed since last reset
	 * 
	 * @return true if anything has been printed since last reset
	 */
	public boolean isDirty();

	/**
	 * Reset anything has been printed since flag
	 */
	public void resetDirty();

	/**
	 * Close the stream
	 */
	public void close();

	/**
	 * Print a line map
	 * 
	 * @param lineInFromSource
	 *            - the line in the read source the line in this source is known
	 */
	public void printLineMap(int lineInFromSource);

	/**
	 * Print the passed data to the stream
	 * 
	 * @param data
	 *            - the content to print
	 */
	public void print(String data);

	/**
	 * Check if the last data is linefeed
	 * 
	 * @return true if the last data printed is a linefeed
	 */
	public boolean isLastLF();

	/**
	 * First print the current indentation then print the passed data to the stream
	 * 
	 * @param data
	 *            - the content to print
	 */
	public void iprint(String data);

	/**
	 * First print the current indentation then print the passed data to the stream and a linefeed
	 * 
	 * @param data
	 *            - the content to print
	 */
	public void iprintln(String data);

	/**
	 * Print the passed data to the stream and a linefeed
	 * 
	 * @param data
	 *            - the content to print
	 */
	public void println(String data);

	/**
	 * Print the passed data to the stream and a linefeed
	 */
	public void println();

	/**
	 * Writes thechar to the stream The data is padded with the pad character if data is less than length
	 * 
	 * @param data
	 *            - String to be written.
	 * @param pad
	 *            - the padding character
	 * @param length
	 *            the total length of the string to write
	 */
	public void print(String data, char pad, int length);

	/**
	 * Writes the char to the stream count times
	 * 
	 * @param ch
	 *            - Character to be written.
	 * @param count
	 *            - the number of times the data should be written
	 */
	public void print(char ch, int count);

	/**
	 * Writes the char to the stream
	 * 
	 * @param ch
	 *            - Character to be written.
	 */
	public void print(char ch);

	/**
	 * Writes the char to the stream
	 * 
	 * @param ch
	 *            - Character to be written.
	 */
	public void print(int ch);

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
	public void printRight(String data, char pad, int length);

	/**
	 * Return the current indentation level
	 * 
	 * @return the indentation which is a number from 0 - n
	 */
	public int getIndent();

	/**
	 * Increment the indentation level
	 */
	public void incIndent();

	/**
	 * Decrement the indentation level
	 */
	public void decIndent();

	/**
	 * Reset the temporary line counter
	 */
	public void resetTempLineCounter();

	/**
	 * Return the temporary line counter
	 */
	public int getTempLineCounter();

	/**
	 * Return the line counter
	 */
	public int getLine();
}
