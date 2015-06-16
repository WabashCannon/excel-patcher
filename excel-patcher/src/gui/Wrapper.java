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

public class Wrapper {
	//Tmp vars
	public static final String FORMAT_FILE_PATH = "rsc/format.txt";
	public static final String OUTPUT_FILE_NAME = "output.xlsx";
	
	/** Input file path */
	String inputFilePath = null;
	/** Output file path */
	String outputFilePath = null;
	/** Settings */
	public enum SettingName{Color, Comment, Delete, FixDataType};
	private boolean[] settings = new boolean[SettingName.values().length];
	
	public Wrapper(){
		setInputFile( loadFromFile(INPUT_FILE_NAME_PATH) );
		setOutputFile( loadFromFile(OUTPUT_FILE_NAME_PATH) );
		
		//set default settings
		setSetting(SettingName.Color, true);
		setSetting(SettingName.Comment, true);
		setSetting(SettingName.Delete, true);
		setSetting(SettingName.FixDataType, true);
		
	}
	
	
	//Temporary place holder methods
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
	public void setSetting(SettingName name, boolean value){
		settings[name.ordinal()] = value;
	}
	
	public boolean getSetting(SettingName name){
		return settings[name.ordinal()];
	}
	
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
	
	public boolean setOutputFile(String filePath){
		if ( DesktopApi.getOs().isLinux() ){
			outputFilePath = filePath+"/";
		} else if ( DesktopApi.getOs().isWindows() ){
			outputFilePath = filePath+"\\";
		}
		
		writeToFile(OUTPUT_FILE_NAME_PATH, filePath);
		return true;
	}
	
	public String getInputFile(){
		return inputFilePath;
	}
	
	public String getOutputFile(){
		return outputFilePath;
	}
	
	public void openInputFile(){
		if ( inputFilePath != null){
			DesktopApi.open(new File(inputFilePath));
		}
	}
	
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
		/*
		Path path = Paths.get(filePath);
		Set<String> output = new HashSet<String>();
		output.add(content);
		try {
			Files.write(path, output);
		} catch (IOException e) {
			e.printStackTrace();
		}
		*/
	}
// #####################################################################################
// ### Global instance implementation
// #####################################################################################
	private static Wrapper wrapper = null;
	
	public static Wrapper getWrapper(){
		if ( wrapper == null ){
			wrapper = new Wrapper();
		}
		return wrapper;
	}
}
