package utils;
import java.io.PrintStream;

public class Logger {
	private static Logger log;
	static final int MAX_LINE_LENGTH = 2000;
	boolean verbose = false;
	PrintStream out = System.out;
	
	public static void log(final String text){
		if ( Logger.log == null ){
			createLogger();
		}
		
		Thread thread = new Thread(new Runnable(){

			@Override
			public void run() {
				String text2 = text;
				text2 += " ";
				String spacer = "        ";
				boolean firstline = true;
				while( text.length() > MAX_LINE_LENGTH ){
					String tmp = text2.substring(0, MAX_LINE_LENGTH);
					int index = tmp.lastIndexOf(' ');
					String line = text2.substring(0, index);
					if ( !firstline ){
						line = spacer+line;
					}
					firstline = false;
					log.out.println(line);
					text2 = text2.substring(index);
				}
				if ( !firstline ){
					text2 = spacer+text2;
				}
				log.out.println(text2);
			}
			
		});
		thread.start();
	}
	
	public static void logVerbose(String text){
		if ( Logger.log == null ){
			createLogger();
		}
		if ( Logger.log.verbose ){
			Logger.log("VERBOSE: "+text);
		}
	}
	
	public static void logWarning(String text){
		Logger.log("WARNING: "+text);
	}
	
	public static void logWarningVerbose(String text){
		if ( Logger.log != null && Logger.log.verbose ){
			Logger.log("VERBOSE: WARNING: "+text);
		}
	}
	
	public static void logCrash(String text){
		Logger.log("Fatal Error: "+text);
		Logger.log("Quitting");
		//System.exit(1);
	}
	
	/**
	 * Constructs the logger and has it print to a text file located
	 * at the path outfile, with verbosity setting of verbose
	 * @param outfile the path of the text file to print to
	 * @param verbose if the logger should print verbose output
	 */
	@Deprecated //TODO: implement
	public Logger(String outfile, boolean verbose){
		//TODO: implement writing to a printstream at outfile
		init(verbose);
	}
	
	public static void setPrintStream(PrintStream ps){
		if ( log == null ){
			createLogger();
		}
		log.out = ps;
	}
	
	public static boolean getVerbosity(){
		if ( log == null ){
			createLogger();
		}
		return log.verbose;
	}
	
	public static void setVerbosity(boolean verbosity){
		if ( log == null ){
			createLogger();
		}
		log.verbose = verbosity;
	}
	
	/**
	 * Constructs the logger and has it print to System.out with the
	 * verbosity setting given
	 * @param verbose is the logger should print verbose output
	 */
	public Logger(boolean verbose){
		init(verbose);
	}
	
	/**
	 * Initializes the logger and enforces it is a singleton
	 * Should only be called from the constructor
	 * @param verbose
	 */
	private void init(boolean verbose){
		if ( Logger.log != null ){
			System.err.println("Logger is a singleton class");
			System.exit(1);
		}
		this.verbose = verbose;
		Logger.log = this;
	}
	
	private static void createLogger(){
		if ( Logger.log == null ){
			Logger.log = new Logger(true);
			Logger.logVerbose("Initializing default logger");
		}
	}
}
