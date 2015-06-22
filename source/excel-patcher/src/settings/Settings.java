package settings;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import utils.Logger;

public class Settings implements Serializable {
	/** Default serial UID */
	private static final long serialVersionUID = 1L;
	
	/** The save path for the settings file */
	private static final String SETTINGS_FILE_PATH = "rsc/settings";
	
	// Global instance
	/** The global instance of the settings */
	private static Settings settings = null;
	
	// Settings state data
	/** The current state of the settings */
	private boolean[] booleanSettings = new boolean[BooleanSetting.values().length];
	private String[] stringSettings = new String[StringSetting.values().length];
	
	/**
	 * Private constructor to suppress external creation
	 */
	private Settings(){
	}
	
	/**
	 * Resets the settings instance to default settings
	 */
	private void resetToDefault(){
		//Reset boolean settings
		for ( BooleanSetting setting : BooleanSetting.values() ){
			setSetting(setting, setting.getDefaultValue());
		}
		
		//Reset string settings
		for ( StringSetting setting : StringSetting.values() ){
			setSetting(setting, setting.getDefaultValue());
		}
		
		//save result
		save();
	}
	
	/**
	 * Sets the specified setting to the given value
	 * 
	 * @param name of setting to set
	 * @param value the setting should have
	 */
	public static void setSetting(BooleanSetting name, boolean value){
		//Load to ensure non-null settings
		load();
		
		//Store the setting
		settings.booleanSettings[name.ordinal()] = value;
		
		//We'll save every time it is modified since it is a small object
		save();
	}
	
	/**
	 * Sets the specified setting to the given value
	 * 
	 * @param name of setting to set
	 * @param value the setting should have
	 */
	public static void setSetting(StringSetting setting, String value){
		//Load to ensure non-null settings
		load();
		
		//Store the setting
		settings.stringSettings[setting.ordinal()] = value;
		
		//We'll save every time it is modified since it is a small object
		save();
	}
	
	/**
	 * Returns the value of the specified setting
	 * 
	 * @param setting of setting to fetch
	 * @return the value of the specified setting
	 */
	public static boolean getSetting(BooleanSetting setting){
		load();
		return settings.booleanSettings[setting.ordinal()];
	}
	
	/**
	 * Returns the value of the specified setting
	 * 
	 * @param setting of setting to fetch
	 * @return the value of the specified setting
	 */
	public static String getSetting(StringSetting setting){
		load();
		return settings.stringSettings[setting.ordinal()];
	}
	
	/**
	 * Saves the settings instance to a file
	 * 
	 * @return if the save was successful
	 */
	private static void save(){
		// Don't save a null object
		if ( settings == null ){
			return;
		}
		//If they have been modified, save them
		try {
			FileOutputStream fOutStream = new FileOutputStream(SETTINGS_FILE_PATH);
			ObjectOutputStream oOutStream = new ObjectOutputStream(fOutStream);
			oOutStream.writeObject(settings);
			oOutStream.close();
			fOutStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Loads the settings from a file, or creates a default Settings object
	 * if the file cannot be loaded.
	 * 
	 * @return the loaded settings object
	 */
	private static void load(){
		if ( settings != null ){
			return;
		}
		try {
			//Try to load the settings from file
			FileInputStream fInStream = new FileInputStream(SETTINGS_FILE_PATH);
			ObjectInputStream oInStream = new ObjectInputStream(fInStream);
			settings = (Settings) oInStream.readObject();
			oInStream.close();
			fInStream.close();
		} catch (IOException | ClassNotFoundException e){
			settings = new Settings();
			settings.resetToDefault();
		}
	}
	
	/** Index set for boolean settings */
	public enum BooleanSetting{
		COLOR ("Color cells", "If the output file should have cells colored", true), 
		COMMENT ("Comment on cells", "If the output file should have cell comments", true), 
		DELETE ("Delete non-required cells", "If cells marked as not required in the format file should have their contents cleared", true),
		COLOR_BLIND ("Color blind mode", "If the output file should replace red coloring with blue coloring. Should make them easier to differentiate for red-green color blindness.", false);
		
		/** Name of the setting. Used for display. */
		private final String name;
		/** Description of the setting. Used for tooltips. */
		private final String description;
		/** Default value for this setting */
		private final boolean defaultValue;
		
		BooleanSetting(String name, String description, boolean defaultValue){
			this.name = name;
			this.description = description;
			this.defaultValue = defaultValue;
		}
		
		public String getName(){
			return name;
		}
		
		public String getDescription(){
			return description;
		}
		
		public boolean getDefaultValue(){
			return defaultValue;
		}
	}
	
	/** Index set for string settings */
	public enum StringSetting{
		INPUT_FILE_PATH (null),
		FORMAT_FILE_PATH ("rsc/format.txt"),
		OUTPUT_FILE_DIRECTORY (null),
		OUTPUT_FILE_NAME ("output.xlsx");
		
		private final String defaultValue;
		
		StringSetting(String defaultValue){
			this.defaultValue = defaultValue;
		}
		
		public String getDefaultValue(){
			return defaultValue;
		}
	}
}
