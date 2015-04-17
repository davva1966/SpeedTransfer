package test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import com.ss.speedtransfer.util.ConnectionManager;


public class FillOracleTable {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Connection con = null;
		try {
			Properties props = new Properties();
			props.put("user", "system");
			props.put("password", "davva");
			
			con = ConnectionManager.getConnection("jdbc:oracle:thin:@//localhost/XE", props);
			ResultSet rs = con.createStatement().executeQuery("SELECT col1 FROM SYSTEM.MYTABLE");
			while (rs.next())
				System.out.println(rs.getString(1));
			
//			con.createStatement().executeUpdate("delete from mytable");
//
//			PreparedStatement stmt = con.prepareStatement("insert into mytable values(?,?,?)");
//
//			for (int i = 0; i < 10000; i++) {
//				stmt.setInt(1, i);
//				stmt.setString(2, "Text for" + i);
//				stmt.setDate(3, new Date(System.currentTimeMillis()));
//				stmt.executeUpdate();
//			}
//			stmt.close();

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
