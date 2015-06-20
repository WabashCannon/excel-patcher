package excel;

import java.util.Set;
import java.util.Vector;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import utils.Logger;
import excel.ExcelUtils.UrgencyLevel;
import format.ColumnFormatData;
import format.DataType;
import format.FormatData;

/**
 * The meat and potatoes of the excel-patcher program. This class
 * has two core commands, cleanOutput and patchAllLoans. See these two
 * methods for what this class does.
 * 
 * @author Ashton Dyer (WabashCannon)
 */
public class ExcelChecker {
	/** Global instance */
	public static ExcelChecker checker = null;
	
	/** Current cell to querry. */
	public Cell currentCell;
	
	//Settings
	/** Setting determining if non-required cells should be deleted */
	public boolean deleteIfNotRequired = true;
	/** Setting determining if the program should auto-correct format issues */
	public boolean tryToFixFormatIssues = true;
	/** Setting determining if cells should be colored on the output workbook */
	public boolean colorFaultyCells = true;
	/** Setting determining if comments should be added to the output workbook */
	public boolean commentOnFaultyCells = true;
	
	//Instance variables
	/** The sheet of the workbook being checked */
	private Sheet sheet;
	/** The SheetData object correlating to the variable sheet */
	private SheetData sheetData;
	/** The format data loaded from the format file */
	private FormatData formatData;
	
	/**
	 * Constructs a new checker for the given workbook and format data
	 * @param wb
	 * @param data
	 */
	public ExcelChecker(Workbook wb, FormatData data){
		//Store the global instance
		ExcelChecker.checker = this;
		
		//Always run on the first sheet of the workbook
		this.sheet = wb.getSheetAt(0);
		
		//Create the sheet data and format data
		this.sheetData = new SheetData(sheet);
		this.formatData = data;
		
		//Rough validation check of the format data
		checkFormatData();
		Logger.log("Format file was most likely valid");
	}
	
	/**
	 * Cleans the excel sheet of any comments and coloring
	 */
	public void cleanOutput(){
		for ( int rowIndex : sheetData.getLoanRowIndexes() ){
			Row row = sheet.getRow(rowIndex);
			
			for ( String header : formatData.getColumnTitles() ){
				int columnIndex = sheetData.getColumnIndex(header);
				Cell cell = row.getCell(columnIndex);
				if ( cell == null ){
					continue;
				}
				
				ExcelUtils.cleanCell(cell);
			}
		}
	}
	
	/**
	 * Checks the format data to ensure it is mostly valid. This is
	 * only a rough check, but it prevents several runtime issues while checking
	 * the excel sheet.
	 */
	private void checkFormatData(){
		//Checks that the format file has rules for all columns
		// and alerts the user if this is not the case
		if ( sheetData.getNumHeaders() != formatData.getNumColumns() ){
			Logger.log("Warning: Format file specified "+formatData.getNumColumns()
					+" columns to check, but "+sheetData.getNumHeaders()
					+" headers were found on the input sheet. I'll only "
					+"work on the columns from the format file.");
		}
		//Checks that all format columns exist in the sheet and alerts the user
		// about each format column that is not in the sheet
		for ( String title : formatData.getColumnTitles() ){
			if ( !sheetData.hasHeader(title) ){
				Logger.log("Warning: Found column header of "+title+" in the format"
						+" file but failed to find it in the input sheet. Maybe a typo?"
						+" I will not work on this format file column.");
				formatData.removeColumn(title);
			}
		}
		//Check all dependencies at least exist as columns
		Vector<String> columnTitles = formatData.getColumnTitles();
		for ( String title : columnTitles ){
			ColumnFormatData columnFormat = formatData.getColumnFormat(title);
			Set<String> deps = columnFormat.getDependencies();
			
			for ( String dep : deps ){
				if ( !sheetData.hasHeader(dep) ){
					Logger.log("Error", "Format data for column "+title+" depended on column "
							+dep+" but "+dep+" was not found in the input file");
				}
			}
		}
	}
	
	// ####################################################
	// ### Checking the input file
	// ####################################################
	
	/**
	 * The public method called to check and patch all of the rows in the
	 * excel sheet.
	 */
	public void patchAllLoans(){
		for ( int rowIndex : sheetData.getLoanRowIndexes() ){
			patchRow(rowIndex);
		}
	}
	
