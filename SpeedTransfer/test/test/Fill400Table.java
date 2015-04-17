package test;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.ss.speedtransfer.util.ConnectionManager;


public class Fill400Table {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Connection con = null;
		try {
			con = ConnectionManager.getConnection("jdbc:as400://PUB1.RZKH.DE/DAVVA19661;user=DAVVA1966;password=davva1");
			
			con.createStatement().executeUpdate("delete from medtable");

			PreparedStatement stmt = con.prepareStatement("insert into medtable values(?,?,?,?)");

			for (int i = 0; i < 2000; i++) {
				stmt.setInt(1, i);
				stmt.setString(2, "Text for" + i);
				stmt.setInt(3, i+10);
				stmt.setDate(4, new Date(System.currentTimeMillis()));
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
			System.exit(0);
		}

	}

}
