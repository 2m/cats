package se.uu.it.cats.pc.gui;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.event.*;

import se.uu.it.cats.pc.network.ConnectionHandler;

import java.util.Hashtable;

public class PanelBluetooth extends JPanel{ // implements ActionListener
  
  private int _areaWidth;
  private JTextArea infoBox;
  
  public PanelBluetooth(int areaWidth) {
	_areaWidth = areaWidth;
	infoBox = new JTextArea(5,43);
	setPreferredSize(new Dimension(_areaWidth,100));
	add(infoBox);
	add(new JScrollPane(infoBox));
	
	setVisible(true);
	for(int i=0;i<50;i++){
		infoBox.append("Info1 \tInfo2 \tInfo3\n");
	}
  }
  
  public void updatePacket(String newPacket) {
	  infoBox.append(newPacket);
  }
}
