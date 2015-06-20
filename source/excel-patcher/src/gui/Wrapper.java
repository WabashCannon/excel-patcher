package gui;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

import org.apache.poi.ss.usermodel.Workbook;

import utils.DesktopApi;
import utils.FileManager;
import utils.Logger;
import excel.ExcelChecker;
import format.FormatData;

/**
 * Wrapper to interface between GUI side and ExcelChecker side.
 * 
 * @author Ashton Dyer (WabashCannon)
 *
 */
public class Wrapper {
	//Tmp vars
	/** The hard coded path to the format file - should be changed to
	 * be a choosable path */
	public static final String FORMAT_FILE_PATH = "rsc/format.txt";
	/** The hard coded output file name - should be changed to be a
	 * choosable path */
	public static final String OUTPUT_FILE_NAME = "output.xlsx";
	
	/** Input file path */
	String inputFilePath = null;
	/** Output file path */
	String outputFilePath = null;
	/** Index set for boolean settings */
	public enum SettingName{Color, Comment, Delete, FixDataType};
	/** The current state of the settings */
	private boolean[] settings = new boolean[SettingName.values().length];
	
	/**
	 * Creates a new wrapper. Attempts to load former input and output paths
	 * and inits with default settings - Should change to load and save settings
	 */
	public Wrapper(){
		setInputFile( loadFromFile(INPUT_FILE_NAME_PATH) );
		setOutputFile( loadFromFile(OUTPUT_FILE_NAME_PATH) );
		
		//set default settings
		setSetting(SettingName.Color, true);
		setSetting(SettingName.Comment, true);
		setSetting(SettingName.Delete, true);
		setSetting(SettingName.FixDataType, true);
		
	}
	
	
	/**
	 * Runs the main check on the excel file
	 */
	public void checkFile(){
		if ( inputFilePath == null ){
			Logger.log("Please select an input file.");
			return;
		}
		if ( outputFilePath == null ){
			Logger.log("Please select an output directory");
			return;
		}
		
		Thread thread = new Thread( new Runnable(){

			@Override
			public void run() {
				
				Logger.log("Checking the input file");
				
				FormatData formatData = new FormatData(FORMAT_FILE_PATH);
				
				//Load the input excel file
				Workbook wb = FileManager.loadExcelFile(inputFilePath);
				
				//Create the checker
				ExcelChecker checker = new ExcelChecker(wb, formatData);
				checker.deleteIfNotRequired = settings[SettingName.Delete.ordinal()];
				checker.colorFaultyCells = settings[SettingName.Color.ordinal()];
				checker.commentOnFaultyCells = settings[SettingName.Comment.ordinal()];
				checker.tryToFixFormatIssues = settings[SettingName.FixDataType.ordinal()];
				checker.patchAllLoans();
				
				//Save the output
				FileManager.saveExcelFile(outputFilePath+OUTPUT_FILE_NAME, wb);
				
				Logger.log("Done checking file, see the output.xlsx");
			}
			
		});
		
		thread.start();
		
	}
	
	/**
	 * Cleans the output excel file
	 */
	public void cleanFile(){
		Thread thread = new Thread( new Runnable(){
			@Override
			public void run() {
				
				Logger.log("Cleaning the output file");
				
				FormatData formatData = new FormatData(FORMAT_FILE_PATH);
				
				//Load the input excel file
				Workbook wb = FileManager.loadExcelFile(outputFilePath+OUTPUT_FILE_NAME);
				
				//Create the checker
				ExcelChecker checker = new ExcelChecker(wb, formatData);
				checker.cleanOutput();
				
				//Save the output
				FileManager.saveExcelFile(outputFilePath+"output.xlsx", wb);
				
				Logger.log("Done cleaning file, see the output.xlsx");
			}
			
		});
		
		thread.start();
	}
	
// #####################################################################################
// ### Getters and setters
// #####################################################################################
	/**
	 * Sets the specified setting to the given value
	 * 
	 * @param name of setting to set
	 * @param value the setting should have
	 */
	public void setSetting(SettingName name, boolean value){
		settings[name.ordinal()] = value;
	}
	
