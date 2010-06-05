package se.uu.it.cats.pc.gui;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.*;


public class MainPanel extends JPanel implements ActionListener{ // implements ActionListener
  
  private Area world;
  private int _areaHeight;
  private JLabel labelArenaWidth;
  private JLabel labelArenaHeight;
  private JTextField inputArenaWidth;
  private JTextField inputArenaHeight;
  private JButton buttonAreaWidthHeight;
  private String temp;
  
  public void actionPerformed(ActionEvent ae) {
	
		if (ae.getSource() == buttonAreaWidthHeight) {
			try {
				temp = inputArenaHeight.getText();
				world.setArenaHeight(Integer.parseInt(temp));
				temp = inputArenaWidth.getText();
				world.setArenaWidth(Integer.parseInt(temp));
			}	
			  	
			catch (NullPointerException e) {
				System.out.println("No data in infoBox");
			}		  
		}
	}

  public MainPanel(int areaHeight) {	
	  
	world = Area.getInstance();
	_areaHeight = areaHeight;
	// Size of window
    setPreferredSize(new Dimension(200,_areaHeight));
    
	// Bakgrundsfärgen
    setBackground(Color.white);
    setBorder(new TitledBorder(
			new LineBorder(Color.gray, 1, true),
			"Settings",
			TitledBorder.LEFT,
			TitledBorder.DEFAULT_POSITION)
	);
    
	setLayout(new GridLayout(12,1,0,0)); //rows, cols, hgap, vgap
	
	labelArenaWidth = new JLabel("Distance X [cm]");
	labelArenaHeight = new JLabel("Distance Y [cm]");
	inputArenaWidth = new JTextField(""+world.getArenaWidth());
	inputArenaHeight = new JTextField(""+world.getArenaHeight());
	inputArenaWidth.setColumns(5); 
	inputArenaHeight.setColumns(5);
	buttonAreaWidthHeight = new JButton("Update");
	buttonAreaWidthHeight.addActionListener(this);
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

