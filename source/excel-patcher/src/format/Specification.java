package format;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import format.conditional.ConditionalExpression;
import utils.Logger;

/**
 * this really needs to be something with generic type arguments, but it's too late now
 * @author ashton
 *
 */
public class Specification {
	/** Comprehensive list of acceptable specification keywords */
	public static final List<String> SPECIFICATION_KEYWORDS = Arrays.asList(new String[]
			{"Required", "Type", "MaxPossibleCharacters", "Value"});
	
	private String type;
	private String value;
	private DataType dataType;
	private ConditionalExpression conditionalExpression = null;
	
	public Specification(String line){
		//Split the line and check the keyword
		String[] splitLine = line.split(":");
		for ( int i = 0 ; i < splitLine.length ; i++ ){
			splitLine[i] = splitLine[i].trim();
		}
		if ( !SPECIFICATION_KEYWORDS.contains(splitLine[0]) ){
			Logger.log("Error", "Expected a specification keyword in format file, but found "+splitLine[0]);
		}
		//Set type
		type = splitLine[0];
		//Actually implement specifications
		if ( type.equals("Required") ){
			conditionalExpression = new ConditionalExpression(splitLine[1]);
		} else if ( type.equals("MaxPossibleCharacters") ){
			//TODO: type check this string to be int
			value = splitLine[1];
		} else if ( type.equals("Type") ){
			dataType = new DataType(splitLine[1]);
		} else if ( type.equals("Value") ){
			conditionalExpression = new ConditionalExpression(splitLine[1]);
		}
	}
	
	// ####################################################
	// ### Getters and setters
	// ####################################################
	public String getType(){
		return type;
	}
	
	public String getValue(){
		if ( type.equals("Required") ){
			String value = conditionalExpression.getValue();
			if ( !KeywordChecker.isBoolean(value) ){
				Logger.log("Error", "Required specification's conditional expression returned non-boolean value: "+value
						+" for conditional expression "+conditionalExpression.toString());
			}
			return KeywordChecker.cleanBoolean(value);
		} else if ( type.equals("MaxPossibleCharacters") ){
			return value;
		} else if ( type.equals("Type") ){
			return dataType.toString();
		} else if ( type.equals("Value") ){
			return conditionalExpression.getValue();
		}
		assert( false );
		return null;
	}
	
	/*
	@Override
	public String toString(){
		return getType()+": "+getValue();
	}
	*/
	
	public Set<String> getDependencies(){
		if ( conditionalExpression != null ){
			return conditionalExpression.getDependencies();
		} else {
			return new HashSet<String>();
		}
	}
}
