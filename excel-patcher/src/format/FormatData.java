package format;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.Vector;

import utils.Logger;

public class FormatData {
	Set<String> finalDependencies;
	Vector<ColumnFormatData> columnFormats = new Vector<ColumnFormatData>();
	
	public FormatData(String formatFilename){
		Scanner scanner = null;
		try{
			scanner = new Scanner( new FileInputStream(formatFilename) );
		} catch ( FileNotFoundException e ){
			Logger.log("Error", "Format file was not found at location "+formatFilename);
		}
		
		while ( scanner.hasNextLine() ){
			String line = scanner.nextLine();
			if ( shouldIgnore(line) ){ continue; }
			//Trim whitespace
			line = line.trim();
			if ( isStartOfColumnDeclaration(line) ){
				//Process ColumnFormatData
				String columnTitle = line.substring(0, line.length()-1);
				loadColumnFormat(scanner, columnTitle);
			} else {
				Logger.log("Expected a column declaration in format file but recieved ");
				Logger.log("Error", line);
			}
		}
	}
	
	// ####################################################
	// ### Getters and Setters
	// ####################################################
	public int getNumColumns(){
		return columnFormats.size();
	}
	
	public Vector<String> getColumnTitles(){
		Vector<String> titles = new Vector<String>();
		for ( ColumnFormatData columnFormat : columnFormats ){
			titles.add( columnFormat.getTitle() );
		}
		return titles;
	}
	
	public ColumnFormatData getColumnFormat(String columnTitle){
		for ( ColumnFormatData columnFormat : columnFormats ){
			if ( columnFormat.getTitle().equals(columnTitle) ){
				return columnFormat;
			}
		}
		Logger.logVerbose("Warning: Could not find column with title "+columnTitle+" in format data");
		return null;
	}
	
	public void removeColumn(String title){
		for ( int i = columnFormats.size()-1 ; i >= 0 ; i-- ){
			if ( columnFormats.get(i).getTitle().equals(title) ){
				columnFormats.remove(i);
			}
		}
	}
	
	public Set<String> getAllDependencies(){
		if ( finalDependencies == null ){
			Logger.log("Error", "Tried to read all dependencies before they were compiled");
		}
		return finalDependencies;
	}
	
	public Set<String> getAllNonDependencies(){
		Set<String> allHeaders = new HashSet<String>();
		allHeaders.addAll(getColumnTitles());
		allHeaders.removeAll(getAllDependencies());
		return allHeaders;
	}
	
	// ####################################################
	// ### "Late" initialization methods
	// ####################################################
	public void compileDependencies(){
		finalDependencies = new HashSet<String>();
		for ( ColumnFormatData columnFormat : columnFormats ){
			finalDependencies.addAll( columnFormat.getFinalDependencies() );
		}
	}
	
	// ####################################################
	// ### Private methods for loading
	// ####################################################
	private void loadColumnFormat(Scanner scanner, String title){
		ColumnFormatData columnFormat = new ColumnFormatData(title);
		String line = "";
		while ( scanner.hasNextLine() ){
			//Get next line
			line = scanner.nextLine();
			if ( shouldIgnore(line) ){ continue; }
			line = line.trim();
			//stop reading if declaration is over
			if ( isEndOfColumnDeclaration(line) ){ break; }
			//Create specification from line and add to columnFormat
			while ( line.endsWith(";")){//multi-line specification
				if ( !scanner.hasNextLine() ){
					Logger.log("Error", "Unexpecet end of format file after \";\"");
				} else {
					String tmp = scanner.nextLine();
					if ( shouldIgnore(tmp) ){ continue; }
					line += tmp.trim();
				}
			}
			Specification specification = new Specification(line);
			columnFormat.addSpecification(specification);
		}
		addColumnFormat(columnFormat);
	}
	
	private void addColumnFormat(ColumnFormatData columnFormat){
		for ( ColumnFormatData data : columnFormats ){
			if ( data.getTitle().equals(columnFormat.getTitle()) ){
				Logger.log("Error", "Found duplicate decleration of column "+columnFormat.getTitle()+" in format file");
			}
		}
		//System.out.println(columnFormat.toString());
		columnFormats.add(columnFormat);
	}
	
	private boolean shouldIgnore(String line){
		if ( line.isEmpty() || line.trim().startsWith("#") ) {
			return true;
		}
		return false;
	}
	
	private boolean isStartOfColumnDeclaration(String line){
		line = line.trim();
		boolean endsWithBrace = line.endsWith("{");
		return endsWithBrace;
	}
	
	private boolean isEndOfColumnDeclaration(String line){
		line = line.trim();
		return line.equalsIgnoreCase("}");
	}
}
