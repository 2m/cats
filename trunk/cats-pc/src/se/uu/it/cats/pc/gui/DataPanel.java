package se.uu.it.cats.pc.gui;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.event.*;

import se.uu.it.cats.pc.network.ConnectionHandler;

import java.util.Hashtable;

public class DataPanel extends JPanel{ // implements ActionListener
  
  private Area world;
  private int _areaHeight;
  private JLabel idLabel;
  
  public DataPanel(Area newArea, int areaHeight) {
	world = newArea;
	_areaHeight = areaHeight;
	// Size of window
    setPreferredSize(new Dimension(200,_areaHeight));
    
    ActionListener buttonListener = new ActionListener() {                    
        public void actionPerformed(ActionEvent e)
        {
        	JButton button = (JButton)e.getSource();
        	Thread t = new Thread(new ConnectionHandler(button.getText()));
    		t.start();
        }
    };
    
	// Bakgrundsfärgen
    setBackground(Color.white);
	setLayout(new GridLayout(12,1,0,0)); //rows, cols, hgap, vgap
	for(int i=0;i < world.getCats().length;i++) {
		idLabel = new JLabel("ID: "+world.getCats()[i].getCatName());
		idLabel.setFont(new Font("Monotype Corsiva",1,17));
		add(idLabel);
		add(new JLabel("Position: ("+world.getCats()[i].getX()+","+world.getCats()[i].getY()+")"));
		JButton button = new JButton(world.getCats()[i].getCatName());
		button.addActionListener(buttonListener);
		add(button);
	}	
  }
};
