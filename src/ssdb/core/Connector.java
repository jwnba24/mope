package ssdb.core;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Connector {
	private static final String USERNAME="root";
	/**管理员密码*/
	private static final String PASSWORD = "letmein";
	//private static final String PASSWORD = "123456";
	/**数据库连接的URL*/
	private static final String DB_URL = "jdbc:mysql://192.168.1.100/test_zeng";
	//private static final String DB_URL = "jdbc:mysql://localhost/test";
	public static Connection openConnection(){
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			return DriverManager.getConnection(DB_URL,USERNAME,PASSWORD);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("connection failed.");
		return null;
	}
	public static String getMasterKey(){
		return PASSWORD;
	}
}
