package test;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.ss.speedtransfer.util.ConnectionManager;


public class FillPostgreSQLTable {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Connection con = null;
		try {
			con = ConnectionManager.getConnection("jdbc:postgresql://localhost/test/?user=postgres&password=davva");
			con.createStatement().executeUpdate("delete from schema1.mytable");

			PreparedStatement stmt = con.prepareStatement("insert into schema1.mytable values(?,?,?)");

			for (int i = 0; i < 10000; i++) {
				stmt.setInt(1, i);
				stmt.setString(2, "Text for" + i);
				stmt.setDate(3, new Date(System.currentTimeMillis()));
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
