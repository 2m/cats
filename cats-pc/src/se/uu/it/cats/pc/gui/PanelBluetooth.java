package se.uu.it.cats.pc.gui;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.swing.event.*;

import se.uu.it.cats.pc.network.ConnectionHandler;

import java.util.Hashtable;

public class PanelBluetooth extends JPanel implements ActionListener{ // implements ActionListener
  
  private int _areaWidth;
  private JTextArea infoBox;
  private JButton savefileButton;
  private String dataToSave;
  
  public void actionPerformed(ActionEvent ae) {
	  //If save-button is pressed  
	  if (ae.getSource() == savefileButton) {
		  try {
			  dataToSave = infoBox.getText();
			  writeFile(dataToSave);
		  }
		  
		  catch (NullPointerException e) {
			  System.out.println("No data in infoBox");
		  }	  
	  }
  }
  
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
	
	// Filewriting button
	savefileButton = new JButton("Save");
	savefileButton.addActionListener(this);
	add(savefileButton);
  }
  
  public void updatePacket(String newPacket) {
	  infoBox.append(newPacket);
  }
  
  // Write to a file
  public void writeFile(String datatext) {
    try {
      String file = "bluetooth_data.txt";
      FileWriter fw = new FileWriter(file);
      BufferedWriter bw = new BufferedWriter(fw);
      PrintWriter outFile = new PrintWriter(bw);

      outFile.println(datatext);
      outFile.close();
    }

    catch (FileNotFoundException e1) {
      System.out.println("File does not exist");
    }

    catch(IOException e2) {
      System.out.println(e2);
    }
  }
}
