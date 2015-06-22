package gui;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import utils.FileManager;
import utils.Logger;
import utils.Logger.LogLevel;

/**
 * This class contains the code's main function and is the JFrame that
 * everything sits it.
 * 
 * @author Ashton Dyer (WabashCannon)
 *
 */
public class GUI extends JFrame {
	private static final long serialVersionUID = -7796330929953296934L;
	
	/**
	 * Creates all of the content for the GUI
	 */
	private void populateWithContent(){
		this.setLayout( new BorderLayout() );
		
		JPanel header = new JPanel();
		header.setLayout(new BorderLayout());
		
		Wrapper wrap = Wrapper.getWrapper();
		FileBrowserPanel fbPanel = new FileBrowserPanel();
		header.add(fbPanel, BorderLayout.NORTH);
		
		ActionButtonsPanel abPanel = new ActionButtonsPanel();
		header.add(abPanel, BorderLayout.SOUTH);
		
		this.add(header, BorderLayout.NORTH);
		
		ConsolePanel cPanel = new ConsolePanel();
		this.add(cPanel, BorderLayout.CENTER);
		
		//Set the loggers' print streams to the GUI console
		String defaultLogger = Logger.getDefaultLoggerName();
		Logger.setPrintStream( defaultLogger, cPanel.getPrintStream() );
		Logger.setPrintStream( "Error", cPanel.getPrintStream() );
	}
	
// #####################################################################################
// ### Entry point of code
// #####################################################################################
	/**
	 * Entry point of the code for running as an application. It just creates
	 * a new GUI and makes it visible.
	 * @param args
	 */
	public static void main(String[] args){
		EventQueue.invokeLater(new Runnable() {
	        
            @Override
            public void run() {
            	GUI gui = new GUI();
                gui.setVisible(true);
            }
        });
	}
	
	/**
	 * Creates a new GUI and initializes the loggers
	 */
	public GUI(){
		//TODO: Init logger
		String defaultLogger = Logger.getDefaultLoggerName();
		Logger.setEnablePrefix(defaultLogger, true);
		Logger.setEnablePrefix("Error", true);
		
		//Set the logger settings
		Logger.setVerbosity(defaultLogger, LogLevel.NORMAL);
		Logger.setVerbosity("Error", LogLevel.NORMAL);
		
		//Set the global gui instance
		gui = this;
		
		//Create the menu bar and add it
		JMenuBar menuBar = new MenuBar();
		this.setJMenuBar(menuBar);
		
		//Create the content and add it
		populateWithContent();
		
		//General settings for the window
		setTitle("Excel Patcher");
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
	}
	
// #####################################################################################
// ### Global instance implementation
// #####################################################################################
	/** Global instance of gui */
	private static GUI gui = null;
	
	/**
	 * Returns the GUI instance
	 * @return the GUI instance
	 */
	public static GUI getGUI(){
		assert( gui != null );
		return gui;
	}
}
