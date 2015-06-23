package gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import utils.FileManager;
import utils.Logger;

public class GeneralActionListener implements ActionListener{
	public enum ActionCommand{ 
		CHECK, 
		CLEAN, 
		EDIT_FORMAT_FILE, 
		OPEN_FORMAT_MANUAL,
		EXIT
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
			case CHECK:
				Wrapper.getWrapper().checkFile();
				break;
			case CLEAN:
				Wrapper.getWrapper().cleanFile();
				break;
			case EDIT_FORMAT_FILE:
				FileManager.editFormatFile();
				break;
			case OPEN_FORMAT_MANUAL:
				FileManager.openFormatManual();
				break;
			case EXIT:
				System.exit(0);
				break;
			default:
				Logger.log("Error", this.getClass().getName()+" cannot process action"
						+" command with the name "+actionCommandName );
				return;
		}
	}
}
