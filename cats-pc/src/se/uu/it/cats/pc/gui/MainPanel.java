package se.uu.it.cats.pc.gui;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.event.*;


public class MainPanel extends JPanel{ // implements ActionListener
  
  private Area world;
  private int _areaHeight;
  private JLabel settingsLabel;
  private JLabel labelArenaWidth;
  private JLabel labelArenaHeight;
  private JTextField inputArenaWidth;
  private JTextField inputArenaHeight;
  private JButton buttonAreaWidthHeight;
  private String temp;
  
  public void actionPerformed(ActionEvent ae) {
		//If save-button is pressed  
	
		if (ae.getSource() == buttonAreaWidthHeight) {
			try {
				temp = inputArenaHeight.getText();
				world.setArenaHeight(Integer.parseInt(temp));
				temp = inputArenaWidth.getText();
				world.setArenaWidth(Integer.parseInt(temp));
				System.out.println("Tryck");
			}	
			  	
			catch (NullPointerException e) {
				System.out.println("No data in infoBox");
			}		  
		}
	}

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
	
	labelArenaWidth = new JLabel("Distance X");
	labelArenaHeight = new JLabel("Distance Y");
	inputArenaWidth = new JTextField(world.getArenaWidth());
	inputArenaHeight = new JTextField(world.getArenaHeight());
	inputArenaWidth.setColumns(5); 
	inputArenaHeight.setColumns(5);
	buttonAreaWidthHeight = new JButton("Update");
	add(labelArenaWidth);
	add(inputArenaWidth);
	add(labelArenaHeight);
	add(inputArenaHeight);
	add(buttonAreaWidthHeight);
	//add(new JLabel("Position: ("+world.getCats()[i].getX()+","+world.getCats()[i].getY()+")"));
	//JButton button = new JButton(world.getCats()[i].getCatName());
	//button.addActionListener(buttonListener);
	//add(button);
		
  }
};

