package gui;

import gui.GeneralActionListener.ActionCommand;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import utils.Logger;

public class PathSelectorActionListener implements ActionListener {
	public enum ActionCommand{
		SELECT_INPUT_FILE_PATH,
		OPEN_INPUT_FILE,
		SELECT_OUTPUT_FILE_DIRECTORY,
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
			case OPEN_INPUT_FILE:
				Wrapper.getWrapper().openInputFile();
				break;
			case SELECT_OUTPUT_FILE_DIRECTORY:
				selectOutputFile();
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
	
	private void setOutputFile(File file){
		boolean changedPath = Wrapper.getWrapper().setOutputFile(file.getAbsolutePath());
		
		if ( changedPath ){
			String fieldName = FileBrowserPanel.PathFieldName.OUTPUT_DIRECTORY_FIELD.name();
			JTextField field = TextFieldRegister.getTextField(fieldName);
			if ( field != null ){
				field.setText(file.getAbsolutePath());
			}
		}
	}
	
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
            final String path = file.getAbsolutePath();
            final String oldText = inputPathField.getText();
            //Log the action
			Logger.logVerbose("Trying to load excel file at "+path);
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
								inputPathField.setText(oldText);
								inputPathField.setEditable(true);
							}
		            	});
		            }
				}
			});
			thread.start();
        }
	}

}
