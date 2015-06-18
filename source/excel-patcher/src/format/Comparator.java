package format;

import utils.Logger;

public class Comparator {
	public static boolean evaluate(String term1, String term2, String comparator){
		//assert( term1 != null && term2 != null && comparator != null);
		String expressionString = term1+" "+comparator+" "+term2;
		if ( !KeywordChecker.isComparator(comparator) ){
			Logger.log("Error", "Expected a comparator but recieved "+comparator);
		}
		if ( ( term1.isEmpty() || term2.isEmpty() )
				&& ( KeywordChecker.isEqual(comparator) || KeywordChecker.isNot(comparator) ) ){
			return compareWithEmpty(term1, term2, comparator);
		}
		boolean oneIsNumber = isNumber(term1);
		boolean twoIsNumber = isNumber(term2);
		if ( oneIsNumber != twoIsNumber ){
			Logger.log("Error", "Comparator cannot compare numerical and non-numerical types: "+expressionString);
		}
		if ( oneIsNumber ){
			return compareNumerics( Double.parseDouble(term1) , Double.parseDouble(term2), comparator);
		} else {
			return compareStrings(term1, term2, comparator);
		}
	}
	
	private static boolean compareStrings(String term1, String term2, String comparator) {
		if ( KeywordChecker.isEqual(comparator) ){
			return term1.equals(term2);
		} else if ( KeywordChecker.isNot(comparator) ){
			return !term1.equals(term2);
		} else {
			Logger.log("Error", "Comparator "+comparator+" is invalid for non-numeric types");
		}
		return false;
	}

	private static boolean compareNumerics(double a, double b, String comparator){
		if ( KeywordChecker.isEqual(comparator) ){
			return a == b;
		} else if (KeywordChecker.isGreater(comparator) ){
			return a > b;
		} else if (KeywordChecker.isLess(comparator) ){
			return a < b;
		} else if (KeywordChecker.isGreaterEqual(comparator) ){
			return a >= b;
		} else if (KeywordChecker.isLessEqual(comparator) ){
			return a <= b;
		} else if (KeywordChecker.isNot(comparator) ){
			return a != b;
		} else {
			Logger.log("Error", "Comparator "+comparator+" is invalid for numeric types.");
		}
		return false;
	}
	
	private static boolean isNumber(String text){
		try {
			Double.parseDouble(text);
		} catch (NumberFormatException e){
			return false;
		}
		return true;
	}
	
	private static boolean compareWithEmpty(String a, String b, String comparator ){
		boolean onlyOneEmpty = ( a.isEmpty() && !b.isEmpty() )
				|| ( !a.isEmpty() && b.isEmpty() );
		if ( KeywordChecker.isEqual(comparator) ){
			return !onlyOneEmpty;
		} else {
			return onlyOneEmpty;
		}
	}
}
