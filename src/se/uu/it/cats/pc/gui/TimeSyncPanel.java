package se.uu.it.cats.pc.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import se.uu.it.cats.brick.network.packet.CloseConnection;
import se.uu.it.cats.brick.network.packet.PlayMusicOrder;
import se.uu.it.cats.brick.network.packet.StartOrder;
import se.uu.it.cats.brick.network.packet.SyncTimeOrder;
import se.uu.it.cats.pc.network.ConnectionManager;

public class TimeSyncPanel extends JPanel
{
	public TimeSyncPanel()
	{
		super(new BorderLayout());
		
		JPanel panel = new JPanel();
		
		JButton button = new JButton("Tell cat1 to sync");
		button.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ae)
			{
				ConnectionManager.getInstance().sendPacketTo("cat1", new SyncTimeOrder());
			}
		});			
		panel.add(button);
		
		button = new JButton("Tell cat2 to sync");
		button.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ae)
			{
				ConnectionManager.getInstance().sendPacketTo("cat2", new SyncTimeOrder());
			}
		});		
		panel.add(button);
		
		button = new JButton("Tell all to start");
		button.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ae)
			{
				ConnectionManager.getInstance().sendPacketTo("cat0", new StartOrder());
				ConnectionManager.getInstance().sendPacketTo("cat1", new StartOrder());
				ConnectionManager.getInstance().sendPacketTo("cat2", new StartOrder());
			}
		});		
		panel.add(button);
		
		button = new JButton("Tell all to play music");
		button.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ae)
			{
				ConnectionManager.getInstance().sendPacketTo("cat0", new PlayMusicOrder());
				ConnectionManager.getInstance().sendPacketTo("cat1", new PlayMusicOrder());
				ConnectionManager.getInstance().sendPacketTo("cat2", new PlayMusicOrder());
			}
		});		
		panel.add(button);
		
		add(panel, BorderLayout.CENTER);
	}
}
