package test;

import java.io.FileOutputStream;
import java.io.InputStream;

import com.ss.speedtransfer.model.QueryDefinition;


public class Test1 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			System.out.println(System.getProperty("java.io.tmpdir"));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
