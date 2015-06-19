package format.conditional;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.Set;
import java.util.Vector;

import format.KeywordChecker;
import utils.Logger;

/**
 * This class translates a string to an evaluable conditional expression
 * object upon instantiation. It allows for multiple statements with different
 * return values to be used (each of which will be a SubConditionalExpression).
 * 
 * @author Ashton Dyer (WabashCannon)
 *
 */
public class ConditionalExpression {
	/** The individual conditional expressions that comprise the whole expression */
	private Vector<SubConditionalExpression> expressions =
			new Vector<SubConditionalExpression>();
	
	/**
	 * Creates a new conditional expression from the provided text
	 * 
	 * @param text to create this conditional expression from
	 */
	public ConditionalExpression(String text){
		text = text.trim();
		
		String[] lines = text.split(";");
		
		for ( String line : lines ){
			readExpression(line);
		}
	}
	
	/**
	 * Returns the value of the first conditional expression that evaluates
	 * to true in this multi-part conditional expression. Defaults to false
	 * if no value is found.
	 * 
	 * @return value of this multi-part conditional expression
	 */
	public String getValue(){
		for ( SubConditionalExpression expression : expressions ){
			if ( expression.isTrue() ){
				return expression.getValue();
			}
		}
		
		Logger.logVerbose("Conditional expression had no true statements");
		for ( SubConditionalExpression expression : expressions ){
			Logger.logVerbose("        "+expression.toString());
		}
		
		return "false";
	}
	
	/**
	 * Provides the dependency set of this expression.
	 * @return this expressions dependencies
	 */
	public Set<String> getDependencies(){
		Set<String> deps = new HashSet<String>();
		for ( SubConditionalExpression expression : expressions ){
			deps.addAll( expression.getDependencies() );
		}
		return deps;
	}
	
	// ####################################################
	// ### Private utility methods
	// ####################################################
	/**
	 * Reads the expression in the given line and stores it in a single part
	 * conditional expression object (ConditionalExpression)
	 * 
	 * @param line to read
	 */
	private void readExpression(String line){
		//Create the new conditional expression data
		SubConditionalExpression data = new SubConditionalExpression();
		expressions.add(data);
		//Check is constant
		if ( isSingleConstant(line) ){
			//Always true
			Condition condition = new Condition("true");
			data.addCondition(condition);
			
			//With a constant value
			data.setValue(line);
			return;
		}
		
		//Check one word scenario
		if ( !line.trim().contains(" ") ){
			String word = line.trim();
			//For one word boolean keywords
			if ( KeywordChecker.isBoolean( word ) ){
				Condition condition = new Condition(word);
				data.addCondition( condition );
				data.setValue(null);
				return;
			//Otherwise one word should be a column title, and evaluated based on existence/boolean value
			} else {
				Condition condition = new Condition(line);
				data.addCondition( condition );
				data.setValue(line);
				return;
			}
		}
		
		Scanner scanner = new Scanner(line);
		scanner.useDelimiter(" ");
		
		LinkedList<String> wordsList = new LinkedList<String>();
		String thisWord = "";
		while ( scanner.hasNext() ){
			thisWord += scanner.next() + " ";
			if ( isCompleteWord(thisWord) ){
				wordsList.add(thisWord.trim());
				thisWord = "";
			}
		}
		
		scanner.close();
		
		String word1 = wordsList.get(0);
		String word2 = wordsList.get(1);
		
		//First word as when is optional
		if ( KeywordChecker.isWhen(word1) ){
			wordsList.removeFirst();
		//First word is a value, which we store, then ignore when
		} else if ( KeywordChecker.isWhen(word2) ) {
			data.setValue(word1);
			wordsList.removeFirst();
			wordsList.removeFirst();
		} else {
			//TODO is default boolean
		}
		
		//Where we build up the condition's source string
		Vector<String> conditionSource = new Vector<String>();
		//While there are more words
		while ( !wordsList.isEmpty() ){
			String nextWord = wordsList.removeFirst();
			if ( KeywordChecker.isLogical(nextWord) ){
				//Check we have a one or three word expression
				if ( conditionSource.size() != 1 && conditionSource.size() != 3 ){
					Logger.log("Error", "Expected 1 or 3 words in condition: "+conditionSource.toString());
				}
				//We have the end of last statement and beginning of next
				String[] conditionSourceArray = conditionSource.toArray( new String[0] );
				Condition condition = new Condition( conditionSourceArray );
				data.addCondition(condition);
				data.addLogical(nextWord);
				
				conditionSource.removeAllElements();
			} else {
				//we have another word current condition source
				conditionSource.add( nextWord );
				//Check it isn't more than 3
				if ( conditionSource.size() > 3 ){
					Logger.log("Error", "Found more than 3 words in condition: "+conditionSource.toString());
				}
			}
		}
		//Fence post
		if ( !conditionSource.isEmpty() ){
			if ( conditionSource.size() != 1 && conditionSource.size() != 3 ){
				Logger.log("Expected 1 or 3 words in condition: "+conditionSource.toString());
				Logger.log("Error", conditionSource.toString());
			}
			//We have the end of last statement and beginning of next
			String[] conditionSourceArray = conditionSource.toArray( new String[0] );
			Condition condition = new Condition( conditionSourceArray );
			data.addCondition(condition);
			
			conditionSource.removeAllElements();
		}
	}
	
	/**
	 * Returns if the provided string is a single "word". This is true when it
	 * contains no spaces, or is surrounded by double-quotes.
	 * 
	 * @param word to test
	 * @return if it is a single word
	 */
	private boolean isCompleteWord(String word){
		word = word.trim();
		if ( word.startsWith("\"") ) {
			return word.endsWith("\"");
		} else {
			return true;
		}
	}
	
	/**
	 * Returns if the line is a single constant term.
	 * 
	 * @param line to test
	 * @return if the line is a single constant term.
	 */
	private boolean isSingleConstant(String line){
		line = line.trim();
		if ( line.startsWith("\"") && line.endsWith("\"") ){
			line = line.substring(1, line.length()-1);
			return !line.contains("\"");
		}
		return false;
	}
	
	@Override
	public String toString(){
		String line = "";
		for ( SubConditionalExpression data : expressions ){
			line += data + "\n";
		}
		return line;
	}
}
