package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.io.PrintStream;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class ConsolePanel extends JPanel {
	private static final long serialVersionUID = -214261387766729929L;
	private PrintStream con;
	
	public ConsolePanel(){
		this.setBackground( new Color( 0, 0, 255) );
		
		setLayout( new BorderLayout() );
		
		
		JTextArea console = new JTextArea();
		
		JScrollPane scrollPanel = new JScrollPane(console, 
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		add(scrollPanel, BorderLayout.CENTER);
		//scrollPanel.add(console);
		
		con=new PrintStream(new TextAreaOutputStream(console));
		System.setOut(con);
		System.setErr(con);
	}
	
	public PrintStream getPrintStream(){
		return con;
	}
}
