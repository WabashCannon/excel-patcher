package format;

import java.util.Arrays;
import java.util.List;

public class KeywordChecker {
	private static final List<String> TRUE_SYNONYMS = Arrays.asList(new String[]
			{"true", "yes"});
	private static final List<String> FALSE_SYNONYMS = Arrays.asList(new String[]
			{"false", "no"});
	private static final List<String> WHEN_KEYWORDS = Arrays.asList(new String[]
			{"when", "if"});
	private static final List<String> EQ_KEYWORDS = Arrays.asList(new String[]
			{"=", "==", "equals", "equal", "is"});
	private static final List<String> LESS_KEYWORDS = Arrays.asList(new String[]
			{"<", "less"});
	private static final List<String> GREATER_KEYWORDS = Arrays.asList(new String[]
			{">", "greater"});
	private static final List<String> NEG_KEYWORDS = Arrays.asList(new String[]
			{"not", "!"});
	private static final List<String> AND_KEYWORDS = Arrays.asList(new String[]
			{"and", "&", "&&"});
	private static final List<String> OR_KEYWORDS = Arrays.asList(new String[]
			{"or", "|", "||"});
	
	public static boolean isConstant(String text){
		return text.substring(0,1).equals("\"") && text.substring(text.length()-1).equals("\"");
	}
	
	public static boolean isBoolean(String text){
		return containsIgnoresCase(TRUE_SYNONYMS, text)
				|| containsIgnoresCase(FALSE_SYNONYMS, text);
	}
	
	public static String cleanBoolean(String text){
		assert( isBoolean(text) );
		if ( containsIgnoresCase(TRUE_SYNONYMS, text) ){
			return "true";
		} else {
			return "false";
		}
	}
	
	public static String stripConstant(String constant){
		assert( isConstant( constant) );
		constant = constant.substring(1);
		constant = constant.substring(0, constant.length()-1);
		return constant;
	}
	
	public static boolean isTrue(String text){
		return containsIgnoresCase(TRUE_SYNONYMS, text);
	}
	
	public static boolean isFalse(String text){
		return containsIgnoresCase(FALSE_SYNONYMS, text);
	}
	
	public static boolean isWhen(String text){
		return containsIgnoresCase(WHEN_KEYWORDS, text);
	}
	
	public static boolean isEqual(String text){
		return containsIgnoresCase(EQ_KEYWORDS, text);
	}
	
	public static boolean isLess(String text){
		return containsIgnoresCase(LESS_KEYWORDS, text);
	}
	
	public static boolean isGreater(String text){
		return containsIgnoresCase(GREATER_KEYWORDS, text);
	}
	
	public static boolean isLessEqual(String text){
		return isCompoundWord(LESS_KEYWORDS, EQ_KEYWORDS, text);
	}
	
	public static boolean isGreaterEqual(String text){
		return isCompoundWord(GREATER_KEYWORDS, EQ_KEYWORDS, text);
	}
	
	public static boolean isNot(String text){
		return containsIgnoresCase(NEG_KEYWORDS, text)
				|| isCompoundWord(NEG_KEYWORDS, EQ_KEYWORDS, text)
				|| isCompoundWord(EQ_KEYWORDS, NEG_KEYWORDS, text);
	}
	
	public static boolean isKeyword(String text){
		return isTrue(text) || isFalse(text) || isWhen(text)
				|| isEqual(text) || isLess(text) || isGreater(text)
				|| isLessEqual(text) || isGreaterEqual(text)
				|| isLogical(text);
	}
	
	public static boolean isLogical(String text){
		return isAnd(text) || isOr(text);
	}
	
	public static boolean isAnd(String text){
		return containsIgnoresCase(AND_KEYWORDS, text);
	}
	
	public static boolean isOr(String text){
		return containsIgnoresCase(OR_KEYWORDS, text);
	}
	
	public static boolean isComparator(String text){
		return isEqual(text) || isLess(text) || isGreater(text)
				|| isLessEqual(text) || isGreaterEqual(text) || isNot(text);
	}
	
	public static boolean containsIgnoresCase(List<String> falseSynonyms, String text){
		for ( String elem : falseSynonyms ){
			if ( elem.equalsIgnoreCase(text) ){
				return true;
			}
		}
		return false;
	}
	
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
