package test;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

import com.ss.speedtransfer.util.ConnectionManager;


public class FillDB2Table {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Connection con = null;
		try {
			
			Properties props = new Properties();
			props.put("user", "DAVID");
			props.put("password", "davva");
			
			con = ConnectionManager.getConnection("jdbc:db2://localhost:50000/test", props);
			con.createStatement().executeUpdate("delete from david.mytable");

			PreparedStatement stmt = con.prepareStatement("insert into david.mytable values(?,?,?)");

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
