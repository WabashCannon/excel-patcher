package format;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;

import utils.Logger;
import excel.ExcelUtils;

public class DataType {
	public static final List<String> DATA_TYPES = Arrays.asList(new String[]
			{"String", "Integer", "Boolean", "Date", "Decimal", "Enumerable"});
	
	private String dataTypeName = null;
	private List<String> enumVals;
	int totalLength = 0;
	int decimalLength = 0;
	
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
	
	private void readEnumerableInput(String text){
		enumVals = new ArrayList<String>();
		
		Pattern p = Pattern.compile("\"([^\"]*)\"");
		Matcher m = p.matcher(text);
		while (m.find()) {
			enumVals.add( m.group(1) );
		}
	}
	
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
				/*
				String str = cell.getCellFormula();
				if ( str.equals("TRUE()") || str.equals("FALSE()")){
					return dataTypeName.equals("Boolean");
				}
				*/
			default:
				break;
		}
		return false;
	}
	
	public RichTextString fixDataType(Cell cell){
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
	
	
	public String getEnumValues(){
		assert( enumVals != null );
		return enumVals.toString();
	}
	
	private boolean isValidNumber(double number, boolean isInt){
		if ( isInt && dataTypeName.equals("Integer") ){ return true; }
		if ( isInt && dataTypeName.equals("Decimal")){ return String.valueOf(number).length() <= totalLength; }
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
	
	private boolean isBooleanString(String text){
		return "true".equals(text) || "false".equals(text);
	}
	
	public static String cellTypeToString(int cellType){
		switch ( cellType ){
		case Cell.CELL_TYPE_BLANK:
			return "BLANK";
		case Cell.CELL_TYPE_BOOLEAN:
			return "Boolean";
		case Cell.CELL_TYPE_ERROR:
			return "ERROR";
		case Cell.CELL_TYPE_FORMULA:
			return "Formula";
		case Cell.CELL_TYPE_NUMERIC:
			return "Numeric";
		case Cell.CELL_TYPE_STRING:
			return "String";
		default:
			return "Index of "+cellType+" is not a valid cell type";
		}
	}
}








