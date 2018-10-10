

package edu.ascendingbid;

import jade.core.AID;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


/**
  @author Giovanni Caire - TILAB
 */
class AuctioneerGui extends JFrame {
	
	private AuctioneerAgent myAgent;
	
	private JTextField titleField, priceField, stockField;
	
	AuctioneerGui(AuctioneerAgent a) {
		super(a.getLocalName());
		
		myAgent = a;
		
		JPanel p = new JPanel();
		p.setLayout(new GridLayout(3,3));
		
		JButton startButton = new JButton("Start");
		startButton.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				try {
					myAgent.start();
				}
				catch (Exception e) {
					JOptionPane.showMessageDialog(AuctioneerGui.this, "Invalid values. "+e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); 
				}
			}
		} );
		p = new JPanel();
		p.add(startButton);
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
}