package se.uu.it.cats.pc.gui.tab;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import se.uu.it.cats.pc.gui.TimeSyncPanel;

public class TimeSyncTab extends JPanel
{
	public TimeSyncTab()
	{
		super(new BorderLayout());
		
		add(new TimeSyncPanel(), BorderLayout.CENTER);
	}
}
