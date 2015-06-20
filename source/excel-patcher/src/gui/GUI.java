package gui;

import gui.Wrapper.SettingName;

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
	private void createContent(){
		this.setLayout( new BorderLayout() );
		
		JPanel header = new JPanel();
		header.setLayout(new BorderLayout());
		
		Wrapper wrap = Wrapper.getWrapper();
		FileBrowserPanel fbPanel = new FileBrowserPanel(wrap.getInputFile(), wrap.getOutputFile());
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
	
	/**
	 * Creates the GUI's menu bar
	 */
	private void createMenuBar(){
		//Init the menu bar
		JMenuBar menubar = new JMenuBar();
		ImageIcon icon = new ImageIcon("exit.png");
		
		//Create the first menu scroll-down
		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		
		//Create check menu item
		JMenuItem checkMenuItem = new JMenuItem("Check", icon);
		checkMenuItem.setMnemonic(KeyEvent.VK_E);
		checkMenuItem.setToolTipText("Runs the check on the input file");
		checkMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				Wrapper.getWrapper().checkFile();
			}
		});
		
		//Create clean menu item
		JMenuItem cleanMenuItem = new JMenuItem("Clean", icon);
		cleanMenuItem.setMnemonic(KeyEvent.VK_E);
		cleanMenuItem.setToolTipText("Cleans the output file of all comments and coloring");
		cleanMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				Wrapper.getWrapper().cleanFile();
			}
		});
		
		//Create edit format file menu item
		JMenuItem editFormatMenuItem = new JMenuItem("Edit format file", icon);
		editFormatMenuItem.setMnemonic(KeyEvent.VK_E);
		editFormatMenuItem.setToolTipText("Opens the format file for editing");
		editFormatMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				FileManager.editFormatFile();
				//Wrapper.getWrapper().editFormatFile();
			}
		});
		
		//Create open format file manual
		JMenuItem openManualMenuItem = new JMenuItem("Open format file manual", icon);
		openManualMenuItem.setMnemonic(KeyEvent.VK_E);
		openManualMenuItem.setToolTipText("Opens the format file manual");
		openManualMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				FileManager.openFormatManual();
			}
		});
		
		//Create and add exit button
		JMenuItem exitMenuItem = new JMenuItem("Exit", icon);
		exitMenuItem.setMnemonic(KeyEvent.VK_E);
		exitMenuItem.setToolTipText("Exit application");
		exitMenuItem.addActionListener(new ActionListener() {
		    @Override
		    public void actionPerformed(ActionEvent event) {
		        System.exit(0);
		    }
		});
		
		//Add all the items to fileMenu
		fileMenu.add(checkMenuItem);
		fileMenu.add(cleanMenuItem);
		fileMenu.add(editFormatMenuItem);
		fileMenu.add(openManualMenuItem);
		fileMenu.add(exitMenuItem);
		
		//Add the fileMenu downs to bar
		menubar.add(fileMenu);
		
		//Create the first menu scroll-down
		JMenu settingsMenu = new JMenu("Settings");
		fileMenu.setMnemonic(KeyEvent.VK_S);
		
		SettingName[] names = Wrapper.SettingName.values();
		for ( final SettingName name : names ){
			String nameStr = name.toString();
			final boolean value = Wrapper.getWrapper().getSetting(name);
			final JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(nameStr, value);
			
			menuItem.addActionListener(new ActionListener() {
			    @Override
			    public void actionPerformed(ActionEvent event) {
			        Wrapper.getWrapper().setSetting(name, menuItem.isSelected());
			    }
			});
			
			settingsMenu.add(menuItem);
		}
		//TODO: Implement verbose logging wisely
		/*
		final JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem("Verbose Logging", Logger.getVerbosity());
		menuItem.addActionListener( new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				Logger.setVerbosity(menuItem.isSelected());
			}
			
		});
		
		settingsMenu.add(menuItem);
		*/
		//Add settings menu to menubar
		menubar.add(settingsMenu);
		
		//Add the menubar to frame
		setJMenuBar(menubar);
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
		
		gui = this;
		
		createMenuBar();
		createContent();
		
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
