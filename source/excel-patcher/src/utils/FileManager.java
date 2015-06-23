package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import settings.Settings;
import settings.Settings.StringSetting;

/**
 * This class is a small static utility class for loading and saving 
 * excel files and format files.
 * 
 * @author Ashton Dyer (WabashCannon)
 *
 */
public class FileManager {
	
	/**
	 * Attempts to load a Workbook from the file specified by filePath. In the case
	 * that it fails to load the file, it returns null and logs an error message
	 * 
	 * @param filePath
	 * @return the Workbook loaded if successful, null otherwise
	 */
	public static Workbook loadExcelFile(String filePath){
		if ( filePath == null ){
			return null;
		}
		try {
			Workbook wb = WorkbookFactory.create(new FileInputStream(filePath));
			return wb;
		} catch (InvalidFormatException e) {
			Logger.log("Error", "Excel file at "+filePath+" was of an invalid format, quitting");
			Logger.logVerbose("Error", e.getMessage());
		} catch (FileNotFoundException e) {
			Logger.log("Error", "Could not find excel file at location \""+filePath+"\", quitting");
			Logger.logVerbose("Error", e.getMessage());
		} catch (IOException e) {
			Logger.log("Error", "IO Exception while loading excel file from location "+filePath);
			Logger.logVerbose("Error", e.getMessage());
		}
		return null;
	}
	
	/**
	 * Attempts to save the given workbook at the location of filePath. If it fails to save
	 * it logs the error.
	 * 
	 * @param filePath to save the workbook to
	 * @param workbook that is to be saved
	 * @return if the save was successful
	 */
	public static boolean saveExcelFile(String filePath, Workbook workbook){
		if ( filePath == null || workbook == null ){
			return false;
		}
		Logger.log("Saving file at "+filePath);
	    FileOutputStream fileOut;
		try {
			fileOut = new FileOutputStream(filePath);
			workbook.write(fileOut);
			fileOut.close();
			return true;
		} catch (FileNotFoundException e) {
			Logger.log("Error", "File not found when saving to "+filePath
					+". Error message: "+e.getMessage());
		} catch (IOException e) {
			Logger.log("Error", "IO exception when saving to "+filePath
					+". Error message: "+e.getMessage());
		}
		return false;
	}
	
	/**
	 * Opens the format file in the OS's default text editor. Implemented using
	 * DesktopApi.open(File file). If it fails to open the file, it logs the error.
	 * 
	 * @see DesktopApi
	 * @return if the file opened successfully
	 */
	public static boolean editFormatFile(){
		String formatFilePath = Settings.getSetting(StringSetting.FORMAT_FILE_PATH);
		File file = new File(formatFilePath);
		
		//Make sure the file exists
		if ( !file.exists() ){
			try {
				file.createNewFile();
			} catch (IOException e) {
				Logger.log("Error", "Format file was not found at "
						+formatFilePath+" and a new one could not be created");
				Logger.logVerbose("Error", e.getMessage());
				return false;
			}
		}
		
		boolean didOpen = DesktopApi.open(file);
		
		if ( !didOpen ){
			Logger.log("Error", "Could not open the format file in the default"
					+" text editor");
		}
		
		return didOpen;
	}
	
	
	public static void openFormatManual(){
		try {
			DesktopApi.browse( new URI("https://github.com/WabashCannon/excel-patcher/blob/master/Format_Manual.md"));
		} catch (URISyntaxException e) {
			Logger.log("Error", e.getMessage());
		}
	}
}
