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
import format.ColumnFormatData;
import format.DataType;
import format.DependencyTree;
import format.FormatData;

public class ExcelChecker {
	//Singleton
	public static ExcelChecker checker = null;
	
	//Current cell to query
	public Cell currentCell;
	
	//TMP: Settings
	public boolean deleteIfNotRequired = true;
	public boolean tryToFixFormatIssues = true;
	public boolean colorFaultyCells = true;
	public boolean commentOnFaultyCells = true;
	
	//Instance variables
	private Sheet sheet;
	private SheetData sheetData;
	private FormatData formatData;
	
	/**
	 * Constructs a new checker for the given workbook and format data
	 * @param wb
	 * @param data
	 */
	public ExcelChecker(Workbook wb, FormatData data){
		/*
		if ( ExcelChecker.checker != null ){
			Logger.logCrash("ExcelChecker is a singleton class, cannot instantiate twice");
		}
		*/
		ExcelChecker.checker = this;
		this.sheet = wb.getSheetAt(0);
		this.sheetData = new SheetData(sheet);
		this.formatData = data;
		
		checkFormatData();
		Logger.log("Format file was most likely valid");
	}
	
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
	 * Checks the format data to ensure it is (mostly) valid
	 */
	private void checkFormatData(){
		if ( sheetData.getNumHeaders() != formatData.getNumColumns() ){
			Logger.logWarning("Format file specified "+formatData.getNumColumns()
					+" columns to check, but "+sheetData.getNumHeaders()
					+" headers were found on the input sheet. I'll only "
					+"work on the columns from the format file.");
		}
		for ( String title : formatData.getColumnTitles() ){
			if ( !sheetData.hasHeader(title) ){
				Logger.logWarning("Found column header of "+title+" in the format"
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
					Logger.logCrash("Format data for column "+title+" depended on column "
							+dep+" but "+dep+" was not found in the input file");
				}
			}
		}
		
		//Check for cyclic deps by making tree
		for ( String title : columnTitles ){
			DependencyTree tree = new DependencyTree(title);
			ColumnFormatData columnFormat = formatData.getColumnFormat(title);
			Set<String> deps = columnFormat.getDependencies();
			/*
			for ( String dep : deps ){
				if ( !formatData.getColumnTitles().contains(dep) ){
					Logger.logCrash("The format file depends on columns that aren't "
							+"declared in it");
				}
			}
			*/
			addChildDependency(tree, title, deps);
			columnFormat.setFinalDependencies( tree.getLeaves() );
			//System.out.println(tree.getLeaves());
			//System.out.println(tree);
		}
		
		formatData.compileDependencies();
		//System.out.println( formatData.getAllDependencies() );
	}
	
	/**
	 * Used to recursively build dependency trees;
	 * @param tree
	 * @param parentName
	 * @param childrenNames
	 */
	private void addChildDependency(DependencyTree tree,
			String parentName, Set<String> childrenNames)
	{
		boolean addedChild = tree.addToChild(parentName, childrenNames);
		if ( !addedChild ){
			return;
		}
		for ( String childName : childrenNames ){
			ColumnFormatData colFormat = formatData.getColumnFormat(childName);
			if ( colFormat == null ){
				return;
			} else {
				addChildDependency(tree, childName, colFormat.getDependencies());
			}
		}
	}
	
	// ####################################################
	// ### Checking the input file
	// ####################################################
	
	public void patchAllLoans(){
		/*
		String column = "IsBalloonMortgage";
		int cellIndex = sheetData.getColumnIndex(column);
		ColumnFormatData format = formatData.getColumnFormat(column);
		Row row = sheet.getRow(1);
		Cell cell = row.getCell(cellIndex);
		if ( cell == null ) { cell = row.createCell(cellIndex); }
		
		Vector<String> errors = checkCellFormat(cell, format);
		System.out.println(errors);
		*/
		//tmp
		//patchRow(3);
		//patchRow(2);
		
		for ( int rowIndex : sheetData.getLoanRowIndexes() ){
			patchRow(rowIndex);
		}
		
	}
	
