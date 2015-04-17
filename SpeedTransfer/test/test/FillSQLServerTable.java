package test;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.ss.speedtransfer.util.ConnectionManager;


public class FillSQLServerTable {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Connection con = null;
		try {
			con = ConnectionManager.getConnection("jdbc:sqlserver://localhost;databaseName=test;user=sa;password=Davva001");
			con.createStatement().executeUpdate("delete from mytable");

			PreparedStatement stmt = con.prepareStatement("insert into mytable1 values(?,?,?)");

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
