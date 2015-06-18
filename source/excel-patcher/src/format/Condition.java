package format;

import java.util.Vector;

import org.apache.poi.ss.usermodel.Cell;

import utils.Logger;
import excel.ExcelChecker;
import excel.ExcelUtils;

public class Condition {
	
	String[] originalExpressionArray;
	String originalExpression = "";
	
	
	private boolean isOneTerm = false;
	private Vector<String> dependencies = new Vector<String>();
	
	public Condition(String expression){
		this(new String[]{expression});
	}
	
	public Condition(String[] expression){
		//System.out.println( Arrays.toString( expression ) );
		assert( expression.length == 1 || expression.length == 3 );
		
		originalExpressionArray = expression;
		for ( String word : expression ){
			originalExpression += word+" ";
		}
		originalExpression = originalExpression.trim();
		
		if ( expression.length == 1 ){
			isOneTerm = true;
			//if it is a one word boolean keyword
			if ( KeywordChecker.isBoolean(expression[0] ) ){
				stringToBoolean(expression[0]);
			//Check other one word cases that fail
			} else if ( KeywordChecker.isConstant(expression[0]) ){
				Logger.log("Error", "Condition statement cannot be only a constant value. Maybe you meant to leave off the quotes?");
			} else if ( KeywordChecker.isKeyword(expression[0]) ){
				Logger.log("Error", "Conditions statement cannot be only a keyword.");
			} else {
				//Assume it is a column name
				dependencies.add(expression[0]);
			}
		//it is a multi-word expression
		} else {
			if ( !KeywordChecker.isKeyword(expression[0])  
					&& !KeywordChecker.isConstant(expression[0])){
				dependencies.add(expression[0]);
			}
			if ( !KeywordChecker.isComparator(expression[1]) ){
				Logger.log("Error", "Expected a comparator as second word in condition"
						+" but recieved "+expression[1]);
			}
			if ( !KeywordChecker.isKeyword(expression[2])  
					&& !KeywordChecker.isConstant(expression[2]) ){
				dependencies.add(expression[2]);
			}
		}
	}
	
	public boolean isTrue(){
		if ( isOneTerm ){
			if ( dependencies.size() == 0 ){
				return stringToBoolean(originalExpressionArray[0]);
			} else {
				Cell cell = ExcelChecker.checker.getColumnForCurrentCellRow(originalExpressionArray[0]);
				return ExcelUtils.cellToBoolean(cell);
			}
		} else {
			String term1 = convertTermToString(originalExpressionArray[0]);
			String term2 = convertTermToString(originalExpressionArray[2]);
			return Comparator.evaluate(term1, term2, originalExpressionArray[1]);
		}
	}
	
	public String toString(){
		if ( isOneTerm ){
			if ( dependencies.size() == 0 ){
				return originalExpressionArray[0];
			} else {
				Cell cell = ExcelChecker.checker.getColumnForCurrentCellRow(originalExpressionArray[0]);
				return String.valueOf(ExcelUtils.cellToBoolean(cell));
			}
		} else {
			String term1 = originalExpressionArray[0];// convertTermToString(originalExpressionArray[0]);
			String term2 = originalExpressionArray[2];//convertTermToString(originalExpressionArray[2]);
			return term1+" "+originalExpressionArray[1]+" "+term2;
		}
	}
	
	public Vector<String> getDependencies(){
		return dependencies;
	}
	
	private boolean stringToBoolean(String text){
		if ( KeywordChecker.isTrue(text) ){
			return true;
		} else if ( KeywordChecker.isFalse(text) ){
			return false;
		} else {
			Logger.log("Error", "Expected either a boolean value or dependency for"
					+" a single word conditional expression, but recieved a keyword: "+text+" in "+originalExpression);
			return false;
		}
	}
	
	private String convertTermToString(String term){
		if ( KeywordChecker.isConstant(term) ){
			return KeywordChecker.stripConstant(term);
		} else {
			Cell cell = ExcelChecker.checker.getColumnForCurrentCellRow(originalExpressionArray[0]);
			//Handle empty cell here TODO!!!!!!!!!!!!
			
			String tmp = ExcelUtils.getCellContentsAsString(cell);
			return tmp;
		}
	}
}
