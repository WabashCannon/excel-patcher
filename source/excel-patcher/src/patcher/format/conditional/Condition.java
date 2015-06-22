package patcher.format.conditional;

import java.util.Vector;

import org.apache.poi.ss.usermodel.Cell;

import patcher.ExcelChecker;
import patcher.ExcelUtils;
import patcher.format.KeywordChecker;
import utils.Logger;

/**
 * This class is used for evaluating a String that contains a logical statement.
 * 
 * @author Ashton Dyer (WabashCannon)
 *
 */
public class Condition {
	/** Array containing the terms of the expression this condition
	 * was created from */
	private String[] originalExpressionArray;
	/** The string from which this condition was initialized */
	private String originalExpression = "";
	
	/** If the expression used to create this condition had only one term */
	private boolean isOneTerm = false;
	/** The excel column titles on which this condition depends */
	private Vector<String> dependencies = new Vector<String>();
	
	/**
	 * Creates a new condition from the given expression string. 
	 * 
	 * @param expression
	 */
	protected Condition(String expression){
		this(new String[]{expression});
	}
	
	/**
	 * Creates a new condition from the given expression string.
	 * @param expression
	 */
	protected Condition(String[] expression){
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
	
	/**
	 * Returns if the condition is true
	 * 
	 * @return if the condition is true
	 */
	protected boolean isTrue(){
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
	
	/**
	 * Returns a vector of the column titles on which this condition depends
	 * @return a vector of the column titles on which this condition depends
	 */
	protected Vector<String> getDependencies(){
		return dependencies;
	}
	
	/**
	 * Casts the provided string to it's boolean representation. If it is not
	 * synonymous with true/false, then an error is logged and false is returned.
	 * 
	 * @param text to convert
	 * @return the boolean cast of the string
	 */
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
	
	/**
	 * Converts the provided term to the string it represents. If the term
	 * is a constant, it strips the double-quotes. If it is a column title,
	 * it gets the contents of the cell.
	 * 
	 * @param term to convert
	 * @return the string that the term represents
	 */
	protected String convertTermToString(String term){
		if ( KeywordChecker.isConstant(term) ){
			return KeywordChecker.stripConstant(term);
		} else {
			Cell cell = ExcelChecker.checker.getColumnForCurrentCellRow(originalExpressionArray[0]);
			//Handle empty cell here TODO!!!!!!!!!!!!
			
			String tmp = ExcelUtils.getCellContentsAsString(cell);
			return tmp;
		}
	}
	
	@Override
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
}
