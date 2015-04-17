package test;

import java.io.IOException;

import com.ss.speedtransfer.util.StringHelper;


public class Test5 {

	public static void main(String[] args) throws Exception {
		try {

			String sql = "SELECT * from '  my update table' where col1 = \"update\" and davva = \"ber'ry \" slut";
			String[] arr = sql.split("(\"([^\\\"]|\\.)*\"|'([^\\']|\\.)*')");
			System.out.println(arr);

		} catch (Exception e) {
			System.out.println(e);
		}

	}

	protected static String encrypt(String text) throws IOException {
		return StringHelper.encodePassword(text);

	}

}
