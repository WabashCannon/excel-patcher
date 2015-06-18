package excel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;

import utils.Logger;

/**
 * Static utility class for modifying POI Workbooks.
 * 
 * @author Ashton Dyer (WabashCannon)
 *
 */
public class ExcelUtils {
	/** Shifting index for converting column characters indexes to column numerical indexes*/
	private static final int ALPHABET_SHIFT_INDEX = (int) 'a' - 1;
	/** Possible levels of urgency used when commenting on cells */
	public enum UrgencyLevel { 
		MINOR, WARNING, CRITICAL, NONE 
	};
	/** Indexes used for coloring excel cells. 3=Green, 5=Yellow, 2=Red */
	private static final short[] COLOR_INDEXES = new short[]{3, 5, 2};
	//TODO: implement rbg colors for cross platform purposes (Libre doesn't seem to match Excel color indexes)
	
	/**
	 * Utility method to get a row if it exists, else create a new
	 * one and return that.
	 * 
	 * @param sheet to get the row from
	 * @param rowIndex for the desired row
	 * @return the Row object at the specified index
	 */
	public static Row getSafeRow(Sheet sheet, int rowIndex ){
		Row row;
		row = sheet.getRow(rowIndex);
		if ( row == null ){
			row = sheet.createRow(rowIndex);
		}
		
		return row;
	}
	
	/**
	 * Returns if the given cell is empty or if its contents are null
	 * 
	 * @param cell to check
	 * @return if the cell is empty or has null contents
	 */
	public static boolean isCellEmpty(Cell cell){
		String contents  = getCellContentsAsString(cell);
		return contents == null || contents.isEmpty();
	}
	
	/**
	 * Adds a cell comment and colors the cell based on the urgency level.
	 * 
	 * @param cell to comment on
	 * @param commentText the text the should be in the excel comment
	 * @param urgency level used for setting the cell color
	 */
	public static void addCellComment(Cell cell, String commentText, UrgencyLevel urgency){
		addCellComment(cell, commentText);
		setCellColor(cell, urgency);
	}
	
	/**
	 * Adds a cell comment containting the commentText to the specified cell. If
	 * the cell already has a comment, it simply appends the comment.
	 * 
	 * @param cell to comment on
	 * @param commentText the text the should be in the excel comment
	 */
	public static void addCellComment(Cell cell, String commentText){
		Comment comment = cell.getCellComment();
		if ( comment == null ){
			Drawing drawing = cell.getSheet().createDrawingPatriarch();
			
			// When the comment box is visible, have it show in a 1x3 space
			CreationHelper factory = cell.getSheet().getWorkbook().getCreationHelper();
		    ClientAnchor anchor = factory.createClientAnchor();
		    Row row = cell.getRow();
		    anchor.setCol1(cell.getColumnIndex());
		    anchor.setCol2(cell.getColumnIndex()+3);
		    anchor.setRow1(row.getRowNum());
		    anchor.setRow2(row.getRowNum()+6);
		    
		    comment = drawing.createCellComment(anchor);
		    comment.setString( new XSSFRichTextString( commentText ) );
		    cell.setCellComment(comment);
		} else {
			String formerComment = comment.getString().getString();
			formerComment += "\n"+commentText;
			comment.setString( new XSSFRichTextString( formerComment ) );
			cell.setCellComment( comment );
		}
	}
	
	/**
	 * Sets the foreground color of the cell based on the urgency level.
	 * UrgencyLevel.NONE yields white cell.
	 * 
	 * Defaults to 
	 * MINOR = GREEN, WARNING = YELLOW, CRITICAL = RED
	 * 
	 * @param cell to color
	 * @param urgency The urgency to color for
	 */
	public static void setCellColor(Cell cell, UrgencyLevel urgency){
		//Saftely get the cell's style
		CellStyle style = cell.getSheet().getWorkbook().createCellStyle();
		if ( cell.getCellStyle() != null ){
			style.cloneStyleFrom( cell.getCellStyle() );
		}
		
		//Set the color
		switch( urgency ){
			case NONE:
				style.setFillPattern(CellStyle.NO_FILL);
				style.setFillForegroundColor(IndexedColors.WHITE.getIndex());
				break;
			default:
				style.setFillForegroundColor(COLOR_INDEXES[urgency.ordinal()]);
			    style.setFillPattern(CellStyle.SOLID_FOREGROUND);
				break;
		}
		cell.setCellStyle(style);
	}
	
