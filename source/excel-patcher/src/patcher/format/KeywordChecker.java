package patcher.format;

import java.util.Arrays;
import java.util.List;

/**
 * A static utility class for checking if strings are synonymous. It is a bit
 * verbose, but it increases legibility throughout the other classes.
 * 
 * @author Ashton Dyer (WabashCannon)
 *
 */
public class KeywordChecker {
	/** List of true synonyms */
	private static final List<String> TRUE_SYNONYMS = Arrays.asList(new String[]
			{"true", "yes"});
	/** List of false synonyms */
	private static final List<String> FALSE_SYNONYMS = Arrays.asList(new String[]
			{"false", "no"});
	/** List of when synonyms */
	private static final List<String> WHEN_KEYWORDS = Arrays.asList(new String[]
			{"when", "if"});
	/** List of equals synonyms */
	private static final List<String> EQ_KEYWORDS = Arrays.asList(new String[]
			{"=", "==", "equals", "equal", "is"});
	/** List of less than synonyms */
	private static final List<String> LESS_KEYWORDS = Arrays.asList(new String[]
			{"<", "less"});
	/** List of greater than synonyms */
	private static final List<String> GREATER_KEYWORDS = Arrays.asList(new String[]
			{">", "greater"});
	/** List of not synonyms */
	private static final List<String> NEG_KEYWORDS = Arrays.asList(new String[]
			{"not", "!"});
	/** List of and synonyms */
	private static final List<String> AND_KEYWORDS = Arrays.asList(new String[]
			{"and", "&", "&&"});
	/** List of or synonyms */
	private static final List<String> OR_KEYWORDS = Arrays.asList(new String[]
			{"or", "|", "||"});
	
	/**
	 * Returns if the given text is a constant expression
	 * @param text to check
	 * @return if the given text is a constant expression
	 */
	public static boolean isConstant(String text){
		return text.substring(0,1).equals("\"") && text.substring(text.length()-1).equals("\"");
	}
	
	/**
	 * Returns if the given text is synonymous with a boolean
	 * @param text to check
	 * @return if the given text is synonymous with a boolean
	 */
	public static boolean isBoolean(String text){
		return containsIgnoresCase(TRUE_SYNONYMS, text)
				|| containsIgnoresCase(FALSE_SYNONYMS, text);
	}
	
	/**
	 * Returns the default representation of a boolean string.
	 * For all false synonyms it returns "false"; for all true synonyms
	 * it returns true. Defaults to false for non-boolean arguments.
	 * 
	 * @param text to convert
	 * @return "false" or "true" depending on if the text is synonymous with
	 * false or true.
	 */
	public static String cleanBoolean(String text){
		assert( isBoolean(text) );
		if ( containsIgnoresCase(TRUE_SYNONYMS, text) ){
			return "true";
		} else {
			return "false";
		}
	}
	
	/**
	 * Strips a constant of its double-quotes
	 * 
	 * @param constant to strip
	 * @return the constant without its double-quotes
	 */
	public static String stripConstant(String constant){
		assert( isConstant( constant) );
		constant = constant.substring(1);
		constant = constant.substring(0, constant.length()-1);
		return constant;
	}
	
	/**
	 * Returns if the given text is synonymous with true
	 * @param text to check
	 * @return if the given text is synonymous with true
	 */
	public static boolean isTrue(String text){
		return containsIgnoresCase(TRUE_SYNONYMS, text);
	}
	
	/**
	 * Returns if the given text is synonymous with false
	 * @param text to check
	 * @return if the given text is synonymous with false
	 */
	public static boolean isFalse(String text){
		return containsIgnoresCase(FALSE_SYNONYMS, text);
	}
	
	/**
	 * Returns if the given text is synonymous with when
	 * @param text to check
	 * @return if the given text is synonymous with when
	 */
	public static boolean isWhen(String text){
		return containsIgnoresCase(WHEN_KEYWORDS, text);
	}
	
	/**
	 * Returns if the given text is synonymous with equals
	 * @param text to check
	 * @return if the given text is synonymous with equals
	 */
	public static boolean isEqual(String text){
		return containsIgnoresCase(EQ_KEYWORDS, text);
	}
	
