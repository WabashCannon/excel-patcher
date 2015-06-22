package patcher.format;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;

import patcher.ExcelUtils;
import utils.Logger;
import utils.Utils;

/**
 * This class is used for storing a data type and checking a cell's contents
 * to see if it matches that data type. 
 * 
 * @author Ashton Dyer (WabashCannon)
 *
 */
public class DataType {
	/** List of allowable data types */
	public static final List<String> DATA_TYPES = Arrays.asList(new String[]
			{"String", "Integer", "Boolean", "Date", "Decimal", "Enumerable"});
	/** This instance's data type */
	private String dataTypeName = null;
	/** If the data type is Enumerable, this list contains possible values */
	private List<String> enumVals;
	/** Total length for decimal data type */
	int totalLength = 0;
	/** Decimal length form decimal data type */
	int decimalLength = 0;
	
	/**
	 * Creates a new DataType object from the given format text
	 * 
	 * @param text to create this DataType from
	 */
	public DataType(String text){
		for ( String typeName : DATA_TYPES ){
			if ( text.startsWith(typeName) ){
				dataTypeName = typeName;
			}
		}
		if ( dataTypeName.equals("Enumerable")){
			readEnumerableInput(text);
		} else if ( dataTypeName.equals("Decimal") ){
			readDecimalInput(text);
		} else if ( dataTypeName == null ){
			Logger.log("Error", "When loading format file, invalid data type of "+text+" was declared");
		}
	}
	
	/**
	 * Loads the possible enumerable values from the input text. Should
	 * only be called when dataTypeName is "Enumerable"
	 * 
	 * @param text to read from
	 */
	private void readEnumerableInput(String text){
		enumVals = new ArrayList<String>();
		
		Pattern p = Pattern.compile("\"([^\"]*)\"");
		Matcher m = p.matcher(text);
		while (m.find()) {
			enumVals.add( m.group(1) );
		}
	}
	
	/**
	 * Loads the total length and decimal length from the input text. Should
	 * only be called when the dataTypeName is "Decimal"
	 * @param text to read from
	 */
	private void readDecimalInput(String text){
		String range = text.substring(dataTypeName.length());
		String[] splitRange = range.split(",");
		//Get first value in range
		String aString = splitRange[0].trim();
		if ( !aString.startsWith("(") ){
			Logger.log("Error", "Decimal range in format file must begin with \"(\"");
		}
		aString = aString.substring(1).trim();
		//safe cast aString to int
		try {
			totalLength = Integer.parseInt(aString);
		} catch (NumberFormatException e){
			Logger.log("Error", "Expected first value in Decimal range to be an integer but recieved \""+aString+"\"");
		}
		//Get second value in range
		String bString = splitRange[1].trim();
		if ( !bString.endsWith(")") ){
			Logger.log("Error", "Decimal range in format file must end with \")\"");
		}
		bString = bString.substring(0, bString.length()-1).trim();
		//Safe cast bString to int
		try {
			decimalLength = Integer.parseInt(bString);
		} catch (NumberFormatException e){
			Logger.log("Error", "Expected second value in Decimal range to be an integer but recieved \""+bString+"\"");
		}
	}
	
	/**
	 * Checks the data type of the current cell and returns if it is correct.
	 * 
	 * @param cell to check
	 * @return if the cell's data type is correct
	 */
	public boolean checkCell(Cell cell){
		switch( cell.getCellType() ){
			case Cell.CELL_TYPE_BOOLEAN:
				return false;
			case Cell.CELL_TYPE_STRING:
				String content = cell.getStringCellValue();
				if (dataTypeName.equals("Enumerable")) { 
					return enumVals.contains(cell.getStringCellValue());
				} else if ( isBooleanString(content) ){
					return dataTypeName.equals("Boolean");
				//} else if ( Utils.isNumber(content) ){
				//	return dataTypeName.equals("Decimal") || dataTypeName.equals("Integer");
				} else {
					return dataTypeName.equals("String");
				}
			case Cell.CELL_TYPE_NUMERIC:
				if (DateUtil.isCellDateFormatted(cell)) {
					return dataTypeName.equals("Date");
				}
				double cellValue = cell.getNumericCellValue();
				long cellValueL = (long)cell.getNumericCellValue();
				
				boolean isInt = (cellValue - cellValueL) == 0;
				return isValidNumber(cellValue, isInt);
			case Cell.CELL_TYPE_FORMULA:
				return false;
			default:
				break;
		}
		return false;
	}
	
