//{{CopyrightNotice}}

package com.ss.speedtransfer.util;

import java.io.ByteArrayOutputStream;

/**
 * The <code>RandomByteArrayOutputStream</code> class
 */
public class RandomByteArrayOutputStream extends ByteArrayOutputStream {

	/**
	 * Initializes a newly created <code>RandomByteArrayOutputStream</code>
	 * 
	 */
	public RandomByteArrayOutputStream() {
	}

	/**
	 * Initializes a newly created <code>RandomByteArrayOutputStream</code>
	 * 
	 * @param arg0
	 */
	public RandomByteArrayOutputStream(int arg0) {
		super(arg0);
	}

	/**
	 * Print the passed data to the stream at the specified index
	 * 
	 * @param index
	 *            - the index where to print
	 * @param data
	 *            - the content to print
	 */
	public void insert(int index, byte[] data) {
		int len = data.length;

		if (count <= index)
			return;

		if (count < index + len)
			return;

		for (int i = 0; i < len; i++)
			buf[index + i] = data[i];
	}
}
