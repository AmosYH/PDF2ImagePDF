package util;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;





//import util.LogController;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.codec.TiffImage;

import com.github.jaiimageio.impl.plugins.tiff.TIFFImageReader;
import com.github.jaiimageio.impl.plugins.tiff.TIFFImageReaderSpi;
import com.github.jaiimageio.impl.plugins.tiff.TIFFImageWriter;
import com.github.jaiimageio.impl.plugins.tiff.TIFFImageWriterSpi;
import com.github.jaiimageio.plugins.tiff.BaselineTIFFTagSet;
import com.github.jaiimageio.plugins.tiff.TIFFDirectory;
import com.github.jaiimageio.plugins.tiff.TIFFField;
import com.github.jaiimageio.plugins.tiff.TIFFTag;


public class TiffImageUtil {
	private static Toolkit toolkit = Toolkit.getDefaultToolkit();
    
	public static void pdf2Tiff(String inFile, String outFile) {
    	System.setProperty("sun.java2d.cmm", "sun.java2d.cmm.kcms.KcmsServiceProvider");
    	
    	PDDocument doc = null;
    	FileOutputStream os = null;
    	InputStream is =null;
    	
        try {
        	is = new FileInputStream(inFile);
        	os = new FileOutputStream(outFile);
         
            doc = PDDocument.load(is);
            int pageCount = doc.getNumberOfPages();
            PDFRenderer renderer = new PDFRenderer(doc);

            List<BufferedImage> piList = new ArrayList<BufferedImage>(pageCount - 1);
            for (int i = 0; i < pageCount; i++) {
                //BufferedImage image = renderer.renderImageWithDPI(i, 100 , ImageType.GRAY);
                //test
            	BufferedImage image = renderer.renderImageWithDPI(i, 200 , ImageType.RGB);
            	
//				Image htImage = null;
//				if (image.getWidth() * image.getHeight() > 4000000){
//					double scaleDownRatio = Math.sqrt((image.getWidth() * image.getHeight() )/ 4000000);
//					int scaledWidth = new Double(image.getWidth() / scaleDownRatio).intValue();
//					int scaledHeight = new Double(image.getHeight() / scaleDownRatio).intValue();
//					htImage = toolkit.createImage(new FilteredImageSource(image.getScaledInstance(scaledWidth, scaledHeight, BufferedImage.SCALE_SMOOTH).getSource(), new Halftone(3, 0)));
//				}else{ 
//					htImage = toolkit.createImage(new FilteredImageSource(image.getSource(), new Halftone(3, 0)));
//				}
				
//				image = new BufferedImage(htImage.getWidth(null), htImage.getHeight(null), BufferedImage.TYPE_INT_RGB);
//				Graphics2D g = image.createGraphics();
//				g.drawImage(htImage, 0, 0, null);
//				g.dispose();
				
                //test
                piList.add(image);
            }
            
            
            Document newPDF = new Document();
            newPDF.setMargins(0, 0, 0, 0);
	        PdfWriter.getInstance(newPDF, os);
	        newPDF.open();
	        
	        for(int i = 0; i < piList.size(); i++){
	            BufferedImage bufferedImage = piList.get(i);
	            ByteArrayOutputStream baos = new ByteArrayOutputStream();
	            ImageIO.write(bufferedImage, "png", baos);
	            com.itextpdf.text.Image tempImage = com.itextpdf.text.Image.getInstance(baos.toByteArray());
	            
	            Rectangle pageSize = new Rectangle(tempImage.getWidth(), tempImage.getHeight());
	            newPDF.setPageSize(pageSize);
	            newPDF.newPage();
	            newPDF.add(tempImage);
	        }
	        newPDF.close();
            
        } catch (DocumentException e) {
        	e.printStackTrace();
		} catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (doc != null) {
                	doc.close();
                }
                if (os != null) {
                	os.close();
                }
                if (is!=null){
                	is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
