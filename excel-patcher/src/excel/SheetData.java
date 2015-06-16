package excel;

import java.util.HashMap;
import java.util.Vector;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import utils.Logger;

public class SheetData {
	
	private Sheet sheet;
	private HashMap<String, Integer> headers = new HashMap<String, Integer>();
	private Vector<Integer> loanRowIndexes = new Vector<Integer>();
	
	public SheetData(Sheet sheet){
		this.sheet = sheet;
		
		//load necessary data
		loadHeaders();
		countLoans();
	}
	
	// ####################################################
	// ### Getters and Setters
	// ####################################################
	public boolean hasHeader(String text){
		return headers.containsKey(text);
	}
	
	public int getNumHeaders(){
		return headers.size();
	}
	
	public Vector<Integer> getLoanRowIndexes(){
		Vector<Integer> copy = new Vector<Integer>();
		for ( int i : loanRowIndexes ){
			copy.add(i);
		}
		return copy;
	}
	
	public int getColumnIndex(String header){
		if ( !headers.containsKey(header) ){
			Logger.log("Error", header+" is not a column header. Did you forget to "
					+"put quotes around a constant in the format file?");
		}
		assert( headers.containsKey(header) );
		return headers.get(header);
	}
	
	public String getHeader(int columnIndex){
		assert( headers.containsValue(columnIndex) );
		for ( String header : headers.keySet() ){
			if ( headers.get(header) == columnIndex ){
				return header;
			}
		}
		assert( false );
		return null;
	}
	// ####################################################
	// ### Private methods for loading
	// ####################################################
	/** Maximum number of consecutive blank rows the program can find before determining there are no more loans */
	private final int MAX_BLANK_COLUMNS = 10;
	/**
	 * Loads all of the headers it can find on the input sheet into headers
	 */
	private void loadHeaders(){
		// Assume headers are in row 0
		Row row = ExcelUtils.getSafeRow(sheet, 0);
		//And start at column 1 (not column 0)
		int columnCount = 0;
		int emptyColumnCount = 0 ;
		while ( emptyColumnCount < MAX_BLANK_COLUMNS ){
			Cell cell = row.getCell(columnCount);
			if ( cell == null ){
				emptyColumnCount++;
			} else {
				emptyColumnCount=0;
				String contents = cell.getStringCellValue();
				headers.put(contents, columnCount);
			}
			columnCount++;
		}
		Logger.logVerbose("Warning: Stopoped searching for headers after column "+ExcelUtils.intToLetter(columnCount));
	}
	
	/** Maximum number of consecutive blank rows the program can find before determining there are no more loans */
	private final int MAX_BLANK_ROWS = 10;
	/**
	 * Counts the number of loans on the input sheet
	 */
	private void countLoans(){
		int rowNum = 1;
		int emptyRowCount = 0;
		while ( emptyRowCount <= MAX_BLANK_ROWS ){
			Row row = sheet.getRow(rowNum);
			boolean rowIsEmpty = true;
			if ( row != null ){
				for ( int i : headers.values() ){
					Cell cell = row.getCell(i);
					boolean cellIsEmpty = ExcelUtils.isCellEmpty(cell);
					if ( !cellIsEmpty ){
						rowIsEmpty = false;
						break;
					}
				}
			}
			if ( rowIsEmpty ){
				emptyRowCount++;
			} else {
				for ( int i = 0 ; i < emptyRowCount ; i++ ){
					Logger.logVerbose("Warning: Found an empty row in the input file at row number "
							+(rowNum-emptyRowCount+i)+".");
				}
				emptyRowCount = 0;
				loanRowIndexes.add(rowNum);
			}
			rowNum++;
		}
		
		Logger.logVerbose("Warning: Stopped searching for loans after row "+(rowNum-2));
	}
}
