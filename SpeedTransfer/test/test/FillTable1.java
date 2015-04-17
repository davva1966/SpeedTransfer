package test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

import com.ss.speedtransfer.util.ConnectionManager;


public class FillTable1 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Connection con = null;
		try {
			con = ConnectionManager.getConnection("jdbc:mysql://test?user=root&password=davva");
			con.createStatement().executeUpdate("delete from mytable1");

			PreparedStatement stmt = con.prepareStatement("insert into mytable1 values(?,?,?,?,?,?)");

			for (int i = 1; i <= 100000; i++) {
				stmt.setInt(1, i);
				if (i > 90000) {
					stmt.setBigDecimal(2, new BigDecimal("-" + i + ".12"));
					stmt.setBigDecimal(3, new BigDecimal("-" + i * 7 + ".1234"));
				} else {
					stmt.setBigDecimal(2, new BigDecimal(i + ".12"));
					stmt.setBigDecimal(3, new BigDecimal(i * 7 + ".1234"));
				}
				stmt.setDate(4, new Date(System.currentTimeMillis()));
				String bo = "";
				if (i % 2 == 1)
					bo = "Y";
				else
					bo = "N";
				stmt.setString(5, bo);
				stmt.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
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
