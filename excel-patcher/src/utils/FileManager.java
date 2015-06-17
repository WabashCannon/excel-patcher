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
		try {
			Workbook wb = WorkbookFactory.create(new FileInputStream(filePath));
			return wb;
		} catch (InvalidFormatException e) {
			Logger.log("Error", "Excel file was of an invalid format, quitting");
			Logger.logVerbose(e.getMessage());
		} catch (FileNotFoundException e) {
			Logger.log("Error", "Could not find excel file at location \""+filePath+"\", quitting");
			Logger.logVerbose(e.getMessage());
		} catch (IOException e) {
			Logger.log("Error", "IO Exception while loading excel file");
			Logger.logVerbose(e.getMessage());
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
	 * DesktopApi.open(File file)
	 * @see DesktopApi
	 */
	public static void editFormatFile(){
		File file = new File(Wrapper.FORMAT_FILE_PATH);
		DesktopApi.open(file);
	}
}
