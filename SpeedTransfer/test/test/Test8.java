package test;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

public class Test8  {

	public static void main(String[] args) throws IOException {

		try {
			// load the driver into memory
			Class.forName("org.relique.jdbc.csv.CsvDriver");

			// create a connection. The first command line parameter is assumed
			// to
			// be the directory in which the .csv files are held
			Connection conn = DriverManager.getConnection("jdbc:relique:csv:D:\\Temp");
			
			// create a Statement object to execute the query with
			Statement stmt = conn.createStatement();

			// Select the ID and NAME columns from sample.csv
			ResultSet results = stmt.executeQuery("SELECT * FROM mytable");
			results.next();
			System.out.println(results.getMetaData().getColumnType(1));
			System.out.println(results.getMetaData().getColumnType(2));
			System.out.println(results.getMetaData().getColumnType(3));
			System.out.println(results.getMetaData().getColumnType(4));
			System.out.println(results.getMetaData().getColumnType(5));
			System.out.println(results.getMetaData().getColumnType(6));

			// dump out the results
//			while (results.next()) {
//				System.out.println("ID= " + results.getString(1) + "   Desc= " + results.getString(2));
//			}

			// clean up
			results.close();
			stmt.close();
			conn.close();
		} catch (Exception e) {
			System.out.println("Oops-> " + e);
		}

	}

}
