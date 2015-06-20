package format.conditional;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import format.DataType;
import format.KeywordChecker;
import utils.Logger;

/**
 * This class acts as the container and interface for specifications.
 * 
 * @author Ashton Dyer (WabashCannon)
 *
 */
public class Specification {
	/** Comprehensive list of acceptable specification keywords */
	public static final List<String> SPECIFICATION_KEYWORDS = Arrays.asList(new String[]
			{"Required", "Type", "MaxPossibleCharacters", "Value"});
	/** The specification type, from the list of SPECIFICATION_KEYWORDS */
	private String type;
	/** The value, or argument, of this specification */
	private String value;
	/** The data type object of this specification. This should only be
	 * stored when type is "Type" */
	private DataType dataType;
	/** The conditional expression for this specification. This should
	 * only be stored when type is Required or Value */
	private ConditionalExpression conditionalExpression = null;
	
	/**
	 * Creates a new specification from the given line.
	 * 
	 * @param line to generate the specification from
	 */
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
	/**
	 * Returns this specification's type
	 * @return this specification's type
	 */
	public String getType(){
		return type;
	}
	
	/**
	 * Returns the value of this specification.
	 * 
	 * @return the value of this specification
	 */
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
	
	/**
	 * Returns the set of column titles on which this specification depends.
	 * 
	 * @return the set of column titles on which this specification depends
	 */
	public Set<String> getDependencies(){
		if ( conditionalExpression != null ){
			return conditionalExpression.getDependencies();
		} else {
			return new HashSet<String>();
		}
	}
}
