package gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import settings.Settings;
import settings.Settings.StringSetting;
import utils.Logger;

/**
 * This class acts as the action listener for all the components in
 * the FileBrowserPanel
 * 
 * @author Ashton Dyer (WabashCannon)
 *
 */
public class PathSelectorActionListener implements ActionListener, FocusListener {
	/** ActionCommands allowable for this action listener */
	public enum ActionCommand{
		SELECT_INPUT_FILE_PATH,
		SET_INPUT_FILE_PATH,
		OPEN_INPUT_FILE,
		SELECT_OUTPUT_FILE_DIRECTORY,
		SET_OUTPUT_FILE_DIRECTORY,
		OPEN_OUTPUT_FILE
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		String actionCommandName = event.getActionCommand();
		ActionCommand actionCommand;
		
		try {
			actionCommand = ActionCommand.valueOf(actionCommandName);
		} catch (IllegalArgumentException | NullPointerException e){
			Logger.log("Error", this.getClass().getName()+" cannot process action"
					+" command with the name "+actionCommandName );
			return;
		}
		
		switch( actionCommand ){
			case SELECT_INPUT_FILE_PATH:
				selectInputFile();
				break;
			case SET_INPUT_FILE_PATH:
				JTextField inputPathField = TextFieldRegister.getTextField(
						FileBrowserPanel.PathFieldName.INPUT_PATH_FIELD.name());
				File inputFile = new File(inputPathField.getText());
				setInputFile(inputFile);
				break;
			case OPEN_INPUT_FILE:
				Wrapper.getWrapper().openInputFile();
				break;
			case SELECT_OUTPUT_FILE_DIRECTORY:
				selectOutputFile();
				break;
			case SET_OUTPUT_FILE_DIRECTORY:
				JTextField outputDirectoryField = TextFieldRegister.getTextField(
						FileBrowserPanel.PathFieldName.OUTPUT_DIRECTORY_FIELD.name());
				File outputDirectory = new File(outputDirectoryField.getText());
				setOutputFile(outputDirectory);
				break;
			case OPEN_OUTPUT_FILE:
				Wrapper.getWrapper().openOutputFile();
				break;
			default:
				Logger.log("Error", this.getClass().getName()+" cannot process action"
						+" command with the name "+actionCommandName );
				return;
		}
	}
	
	/**
	 * Opens a file browser used to select the output directory
	 */
	private void selectOutputFile(){
		final JFileChooser fc = new JFileChooser();
		fc.setDialogTitle("Choose an output folder...");
	    fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	    fc.setAcceptAllFileFilterUsed(false);
	    
		int returnVal = fc.showOpenDialog( GUI.getGUI().getContentPane() );
		
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			setOutputFile(file);
		}
	}
	
	/**
	 * Sets the output file directory to be that of the given file
	 * if it is valid. Refreshes the necessary text field with the new
	 * value.
	 * 
	 * @param file to set as the new output directory
	 */
	private void setOutputFile(File file){
		Wrapper.getWrapper().setOutputFile(file.getAbsolutePath());
		
		String fieldName = FileBrowserPanel.PathFieldName.OUTPUT_DIRECTORY_FIELD.name();
		JTextField field = TextFieldRegister.getTextField(fieldName);
		if ( field != null ){
			field.setText(Settings.getSetting(StringSetting.OUTPUT_FILE_DIRECTORY));
		}
	}
	
	/**
	 * Opens a browser to allow the user to select a new input file
	 */
	private void selectInputFile(){
		//Get the input path field from registrar
		final JTextField inputPathField = TextFieldRegister.getTextField(
				FileBrowserPanel.PathFieldName.INPUT_PATH_FIELD.name() );
		
		//Create and set up the file chooser
		final JFileChooser fc = new JFileChooser();
		fc.setDialogTitle("Choose an input excel file...");
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Excel files", "xls", "xlsx");
		fc.setFileFilter(filter);
		
		int returnVal = fc.showOpenDialog( GUI.getGUI().getContentPane() );
		
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			//Store the file and old text
			File file = fc.getSelectedFile();
            setInputFile(file);
        }
	}
	
	private void setInputFile(File file){
		//Get the input path field from registrar
		final JTextField inputPathField = TextFieldRegister.getTextField(
				FileBrowserPanel.PathFieldName.INPUT_PATH_FIELD.name() );
		
		final String path = file.getAbsolutePath();
        
		//Try to load in thread so that we don't have to wait for checking
		Thread thread = new Thread(new Runnable(){
			@Override
			public void run() {
				//Store initial text for restoring
				SwingUtilities.invokeLater( new Runnable(){
					@Override
					public void run() {
						inputPathField.setEditable(false);
						inputPathField.setText("Checking file...");
					}
				});
				
	            
	            final boolean success = Wrapper.getWrapper().setInputFile(path);
	            if ( success ) {
	            	SwingUtilities.invokeLater( new Runnable(){
						@Override
						public void run() {
							Logger.logVerbose("Succesfully loaded file");
							inputPathField.setText(path);
							inputPathField.setEditable(true);
						}
	            	});
	            } else {
	            	//Set error message
	            	//Set oldText back
	            	SwingUtilities.invokeLater( new Runnable(){
						@Override
						public void run() {
							inputPathField.setText("Failed to load file");
						}
	            	});
	            	
	            	//sleep
	            	try {
	            		Thread.sleep(3000);
	            	} catch (Exception e){
	            		e.printStackTrace();
	            	}
	            	
	            	//Set oldText back
	            	SwingUtilities.invokeLater( new Runnable(){
						@Override
						public void run() {
							inputPathField.setText(Settings.getSetting(StringSetting.INPUT_FILE_PATH));
							inputPathField.setEditable(true);
						}
	            	});
	            }
			}
		});
		thread.start();
	}

	@Override
	public void focusGained(FocusEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void focusLost(FocusEvent event) {
		JTextField inputPathField = TextFieldRegister.getTextField(
				FileBrowserPanel.PathFieldName.INPUT_PATH_FIELD.name());
		String inputFileName = Settings.getSetting(StringSetting.INPUT_FILE_PATH);
		if ( !inputPathField.getText().equals(inputFileName) ){
			File inputFile = new File(inputPathField.getText());
			setInputFile(inputFile);
		}
			
		
		JTextField outputDirectoryField = TextFieldRegister.getTextField(
				FileBrowserPanel.PathFieldName.OUTPUT_DIRECTORY_FIELD.name());
		String outputDirectory = Settings.getSetting(StringSetting.OUTPUT_FILE_DIRECTORY);
		if ( !outputDirectoryField.getText().equals(outputDirectory) ){
			File outputDirectoryFile = new File(outputDirectoryField.getText());
			setOutputFile(outputDirectoryFile);
		}
	}
}
