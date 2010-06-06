package se.uu.it.cats.pc.gui.tab;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import se.uu.it.cats.pc.gui.GuidePanel;
import se.uu.it.cats.pc.gui.TimeSyncPanel;

public class GuideTab extends JPanel
{
	public GuideTab()
	{
		super(new BorderLayout());
		
		add(new GuidePanel(), BorderLayout.CENTER);
	}
}