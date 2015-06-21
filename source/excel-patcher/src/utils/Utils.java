package utils;

import java.util.Set;

/**
 * Static utility class used for various tasks.
 * 
 * @author Ashton Dyer (WabashCannon)
 *
 */
public class Utils {
	//suppress constructor
	private Utils(){
		
	}
	
	/**
	 * Returns a cleaner string representation of the set than Set.toString()
	 * 
	 * @param setToPrint
	 * @return a clean string representation of the setToPrint
	 */
	public static String nicePrint(Set<String> setToPrint){
		String output = "";
		
		String[] elements = setToPrint.toArray(new String[setToPrint.size()]);
		//For empty sets
		if ( elements.length == 0 ){
			return "Tried to print the empty set";
		} else if ( elements.length == 1 ){
			return elements[0];
		} else if ( elements.length == 2 ){
			return elements[0] + " and " + elements[1];
		}
		
		for ( int i = 0 ; i < elements.length ; i++ ){
			if ( i != elements.length - 1 ){
				output += elements[i]+", ";
			} else {
				output += "and "+elements[i];
			}
		}
		
		return output;
	}
	
	/**
	 * Determines if the provided text is numeric.
	 * 
	 * @param text to check
	 * @return if the text is numeric
	 */
	public static boolean isNumber(String text){
		try {
			Double.parseDouble(text);
		} catch (NumberFormatException e){
			return false;
		}
		return true;
	}
}
