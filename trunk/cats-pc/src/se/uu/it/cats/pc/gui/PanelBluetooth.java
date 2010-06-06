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

import se.uu.it.cats.pc.network.ConnectionManager;

import se.uu.it.cats.brick.network.packet.CloseConnection;
import se.uu.it.cats.brick.network.packet.Timestamp;
import se.uu.it.cats.brick.network.packet.PFMeasurement;
import se.uu.it.cats.brick.network.packet.SimpleMeasurement;

public class PanelBluetooth extends JPanel{

	private static JTextArea _infoBox;
	
	private ConnectionPanel[] _connectionPanels = new ConnectionPanel[Area.CAT_COUNT];

	public PanelBluetooth() {
		super(new BorderLayout());		
		setVisible(true);
		
		setBorder(new TitledBorder(
				new LineBorder(Color.gray, 1, false),
				"Connectivity",
				TitledBorder.LEFT,
				TitledBorder.DEFAULT_POSITION)
		);

		// connection panels
		JPanel panel = new JPanel(new BorderLayout());
		_connectionPanels[0] = new ConnectionPanel("cat0");
		panel.add(_connectionPanels[0], BorderLayout.NORTH);		
		_connectionPanels[1] = new ConnectionPanel("cat1");
		panel.add(_connectionPanels[1], BorderLayout.CENTER);
		_connectionPanels[2] = new ConnectionPanel("cat2");
		panel.add(_connectionPanels[2], BorderLayout.SOUTH);
		add(panel, BorderLayout.WEST);

		panel = new JPanel(new BorderLayout());	
		panel.setBorder(new TitledBorder(
				new LineBorder(Color.gray, 1, false),
				"Network log",
				TitledBorder.LEFT,
				TitledBorder.DEFAULT_POSITION)
		);

		// info box for packets
		_infoBox = new JTextArea("Log window for incomming packets.", 4, 43);
		panel.add(new JScrollPane(_infoBox), BorderLayout.CENTER);

		// log buttons panel
		JPanel buttonPanel = new JPanel(new BorderLayout());

		// save log to file button
		JButton savefileButton = new JButton("Save");
		savefileButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ae)
			{
				writeFile(_infoBox.getText());
			}
		});
		buttonPanel.add(savefileButton, BorderLayout.NORTH);

		// clear log window button
		JButton clearButton = new JButton("Clear");
		clearButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ae)
			{
				_infoBox.setText("");
			}
		});
		buttonPanel.add(clearButton, BorderLayout.SOUTH);

		panel.add(buttonPanel, BorderLayout.EAST);

		panel.add(new PacketPanel(), BorderLayout.SOUTH);

		add(panel, BorderLayout.CENTER);
	}

	public static void updatePacket(String newPacket) {
		_infoBox.append(newPacket+"\n");
	}

	// Write to a file
	public void writeFile(String datatext) {
		try
		{
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
	
	public void repaint()
	{
		super.repaint();
		
		try
		{
			for (ConnectionPanel cp: _connectionPanels)
				cp.repaint();
		}
		catch (NullPointerException ex) {}
	}

	private class ConnectionPanel extends JPanel implements ActionListener
	{
		String[] _catNames = Area.getInstance().getCatNames();

		String _catName = null;
		
		JButton connectionButton = null;

		public ConnectionPanel(String catName)
		{
			_catName = catName;

			setBorder(new TitledBorder(
					new LineBorder(Color.gray, 1, false),
					catName,
					TitledBorder.LEFT,
					TitledBorder.DEFAULT_POSITION)
			);

			connectionButton = new JButton("Connect")
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
		
		public void repaint()
		{
			try
			{
				super.repaint();
				connectionButton.repaint();
			}
			catch (NullPointerException ex) {}
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
			JRadioButton catButton = new JRadioButton("cat0");
			catButton.setSelected(true);
			catButton.setActionCommand("cat0");
			_receiverGroup.add(catButton);
			panel.add(catButton);

			catButton = new JRadioButton("cat1");
			catButton.setActionCommand("cat1");
			_receiverGroup.add(catButton);
			panel.add(catButton);

			catButton = new JRadioButton("cat2");
			catButton.setActionCommand("cat2");
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
					_param1.setText("id");
					_param2.setText("angle");
					_param3.setText("camAngle");
					_param4.setText("");
					_param5.setText("");
				}
				else if (e.getSource() instanceof JButton)
				{
					ConnectionManager.getInstance().sendPacketTo(receiver, new SimpleMeasurement(
							Integer.valueOf(_param1.getText()),
							Float.valueOf(_param2.getText()),
							Float.valueOf(_param3.getText())							
					));
				}
			}
		}
	}
}
