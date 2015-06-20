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

import utils.Logger;

/**
 * The JPanel containing the input/output path text fields and the respective
 * browse and open buttons.
 * @author Ashton Dyer (WabashCannon)
 *
 */
public class FileBrowserPanel extends JPanel {
	
	private static final long serialVersionUID = 1155275561612671359L;
	
	/**
	 * Creates a new file browser panel with the specified input filePath and
	 * output filePath
	 * @param inputFile
	 * @param outputFile
	 */
	public FileBrowserPanel(String inputFile, String outputFile){
		this.inputFile = inputFile;
		this.outputFile = outputFile;
		
		//Tmp stuff
		this.setBackground( new Color(255, 0, 0) );
		
		//Set the layout manager
		setLayout( new BorderLayout() );
		
		//Fill it with content
		createBrowserLines();
	}
	
	/**
	 * Creates a new file browser panel with empty fields.
	 */
	public FileBrowserPanel(){
		this(null, null);
	}
	
	/** The inputFilePath */
	private String inputFile = null;
	/** The outputFilePath */
	private String outputFile = null;
	
	/** The size of the border in pixels */
	private int borderSize = 5;
	/** Each row's JPanel border */
	Border border = new EmptyBorder(borderSize, borderSize, borderSize, borderSize);
	/** The label border */
	Border border2 = new EmptyBorder(borderSize, borderSize, borderSize, borderSize);
	/**
	 * Creates text fields and buttons
	 */
	private void createBrowserLines(){
		JPanel inputPanel = new JPanel();
		inputPanel.setBorder(border);
		inputPanel.setLayout( new BorderLayout() );
		
		JLabel inputLabel = new JLabel("Input File: ");
		inputLabel.setBorder( border2 );
		
		final JTextField inputPathField = new JTextField();
		if ( inputFile != null ){
			inputPathField.setText(inputFile);
		}
		
		JButton inputButton = new JButton("Browse");
		inputButton.addActionListener( new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
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
		});
		
		JButton inputOpenButton = new JButton("Open");
		inputOpenButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Wrapper.getWrapper().openInputFile();
			}
		});
		
		JPanel inputButtonsPanel = new JPanel();
		inputButtonsPanel.add(inputButton);
		inputButtonsPanel.add(inputOpenButton);
		
		inputPanel.add(inputLabel, BorderLayout.WEST);
		inputPanel.add(inputPathField, BorderLayout.CENTER);
		inputPanel.add(inputButtonsPanel, BorderLayout.EAST);
		
		add(inputPanel, BorderLayout.NORTH );
		
		//Output line
		JPanel outputPanel = new JPanel();
		outputPanel.setBorder(border);
		outputPanel.setLayout( new BorderLayout() );
		
		JLabel outputLabel = new JLabel("Output Folder:");
		outputLabel.setBorder(border);
		
		final JTextField outputPathField = new JTextField();
		if ( outputFile != null ){
			outputPathField.setText(outputFile);
		}
		JButton outputButton = new JButton("Browse");
		outputButton.addActionListener( new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				final JFileChooser fc = new JFileChooser();
				fc.setDialogTitle("Choose an output folder...");
			    fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			    fc.setAcceptAllFileFilterUsed(false);
			    
				int returnVal = fc.showOpenDialog( GUI.getGUI().getContentPane() );
				
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					outputPathField.setText(file.getAbsolutePath());
					Wrapper.getWrapper().setOutputFile(file.getAbsolutePath());
				}
			}
		});
		
		JButton outputOpenButton = new JButton("Open");
		outputOpenButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Wrapper.getWrapper().openOutputFile();
			}
		});
		
		JPanel outputButtonsPanel = new JPanel();
		outputButtonsPanel.add(outputButton);
		outputButtonsPanel.add(outputOpenButton);
		
		outputPanel.add(outputLabel, BorderLayout.WEST);
		outputPanel.add(outputPathField, BorderLayout.CENTER);
		outputPanel.add(outputButtonsPanel, BorderLayout.EAST);
		
		add(outputPanel, BorderLayout.SOUTH );
		
	}
}
