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

public class ExcelUtils {
	private static final int ALPHABET_SHIFT_INDEX = (int) 'a' - 1;
	/**
	 * Utility method to get a row if it exists, else create a new
	 * one and return that
	 * @param sheet
	 * @param rowIndex
	 * @return
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
	 * Returns if the given cell is empty
	 * Checked by cell.getStringCellValue().isEmpty();
	 * @param cell
	 * @return
	 */
	public static boolean isCellEmpty(Cell cell){
		String contents  = getCellContentsAsString(cell);
		return contents == null || contents.isEmpty();
		/*
		if ( cell == null ){return true;}
		
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_STRING:
                return cell.getRichStringCellValue().getString().isEmpty();
            case Cell.CELL_TYPE_NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                	return cell.getDateCellValue().toString().isEmpty();
                } else {
                	return String.valueOf(cell.getNumericCellValue()).isEmpty();
                }
            case Cell.CELL_TYPE_BOOLEAN:
            	return String.valueOf(cell.getBooleanCellValue()).isEmpty();
            case Cell.CELL_TYPE_FORMULA:
            	return cell.getCellFormula().isEmpty();
            default:
                return true;
        }
        */
	}
	
	public static void addCellComment(Cell cell, String commentText, int urgency){
		addCellComment(cell, commentText);
		setCellColor(cell, urgency);
	}
	
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
	
	// 3, 5, 2
	private static final short[] COLOR_INDEXES = new short[]{3, 5, 2};
			/*
			{IndexedColors.GREEN.getIndex(), IndexedColors.YELLOW.getIndex(),
		IndexedColors.RED.getIndex()};
		*/
	public static void setCellColor(Cell cell, int colorIndex){
		assert( colorIndex >= -1 && colorIndex < COLOR_INDEXES.length);
		
		CellStyle style = cell.getSheet().getWorkbook().createCellStyle();
		if ( cell.getCellStyle() != null ){
			style.cloneStyleFrom( cell.getCellStyle() );
		}
		
		if ( colorIndex == -1 ){
			style.setFillPattern(CellStyle.NO_FILL);
			style.setFillForegroundColor(IndexedColors.WHITE.getIndex());
		} else {
			style.setFillForegroundColor(COLOR_INDEXES[colorIndex]);
		    style.setFillPattern(CellStyle.SOLID_FOREGROUND);
		}
	    cell.setCellStyle(style);
	}
	
	public static void cleanCell(Cell cell){
		cell.setCellComment(null);
		setCellColor(cell, -1);
		
	}
	
	public static Object getCellContents(Cell cell){
		if ( cell == null ){return null;}
		
		switch (cell.getCellType()) {
			case Cell.CELL_TYPE_STRING:
				return cell.getRichStringCellValue();
			case Cell.CELL_TYPE_NUMERIC:
				if (DateUtil.isCellDateFormatted(cell)) {
					return cell.getDateCellValue();
				} else {
					return cell.getNumericCellValue();
				}
			case Cell.CELL_TYPE_BOOLEAN:
				return cell.getBooleanCellValue();
			case Cell.CELL_TYPE_FORMULA:
				return cell.getCellFormula();
			default:
				return null;
		}
	}
	
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
	
	public static boolean cellToBoolean(Cell cell){
		if ( cell == null || cell.getCellType()!=Cell.CELL_TYPE_BOOLEAN ){
			return !ExcelUtils.isCellEmpty(cell);
		} else {
			return cell.getBooleanCellValue();
		}
	}
	
	/**
	 * returns the 0-indexed index for an excel column heading
	 * @param letter
	 * @return
	 */
	public static int letterToInt(String letter){
		letter = letter.toLowerCase();
		int result = 0;
		for ( int i = 0 ; i < letter.length() ; i++ ){
			char ch = letter.charAt(i);
			if ( !Character.isLetter(ch) ){
				Logger.log("Error", "Tried to convert a non-alphabetic string to an int");
			}
			int index = (int)ch - ALPHABET_SHIFT_INDEX;
			int power = letter.length() - i - 1;
			result += index * Math.pow( 26.0, power );
		}
		return result;
	}
	
	/**
	 * returns the 0-indexed excel column heading for an int
	 * @param i
	 * @return
	 */
	public static String intToLetter(int i){
		i+=1;
		String str = "";
		int r = i%26;
		do {
			str = iIntToLetter(r) + str;
			i /= 26;
			r = i%26;
		} while ( r != 0 );
		
		return str;
	}
	
	private static String iIntToLetter(int i){
		if ( i == 0 ){ i+=26; }
		String str = "" + (char)(i+ALPHABET_SHIFT_INDEX);
		return str.toUpperCase();
	}
}
