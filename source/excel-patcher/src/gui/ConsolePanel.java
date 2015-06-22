package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.io.PrintStream;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * The JPanel containing the console.
 * 
 * @author Ashton Dyer (WabashCannon)
 *
 */
public class ConsolePanel extends JPanel {
	private static final long serialVersionUID = -214261387766729929L;
	/** The printstream that prints to the TextAreaOutputStream */
	private PrintStream printStream;
	
	/**
	 * Creates a new console panel and sets the System to print to it.
	 */
	public ConsolePanel(){
		this.setBackground( new Color( 0, 0, 255) );
		
		setLayout( new BorderLayout() );
		
		
		JTextArea console = new JTextArea();
		
		JScrollPane scrollPanel = new JScrollPane(console, 
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		add(scrollPanel, BorderLayout.CENTER);
		//scrollPanel.add(console);
		
		printStream=new PrintStream(new TextAreaOutputStream(console));
		//System.setOut(printStream);
		//System.setErr(printStream);
	}
	
	/**
	 * Returns the print stream of this ConsolePanel
	 * 
	 * @return the print stream of this ConsolePanel
	 */
	public PrintStream getPrintStream(){
		return printStream;
	}
}
