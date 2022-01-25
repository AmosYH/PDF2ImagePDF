package util;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;



public class DBManager {

	private static Connection  conn;

	private final static String DB_DOMAIN = Utility.getProperty("DB_DOMAIN");
	private final static String DB_DATABASENAME = Utility.getProperty("DB_DATABASENAME");
	private final static String DB_USER = Utility.getProperty("DB_USER");
	private static String DB_PASSWORD = "";
	static {
		try {
			DB_PASSWORD = new String(
					AES256.decryptByDefaultKey(
							Base64.decode(Utility.getProperty("DB_PASSWORD"))));
		} catch (IOException e) {
			LogUtil.writeLog("Init Database Connection Error - e: " + e);
		}
	}
		
	public static void closeConnection(Connection conn) {
		try {
			conn.close();
		} catch (SQLException e) {
			//LogController.writeMessage(LogController.ERROR, "DBManager", "closeConnection", e.getMessage());
			LogUtil.writeLog("Close Connection - e: " + e);
		}
	}
	
	
	 public static Connection makeConnection() {
		boolean accessible = false;
		try {
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			String url = "jdbc:sqlserver://" + DB_DOMAIN + ";databaseName=" + DB_DATABASENAME + ";user=" + DB_USER + ";password=" + DB_PASSWORD;
			conn = DriverManager.getConnection(url);
			LogUtil.writeLog("Success to make connection to database");
			accessible = true;
		} catch (Exception e) {
			LogUtil.writeLog("makeConnection - e: " + e);
		}
		return conn;
	}
	 /*
	public static Connection makeConnection() {
		try {
			InitialContext context = new InitialContext();
			DataSource dataSource = (DataSource) context.lookup(jdbcName);
			return dataSource.getConnection();
		} catch (Exception e) {
			LogUtil.writeLog("makeConnection - e: " + e);
			return null;
		}
	}
	*/

}
