package settings;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.poi.ss.usermodel.Workbook;

import settings.Settings.StringSetting;
import utils.FileManager;

public class SettingChecker {
	public static boolean validateCurrentSettings(){
		String inputFilePath = Settings.getSetting(StringSetting.INPUT_FILE_PATH);
		boolean goodInputPath = checkInputFilePath(inputFilePath);
		
		String outputFileDirectory = Settings.getSetting(StringSetting.OUTPUT_FILE_DIRECTORY);
		String outputFileName = Settings.getSetting(StringSetting.OUTPUT_FILE_NAME);
		boolean goodOutputDirectory = checkOutputDirectory(outputFileDirectory);
		boolean goodOutputFile = checkOutputFile(outputFileDirectory+"/"+outputFileName);
		
		String formatFilePath = Settings.getSetting(StringSetting.FORMAT_FILE_PATH);
		boolean goodFormatPath = checkFormatFilePath(formatFilePath);
		
		return goodInputPath && goodOutputDirectory && goodOutputFile && goodFormatPath;
	}
	
	public static boolean checkInputFilePath(String filePath){
		// null paths are invalid
		if ( filePath == null ){
			return false;
		}
		
		// If the workbook can't be loaded it is invalid
		Workbook wb = FileManager.loadExcelFile(filePath);
		if ( wb == null ){
			return false;
		}
		
		//Otherwise, it is a valid file path
		return true;
	}

	public static boolean checkOutputDirectory(String outputDirectory) {
		File file = new File(outputDirectory);
		Path path = file.toPath();
		
		return Files.exists(path) && Files.isDirectory(path);
	}
	
	public static boolean checkOutputFile(String outputFilePath){
		File file = new File(outputFilePath);
		Path path = file.toPath();
		
		return Files.exists(path) && Files.isWritable(path);
	}
	
	public static boolean checkFormatFilePath(String formatFilePath){
		File file = new File(formatFilePath);
		Path path = file.toPath();
		
		return Files.exists(path) && Files.isRegularFile(path);
	}
}