	/**
	 * Returns if the given text is synonymous with less than
	 * @param text to check
	 * @return if the given text is synonymous with less than
	 */
	public static boolean isLess(String text){
		return containsIgnoresCase(LESS_KEYWORDS, text);
	}
	
	/**
	 * Returns if the given text is synonymous with greater than
	 * @param text to check
	 * @return if the given text is synonymous with greater than
	 */
	public static boolean isGreater(String text){
		return containsIgnoresCase(GREATER_KEYWORDS, text);
	}
	
	/**
	 * Returns if the given text is synonymous with less than or equal to
	 * @param text to check
	 * @return if the given text is synonymous with less than or equal to
	 */
	public static boolean isLessEqual(String text){
		return isCompoundWord(LESS_KEYWORDS, EQ_KEYWORDS, text);
	}
	
	/**
	 * Returns if the given text is synonymous with a greater than or equal to
	 * @param text to check
	 * @return if the given text is synonymous with a greater than or equal to
	 */
	public static boolean isGreaterEqual(String text){
		return isCompoundWord(GREATER_KEYWORDS, EQ_KEYWORDS, text);
	}
	
	/**
	 * Returns if the given text is synonymous with not
	 * @param text to check
	 * @return if the given text is synonymous with not
	 */
	public static boolean isNot(String text){
		return containsIgnoresCase(NEG_KEYWORDS, text)
				|| isCompoundWord(NEG_KEYWORDS, EQ_KEYWORDS, text)
				|| isCompoundWord(EQ_KEYWORDS, NEG_KEYWORDS, text);
	}
	
	/**
	 * Returns if the given text is a keyword
	 * @param text to check
	 * @return if the given text is a keyword
	 */
	public static boolean isKeyword(String text){
		return isTrue(text) || isFalse(text) || isWhen(text)
				|| isEqual(text) || isLess(text) || isGreater(text)
				|| isLessEqual(text) || isGreaterEqual(text)
				|| isLogical(text);
	}
	
	/**
	 * Returns if the given text is a logical operator
	 * @param text to check
	 * @return if the given text is a logical operator
	 */
	public static boolean isLogical(String text){
		return isAnd(text) || isOr(text);
	}
	
	/**
	 * Returns if the given text is synonymous with and
	 * @param text to check
	 * @return if the given text is synonymous with and
	 */
	public static boolean isAnd(String text){
		return containsIgnoresCase(AND_KEYWORDS, text);
	}
	
	/**
	 * Returns if the given text is synonymous with or
	 * @param text to check
	 * @return if the given text is synonymous with or
	 */
	public static boolean isOr(String text){
		return containsIgnoresCase(OR_KEYWORDS, text);
	}
	
	/**
	 * Returns if the given text is a comparator
	 * @param text to check
	 * @return if the given text is a comparator
	 */
	public static boolean isComparator(String text){
		return isEqual(text) || isLess(text) || isGreater(text)
				|| isLessEqual(text) || isGreaterEqual(text) || isNot(text);
	}
	
	/**
	 * Returns if listToCheck contains text, ignoring the case of text.
	 * 
	 * @param listToCheck if it contains text
	 * @param text to check if in listToCheck
	 * @return if listToCheck contains text, ignoring case
	 */
	public static boolean containsIgnoresCase(List<String> listToCheck, String text){
		for ( String elem : listToCheck ){
			if ( elem.equalsIgnoreCase(text) ){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns if the word is a compound word created from a word in list a and
	 * a word in list b. Ignores case.
	 * 
	 * @param a list that may contain the first word in the compound word
	 * @param b list that may contain the second word in the compound word
	 * @param word to check if it is a compound word from list a and b
	 * @return if the word is a compound word created from a word in list a and
	 * a word in list b
	 */
	private static boolean isCompoundWord(List<String> a, List<String> b, String word){
		word = word.toLowerCase();
		for ( String wordA : a ){
			wordA = wordA.toLowerCase();
			if ( word.startsWith(wordA) ){
				String wordB = word.substring(wordA.length());
				if ( containsIgnoresCase(b, wordB) ){
					return true;
				}
			}
		}
		return false;
	}
}