	/**
	 * Checks and patches an individual row.
	 * 
	 * @param rowIndex of row to check
	 */
	private void patchRow(int rowIndex){
		Logger.logVerbose("Working on row "+rowIndex);
		Row row = sheet.getRow(rowIndex);
		
		//Try to meet all dependency requirements for this row, and store if it works
		boolean goodDependencies = patchRowDependencies(row);
		
		//If the dependencies are met we can check and correct the remaining cells
		if ( goodDependencies ){
			Logger.logVerbose("Resolved dependencies on this row. Continuing");
			//Patch non-dependency cells
			patchRowIndependents(row);
			patchRowDependents(row);
		} else {
			//If dependencies are unmet, it is hard to check and correct
			// the other cells in the row so we quit
			Logger.logVerbose("Failed to resolve dependencies on row "+rowIndex
					+". Skipping this row");
			return;
		}
		
		//Runs one final check on this row, and tags cells with comments and colors
		checkAndTagAll(row);
	}
	
	/**
	 * For each cell in the row, if it is not filled correctly, tag it as an incorrect
	 * cell.
	 * 
	 * @param row to check and tag cells in
	 */
	private void checkAndTagAll(Row row){
		Vector<String> allHeaders = formatData.getColumnTitles();
		for ( String header : allHeaders ){
			int cellIndex = sheetData.getColumnIndex(header);
			Cell cell = row.getCell(cellIndex);
			if ( cell == null ){
				cell = row.createCell(cellIndex);
			}
			ColumnFormatData format = formatData.getColumnFormat(header);
			checkAndTag(cell, format, 1);
		}
	}
	
	/**
	 * Check if the given cell is valid under the given format data. If it is not,
	 * add comments for the errors and color it based on the urgency.
	 * 
	 * @param cell to check and tag
	 * @param format to use when checking the cell
	 * @param urgency of the check, used in coloring the cell
	 */
	private void checkAndTag(Cell cell, ColumnFormatData format, int urgency) {
		if ( !format.isRequired() ){
			return;
		}
		Vector<String> errors = checkCellFormat(cell, format);
		if ( errors.size() != 0 ){
			int rowNum = cell.getRow().getRowNum();
			String columnIndex = ExcelUtils.intToLetter(cell.getColumnIndex());
			Logger.log(columnIndex+(rowNum+1)+": "+format.getTitle()+": "+errors);
			addCellComment(cell, errors, UrgencyLevel.values()[urgency]);
		}
	}
	
	/**
	 * For the given row, attempt to patch all the dependencies that arise from
	 * the format data. Returns if all the dependency columns were correct or corrected.
	 * 
	 * @param row to resolve dependencies on
	 * @return if the dependencies are resolved
	 */
	private boolean patchRowDependencies(Row row){
		boolean completeSuccess = true;
		//Get all core dependencies
		Set<String> dependencies = formatData.getAllDependencies();
		//For each of these columns
		for ( String header : dependencies ){
			//Collect some useful variables
			int columnIndex = sheetData.getColumnIndex(header);
			Cell dependencyCell = row.getCell(columnIndex);
			if ( dependencyCell == null ){
				dependencyCell = row.createCell(columnIndex);
			}
			currentCell = dependencyCell;
			ColumnFormatData dependencyFormat = formatData.getColumnFormat(header);
			
			//Log what we are doing
			Logger.logVerbose("Working on dependency "+header+" at column "
					+ExcelUtils.intToLetter(columnIndex) );
			
			//Check it and get any error messages
			Vector<String> errors = checkCellFormat(dependencyCell, dependencyFormat);
			
			//If there are errors
			if ( errors.size() > 0 ){
				//try to fill
				boolean filled = fillCell(dependencyCell, dependencyFormat);
				if ( filled ){
					Vector<String> errors2 = checkCellFormat(dependencyCell, dependencyFormat);
					if ( errors2.size() > 0 ){ 
						Logger.logVerbose("Could not fix dependency. Autofill Errors: "+errors2);
						completeSuccess = false;
					} else {
						//filled with no errors
						Logger.logVerbose("Passed Check after autofill");
					}
				} else {
					//Here we failed to fill
					Logger.logVerbose("Could not fix dependency. Errors: "+errors);
					completeSuccess = false;
				}
			} else {
				Logger.logVerbose("Passed Check");
			}
		}
		if ( !completeSuccess ){
			Logger.log("Warning: Failed to resolve core dependencies on row "+row.getRowNum()
					+". Try filling the cells in these columns and running this again: ");
			for ( String header : dependencies ){
				//Collect some useful variables
				int columnIndex = sheetData.getColumnIndex(header);
				Cell dependencyCell = row.getCell(columnIndex);
				if ( dependencyCell == null ){
					dependencyCell = row.createCell(columnIndex);
				}
				currentCell = dependencyCell;
				ColumnFormatData dependencyFormat = formatData.getColumnFormat(header);
				
				checkAndTag(dependencyCell, dependencyFormat, 2);
			}
		}
		return completeSuccess;
	}
	
