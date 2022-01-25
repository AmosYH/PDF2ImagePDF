package util;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class LogUtil {
	public static void writeLog(String s) {
		try {
			//create directory for storing consolidation logs
			File file = new File("log"); 
			if (!file.exists()) {
	            if (file.mkdir()) {
	                System.out.println("Directory is created!");
	            } else {
	                System.out.println("Failed to create directory!");
	            }
	        }
			//start writing log
			File logFile = new File("log\\log_" + (new SimpleDateFormat("yyyyMMdd")).format(new Date()) + ".log");
			FileWriter writer;
			if (!logFile.exists())
				writer = new FileWriter(logFile);
			else
				writer = new FileWriter(logFile, true);
			
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			writer.write(dateFormat.format(new Date()) + " " + s + "\n");
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
