package format;

import java.util.Vector;

import org.apache.poi.ss.usermodel.Cell;

import excel.ExcelChecker;
import excel.ExcelUtils;

public class ConditionalExpressionData {
	private String value;
	private Vector<Condition> conditions = new Vector<Condition>();
	private Vector<String> logicals = new Vector<String>();
	
	public boolean isBoolean(){
		return ( value == null ) || KeywordChecker.isBoolean(value);
	}
	
	public boolean isTrue(){
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
	
	public Vector<String> getDependencies(){
		Vector<String> deps = new Vector<String>();
		for ( Condition cond : conditions ){
			deps.addAll( cond.getDependencies() );
		}
		
		return deps;
	}
	
	public void setValue(String str){
		value = str;
	}
	
	public String getValue(){
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
	
	public void addCondition(Condition cond){
		conditions.add(cond);
	}
	
	public void addLogical(String str){
		logicals.add(str);
	}
	
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
