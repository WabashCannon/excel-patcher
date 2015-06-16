package utils;
import gui.Wrapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

/**
 * Small utility class for loading and saving excel files
 * @author ashton
 *
 */
public class FileManager {
	
	public static Workbook loadExcelFile(String filename){
		try {
			Workbook wb = WorkbookFactory.create(new FileInputStream(filename));
			return wb;
		} catch (InvalidFormatException e) {
			Logger.log("Excel file was of an invalid format, quitting");
			Logger.logVerbose(e.getMessage());
		} catch (FileNotFoundException e) {
			Logger.log("Could not find excel file at location \""+filename+"\", quitting");
			Logger.logVerbose(e.getMessage());
		} catch (IOException e) {
			Logger.log("IO Exception while loading excel file");
			Logger.logVerbose(e.getMessage());
		} catch (Exception e){
			Logger.log("Exception while loading excel file");
			Logger.logVerbose(e.getMessage());
		}
		return null;
	}
	
	public static void saveExcelFile(String filename, Workbook wb){
		Logger.log("Saving file at "+filename);
	    FileOutputStream fileOut;
		try {
			fileOut = new FileOutputStream(filename);
			wb.write(fileOut);
			fileOut.close();
		} catch (FileNotFoundException e) {
			Logger.logCrash("File not found when saving to "+filename
					+". Error message: "+e.getMessage());
		} catch (IOException e) {
			Logger.logCrash("IO exception when saving to "+filename
					+". Error message: "+e.getMessage());
		}
	    
	}
	
	public static void editFormatFile(){
		File file = new File(Wrapper.FORMAT_FILE_PATH);
		openFile(file);
		/*
		try{
			Desktop.getDesktop().edit(file);
			/*
			if (System.getProperty("os.name").toLowerCase().contains("windows")) {
				System.out.println("1");
				String cmd = "rundll32 url.dll,FileProtocolHandler " + file.getCanonicalPath();
				Runtime.getRuntime().exec(cmd);
			} else {
				System.out.println("2");
				Desktop.getDesktop().edit(file);
			}
		} catch (Exception e){
			e.printStackTrace();
		}
		*/
	}
	
	public static void openFile(File file){
		DesktopApi.open(file);
		/*
		try{
			Desktop.getDesktop().edit(file);
			
			if (System.getProperty("os.name").toLowerCase().contains("windows")) {
				System.out.println("1");
				String cmd = "rundll32 url.dll,FileProtocolHandler " + file.getCanonicalPath();
				Runtime.getRuntime().exec(cmd);
			} else {
				System.out.println("2");
				Desktop.getDesktop().edit(file);
			}
		} catch (Exception e){
			e.printStackTrace();
		}
		*/
	}
}
