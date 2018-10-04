

package edu.marketplace;

import jade.core.AID;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
  @author Giovanni Caire - TILAB
 */
class BookAdvertiserGui extends JFrame {	
	private BookAdvertiserAgent myAgent;
	
	private List l1;
	
	BookAdvertiserGui(BookAdvertiserAgent a) {
		super(a.getLocalName());
		
		myAgent = a;
		
		JPanel p = new JPanel();		
		p.setLayout(new GridLayout(2, 2));
		
        l1=new List(5);  
        l1.setBounds(100,100, 75,75);
        p.add(l1);  
		
		getContentPane().add(p, BorderLayout.CENTER);
		
		// Make the agent terminate when the user closes 
		// the GUI using the button on the upper right corner	
		addWindowListener(new	WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				myAgent.doDelete();
			}
		} );
		
		setResizable(false);
	}
	
	public void showGui() {
		pack();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int centerX = (int)screenSize.getWidth() / 2;
		int centerY = (int)screenSize.getHeight() / 2;
		setLocation(centerX - getWidth() / 2, centerY - getHeight() / 2);
		super.setVisible(true);
	}

	public void addToGui(String book) {
		
		l1.add(book);
	}
	
	public void removeFromGui(String book) {
		
		l1.remove(book);
	}
}