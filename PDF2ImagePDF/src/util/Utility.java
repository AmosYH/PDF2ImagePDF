package util;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

public class Utility {
	private static Properties config = new Properties();
	
	static {
		try {
			File configFile = new File(Thread.currentThread().getContextClassLoader().getResource("config.properties").getFile());
			config.load(new FileInputStream(configFile));
		} catch (Exception ex) {
			LogUtil.writeLog("init file error: " + ex);
		}
	}
	
	public static String getProperty(String key, String defVal){
		String propVal = getProperty(key);
		if("".equals(propVal)){
			return defVal;
		}else{
			return propVal;
		}	
	}
	public static String getProperty(String key) {
		if (key == null)
			return "";

		String v = config.getProperty(key);
		return v.trim();
	}
	public static String nullFilter(String input){
		if(input == null)
			return "";
		return input;
	}
	
}