	/**
	 * Clears any comments and foreground coloring from the cell.
	 * @param cell to clean
	 */
	public static void cleanCell(Cell cell){
		//Clear the cell comment
		cell.setCellComment(null);
		//Give the cell a white background
		setCellColor(cell, UrgencyLevel.NONE);
	}
	
	/**
	 * Safely pulls the cell's contents, converts them to a string, 
	 * and returns the string.
	 * 
	 * @param cell to get contents from
	 * @return the cell's contents as a String
	 */
	public static String getCellContentsAsString(Cell cell){
		if ( cell == null ){return "";}
		String content = null;
		
		switch (cell.getCellType()) {
			case Cell.CELL_TYPE_STRING:
				content = cell.getRichStringCellValue().toString();
				break;
			case Cell.CELL_TYPE_NUMERIC:
				if (DateUtil.isCellDateFormatted(cell)) {
					content =  cell.getDateCellValue().toString();
				} else {
					content = String.valueOf(cell.getNumericCellValue());
				}
				break;
			case Cell.CELL_TYPE_BOOLEAN:
				content = String.valueOf(cell.getBooleanCellValue());
				break;
			case Cell.CELL_TYPE_FORMULA:
				content = cell.getCellFormula();
				break;
			default:
				break;
		}
		
		if ( content == null ){
			content = "";
		}
		return content;
	}
	
	/**
	 * Determines if the cell if "true" or "false" based on it's contents.
	 * <p>
	 * If the cell is not a boolean, it evaluates to if the cell is empty. 
	 * If it is boolean it simply returns the cell value.
	 * 
	 * @param cell to evaluate as boolean
	 * @return the cell's boolean evaluation
	 */
	public static boolean cellToBoolean(Cell cell){
		if ( cell == null || cell.getCellType()!=Cell.CELL_TYPE_BOOLEAN ){
			return !ExcelUtils.isCellEmpty(cell);
		} else {
			return cell.getBooleanCellValue();
		}
	}
	
	/**
	 * Returns the 0-indexed index for a String index like the ones used in Excel column
	 * headings
	 * 
	 * @param stringIndex to convert to an integer index
	 * @return the stringIndex's equivalent integer index
	 */
	public static int letterToInt(String stringIndex){
		stringIndex = stringIndex.toLowerCase();
		int result = 0;
		for ( int i = 0 ; i < stringIndex.length() ; i++ ){
			char ch = stringIndex.charAt(i);
			if ( !Character.isLetter(ch) ){
				Logger.log("Error", "Tried to convert a non-alphabetic string to an int");
			}
			int index = (int)ch - ALPHABET_SHIFT_INDEX;
			int power = stringIndex.length() - i - 1;
			result += index * Math.pow( 26.0, power );
		}
		return result;
	}
	
	/**
	 * Returns the 0-indexed String index correlating to the given integer index
	 * 
	 * @param index integer index to convert
	 * @return the 0-indexed String index correlating to the given integer index
	 */
	public static String intToLetter(int index){
		index+=1;
		String str = "";
		int r = index%26;
		do {
			str = iIntToLetter(r) + str;
			index /= 26;
			r = index%26;
		} while ( r != 0 );
		
		return str;
	}
	
	/**
	 * Internal conversion from int to letter 
	 * 
	 * @param letterIndex to convert to a letter
	 * @return String containing the letter that was indexed
	 */
	private static String iIntToLetter(int letterIndex){
		if ( letterIndex == 0 ){ letterIndex+=26; }
		String str = "" + (char)(letterIndex+ALPHABET_SHIFT_INDEX);
		return str.toUpperCase();
	}
}
