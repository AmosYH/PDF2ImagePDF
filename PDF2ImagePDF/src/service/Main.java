package service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;

import util.LogUtil;
import util.TiffImageUtil;

public class Main {
	private static int movePDF(String pdfPath,String destPath)  {		
		LogUtil.writeLog("Start movePDF");
				
		boolean temp=false;
		try {
			File source = new File(pdfPath);
			File dest = new File(destPath);
			Files.move(source.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
			temp = true;
		//	Files.copy(source.toPath(), dest.toPath(), StandardCopyOption.COPY_ATTRIBUTES);
		//	source.delete();
		} catch (Exception e) {
			e.printStackTrace();
			LogUtil.writeLog(e.getMessage());
		}
		
		int result;
        if(temp != false) {
        	LogUtil.writeLog("File moved successfully");
        	result = 0;
        }else {
            LogUtil.writeLog("Failed to move the file");
            result = 1;
        }
		return result;	
	}
	
	private static int pdf2tiff_batch(String inFilePath,String outFilePath)  {
		
		 File folder = new File(inFilePath);
		 File [] listOfFile = folder.listFiles();
		 String pdfPath;
		 String tiffPath;
		 String destPath;		 
		 HashMap<String, Integer> reportResult = new HashMap<String, Integer>();
		 Integer result = 0;
		 
		 for(int i=0;i<listOfFile.length;i++){
			 if(listOfFile[i].isFile()){
				 pdfPath=(inFilePath+listOfFile[i].getName());
				 LogUtil.writeLog("pdfPath: " +pdfPath);
				 tiffPath=(outFilePath+listOfFile[i].getName().split("\\.")[0]+".pdf"); 
				 LogUtil.writeLog("tiffPath:"+tiffPath);
				 destPath=(outFilePath+listOfFile[i].getName().split("\\.")[0]+".pdf");
				 LogUtil.writeLog("destPath:"+destPath);
				 
				 try { 
					 TiffImageUtil.pdf2Tiff(pdfPath,tiffPath);
					 //result = movePDF(pdfPath,destPath);
			 	 }catch(Exception e) {
		        	e.printStackTrace();
		        	LogUtil.writeLog(e.getMessage());
		         }
				 
				 reportResult.put(pdfPath, result);
			 }
		 }
		LogUtil.writeLog("Start send Summary Email"); 
		return 0;
		
	}
	
	public static void main(String[] args) {
		LogUtil.writeLog("Start PDF2tiff");
		String inFilePath="D:\\Users\\TCTR581\\Desktop\\JAR&BAT\\test_pdf2tiff\\Files\\In\\";
		LogUtil.writeLog("inFilePath:"+inFilePath);
		String outFilePath="D:\\Users\\TCTR581\\Desktop\\JAR&BAT\\test_pdf2tiff\\Files\\Out\\";
		LogUtil.writeLog("outFilePath:"+outFilePath);
		pdf2tiff_batch(inFilePath,outFilePath);	
		LogUtil.writeLog("PDF2tiff end");
	}
}
