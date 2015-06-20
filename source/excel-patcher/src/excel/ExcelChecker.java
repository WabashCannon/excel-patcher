package excel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import utils.Logger;
import utils.Utils;
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
	
	// State data used while checking
	/** Current row to query */
	private Row currentRow;
	/** Current cell to query. */
	private Cell currentCell;
	
	/** Enum for indexing the status of dependency resolution for
	 * a column */
	public enum ResolvedStatus{ RESOLVED, UNRESOLVED, NOT_CHECKED };
	
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
		Set<String> columnTitles = formatData.getColumnTitles();
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
		currentRow = sheet.getRow(rowIndex);
		
		// Create a map to store the status of each column in the row
		Set<String> columnNames = formatData.getColumnTitles();
		Map<String, ResolvedStatus> columnStatuses = 
				new HashMap<String, ResolvedStatus>();
		for ( String columnName : columnNames ){
			columnStatuses.put(columnName, ResolvedStatus.NOT_CHECKED);
		}
		LinkedList<String> parentStack = new LinkedList<String>();
		
		//Check all the cells, since names to check is all column names
		checkCellsWithNames(columnNames, columnStatuses, parentStack);
		
		//TMP stuff
		Logger.log("");
		Logger.log("Row "+rowIndex);
		Logger.log(columnStatuses.toString());
	}
	
	/**
	 * Checks all of the cells in the column's with titles in namesToCheck.
	 * Returns if all of the cells to check had successfully met their
	 * dependencies.
	 * 
	 * @param namesToCheck set of names to check
	 * @param allStatuses the map of names to resolved statuses for all columns
	 * @param parentStack stack of names of parents traversed
	 * @return a set of the names of unresolved columns
	 */
	private Set<String> checkCellsWithNames(Set<String> namesToCheck,
			Map<String, ResolvedStatus> allStatuses, 
			LinkedList<String> parentStack) {
		//Create set to track unresolved columns
		Set<String> unresolvedNames = new HashSet<String>();
		
		// Tracks if all names in namesToCheck have been checked
		boolean checkedAll = false;
		
		while ( !checkedAll ){
			//find next name to check
			String nameToCheck = getNextName(namesToCheck, allStatuses);
			
			//Check the name if one was found
			if ( nameToCheck != null ){
				//First check that the cell is not a parent of itself
				// otherwise, it is circularly dependent
				if ( parentStack.contains(nameToCheck) ){
					//Mark cell as unresolvable and continue
					allStatuses.remove(nameToCheck);
					allStatuses.put(nameToCheck, ResolvedStatus.UNRESOLVED);
					
					unresolvedNames.add(nameToCheck);
					continue;
				}
				
				//Check children if they exists
				Set<String> childrenNames = formatData.getColumnFormat(nameToCheck).getDependencies();
				if ( !childrenNames.isEmpty() ){
					parentStack.push(nameToCheck);
					Set<String> unresolvedChildren = 
							checkCellsWithNames(childrenNames, allStatuses, parentStack);
					parentStack.pop();
					//If the children did not resolve, the parent won't
					//so we should abort
					if ( unresolvedChildren.size() != 0 ){
						//Modify cell status
						allStatuses.remove(nameToCheck);
						allStatuses.put(nameToCheck, ResolvedStatus.UNRESOLVED);
						
						//Comment on the cells
						addCellComment(nameToCheck, 
								"Could not check cell because it depends on poorly filled cells. This cell depended on cells "+Utils.nicePrint(unresolvedChildren),
								UrgencyLevel.WARNING);
						for ( String childName : unresolvedChildren ){
							addCellComment(childName, "", UrgencyLevel.CRITICAL);
							addCellComment(childName, "--- Critical Errors", UrgencyLevel.CRITICAL);
							addCellComment(childName, "Cell "+nameToCheck+" needs this cell to be nicely filled before it can be checked", UrgencyLevel.CRITICAL);
						}
						
						//Add to unresolved list and continue
						unresolvedNames.add(nameToCheck);
						continue;
					}
				}
				
				//Now check the cell itself
				boolean resolved = patchCell(nameToCheck);
				
				//And update the results of the check
				allStatuses.remove(nameToCheck);
				if ( resolved ){
					allStatuses.put(nameToCheck, ResolvedStatus.RESOLVED);
				} else {
					allStatuses.put(nameToCheck, ResolvedStatus.UNRESOLVED);
					unresolvedNames.add(nameToCheck);
				}
				
			} else { //nameToCheck == null
				checkedAll = true;
			}
		}
		
		return unresolvedNames;
		
	}
	
	/**
	 * Returns a name in the list of namesToCheck if allStatuses has
	 * that name's ResolvedStatus as NOT_CHECKED. null is returned
	 * if no such name is found.
	 * 
	 * @param namesToCheck
	 * @param allStatuses
	 * @return a name in namesToCheck that has a resolved status of
	 * NOT_CHECKED. null if none is found
	 */
	private String getNextName(Set<String> namesToCheck, 
			Map<String, ResolvedStatus> allStatuses){
		String nameToCheck = null;
		for ( String name : namesToCheck ){
			if ( allStatuses.get(name) == ResolvedStatus.NOT_CHECKED ){
				nameToCheck = name;
				break;
			}
		}
		return nameToCheck;
	}
	
	
	private boolean patchCell(String cellName){
		int columnIndex = sheetData.getColumnIndex(cellName);
		Cell cell = ExcelUtils.getSafeCell(currentRow, columnIndex);
		
		ColumnFormatData data = formatData.getColumnFormat(cellName);
		return patchCell(cell, data);
	}
	
	/**
	 * Checks and patches the specified cells using the given column format data.
	 * 
	 * @param cell to check
	 * @param format data to check with
	 */
	private boolean patchCell(Cell cell, ColumnFormatData format){
		//Store the current cell
		currentCell = cell;
		
		//Check it and get any error messages
		Vector<String> errors = checkCellFormat(cell, format);
		
		//If there are no errors
		if ( errors.size() == 0 ){
			return true;
		} else {
			fillCell(cell, format);
			errors = checkCellFormat(cell, format);
			
			if ( errors.size() > 0 ){
				//Comment on a cell that had errors and was not fillable
				addCellComment(cell, "--- Format Errors", UrgencyLevel.WARNING);
				addCellComments(cell, errors, UrgencyLevel.WARNING);
				return false;
			}
			return true;
		}
		/*
		//If there are no errors
		if ( errors.size() == 0 ){
			return true;
		
			
		} else { //There were initially errors
			
			//try to fill
			boolean filled = fillCell(cell, format);
			
			if ( filled ){ //If it was fillable
				
				Vector<String> errors2 = checkCellFormat(cell, format);
				
				//There were no errors after fill
				if ( errors2.size() == 0 ){
					return true;
					
				} else { //there were errors after fill
					return false;
				}
				
			} else { //Cell could not be filled
				//Comment on a cell that had errors and was not fillable
				addCellComments(cell, errors, UrgencyLevel.WARNING);
				return false;
			}
		}
		*/
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
		Vector<String> errors = new Vector<String>();
		
		if ( !checkIsRequired(cell, format) ){
			errors.add("This cell is required and should not be blank.");
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
			errors.add("Has the wrong value filled in.");
		}
		
		if ( !checkMaxCharacterCount(cell, format) ){
			String error = "Exceeds max character count of ";
			error += format.getMaxCharacterCount();
			error += " with value "+ExcelUtils.getCellContentsAsString(cell)+".";
			errors.add(error);
		}
		if ( !checkDataType(cell, format) ){
			String error = "Should have the data type of ";
			error += format.getType().toString() + " but was a value of \"";
			error += ExcelUtils.getCellContentsAsString(cell) + "\".";
			errors.add(error);
			if ( format.getType().toString().equals("Enumerable") ){
				String error2 = "Should be one of the following strings: "+format.getType().getEnumValues();
				errors.add(error2);
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
		//Get the max character count
		int maxCharCount = format.getMaxCharacterCount();
		
		// Need special handling for dates
		if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC 
				&& DateUtil.isCellDateFormatted(cell)) {
			return maxCharCount >= 10;
		}
		
		//For "formula" boolean contents, we want the length of "false"
		// or "true" not "TRUE()" or "FALSE()". Ignores difference in 4 and 5
		// characters.
		String contents = ExcelUtils.getCellContentsAsString(cell);
		if ( contents.equals("TRUE()") || contents.equals("FALSE()") ){
			return maxCharCount > 5;
		}
		
		//If the above don't apply, just check the contents
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
			//Comment on the change
			String comm = "Changed to fix a wrong value. Had value of \"" 
					+ ExcelUtils.getCellContentsAsString(cell) + "\".";
			addCellComment(cell, comm , UrgencyLevel.MINOR);
			
			//Clear the cell and set the new value
			cell.setCellType(Cell.CELL_TYPE_BLANK);
			cell.setCellValue( autofillValue );
			
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
	private void addCellComment(String cellName, String comment, 
			UrgencyLevel urgency){
		int cellIndex = sheetData.getColumnIndex(cellName);
		Cell cell = ExcelUtils.getSafeCell(currentRow, cellIndex);
		addCellComment(cell, comment, urgency);
	}
	
	/**
	 * Wrapper method to call ExcelUtils' comment method if the settings permit 
	 * it.
	 * 
	 * @param cell to comment on
	 * @param comments to put in the comment
	 * @param urgency used in coloring the cell
	 */
	private void addCellComments(Cell cell, Vector<String> comments, 
			UrgencyLevel urgency){
		for ( String comment : comments ){
			addCellComment(cell, comment, urgency);
		}
	}
	
	/**
	 * Wrapper method to call ExcelUtils' comment method if the settings permit 
	 * it.
	 * 
	 * @param cell to comment on
	 * @param comment text to put in the comment
	 * @param urgency used in coloring the cell
	 */
	private void addCellComment(Cell cell, String comment, 
			UrgencyLevel urgency){
		if ( colorFaultyCells ){
			ExcelUtils.setCellColor(cell, urgency);
		}
		if ( commentOnFaultyCells ){
			ExcelUtils.addCellComment(cell, comment);
		}
	}
	
	/**
	 * Wrapper method to call ExcelUtils' comment method if the settings permit 
	 * it.
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