	/**
	 * Attempts to fix the data type in the provided cell.
	 * 
	 * @param cell to fix the data type of
	 * @return the proper cell contents if fix was successful, otherwise null
	 */
	public RichTextString fixDataType(Cell cell){
		// If the cell is empty, don't fix it
		if ( ExcelUtils.isCellEmpty(cell) ){
			return null;
		}
		
		// Try to fix double cells
		if ( dataTypeName.equals("Decimal") && cell.getCellType() == Cell.CELL_TYPE_STRING ){
			String str = cell.getStringCellValue();
			try {
				double dbl = Double.parseDouble(str);
				cell.setCellType(Cell.CELL_TYPE_NUMERIC);
				cell.setCellValue(dbl);
			} catch ( NumberFormatException e ){
				//Do nothing
			}
		}
		
		if ( dataTypeName.equals("Decimal") && cell.getCellType() == Cell.CELL_TYPE_NUMERIC ){
			double dbl = cell.getNumericCellValue();
			String str = String.valueOf(dbl);
			String[] arr = str.split("\\.");
			if ( arr.length == 2 ){
				int totalLength = str.length();
				int decimalLength = arr[1].length();
				int totalCut = totalLength - this.totalLength;
				int decimalCut = decimalLength - this.decimalLength;
				int toCut = Math.max(totalCut, decimalCut);
				if ( toCut > 0 ){
					if ( toCut <= decimalLength ) {
						arr[1] = arr[1].substring(0, arr[1].length()-toCut);
						cell.setCellValue(Double.parseDouble(arr[0]+"."+arr[1]));
					} else if ( arr[0].length() <= this.totalLength ){
						cell.setCellValue(Double.parseDouble(arr[0]));
					}
				}
			}
		}
		
		// Try to fix int cells
		if ( dataTypeName.equals("Integer") && cell.getCellType() == Cell.CELL_TYPE_STRING ){
			String str = cell.getStringCellValue();
			try{
				int i = Integer.parseInt(str);
				cell.setCellType(Cell.CELL_TYPE_NUMERIC);
				cell.setCellValue(i);
			} catch ( NumberFormatException e ){
				try {
					double dbl = Double.parseDouble(str);
					cell.setCellType(Cell.CELL_TYPE_NUMERIC);
					cell.setCellValue( (int) dbl );
				} catch ( NumberFormatException e2 ){
					//Do nothing
				}
			}
		}
		
		// Try to fix boolean cells
		if ( dataTypeName.equals("Boolean") && cell.getCellType() == Cell.CELL_TYPE_BOOLEAN ){
			boolean bool = cell.getBooleanCellValue();
			return new XSSFRichTextString( String.valueOf(bool) );
		} else if ( dataTypeName.equals("Boolean") && cell.getCellType() == Cell.CELL_TYPE_FORMULA ){
			String formula = cell.getCellFormula();
			if ( formula.equals("=TRUE()") || formula.equals("TRUE()") ){
				return new XSSFRichTextString("true");
			} else if ( formula.equals("=FALSE()") || formula.equals("FALSE()") ){
				return new XSSFRichTextString("false");
			}
		} else if ( dataTypeName.equals("Boolean") && cell.getCellType() == Cell.CELL_TYPE_NUMERIC ){
			double number = cell.getNumericCellValue();
			if ( number == 1 ){
				return new XSSFRichTextString("true");
			} else if ( number == 0 ){
				return new XSSFRichTextString("false");
			}
		}
		
		//Try to fix enumerable cells
		if ( dataTypeName.equals("Enumerable") ){
			String content = ExcelUtils.getCellContentsAsString(cell);
			for ( String enumVal : enumVals ){
				if ( enumVal.toLowerCase().startsWith( content.toLowerCase() ) ||
						content.toLowerCase().startsWith( enumVal.toLowerCase() ) ){
					return new XSSFRichTextString(enumVal);
				}
			}
		}
		return null;
	}
	
	/**
	 * Checks if the number provided is the correct data type.
	 * 
	 * @param number to check
	 * @param isInt if the number is an integer
	 * @return 
	 */
	private boolean isValidNumber(double number, boolean isInt){
		if ( isInt && dataTypeName.equals("Integer") ){ return true; }
		if ( isInt && dataTypeName.equals("Decimal")){ return true; }//String.valueOf(number).length() <= totalLength; }
		if ( !isInt && dataTypeName.equals("Decimal") ){
			String asString = String.valueOf(number);
			String[] split = asString.split("\\.");
			if ( split.length != 2 ){
				Logger.log("Error", "How did splitting a decimal number yeild > 2 parts: "+asString+" -> "+Arrays.toString(split));
			}
			return asString.length() <= totalLength+1 && split[1].length() <=decimalLength;
		}
		return false;
	}
	
	/**
	 * Checks if a string either "true" or "false"
	 * @param text to check
	 * @return if the text is "true" or "false"
	 */
	private boolean isBooleanString(String text){
		return "true".equals(text) || "false".equals(text);
	}
	
	/**
	 * Returns the string representation of the list of enumerable values
	 * @return the string representation of the list of enumerable values
	 */
	public String getEnumValues(){
		assert( enumVals != null );
		return enumVals.toString();
	}
	
	/**
	 * Returns the name of this objects data type name
	 * @return the name of this objects data type name
	 */
	public String getTypeName(){
		return dataTypeName;
	}
	
	@Override
	public String toString(){
		String str = dataTypeName;
		if ( dataTypeName.equals("Decimal") ){
			str+="("+String.valueOf(totalLength)+",";
			str+=String.valueOf(decimalLength)+")";
		}
		if ( dataTypeName.equals("Enumerable") ){
			for ( String enumVal : enumVals ){
				str += " \""+enumVal+"\"";
			}
		}
		
		return str;
	}
}








