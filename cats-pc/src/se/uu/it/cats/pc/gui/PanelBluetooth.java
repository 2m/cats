package se.uu.it.cats.pc.gui;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.*;

import se.uu.it.cats.pc.network.ConnectionHandler;
import se.uu.it.cats.pc.network.ConnectionManager;

import se.uu.it.cats.brick.network.packet.CloseConnection;
import se.uu.it.cats.brick.network.packet.Timestamp;
import se.uu.it.cats.brick.network.packet.PFMeasurement;
import se.uu.it.cats.brick.network.packet.SimpleMeasurement;

import java.util.Hashtable;

public class PanelBluetooth extends JPanel implements ActionListener{
  
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
	super(new BorderLayout());
	_areaWidth = areaWidth;
	setVisible(true);
	
	// connection panels
	JPanel panel = new JPanel(new BorderLayout());
	panel.add(new ConnectionPanel("cat1"), BorderLayout.NORTH);
	panel.add(new ConnectionPanel("cat2"), BorderLayout.CENTER);
	panel.add(new ConnectionPanel("cat3"), BorderLayout.SOUTH);
	add(panel, BorderLayout.WEST);

	panel = new JPanel(new BorderLayout());	
	panel.setBorder(new TitledBorder(
			new LineBorder(Color.gray, 1, false),
			"Network log",
			TitledBorder.LEFT,
			TitledBorder.DEFAULT_POSITION)
	);
	// info box for packets
	infoBox = new JTextArea(4, 43);
	panel.add(new JScrollPane(infoBox), BorderLayout.CENTER);
	for(int i=0;i<50;i++){
		infoBox.append("Info1 \tInfo2 \tInfo3\n");
	}
	
	// Filewriting button
	savefileButton = new JButton("Save");
	savefileButton.addActionListener(this);
	panel.add(savefileButton, BorderLayout.EAST);
	
	panel.add(new PacketPanel(), BorderLayout.SOUTH);
	
	add(panel, BorderLayout.CENTER);
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
  
  private class ConnectionPanel extends JPanel implements ActionListener
  {
	  String[] _catNames = Area.getInstance().getCatNames();
	  
	  String _catName = null;
	  
	  public ConnectionPanel(String catName)
	  {
		  _catName = catName;
		  
		  setBorder(new TitledBorder(
					new LineBorder(Color.gray, 1, false),
					catName,
					TitledBorder.LEFT,
					TitledBorder.DEFAULT_POSITION)
		  );
		  
		  JButton connectionButton = new JButton("Connect")
		  {
			  public void repaint()
			  {
				  int i = ConnectionManager.getInstance().getIdByName(_catName);
				  if (ConnectionManager.getInstance().isAlive(i))
					  this.setText("Connected");
				  else if (ConnectionManager.getInstance().isCreated(i))
					  this.setText("Connecting");
				  else
					  this.setText("Connect");
				  
				  super.repaint();
			  }
		  };
		  connectionButton.addActionListener(this);
		  add(connectionButton);
		  
		  add(new JLabel("FW traffic to:"));
		  
		  ActionListener ae = new ActionListener()
		  {
			  public void actionPerformed(ActionEvent ae)
			  {
				  JCheckBox checkBox = (JCheckBox)ae.getSource();
				  if (checkBox.isSelected())
					  ConnectionManager.getInstance().remIgnore(_catName, checkBox.getText());
				  else
					  ConnectionManager.getInstance().addIgnore(_catName, checkBox.getText());
			  }
		  };
		  
		  for (String name: _catNames)
		  {
			  if (!name.equals(catName))
			  {
				  JCheckBox checkBox = new JCheckBox(name);
				  checkBox.setSelected(true);
				  checkBox.addActionListener(ae);
				  add(checkBox);
			  }
		  }
	  }
	  
	  public void actionPerformed(ActionEvent ae)
	  {
		  // if connect/disconnect button is pressed
		  ConnectionManager.getInstance().openConnection(_catName);
	  }
  }
  
  private class PacketPanel extends JPanel implements ActionListener
  {
	  JTextField _param1;
	  JTextField _param2;
	  JTextField _param3;
	  JTextField _param4;
	  JTextField _param5;
	  
	  JComboBox _packetList = null;
	  ButtonGroup _receiverGroup = null;
	  
