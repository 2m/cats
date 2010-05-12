package se.uu.it.cats.pc.gui;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import java.awt.*;
import java.awt.event.*;

import se.uu.it.cats.pc.network.ConnectionManager;

public class DataPanel extends JPanel{ // implements ActionListener
  
  private Area world;
  private int _areaHeight;
  private JLabel idLabel;
  
  public DataPanel(int areaHeight) {
	world = Area.getInstance();
	_areaHeight = areaHeight;
	// Size of window
    //setPreferredSize(new Dimension(200,_areaHeight));
    
	// Bakgrundsfärgen
    setBackground(Color.white);
    setBorder(new TitledBorder(
			new LineBorder(Color.gray, 1, true),
			"Information",
			TitledBorder.LEFT,
			TitledBorder.DEFAULT_POSITION)
	);
    
	setLayout(new GridLayout(12,1,0,0)); //rows, cols, hgap, vgap
	for(int i=0;i < world.getCats().length;i++) {
		idLabel = new JLabel("ID: "+world.getCats()[i].getCatName());
		idLabel.setFont(new Font("Monotype Corsiva",1,17));
		add(idLabel);
		add(new JLabel("Position: ("+world.getCats()[i].getX()+","+world.getCats()[i].getY()+")"));
		add(new JLabel("Angle: "+world.getCats()[i].getAngle_c()+", Angle cam: "+world.getCats()[i].getAngle_cam()));

	}	
  }
};