	private void patchRow(int rowIndex){
		Logger.logVerbose("Working on row "+rowIndex);
		Row row = sheet.getRow(rowIndex);
		
		boolean goodDependencies = patchRowDependencies(row);
		if ( goodDependencies ){
			Logger.logVerbose("Resolved dependencies on this row. Continuing");
			//Patch non-dependency cells
			patchRowIndependents(row);
			patchRowDependents(row);
		} else {
			Logger.logVerbose("Failed to resolve dependencies on row "+rowIndex
					+". Skipping this row");
			//checkAndTagAll(row);
			return;
		}
		
		checkAndTagAll(row);
	}
	
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
	
	private void checkAndTag(Cell cell, ColumnFormatData format, int urgency) {
		if ( !format.isRequired() ){
			return;
		}
		Vector<String> errors = checkCellFormat(cell, format);
		if ( errors.size() != 0 ){
			int rowNum = cell.getRow().getRowNum();
			String columnIndex = ExcelUtils.intToLetter(cell.getColumnIndex());
			Logger.log(columnIndex+(rowNum+1)+": "+format.getTitle()+": "+errors);
			addCellComment(cell, errors, urgency);
		}
	}
	
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
			Logger.logWarning("Failed to resolve core dependencies on row "+row.getRowNum()
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
			
			/*
			//Skip not required cells
			if ( !format.isRequired() ){
				continue;
			}
			*/
			
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
			
			/*
			//Skip not required cells
			if ( !format.isRequired() ){
				continue;
			}
			*/
			
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
	
	private boolean fillCell(Cell cell, ColumnFormatData format){
		assert( cell != null );
		RichTextString autofillValue = format.getValue();
		if ( autofillValue != null ){
			//System.out.println(autofillValue.toString());
			String comm = "Changed to fix a wrong value. Had value of "+ExcelUtils.getCellContentsAsString(cell);
			
			cell.setCellType(Cell.CELL_TYPE_BLANK);
			cell.setCellValue( autofillValue );
			addCellComment(cell, comm , 0);
			return true;
		} else {
			return false;
		}
	}
	
	// ####################################################
	// ### 5 checkers for cell
	// ####################################################
	private boolean checkIsRequired(Cell cell, ColumnFormatData format){
		boolean isRequired = format.isRequired();
		if ( isRequired ){
			return !ExcelUtils.isCellEmpty(cell);
		} else {
			if ( deleteIfNotRequired && !ExcelUtils.isCellEmpty(cell) ){
				String oldValue = ExcelUtils.getCellContentsAsString(cell);
				cell.setCellValue("");
				addCellComment(cell, "Deleted content since not required. Value was "+oldValue, 0);
			}
			return true;
		}
	}
	
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
	
	private boolean checkDataType(Cell cell, ColumnFormatData format){
		//System.out.println( format.getTitle() + " : " + ExcelUtils.getCellContentsAsString(cell));
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
					addCellComment(cell, "Changed to try and fix data type.", 0);
				}
			}
			return goodType;
		}
	}
	
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
	
	// ####################################################
	// ### public cell checker methods for dependencies
	// ####################################################
	public Cell getColumnForCurrentCellRow(String title){
		if ( currentCell == null ){
			Logger.logWarning("Attempted to query currentCell when it was null");
			return null;
		}
		int columnIndex =  sheetData.getColumnIndex(title);
		Row row = currentCell.getRow();
		Cell cell = row.getCell(columnIndex);
		if ( cell == null ){
			//System.out.println("Query returned null cell for column "+title);
		}
		return cell;
	}
	
	// ####################################################
	// ### private utility methods
	// ####################################################
	private void addCellComment(Cell cell, String comment, int urgency){
		if ( colorFaultyCells ){
			ExcelUtils.setCellColor(cell, urgency);
		}
		if ( commentOnFaultyCells ){
			ExcelUtils.addCellComment(cell, comment);
		}
	}
	
	private void addCellComment(Cell cell, Vector<String> comments, int urgency){
		for ( String comment : comments ){
			addCellComment(cell, comment, urgency);
		}
	}
}
