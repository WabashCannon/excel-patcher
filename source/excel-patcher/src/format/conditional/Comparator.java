package format.conditional;

import format.KeywordChecker;
import utils.Logger;
import utils.Utils;

/**
 * This class is a static utility class for carrying out comparisons. It mostly
 * acts as a translator from Strings which contain boolean statements to the
 * boolean evaluation of that statement.
 * 
 * @author Ashton Dyer (WabashCannon)
 *
 */
public class Comparator {
	/**
	 * private constructor used to suppress instantiation of a static
	 * utility class
	 */
	private Comparator(){}
	
	/**
	 * Binary comparison of term1 and term2 using the comparator given.
	 * 
	 * @param term1
	 * @param term2
	 * @param comparator
	 * @return
	 */
	protected static boolean evaluate(String term1, String term2, String comparator){
		//assert( term1 != null && term2 != null && comparator != null);
		String expressionString = term1+" "+comparator+" "+term2;
		if ( !KeywordChecker.isComparator(comparator) ){
			Logger.log("Error", "Expected a comparator but recieved "+comparator);
		}
		if ( ( term1.isEmpty() || term2.isEmpty() )
				&& ( KeywordChecker.isEqual(comparator) || KeywordChecker.isNot(comparator) ) ){
			return compareWithEmpty(term1, term2, comparator);
		}
		boolean oneIsNumber = Utils.isNumber(term1);
		boolean twoIsNumber = Utils.isNumber(term2);
		if ( oneIsNumber != twoIsNumber ){
			Logger.log("Error", "Comparator cannot compare numerical and non-numerical types: "+expressionString);
		}
		if ( oneIsNumber ){
			return compareNumerics( Double.parseDouble(term1) , Double.parseDouble(term2), comparator);
		} else {
			return compareStrings(term1, term2, comparator);
		}
	}
	
	/**
	 * Returns the result of a comparison between two Strings
	 * 
	 * @param term1 first term to compare
	 * @param term2 second term to compare
	 * @param comparator to use in comparison
	 * @return boolean evaluation of the comparison
	 */
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
	
	/**
	 * Returns the result of a numerical comparison between two doubles
	 * using the provided comparator.
	 * 
	 * @param d1 First number to compare
	 * @param d2 Second number to compare
	 * @param comparator to use in comparison
	 * @return boolean evaluation of the comparison
	 */
	private static boolean compareNumerics(double d1, double d2, String comparator){
		if ( KeywordChecker.isEqual(comparator) ){
			return d1 == d2;
		} else if (KeywordChecker.isGreater(comparator) ){
			return d1 > d2;
		} else if (KeywordChecker.isLess(comparator) ){
			return d1 < d2;
		} else if (KeywordChecker.isGreaterEqual(comparator) ){
			return d1 >= d2;
		} else if (KeywordChecker.isLessEqual(comparator) ){
			return d1 <= d2;
		} else if (KeywordChecker.isNot(comparator) ){
			return d1 != d2;
		} else {
			Logger.log("Error", "Comparator "+comparator+" is invalid for numeric types.");
		}
		return false;
	}
	
	/**
	 * Runs a comparison between a and b, assuming at least one is empty.
	 * Assumes the comparator is synonymous with either == or !=.
	 * 
	 * @param a first term to compare
	 * @param b second term to compare
	 * @param comparator to use, should be synonymous with == or !=
	 * @return the result of the comparison
	 */
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
