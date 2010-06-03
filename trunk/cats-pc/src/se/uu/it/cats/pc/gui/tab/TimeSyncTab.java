package se.uu.it.cats.pc.gui.tab;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import se.uu.it.cats.pc.gui.TimeSyncPanel;

public class TimeSyncTab extends JPanel
{
	public TimeSyncTab()
	{
		super(new BorderLayout());
		
		add(new TimeSyncPanel(), BorderLayout.CENTER);
	}
}