	  public PacketPanel()
	  {
		  super(new BorderLayout());
		  
		  setBorder(new TitledBorder(
					new LineBorder(Color.gray, 1, false),
					"Send packet",
					TitledBorder.LEFT,
					TitledBorder.DEFAULT_POSITION)
		  );
		  
		  String[] packetStrings = {
				  "-1 CloseConnection",
				  "0x00 TimeStamp",
				  "0x01 PFMeasurement",
				  "0x02 SimpleMeasurement"
		  };

		  JPanel panel = new JPanel();
		  _packetList = new JComboBox(packetStrings);
		  _packetList.addActionListener(this);
		  panel.add(_packetList);
		  
		  _param1 = new JTextField(5);
		  _param2 = new JTextField(5);
		  _param3 = new JTextField(5);
		  _param4 = new JTextField(5);
		  _param5 = new JTextField(5);
		  panel.add(_param1);
		  panel.add(_param2);
		  panel.add(_param3);
		  panel.add(_param4);
		  panel.add(_param5);
		  
		  add(panel, BorderLayout.NORTH);
		  
		  _receiverGroup = new ButtonGroup();
		  
		  panel = new JPanel();
		  JRadioButton catButton = new JRadioButton("cat1");
		  catButton.setSelected(true);
		  catButton.setActionCommand("cat1");
		  _receiverGroup.add(catButton);
		  panel.add(catButton);
		  
		  catButton = new JRadioButton("cat2");
		  catButton.setActionCommand("cat2");
		  _receiverGroup.add(catButton);
		  panel.add(catButton);
		  
		  catButton = new JRadioButton("cat3");
		  catButton.setActionCommand("cat3");
		  _receiverGroup.add(catButton);
		  panel.add(catButton);
		  
		  JButton sendButton = new JButton("Send");
		  sendButton.addActionListener(this);
		  panel.add(sendButton);
		  
		  add(panel, BorderLayout.SOUTH);
	  }
	  
	  public void actionPerformed(ActionEvent e)
	  {
		  String packetName = (String)_packetList.getSelectedItem();
		  String receiver = _receiverGroup.getSelection().getActionCommand();
		  
			if (packetName.equals("-1 CloseConnection"))
			{
				if (e.getSource() instanceof JComboBox)
				{
					_param1.setText("");
					_param2.setText("");
					_param3.setText("");
					_param4.setText("");
					_param5.setText("");
				}
				else if (e.getSource() instanceof JButton)
				{
					ConnectionManager.getInstance().sendPacketTo(receiver, new CloseConnection());
				}
			}
			else if (packetName.equals("0x00 TimeStamp"))
			{
				if (e.getSource() instanceof JComboBox)
				{
					_param1.setText("timestamp");
					_param2.setText("roundTripTime");
					_param3.setText("");
					_param4.setText("");
					_param5.setText("");
				}
				else if (e.getSource() instanceof JButton)
				{
					ConnectionManager.getInstance().sendPacketTo(receiver, new Timestamp(
							Integer.valueOf(_param1.getText())
					));
				}
			}
			else if (packetName.equals("0x01 PFMeasurement"))
			{
				if (e.getSource() instanceof JComboBox)
				{
					_param1.setText("id");
					_param2.setText("y");
					_param3.setText("angle_m");
					_param4.setText("x_c");
					_param5.setText("y_c");
				}
				else if (e.getSource() instanceof JButton)
				{
					ConnectionManager.getInstance().sendPacketTo(receiver, new PFMeasurement(
							Integer.valueOf(_param1.getText()),
							Integer.valueOf(_param2.getText()),
							Float.valueOf(_param3.getText()),
							Float.valueOf(_param4.getText()),
							Float.valueOf(_param5.getText())
					));
				}
			}
			else if (packetName.equals("0x02 SimpleMeasurement"))
			{
				if (e.getSource() instanceof JComboBox)
				{
					_param1.setText("angle");
					_param2.setText("");
					_param3.setText("");
					_param4.setText("");
					_param5.setText("");
				}
				else if (e.getSource() instanceof JButton)
				{
					ConnectionManager.getInstance().sendPacketTo(receiver, new SimpleMeasurement(
							Integer.valueOf(_param1.getText())
					));
				}
			}
	  }
  }
}
