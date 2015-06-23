package patcher.format;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.Vector;

import patcher.format.conditional.Specification;
import utils.Logger;

public class FormatData {
	/** The finalized set of minimal dependencies for this sheet */
	public DependencyDigraph dependencyGraph = null;
	/** The container of format data for each column */
	private Vector<ColumnFormatData> columnFormats = new Vector<ColumnFormatData>();
	
	/**
	 * Creates a new format data from the format file located at
	 * the specified formatFilePath
	 * 
	 * @param formatFilePath
	 */
	public FormatData(String formatFilePath){
		Scanner scanner = null;
		try{
			scanner = new Scanner( new FileInputStream(formatFilePath) );
		} catch ( FileNotFoundException e ){
			Logger.log("Error", "Format file was not found at location "+formatFilePath);
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
		
		dependencyGraph = new DependencyDigraph();
		for ( ColumnFormatData columnFormat : columnFormats ){
			String columnName = columnFormat.getTitle();
			Set<String> columnDependencies = columnFormat.getDependencies();
			for ( String dependency : columnDependencies ){
				dependencyGraph.addChildToParent(columnName, dependency);
			}
		}
		
	}
	
	// ####################################################
	// ### Getters and Setters
	// ####################################################
	/**
	 * Returns the number of columns that format data exists for.
	 * 
	 * @return the number of columns that format data exists for.
	 */
	public int getNumColumns(){
		return columnFormats.size();
	}
	
	/**
	 * Returns a vector containing all of the column titles for which
	 * format data exists.
	 * 
	 * @return a vector containing all of the column titles for which
	 * format data exists.
	 */
	public Set<String> getColumnTitles(){
		Set<String> titles = new HashSet<String>();
		for ( ColumnFormatData columnFormat : columnFormats ){
			titles.add( columnFormat.getTitle() );
		}
		return titles;
	}
	
	/**
	 * Returns the ColumnFormatData corresponding to the specified column
	 * title if it is found, null otherwise.
	 * 
	 * @param columnTitle to get ColumnFormatData for
	 * @return the ColumnFormatData corresponding to the columnTitle provided
	 */
	public ColumnFormatData getColumnFormat(String columnTitle){
		for ( ColumnFormatData columnFormat : columnFormats ){
			if ( columnFormat.getTitle().equals(columnTitle) ){
				return columnFormat;
			}
		}
		Logger.logVerbose("Warning: Could not find column with title "+columnTitle+" in format data");
		return null;
	}
	
	/**
	 * Removes the column format data corresponding to the provided column title.
	 * @param title of the column to remove
	 */
	public void removeColumn(String title){
		for ( int i = columnFormats.size()-1 ; i >= 0 ; i-- ){
			if ( columnFormats.get(i).getTitle().equals(title) ){
				columnFormats.remove(i);
			}
		}
	}
	
	// ####################################################
	// ### Private methods for loading
	// ####################################################
	/**
	 * Loads the data from the format file and stores it in the
	 * column format data vector.
	 * 
	 * @param scanner that 
	 * @param title
	 */
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
		columnFormats.add(columnFormat);
	}
	
	/**
	 * Utility method for determining if a line should be skipped when
	 * reading the format file
	 * 
	 * @param line to check
	 * @return if it is a comment line
	 */
	private boolean shouldIgnore(String line){
		if ( line.isEmpty() || line.trim().startsWith("#") ) {
			return true;
		}
		return false;
	}
	
	/**
	 * Utility method to determine if a line is the first line in
	 * a column format declaration
	 * 
	 * @param line to test
	 * @return if the line is the first one in a column format declaration
	 */
	private boolean isStartOfColumnDeclaration(String line){
		line = line.trim();
		boolean endsWithBrace = line.endsWith("{");
		return endsWithBrace;
	}
	
	/**
	 * Utility method to determine if a line is the last line in
	 * a column format declaration
	 * 
	 * @param line to test
	 * @return if the line is the last one in a column format declaration
	 */
	private boolean isEndOfColumnDeclaration(String line){
		line = line.trim();
		return line.equalsIgnoreCase("}");
	}
}
