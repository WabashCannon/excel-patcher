package gui;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JTextField;

/**
 * This is a small class used to register swing components for global lookup
 * by name.
 * 
 * @author Ashton Dyer (WabashCannon)
 *
 */
public class TextFieldRegister {
	/** Map of all the JTextFields and their lookup names */
	private static Map<String, JTextField> textFields =
		 new HashMap<String, JTextField>();
	
	/**
	 * Registers a new textField with the lookup key of name.
	 * 
	 * @param name to use as lookup key
	 * @param textField to register for looking up
	 * @return if the field was added (true if it wasn't already in the register)
	 */
	public static boolean put(String name, JTextField textField){
		if ( textFields.containsValue(textField) ){
			return false;
		}
		textFields.put(name, textField);
		return true;
	}
	
	/**
	 * Gets a text field registered with the given name.
	 * Returns null if no field was found.
	 * 
	 * @param name to use as lookup key
	 * @return the text field registered under the given name
	 */
	public static JTextField getTextField(String name){
		if ( textFields.containsKey(name) ){
			return textFields.get(name);
		}
		return null;
	}
}