	/**
	 * Returns the value of the specified setting
	 * 
	 * @param name of setting to check
	 * @return the value of the specified setting
	 */
	public boolean getSetting(SettingName name){
		return settings[name.ordinal()];
	}
	
	/**
	 * Sets the input file to be the specified filePath if it is valid.
	 * If it isn't, the file path will remain unchanged.
	 * 
	 * @param filePath to change to
	 * @return if the change was successful
	 */
	public boolean setInputFile(String filePath){
		if ( filePath == null ){
			return false;
		}
		Workbook wb = FileManager.loadExcelFile(filePath);
		if ( wb == null ){
			return false;
		} else {
			inputFilePath = filePath;
			writeToFile(INPUT_FILE_NAME_PATH, filePath);
			return true;
		}
	}
	
	/**
	 * Sets the output file path 
	 * @param filePath
	 * @return if the change was successful
	 */
	public boolean setOutputFile(String filePath){
		if ( DesktopApi.getOs().isLinux() ){
			outputFilePath = filePath+"/";
		} else if ( DesktopApi.getOs().isWindows() ){
			outputFilePath = filePath+"\\";
		}
		
		writeToFile(OUTPUT_FILE_NAME_PATH, filePath);
		return true;
	}
	
	/**
	 * Returns the current input file path
	 * 
	 * @return the current input file path
	 */
	public String getInputFile(){
		return inputFilePath;
	}
	
	/**
	 * Returns the current output file path
	 * 
	 * @return the current output file path
	 */
	public String getOutputFile(){
		return outputFilePath;
	}
	
	/**
	 * Attempts to open the input file in the OS's default program
	 */
	public void openInputFile(){
		if ( inputFilePath != null){
			DesktopApi.open(new File(inputFilePath));
		}
	}
	
	/**
	 * Attempts to open the output file in the OS's default program
	 */
	public void openOutputFile(){
		if ( outputFilePath != null ){
			File file = new File(outputFilePath+OUTPUT_FILE_NAME);
			DesktopApi.open(file);
		}
	}
	
// #####################################################################################
// ### Hacks for saving settings
// #####################################################################################
	/** Settings folder relative path */
	String SETTINGS_PATH = "settings/";
	/** Input file name setting */
	String INPUT_FILE_NAME_PATH = "rsc/inputFileName.txt";
	/** Input file name setting */
	String OUTPUT_FILE_NAME_PATH = "rsc/outputFileName.txt";
	
	/**
	 * Reads a text file and returns the first line
	 * @param filename to read from
	 * @return the first line of the text file
	 */
	@SuppressWarnings("resource")
	private String loadFromFile(String filename){
		try{
			File file = new File(filename);
			Scanner scanner = new Scanner(file);
			if ( scanner.hasNextLine() ){
				String line = scanner.nextLine();
				return line;
			}
			scanner.close();
			Logger.logVerbose("Loaded from file "+filename);
		} catch ( Exception e){
			Logger.logVerbose("Failed to load "+filename+" setting from file.");
			Logger.logVerbose(e.getMessage());
		}
		return null;
	}
	
	/**
	 * Utility method to save a string to a text file
	 * 
	 * @param filePath to save text to
	 * @param content to write to the file
	 */
	private void writeToFile(String filePath, String content){
		if ( filePath == null || content == null ){
			return;
		}
        try {
			Files.write(Paths.get(filePath), content.getBytes());
			Logger.logVerbose("Saved to file "+filePath);
		} catch (IOException e) {
			Logger.logVerbose("Failed to write to file at location: "+filePath+" with content of "+content);
			Logger.logVerbose(e.getMessage());
		}
	}
// #####################################################################################
// ### Global instance implementation
// #####################################################################################
	/** Global instance of the wrapper */
	private static Wrapper wrapper = null;
	
	/**
	 * Returns the global instance of the wrapper
	 * 
	 * @return the global instance of the wrapper
	 */
	public static Wrapper getWrapper(){
		if ( wrapper == null ){
			wrapper = new Wrapper();
		}
		return wrapper;
	}
}
