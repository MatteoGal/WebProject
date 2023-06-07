package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class Util {
	public static final String USER = "root";
	public static final String PWD = "root";
	public static final String DRIVER_CLASS = "com.mysql.cj.jdbc.Driver";
	public static final String DB_NAME = "mail_db";
	public static final String DB_URL = "jdbc:mysql://localhost:3306/" + DB_NAME;
		
	public static final Connection initDbConnection() {
		try {
			Class.forName(DRIVER_CLASS);
			
		    Properties connectionProps = new Properties();
		    connectionProps.put("user", USER);
		    connectionProps.put("password", PWD);
	
	        return DriverManager.getConnection(DB_URL, connectionProps);
		        	
    	} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
}