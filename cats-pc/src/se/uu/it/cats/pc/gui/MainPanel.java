package se.uu.it.cats.pc.gui;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;


public class MainPanel extends JPanel{ // implements ActionListener
  
  private Area world;
  private int _areaHeight;
  private JLabel settingsLabel;
  
  public MainPanel(Area newArea, int areaHeight) {
	world = newArea;
	_areaHeight = areaHeight;
	// Size of window
    setPreferredSize(new Dimension(200,_areaHeight));
    
	// Bakgrundsfärgen
    setBackground(Color.white);
	setLayout(new GridLayout(12,1,0,0)); //rows, cols, hgap, vgap
	
	settingsLabel = new JLabel("Settings");
	settingsLabel.setFont(new Font("Monotype Corsiva",1,24));
	add(settingsLabel);
	//add(new JLabel("Position: ("+world.getCats()[i].getX()+","+world.getCats()[i].getY()+")"));
	//JButton button = new JButton(world.getCats()[i].getCatName());
	//button.addActionListener(buttonListener);
	//add(button);
		
  }
};