	/**
	 * Checks all independent cells in the row, and corrects any that are correctable.
	 * 
	 * @param row to check and patch independent cells in
	 */
	private void patchRowIndependents(Row row){
		//Get headers of non-dependency cells
		Set<String> dependencyHeaders = formatData.getAllDependencies();
		Vector<String> allHeaders = formatData.getColumnTitles();
		allHeaders.removeAll(dependencyHeaders);
		
		for ( String header : allHeaders ){
			//Collect some useful variables
			int columnIndex = sheetData.getColumnIndex(header);
			Cell cell = row.getCell(columnIndex);
			if ( cell == null ){
				cell = row.createCell(columnIndex);
			}
			currentCell = cell;
			ColumnFormatData format = formatData.getColumnFormat(header);
			
			//Skip all rows that are not independent
			if ( format.getDependencies().size() > 0 ){
				continue;
			}
			
			//Log what we are doing
			Logger.logVerbose("Working on independent "+header+" at column "
					+ExcelUtils.intToLetter(columnIndex) );
			
			//Do the actual checking
			//Check it and get any error messages
			Vector<String> errors = checkCellFormat(cell, format);
			
			//If there are errors
			if ( errors.size() > 0 ){
				//try to fill
				boolean filled = fillCell(cell, format);
				if ( filled ){
					Vector<String> errors2 = checkCellFormat(cell, format);
					if ( errors2.size() > 0 ){ 
						Logger.logVerbose("Could not fix dependency. Autofill Errors: "+errors2);
					} else {
						//filled with no errors
						Logger.logVerbose("Passed Check after autofill");
					}
				} else {
					//Here we failed to fill
					Logger.logVerbose("Could not fix dependency. Errors: "+errors);
				}
			} else {
				Logger.logVerbose("Passed Check");
			}
		}
	}
	
	/**
	 * Checks all dependent cells in the row, and corrects any that are correctable.
	 * 
	 * @param row to check and patch dependent cells in
	 */
	private void patchRowDependents(Row row){
		//Get headers of non-dependency cells
		Set<String> dependencyHeaders = formatData.getAllDependencies();
		Vector<String> allHeaders = formatData.getColumnTitles();
		allHeaders.removeAll(dependencyHeaders);
		
		for ( String header : allHeaders ){
			//Collect some useful variables
			int columnIndex = sheetData.getColumnIndex(header);
			Cell cell = row.getCell(columnIndex);
			if ( cell == null ){
				cell = row.createCell(columnIndex);
			}
			currentCell = cell;
			ColumnFormatData format = formatData.getColumnFormat(header);
			
			//Skip all rows that are not dependent
			if ( format.getDependencies().size() == 0 ){
				continue;
			}
			
			//Log what we are doing
			Logger.logVerbose("Working on dependent "+header+" at column "
					+ExcelUtils.intToLetter(columnIndex) );
			
			//Do the actual checking
			patchCell(cell, format);
		}
	}
	
	/**
	 * Checks and patches the specified cells using the given column format data.
	 * 
	 * @param cell to check
	 * @param format data to check with
	 */
	private void patchCell(Cell cell, ColumnFormatData format){
		// all dependency headers
		Set<String> dependencyHeaders = format.getDependencies();
		// for each dep
		for ( String dependencyHeader : dependencyHeaders ){
			// collect the dep cell and format
			int depCellIndex = sheetData.getColumnIndex(dependencyHeader);
			Cell depCell = cell.getRow().getCell(depCellIndex);
			if ( depCell == null ){
				depCell = cell.getRow().createCell(depCellIndex);
			}
			ColumnFormatData depFormat = formatData.getColumnFormat(dependencyHeader);
			//check it
			Vector<String> depErrors = checkCellFormat(depCell, depFormat);
			if ( depErrors.size() != 0 ){
				Logger.logVerbose("Working on dependent: "+dependencyHeader);
				patchCell( depCell, depFormat );
				depErrors = checkCellFormat(depCell, depFormat);
				if ( depErrors.size() != 0 ){
					//failed to resolve deps - should not happen
				}
			}
		}
		
		//Check it and get any error messages
		Vector<String> errors = checkCellFormat(cell, format);
		
		//If there are errors
		if ( errors.size() > 0 ){
			//try to fill
			boolean filled = fillCell(cell, format);
			if ( filled ){
				Vector<String> errors2 = checkCellFormat(cell, format);
				if ( errors2.size() > 0 ){ 
					Logger.logVerbose("Could not fix dependency. Autofill Errors: "+errors2);
				} else {
					//filled with no errors
					Logger.logVerbose("Passed Check after autofill");
				}
			} else {
				//Here we failed to fill
				Logger.logVerbose("Could not fix dependency. Errors: "+errors);
			}
		} else {
			Logger.logVerbose("Passed Check");
		}
	}
	
