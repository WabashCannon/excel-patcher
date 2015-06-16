package utils;
import java.io.PrintStream;
import java.util.HashMap;

public class Logger {
	/** Potential verbosity settings */
	public enum LogLevel{ NONE, NORMAL, VERBOSE }
	/** Map containing the loggers. Keys are string identifiers */
	private static HashMap<String, LoggerSetting> loggerSettings = new HashMap<String, LoggerSetting>();
	/** Default logger implemented for more convenient logging */
	private static String defaultLoggerSettingName = "Default";
	
	//#################################################################
	//### Public General Methods
	//#################################################################
	/**
	 * Logs the given message using the default logger settings.
	 * 
	 * @param message to log
	 */
	public static void log(String message){
		log(defaultLoggerSettingName, message);
	}
	
	/**
	 * Logs the given message to the specified logger settings.
	 * 
	 * @param loggerName
	 * @param message
	 */
	public static void log(String loggerName, String message){
		LoggerSetting setting = getSetting(loggerName);
		log(setting, message);
	}
	
	/**
	 * Logs the message to the default logger settings if they have a verbosity level
	 * of LogLevel.VERBOSE
	 * @param message
	 */
	public static void logVerbose(String message){
		logVerbose(defaultLoggerSettingName, message);
	}
	
	/**
	 * Logs the message to the specified logger settings if they have a verbosity level
	 * of LogLevel.VERBOSE
	 * @param message
	 */
	public static void logVerbose(String loggerName, String message){
		LoggerSetting setting = getSetting(loggerName);
		if ( setting.verbosity == LogLevel.VERBOSE ){
			log(setting, "[VERBOSE] "+message);
		}
	}

	/**
	 * Sets the default logger to the specified logger if it exists. If it does not
	 * it creates a logger with the given name and sets it as the default.
	 * @param loggerName
	 */
	public static void setDefaultLogger(String loggerName){
		defaultLoggerSettingName = loggerName;
		
	}
	
	//#################################################################
	//### Wrapper methods for getting/setting logger settings
	//#################################################################
	public static String getDefaultLoggerName(){
		return defaultLoggerSettingName;
	}
	
	/**
	 * Sets the given logger's PrintStream
	 * @param loggerName
	 * @param ps
	 */
	public static void setPrintStream(String loggerName, PrintStream ps){
		LoggerSetting setting = getSetting(loggerName);
		setting.printStream = ps;
	}
	
	/**
	 * Returns the specified logger's current PrintStream
	 * @param loggerName
	 * @return the logger's PrintStream
	 */
	public static PrintStream getPrintStream(String loggerName){
		return getSetting(loggerName).printStream;
	}
	
	/**
	 * Sets the given logger's verbosity
	 * @param loggerName
	 * @param verbosity
	 */
	public static void setVerbosity(String loggerName, LogLevel verbosity){
		LoggerSetting setting = getSetting(loggerName);
		setting.verbosity = verbosity;
	}
	
	/**
	 * Returns the specified logger's verbosity
	 * @param loggerName
	 * @return the logger's verbosity
	 */
	public static LogLevel getVerbosity(String loggerName){
		return getSetting(loggerName).verbosity;
	}
	
	/**
	 * Sets the given logger's maximum characters per line
	 * @param loggerName
	 * @param maxLineLength
	 */
	public static void setMaxLineLength(String loggerName, int maxLineLength){
		LoggerSetting setting = getSetting(loggerName);
		setting.maxLineLength = maxLineLength;
	}
	
	/**
	 * Return's the specified logger's max characters per line
	 * @param loggerName
	 * @return the logger's max characters per line
	 */
	public static int getMaxLineLength(String loggerName){
		return getSetting(loggerName).maxLineLength;
	}
	
	/**
	 * Sets whether the given logger should print it's name as a prefix on each message
	 * @param loggerName
	 * @param enablePrefix
	 */
	public static void setEnablePrefix(String loggerName, boolean enablePrefix){
		LoggerSetting setting = getSetting(loggerName);
		setting.enablePrefix = enablePrefix;
	}
	
	/**
	 * Returns if the specified logger prints its name before each line
	 * @param loggerName
	 * @return if the logger prints its own name as a prefix
	 */
	public static boolean isPrefixEnabled(String loggerName){
		return getSetting(loggerName).enablePrefix;
	}
		
		
	//#################################################################
	//### Private Methods
	//#################################################################
	/**
	 * Creates a new logger setting
	 * @param string
	 */
	private static void createSetting(String loggerName) {
		LoggerSetting setting = new LoggerSetting(loggerName);
		loggerSettings.put(loggerName, setting);
	}
	
	/**
	 * Returns the settings for the given loggerName. Creates default ones
	 * if they do not already exist.
	 * 
	 * @param loggerName
	 */
	private static LoggerSetting getSetting(String loggerName){
		if ( !loggerSettings.containsKey(loggerName) ){
			createSetting(loggerName);
		}
		
		return loggerSettings.get(loggerName);
	}

	/**
	 * Internal method for actually logging the messages.
	 * 
	 * @param loggerSetting to use when logging
	 * @param message to log
	 */
	@Deprecated
	private static void log(LoggerSetting loggerSetting, String message){
		//Split the message into lines
		String[] lines = message.split("\\r?\\n");
		
		//If we need to prepend prefix for first line
		if ( loggerSetting.enablePrefix ){
			lines[0] = "["+loggerSetting.name+"] " + lines[0];
			for ( int i = 1 ; i < lines.length ; i++ ){
				lines[i] = "   "+lines[i];
			}
		}
		
		//for each line, print with word wrap
		for ( String line : lines ){
			while ( line.length() > loggerSetting.maxLineLength ){
				//Find the index of the last space prior to character maxLineLength
				String tmp = line.substring(0, loggerSetting.maxLineLength);
				int lastSpaceIndex = tmp.lastIndexOf(' ');
				
				//If no space, we wrap at exactly maxLineLength
				if ( lastSpaceIndex == -1 ){
					String toPrint = line.substring(0,loggerSetting.maxLineLength);
					loggerSetting.printStream.println(toPrint);
					line = line.substring(loggerSetting.maxLineLength);
				} else {
					//Otherwise we wrap at the last space
					String toPrint = line.substring(0, lastSpaceIndex);
					loggerSetting.printStream.println(toPrint);
					line = line.substring(lastSpaceIndex);
				}
				
				//For wrapped text, tab indent if prefix is enabled
				if ( loggerSetting.enablePrefix ){
					line = "   "+line;
				}
			}
			loggerSetting.printStream.println(line);
		}
	}
	
	//#################################################################
	//### Small struct for each logger's settings
	//#################################################################
	/**
	 * Private class used to store settings along with each logger.
	 * 
	 * @author Ashton Dyer
	 */
	private static class LoggerSetting{
		private static final int DEFAULT_MAX_LINE_LENGTH = 1000;
		
		/** The name of the logger */
		private String name;
		/** PrintStream to print messages to */
		public PrintStream printStream = System.out;
		/** This logger's max characters per line */
		public int maxLineLength = DEFAULT_MAX_LINE_LENGTH;
		/** If this logger should prepend messages with it's name */
		public boolean enablePrefix = false;
		/** If messages logged using logVerbose should be printed */
		public LogLevel verbosity = LogLevel.NORMAL;
		
		public LoggerSetting(String name){
			this.name = name;
		}
	}
}
