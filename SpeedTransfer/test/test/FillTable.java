package test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.ss.speedtransfer.util.ConnectionManager;


public class FillTable {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Connection con = null;
		try {
			con = ConnectionManager.getConnection("jdbc:mysql://localhost/test?user=root&password=davva");
			con.createStatement().executeUpdate("delete from numrisk");

			PreparedStatement stmt = con.prepareStatement("insert into numrisk values(?)");

			for (int i = 0; i < 100; i++) {
				stmt.setString(1, i + "7.123");
//				stmt.setString(2, "Text for" + i);
//				stmt.setString(3, "Text for" + i);
//				stmt.setString(4, "Text for" + i);
//				stmt.setString(5, "Text for" + i);
//				stmt.setString(6, "Text for" + i);
				stmt.executeUpdate();
			}
			stmt.close();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				con.close();
			} catch (SQLException e) {
			}
		}

	}

}
