package gui;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

import org.apache.poi.ss.usermodel.Workbook;

import settings.SettingChecker;
import settings.Settings;
import settings.Settings.StringSetting;
import utils.DesktopApi;
import utils.FileManager;
import utils.Logger;
import patcher.ExcelChecker;
import patcher.format.FormatData;

/**
 * Wrapper to interface between GUI side and ExcelChecker side.
 * 
 * @author Ashton Dyer (WabashCannon)
 *
 */
public class Wrapper {
	/**
	 * Suppress default constructor
	 */
	private Wrapper(){
	}
	
	
	/**
	 * Runs the main check on the excel file
	 */
	synchronized public void checkFile(){
		if ( Wrapper.isBusy() ){
			Logger.log("Error", "Wrapper is busy");
			return;
		}
		
		final String inputFilePath = Settings.getSetting(StringSetting.INPUT_FILE_PATH);
		if ( inputFilePath == null ){
			Logger.log("Please select an input file.");
			return;
		}
		
		final String outputFileDirectory = Settings.getSetting(StringSetting.OUTPUT_FILE_DIRECTORY);
		if ( outputFileDirectory == null ){
			Logger.log("Please select an output directory");
			return;
		}
		
		final String outputFileName = Settings.getSetting(StringSetting.OUTPUT_FILE_NAME);
		if ( outputFileName == null ){
			Logger.log("Please select an output file name");
			return;
		}
		
		Thread thread = new Thread( new Runnable(){

			@Override
			public void run() {
				try{
					Logger.log("Checking the input file");
					
					String formatFilePath = Settings.getSetting(StringSetting.FORMAT_FILE_PATH);
					FormatData formatData = new FormatData(formatFilePath);
					
					//Load the input excel file
					Workbook wb = FileManager.loadExcelFile(inputFilePath);
					
					//Create the checker
					ExcelChecker checker = new ExcelChecker(wb, formatData);
					checker.patchAllLoans();
					
					//Save the output
					FileManager.saveExcelFile(outputFileDirectory+"/"+outputFileName, wb);
					
					Logger.log("Done checking file.");
				} finally {
					Wrapper.setBusy(false);
				}
			}
	
		});
		
		Wrapper.setBusy(true);
		thread.start();
	}
	
	/**
	 * Cleans the output excel file
	 */
	synchronized public void cleanFile(){
		if ( Wrapper.isBusy() ){
			Logger.log("Error", "Wrapper is busy");
			return;
		}
		
		final String outputFileDirectory = Settings.getSetting(StringSetting.OUTPUT_FILE_DIRECTORY);
		if ( outputFileDirectory == null ){
			Logger.log("Please select an output directory");
			return;
		}
		
		final String outputFileName = Settings.getSetting(StringSetting.OUTPUT_FILE_NAME);
		if ( outputFileName == null ){
			Logger.log("Please select an output file name");
			return;
		}
		
		File file = new File(outputFileDirectory+"/"+outputFileName);
		Path path = file.toPath();
		boolean exists = Files.exists(path);
		if ( !exists ){
			Logger.log("Output file does not exists, so it cannot be cleaned");
			return;
		}
		
		Thread thread = new Thread( new Runnable(){
			@Override
			public void run() {
				try {
					//Log starting
					Logger.log("Cleaning the output file");
					
					//Create the format data
					String formatFilePath = Settings.getSetting(StringSetting.FORMAT_FILE_PATH);
					FormatData formatData = new FormatData(formatFilePath);
					
					//Load the input excel file
					Workbook wb = FileManager.loadExcelFile(outputFileDirectory+"/"+outputFileName);
					
					//Create the checker
					ExcelChecker checker = new ExcelChecker(wb, formatData);
					checker.cleanOutput();
					
					//Save the output
					FileManager.saveExcelFile(outputFileDirectory+"/"+outputFileName, wb);
					
					//Log completion
					Logger.log("Done cleaning file, see the output.xlsx");
				} finally {
					Wrapper.setBusy(false);
				}
			}
			
		});
		
		Wrapper.setBusy(true);
		thread.start();
	}
	
// #####################################################################################
// ### Getters and setters
// #####################################################################################
	/** If the wrapper is already busy running something */
	private boolean isBusy = false;
	
	/**
	 * Sets the busy state of the wrapper
	 * @param isBusy the new state of the wrapper
	 */
	synchronized public static void setBusy(boolean isBusy){
		Wrapper.getWrapper().isBusy = isBusy;
	}
	
	/**
	 * Returns if the wrapper is busy
	 * @return if the wrapper is busy
	 */
	synchronized public static boolean isBusy(){
		return Wrapper.getWrapper().isBusy;
	}
	
	/**
	 * Sets the input file to be the specified filePath if it is valid.
	 * If it isn't, the file path will remain unchanged.
	 * 
	 * @param filePath to change to
	 * @return if the change was successful
	 */
	public boolean setInputFile(String filePath){
		boolean goodFilePath = SettingChecker.checkInputFilePath(filePath);
		if ( goodFilePath ){
			Settings.setSetting(StringSetting.INPUT_FILE_PATH, filePath);
		}
		return goodFilePath;
	}
	
	/**
	 * Sets the output file path 
	 * @param filePath
	 * @return if the change was successful
	 */
	public boolean setOutputFile(String outputDirectory){
		boolean isGoodDirectory = SettingChecker.checkOutputDirectory(outputDirectory);
		
		if ( isGoodDirectory ){
			StringSetting setting = StringSetting.OUTPUT_FILE_DIRECTORY;
			Settings.setSetting(setting, outputDirectory);
			return true;
		}
		
		return false;
	}
	
	/**
	 * Attempts to open the input file in the OS's default program (Excel, Libre, ect.)
	 */
	public void openInputFile(){
		String inputFilePath = Settings.getSetting(StringSetting.INPUT_FILE_PATH);
		if ( inputFilePath != null){
			DesktopApi.open(new File(inputFilePath));
		}
	}
	
	/**
	 * Attempts to open the output file in the OS's default program
	 */
	public void openOutputFile(){
		String outputFileDirectory = Settings.getSetting(StringSetting.OUTPUT_FILE_DIRECTORY);
		String outputFileName = Settings.getSetting(StringSetting.OUTPUT_FILE_NAME);
		if ( outputFileDirectory != null && outputFileName != null ){
			File file = new File(outputFileDirectory+"/"+outputFileName);
			DesktopApi.open(file);
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
