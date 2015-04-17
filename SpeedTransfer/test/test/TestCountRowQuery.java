package test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import com.ss.speedtransfer.util.ConnectionManager;


public class TestCountRowQuery {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Connection con = null;
		try {
			// Oracle
//			System.out.println("Oracle");
//			Properties props = new Properties();
//			props.put("user", "system");
//			props.put("password", "davva");
//			con = ConnectionManager.getConnection("jdbc:oracle:thin:@//localhost/XE", props);
//			ResultSet rs = con.createStatement().executeQuery("SELECT COUNT(1) FROM (select * from system.mytable)");
//			while (rs.next())
//				System.out.println(rs.getString(1));
			
			
			// SQL Server
//			System.out.println("SQL Server");
//			props = new Properties();
//			props.put("user", "sa");
//			props.put("password", "Davva001");
//			props.put("databaseName", "test");
//			con = ConnectionManager.getConnection("jdbc:sqlserver://localhost", props);
//			rs = con.createStatement().executeQuery("SELECT COUNT(1) FROM (select * from mytable1) as temp");
//			while (rs.next())
//				System.out.println(rs.getString(1));
			
			// MySQL
//			System.out.println("MySQL");
//			props = new Properties();
//			props.put("user", "root");
//			props.put("password", "davva");
//			con = ConnectionManager.getConnection("jdbc:mysql://localhost/test", props);
//			rs = con.createStatement().executeQuery("SELECT COUNT(1) FROM (select * from mytable1) as temp");
//			while (rs.next())
//				System.out.println(rs.getString(1));
			
			// iSeries
//			System.out.println("iSeries");
//			props = new Properties();
//			props.put("user", "asw5sec");
//			props.put("password", "mtaasw04");
//			con = ConnectionManager.getConnection("jdbc:as400://10.2.1.203/mta550efta", props);
//			rs = con.createStatement().executeQuery("SELECT COUNT(1) FROM (select * from sroctlc1) as temp");
//			while (rs.next())
//				System.out.println(rs.getString(1));
			
			// postgreSQL
			System.out.println("postgreSQL");
			Properties props = new Properties();
			props.put("user", "postgres");
			props.put("password", "davva");
			con = ConnectionManager.getConnection("jdbc:postgresql://localhost/test", props);
			ResultSet rs = con.createStatement().executeQuery("SELECT COUNT(1) FROM (select * from schema1.mytable) as temp");
			while (rs.next())
				System.out.println(rs.getString(1));

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
