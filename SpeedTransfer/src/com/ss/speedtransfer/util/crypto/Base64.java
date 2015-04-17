package com.ss.speedtransfer.util.crypto;

import java.io.IOException;

public class Base64 {

	public static int ENCRYPT = 1;
	public static int DECRYPT = 2;

	public static String coding(String data, int type) throws IOException {

		if (type == ENCRYPT) {
			BASE64Encoder encoder = new BASE64Encoder();
			String str = encoder.encodeBuffer(data.getBytes());
			return str;

		} else if (type == DECRYPT) {
			BASE64Decoder decoder = new BASE64Decoder();
			byte[] bytes = decoder.decodeBuffer(data);
			return new String(bytes);
		}

		return null;
	}

}
