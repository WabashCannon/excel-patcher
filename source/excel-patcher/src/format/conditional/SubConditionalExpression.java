package format.conditional;

import java.util.Vector;

import org.apache.poi.ss.usermodel.Cell;

import excel.ExcelChecker;
import excel.ExcelUtils;
import format.KeywordChecker;

/**
 * This class contains a single conditional expression and is used for evaluating
 * it to attain it's value.
 * 
 * @author Ashton Dyer (WabashCannon)
 *
 */
public class SubConditionalExpression {
	/** value of the conditional expression when it evaluates to true */
	private String value = null;
	/** The conditions that comprise the logical part of the expression */
	private Vector<Condition> conditions = new Vector<Condition>();
	/** The logical comparators used to relate the conditions */
	private Vector<String> logicals = new Vector<String>();
	
	/**
	 * Returns if the value of this expression is a boolean
	 * @return if the value of this expression is a boolean
	 */
	protected boolean isBoolean(){
		return ( value == null ) || KeywordChecker.isBoolean(value);
	}
	
	/**
	 * Returns if the logical expression in this conditional expression
	 * evaluates to true.
	 * 
	 * @return if this conditional expression is true.
	 */
	protected boolean isTrue(){
		assert( logicals.size() == conditions.size()-1 );
		boolean isTrue = conditions.get(0).isTrue();
		
		for ( int condIndex = 1 ; condIndex < conditions.size() ; condIndex++ ){
			if ( KeywordChecker.isAnd( logicals.get(condIndex-1) ) ){
				isTrue = isTrue && conditions.get(condIndex).isTrue();
			} else if ( KeywordChecker.isOr( logicals.get(condIndex-1) ) ){
				isTrue = isTrue || conditions.get(condIndex).isTrue();
			} else {
				assert( false );
			}
		}
		return isTrue;
	}
	
	/**
	 * Returns a vector of the column titles on which this expression
	 * depends.
	 * @return  a vector of the column titles on which this expression
	 * depends.
	 */
	protected Vector<String> getDependencies(){
		Vector<String> deps = new Vector<String>();
		// Add dependencies due to conditions
		for ( Condition cond : conditions ){
			deps.addAll( cond.getDependencies() );
		}
		//Add dependencies for value
		if ( value != null && !KeywordChecker.isBoolean(value) &&
				!KeywordChecker.isConstant(value) ){
			deps.add(value);
		}
		
		return deps;
	}
	
	/**
	 * Sets the value of this conditional expression
	 * @param str the new value of this conditional expression
	 */
	protected void setValue(String str){
		value = str;
	}
	
	/**
	 * Evaluates this expression and returns value when true, and "false" when
	 * false.
	 * @return value when the expression is true, and "false" when false.
	 */
	protected String getValue(){
		if ( isBoolean() ){
			if ( value != null ){
				if ( KeywordChecker.isTrue( value ) ){
					return String.valueOf(isTrue());
				} else {
					return String.valueOf(!isTrue());
				}
			} else {
				return String.valueOf(isTrue());
			}
		} else if ( KeywordChecker.isConstant(value) ){
			return KeywordChecker.stripConstant(value);
		} else {
			Cell cell = ExcelChecker.checker.getColumnForCurrentCellRow(value);
			String tmp = ExcelUtils.getCellContentsAsString(cell);
			return tmp;
		}
		//return isBoolean() ? String.valueOf(isTrue()) : value;
	}
	
	/**
	 * Appends a condition to the conditions list
	 * 
	 * @param condition to add to the list
	 */
	protected void addCondition(Condition condition){
		conditions.add(condition);
	}
	
	/**
	 * Appends a logical operator to the logicals list
	 * @param str logical operator to add
	 */
	protected void addLogical(String str){
		logicals.add(str);
	}
	
	@Override
	public String toString(){
		String str = "";
		str += value;
		str += " : ";
		for ( int i = 0 ; i < conditions.size() ; i++ ){
			str += "["+conditions.get(i).toString()+"]";
			if ( i < logicals.size() ){
				str += " "+logicals.get(i)+" ";
			}
		}
		return str;
	}
}
