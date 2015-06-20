package gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

/**
 * The gui panel that contains the "Check" and "Clean" buttons
 * 
 * @author Ashton Dyer (WabashCannon)
 *
 */
public class ActionButtonsPanel extends JPanel {
	private static final long serialVersionUID = -8805326649043675900L;
	
	/**
	 * Creates the action buttons panel and populates it with functioning
	 * buttons.
	 */
	public ActionButtonsPanel(){
		setLayout( new BoxLayout(this, BoxLayout.X_AXIS) );
		
		JButton checkButton = new JButton("Check");
		checkButton.setToolTipText("Runs the checker and produces an output excel file");
		checkButton.addActionListener( new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				Wrapper.getWrapper().checkFile();
			}
		});
		
		JButton cleanButton = new JButton("Clean");
		cleanButton.setToolTipText("Cleans the output file of any comments and cell coloring");
		cleanButton.addActionListener( new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				Wrapper.getWrapper().cleanFile();
			}
		});
		
		add(checkButton);
		add(cleanButton);
	}
	
}
