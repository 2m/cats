package se.uu.it.cats.pc.gui;

import java.awt.*;
import javax.swing.*;
import java.io.*;
import java.awt.event.*;
import javax.swing.event.*;

public class GrafikTest extends JFrame {

  public static void main(String[] args) {
	
	JFrame frame = new JFrame("Arena");
	
	JPanel panelLeft = new JPanel(new BorderLayout());
	JPanel panelRight = new JPanel();
	
	JButton button1 = new JButton("Button Text");
	JLabel label1 = new JLabel("Label Text");
	panelLeft.add(button1, BorderLayout.NORTH);
	panelLeft.add(label1, BorderLayout.EAST);
	
	frame.setDefaultCloseOperation(frame.EXIT_ON_CLOSE);

	frame.getContentPane().add(panelLeft);
	//frame.getContentPane().add(panelRight);

	
	frame.pack();
	frame.show();
	

	
  }
}
