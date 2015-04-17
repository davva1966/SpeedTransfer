package test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.ss.speedtransfer.util.ConnectionManager;


public class Query400Table {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Connection con = null;
		try {
			con = ConnectionManager.getConnection("jdbc:as400://PUB1.RZKH.DE/DAVVA19661;user=DAVVA1966;password=davva1");
			
			ResultSet rs = con.createStatement().executeQuery("select * from mytable");
			while (rs.next()) {
				System.out.println(rs.getString(1));
			}
			
			rs.close();
			
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
