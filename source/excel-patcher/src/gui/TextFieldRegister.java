package gui;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JTextField;

public class TextFieldRegister {
	private static Map<String, JTextField> textFields =
		 new HashMap<String, JTextField>();
	
	public static boolean put(String name, JTextField textField){
		if ( textFields.containsValue(textField) ){
			return false;
		}
		textFields.put(name, textField);
		return true;
	}
	
	public static JTextField getTextField(String name){
		if ( textFields.containsKey(name) ){
			return textFields.get(name);
		}
		return null;
	}
}