	/**
	 * Checks that the cell's format matches that specified in the column
	 * format data.
	 * 
	 * @param cell to check
	 * @param format data to check against
	 * @return a list of the errors
	 */
	private Vector<String> checkCellFormat(Cell cell, ColumnFormatData format){
		currentCell = cell;
		Vector<String> errors = new Vector<String>();
		
		//Check for dependency errors
		Vector<String> dependencyErrors = checkCellDependencies(cell, format);
		if ( dependencyErrors.size() > 0 ){
			return dependencyErrors;
		}
		/*
		if ( !format.isRequired() ){
			return errors;
		}
		*/
		//With all dependencies ok we can actually check the cell
		if ( !checkIsRequired(cell, format) ){
			errors.add("This cell is required and should not be blank");
		}
		
		if ( !format.isRequired() && deleteIfNotRequired ){
			return errors;
		}
		/*
		//TODO wtf
		if ( !format.isRequired() ){
			return errors;
		}
		*/
		if ( !checkValue(cell, format) ){
			errors.add("Has the wrong value filled in");
		}
		
		if ( !checkMaxCharacterCount(cell, format) ){
			String error = "Exceeds max character count of ";
			error += format.getMaxCharacterCount();
			error += " with value "+ExcelUtils.getCellContentsAsString(cell);
			errors.add(error);
		}
		if ( !checkDataType(cell, format) ){
			String error = "Should have the data type of ";
			error += format.getType().toString() + " but was a value of ";
			error += ExcelUtils.getCellContentsAsString(cell) + ".";
			errors.add(error);
			if ( format.getType().toString().equals("Enumerable") ){
				String error2 = "Should be one of the following strings: "+format.getType().getEnumValues();
				errors.add(error2);
			}
		}
		
		return errors;
	}
	
	/**
	 * Checks that the column format data's dependency cells for the given cell
	 * are met.
	 * 
	 * @param cell to check
	 * @param format data to check against
	 * @return a list of the unmet dependency cells
	 */
	private Vector<String> checkCellDependencies(Cell cell, ColumnFormatData format){
		Vector<String> errors = new Vector<String>();
		
		//If this cell has unresolved dependencies we can't properly check it
		Set<String> dependencyHeaders = format.getDependencies();
		//for each dependency
		for ( String dependencyHeader : dependencyHeaders ){
			//get the cell and format
			int dependencyCellIndex = sheetData.getColumnIndex(dependencyHeader);
			Cell dependencyCell = cell.getRow().getCell(dependencyCellIndex);
			ColumnFormatData dependencyFormat = formatData.getColumnFormat(dependencyHeader);
			//and check them
			Vector<String> dependencyErrors = checkCellFormat(dependencyCell, dependencyFormat);
			// and if there are errors
			if ( dependencyErrors.size() != 0 ){
				//We have unmet dependencies
				errors.add("Unmet dependency : "+dependencyHeader);
			}
		}
		return errors;
	}
	
	// ####################################################
	// ### 4 checkers for cell
	// ####################################################
	/**
	 * Checks that the format data's "Required" condition is met
	 * 
	 * @param cell to check
	 * @param format data to check against
	 * @return if the condition is met
	 */
	private boolean checkIsRequired(Cell cell, ColumnFormatData format){
		boolean isRequired = format.isRequired();
		if ( isRequired ){
			return !ExcelUtils.isCellEmpty(cell);
		} else {
			if ( deleteIfNotRequired && !ExcelUtils.isCellEmpty(cell) ){
				String oldValue = ExcelUtils.getCellContentsAsString(cell);
				cell.setCellValue("");
				addCellComment(cell, 
						"Deleted content since not required. Value was "+oldValue, 
						UrgencyLevel.MINOR);
			}
			return true;
		}
	}
	
