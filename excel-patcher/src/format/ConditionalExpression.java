package format;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.Set;
import java.util.Vector;

import utils.Logger;

public class ConditionalExpression {
	
	private Vector<ConditionalExpressionData> expressions =
			new Vector<ConditionalExpressionData>();
	
	public ConditionalExpression(String text){
		text = text.trim();
		
		String[] lines = text.split(";");
		
		for ( String line : lines ){
			readExpression(line);
		}
	}
	
	//Returns prefix value
	public String getValue(){
		for ( ConditionalExpressionData expression : expressions ){
			if ( expression.isTrue() ){
				return expression.getValue();
			}
		}
		
		Logger.logVerbose("Conditional expression had no true statements");
		for ( ConditionalExpressionData expression : expressions ){
			Logger.logVerbose("        "+expression.toString());
		}
		
		return "false";
	}
	
	public Set<String> getDependencies(){
		Set<String> deps = new HashSet<String>();
		for ( ConditionalExpressionData expression : expressions ){
			deps.addAll( expression.getDependencies() );
		}
		return deps;
	}
	
	// ####################################################
	// ### Private utility methods
	// ####################################################
	private void readExpression(String line){
		//Create the new conditional expression data
		ConditionalExpressionData data = new ConditionalExpressionData();
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
		
		// is not a column
		//For the multi-word scenario
		//read the value if there is one, else is boolean
		//read the expression
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
		
		//Process the conditionals
		
		//while more
			//read until word is a boolean operator
			//Add condition on what we read
			//Add boolean operator
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
		
		//System.out.println( data );
		
		/*
		String word = wordsList.removeFirst();
		//Read the first word and store a value
		if ( KeywordChecker.isWhen(word) ){
			data.setValue( null );
		} else {
			data.setValue( word );
			word = wordsList.removeFirst();
			if ( !KeywordChecker.isWhen(word) ){
				Logger.log("Error", "Expected keyword \"when\" to be first or second in conditional expression");
			}
		}
		//Buffer for words as we work
		LinkedList<String> currentQueue = new LinkedList<String>();
		//Read each condition
		while ( !wordsList.isEmpty() ){
			//read until the end or the next logical operator
			word = wordsList.removeFirst();
			while ( !KeywordChecker.isLogical(word) && !wordsList.isEmpty() ){
				currentQueue.add(word);
				word = wordsList.removeFirst();
			}
			//Add word to logicals,
			if ( KeywordChecker.isLogical(word) ){
				data.addLogical(word);
			} else {//or it was the final word in final condition
				currentQueue.add(word);
			}
			// Check that the condition read is reasonable
			if ( currentQueue.size() != 1 && currentQueue.size() != 3 ){
				String condition = "";
				for ( String tmp : currentQueue ){
					condition += tmp+" ";
				}
				Logger.log("Error", "Expected condition to contain 1 or 3 words recieved \""+condition+"\"");
			}
			//Add logical expression to vector
			String[] conditionString = new String[currentQueue.size()];
			conditionString = currentQueue.toArray(conditionString);
			Condition condition = new Condition(conditionString);
			data.addCondition(condition);
			
			//Clear the queue
			currentQueue.clear();
			
		}
		*/
	}
	
	private boolean isCompleteWord(String word){
		word = word.trim();
		if ( word.startsWith("\"") ) {
			return word.endsWith("\"");
		} else {
			return true;
		}
	}
	
	private boolean isSingleConstant(String line){
		line = line.trim();
		if ( line.startsWith("\"") && line.endsWith("\"") ){
			line = line.substring(1, line.length()-1);
			return !line.contains("\"");
		}
		return false;
	}
	
	public String toString(){
		String line = "";
		for ( ConditionalExpressionData data : expressions ){
			line += data + "\n";
		}
		return line;
	}
}
