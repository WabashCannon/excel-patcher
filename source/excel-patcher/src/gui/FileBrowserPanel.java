package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import settings.Settings;
import settings.Settings.StringSetting;
import utils.Logger;

/**
 * The JPanel containing the input/output path text fields and the respective
 * browse and open buttons.
 * @author Ashton Dyer (WabashCannon)
 *
 */
public class FileBrowserPanel extends JPanel {
	
	private static final long serialVersionUID = 1155275561612671359L;
	
	public enum PathFieldName{
		INPUT_PATH_FIELD,
		OUTPUT_DIRECTORY_FIELD
	}
	/**
	 * Creates a new file browser panel with the specified input filePath and
	 * output filePath
	 * @param inputFile
	 * @param outputFile
	 */
	public FileBrowserPanel(){
		//Tmp stuff
		this.setBackground( new Color(255, 0, 0) );
		
		//Set the layout manager
		setLayout( new BorderLayout() );
		
		//Fill it with content
		createBrowserLines();
	}
	
	/** The size of the border in pixels */
	private int borderSize = 5;
	/** Each row's JPanel border */
	Border border = new EmptyBorder(borderSize, borderSize, borderSize, borderSize);
	/**
	 * Creates text fields and buttons
	 */
	private void createBrowserLines(){
		PathSelectorActionListener actionListener = new PathSelectorActionListener();
		
		//
		// Begin creation of the input file path line
		// 
		//Create input file path panel
		JPanel inputPanel = new JPanel();
		inputPanel.setBorder(border);
		inputPanel.setLayout( new BorderLayout() );
		
		//Create input file path label
		JLabel inputLabel = new JLabel("Input File: ");
		inputLabel.setBorder( border );
		
		//Create input file path text field and register it
		final JTextField inputPathField = new JTextField();
		inputPathField.setActionCommand(
				PathSelectorActionListener.ActionCommand.SET_INPUT_FILE_PATH.name());
		inputPathField.addActionListener(actionListener);
		inputPathField.addFocusListener(actionListener);
		TextFieldRegister.put(PathFieldName.INPUT_PATH_FIELD.name(),
				inputPathField);
		
		//Set input file path text field to initial settings value
		String inputFilePath = Settings.getSetting(StringSetting.INPUT_FILE_PATH);
		if ( inputFilePath != null ){
			inputPathField.setText(inputFilePath);
		}
		
		//Create input file path browse button
		JButton inputButton = new JButton("Browse");
		inputButton.setActionCommand(
				PathSelectorActionListener.ActionCommand.SELECT_INPUT_FILE_PATH.name());
		inputButton.addActionListener(actionListener);
		
		//Create input file open button
		JButton inputOpenButton = new JButton("Open");
		inputOpenButton.setActionCommand(
				PathSelectorActionListener.ActionCommand.OPEN_INPUT_FILE.name());
		inputOpenButton.addActionListener(actionListener);
		
		//Create input path buttons panel
		JPanel inputButtonsPanel = new JPanel();
		inputButtonsPanel.add(inputButton);
		inputButtonsPanel.add(inputOpenButton);
		
		inputPanel.add(inputLabel, BorderLayout.WEST);
		inputPanel.add(inputPathField, BorderLayout.CENTER);
		inputPanel.add(inputButtonsPanel, BorderLayout.EAST);
		
		add(inputPanel, BorderLayout.NORTH );
		
		//
		// Begin creation of the output directory line
		// 
		JPanel outputPanel = new JPanel();
		outputPanel.setBorder(border);
		outputPanel.setLayout( new BorderLayout() );
		
		JLabel outputLabel = new JLabel("Output Folder:");
		outputLabel.setBorder(border);
		
		//Create and register the output directory field
		final JTextField outputPathField = new JTextField();
		outputPathField.setActionCommand(
				PathSelectorActionListener.ActionCommand.SET_OUTPUT_FILE_DIRECTORY.name());
		outputPathField.addActionListener(actionListener);
		outputPathField.addFocusListener(actionListener);
		TextFieldRegister.put(PathFieldName.OUTPUT_DIRECTORY_FIELD.name(), 
				outputPathField);
		//Set the initial output directory from the settings
		String outputFileDirectory = Settings.getSetting(StringSetting.OUTPUT_FILE_DIRECTORY);
		if ( outputFileDirectory != null ){
			outputPathField.setText(outputFileDirectory);
		}
		
		//Create the button to browse for an output directory
		JButton outputButton = new JButton("Browse");
		outputButton.setActionCommand(
				PathSelectorActionListener.ActionCommand.SELECT_OUTPUT_FILE_DIRECTORY.name());
		outputButton.addActionListener(actionListener);
		
		//Create the button to open the output file
		JButton outputOpenButton = new JButton("Open");
		outputOpenButton.setActionCommand(
				PathSelectorActionListener.ActionCommand.OPEN_OUTPUT_FILE.name());
		outputOpenButton.addActionListener(actionListener);
		
		//Create the panel for the output field stuff
		JPanel outputButtonsPanel = new JPanel();
		outputButtonsPanel.add(outputButton);
		outputButtonsPanel.add(outputOpenButton);
		
		outputPanel.add(outputLabel, BorderLayout.WEST);
		outputPanel.add(outputPathField, BorderLayout.CENTER);
		outputPanel.add(outputButtonsPanel, BorderLayout.EAST);
		
		add(outputPanel, BorderLayout.SOUTH );
		
	}
}
