package gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import settings.Settings;
import settings.Settings.BooleanSetting;
import utils.FileManager;

public class MenuBar extends JMenuBar {
	
	public MenuBar(){
		GeneralActionListener actionListener = new GeneralActionListener();
		
		//Init the menu bar
		ImageIcon icon = new ImageIcon("exit.png");
		
		//Create the first menu scroll-down
		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		
		//Create check menu item
		JMenuItem checkMenuItem = new JMenuItem("Check", icon);
		checkMenuItem.setMnemonic(KeyEvent.VK_E);
		checkMenuItem.setToolTipText("Runs the check on the input file");
		checkMenuItem.setActionCommand(
				GeneralActionListener.ActionCommand.CHECK.name() );
		checkMenuItem.addActionListener(actionListener);
		
		//Create clean menu item
		JMenuItem cleanMenuItem = new JMenuItem("Clean", icon);
		cleanMenuItem.setMnemonic(KeyEvent.VK_E);
		cleanMenuItem.setToolTipText("Cleans the output file of all comments and coloring");
		cleanMenuItem.setActionCommand(
				GeneralActionListener.ActionCommand.CLEAN.name() );
		cleanMenuItem.addActionListener(actionListener);
		
		//Create edit format file menu item
		JMenuItem editFormatMenuItem = new JMenuItem("Edit format file", icon);
		editFormatMenuItem.setMnemonic(KeyEvent.VK_E);
		editFormatMenuItem.setToolTipText("Opens the format file for editing");
		editFormatMenuItem.setActionCommand(
				GeneralActionListener.ActionCommand.EDIT_FORMAT_FILE.name() );
		editFormatMenuItem.addActionListener(actionListener);
		
		//Create open format file manual
		JMenuItem openManualMenuItem = new JMenuItem("Open format file manual", icon);
		openManualMenuItem.setMnemonic(KeyEvent.VK_E);
		openManualMenuItem.setToolTipText("Opens the format file manual");
		openManualMenuItem.setActionCommand(
				GeneralActionListener.ActionCommand.OPEN_FORMAT_MANUAL.name() );
		openManualMenuItem.addActionListener(actionListener);
		
		//Create and add exit button
		JMenuItem exitMenuItem = new JMenuItem("Exit", icon);
		exitMenuItem.setMnemonic(KeyEvent.VK_E);
		exitMenuItem.setToolTipText("Exit application");
		exitMenuItem.setActionCommand(
				GeneralActionListener.ActionCommand.EXIT.name());
		exitMenuItem.addActionListener(actionListener);
		
		//Add all the items to fileMenu
		fileMenu.add(checkMenuItem);
		fileMenu.add(cleanMenuItem);
		fileMenu.add(editFormatMenuItem);
		fileMenu.add(openManualMenuItem);
		fileMenu.add(exitMenuItem);
		
		//Add the fileMenu downs to bar
		add(fileMenu);
		
		//Create the first menu scroll-down
		JMenu settingsMenu = new JMenu("Settings");
		fileMenu.setMnemonic(KeyEvent.VK_S);
		
		for ( final BooleanSetting setting : BooleanSetting.values() ){
			String nameStr = setting.getName();
			String tooltip = setting.getDescription();
			final boolean value = Settings.getSetting(setting);
			final JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(nameStr, value);
			menuItem.setToolTipText(tooltip);
			
			menuItem.addActionListener(new ActionListener() {
			    @Override
			    public void actionPerformed(ActionEvent event) {
			        Settings.setSetting(setting, menuItem.isSelected());
			    }
			});
			
			settingsMenu.add(menuItem);
		}
		
		//Add settings menu to menubar
		add(settingsMenu);
	}
}
