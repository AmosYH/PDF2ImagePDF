package dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import util.LogUtil;

public class SystemParameterDAO {
	
	public static HashMap<String, String> getAllSystemParameters(Connection conn){
		
		Statement stmt = null;
		ResultSet rs = null;
		
		HashMap<String, String> map = new HashMap<String, String>();
		String selectSql="select * from [eForm].[dbo].[P01_system_parameters];";
				
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(selectSql);

			while (rs.next()) {
				map.put(rs.getString("parameter_name"), rs.getString("parameter_value"));
			}

		}catch (SQLException e) {			
			LogUtil.writeLog("getSystemParameters e: " + e);
		} finally{
			try {
				if(stmt != null)
					stmt.close();
			} catch (SQLException e) {
				LogUtil.writeLog("getSystemParameters e: " + e);				
			}
		}
		
		return map;
	}

	public static HashMap<String, String> getSystemParameters(String[] email_setting_keys, Connection conn) {
		LogUtil.writeLog("SystemParameterDAO Start to retrieveReport system parameter");
		Statement stmt = null;
		ResultSet rs = null;
		
		HashMap<String, String> map = new HashMap<String, String>();
		String selectSql="select parameter_name, parameter_value from [eForm].[dbo].[P01_system_parameters] "
				+ "where ";	
		for (int i=0;i<email_setting_keys.length;i++){
			selectSql+="parameter_name='" + email_setting_keys[i] + "'";
			
			if (i<email_setting_keys.length-1)
				selectSql+=" OR ";
		}
		
		selectSql += ";";
		
		LogUtil.writeLog("Debuguse selectSql: " + selectSql);
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(selectSql);

			while (rs.next()) {
				map.put(rs.getString("parameter_name"), rs.getString("parameter_value"));
			}

		}catch (SQLException e) {			
			LogUtil.writeLog("getSystemParameters e: " + e);
		} finally{
			try {
				if(stmt != null)
					stmt.close();
			} catch (SQLException e) {
				LogUtil.writeLog("getSystemParameters e: " + e);				
			}
		}
		
		return map;
	}

}