	/**
	 * Checks that the format data's "MaxPossibleCharacters" condition is met
	 * 
	 * @param cell to check
	 * @param format data to check against
	 * @return if the condition is met
	 */
	private boolean checkMaxCharacterCount(Cell cell, ColumnFormatData format){
		int maxCharCount = format.getMaxCharacterCount();
		
		if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC 
				&& DateUtil.isCellDateFormatted(cell)) {
			return maxCharCount >= 10;
		}
		
		String contents = ExcelUtils.getCellContentsAsString(cell);
		if ( contents.equals("TRUE()") || contents.equals("FALSE()") ){
			contents = "true";
		}
		
		return contents.length() <= maxCharCount;
	}
	
	/**
	 * Checks that the format data's "Type" condition is met
	 * 
	 * @param cell to check
	 * @param format data to check against
	 * @return if the condition is met
	 */
	private boolean checkDataType(Cell cell, ColumnFormatData format){
		DataType dataType = format.getType();
		if ( dataType == null ){
			return true;
		} else if ( cell == null ){
			return false;
		} else {
			boolean goodType = dataType.checkCell(cell);
			if ( !goodType ){
				RichTextString newContent = dataType.fixDataType(cell);
				if ( newContent != null ){
					cell.setCellType(Cell.CELL_TYPE_BLANK);
					cell.setCellValue(newContent);
					goodType = dataType.checkCell(cell);
					addCellComment(cell, "Changed to try and fix data type.", 
							UrgencyLevel.MINOR);
				}
			}
			return goodType;
		}
	}
	
	/**
	 * Checks that the format data's "Value" condition is met
	 * 
	 * @param cell to check
	 * @param format data to check against
	 * @return if the condition is met
	 */
	private boolean checkValue(Cell cell, ColumnFormatData format){
		RichTextString desiredValue = format.getValue();
		if ( desiredValue == null ){
			return true;
		} else {
			String value = ExcelUtils.getCellContentsAsString(cell);
			String desired = desiredValue.toString();
			return value.equals(desired);
		}
	}
	
	/**
	 * Attempts to auto-fill the cell based on the given format data. Returns 
	 * if the auto-fill was successful.
	 * 
	 * @param cell to try to fill
	 * @param format data to use when trying to fill
	 * @return if the cell was filled successfully
	 */
	private boolean fillCell(Cell cell, ColumnFormatData format){
		assert( cell != null );
		RichTextString autofillValue = format.getValue();
		if ( autofillValue != null ){
			String comm = "Changed to fix a wrong value. Had value of "+ExcelUtils.getCellContentsAsString(cell);
			
			cell.setCellType(Cell.CELL_TYPE_BLANK);
			cell.setCellValue( autofillValue );
			addCellComment(cell, comm , UrgencyLevel.MINOR);
			return true;
		} else {
			return false;
		}
	}
	
	// ####################################################
	// ### public cell checker methods for dependencies
	// ####################################################
	/**
	 * Returns the cell in the column with the given title that is in the row of
	 * the currentCell. This method is a bit of a visibility hack, so that other classes
	 * can query the sheet. Ideally I will fix this for encapsulation purposes.
	 * 
	 * @param title of the column that the desired cell is in
	 * @return the cell in the column with the specified title and the current cell's row
	 */
	public Cell getColumnForCurrentCellRow(String title){
		if ( currentCell == null ){
			Logger.log("Warning: Attempted to query currentCell when it was null");
			return null;
		}
		int columnIndex =  sheetData.getColumnIndex(title);
		Row row = currentCell.getRow();
		Cell cell = row.getCell(columnIndex);
		return cell;
	}
	
	// ####################################################
	// ### private utility methods
	// ####################################################
	/**
	 * Wrapper method to call ExcelUtils' comment method if the settings permit it.
	 * 
	 * @param cell to comment on
	 * @param comment text to put in the comment
	 * @param urgency used in coloring the cell
	 */
	private void addCellComment(Cell cell, String comment, UrgencyLevel urgency){
		if ( colorFaultyCells ){
			ExcelUtils.setCellColor(cell, urgency);
		}
		if ( commentOnFaultyCells ){
			ExcelUtils.addCellComment(cell, comment);
		}
	}
	
	/**
	 * Wrapper method to call ExcelUtils' comment method if the settings permit it.
	 * 
	 * @param cell to comment on
	 * @param comments to add to the cell
	 * @param urgency used in coloring the cell
	 */
	private void addCellComment(Cell cell, Vector<String> comments, UrgencyLevel urgency){
		for ( String comment : comments ){
			addCellComment(cell, comment, urgency);
		}
	}
}
