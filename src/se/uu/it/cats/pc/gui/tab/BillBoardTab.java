package se.uu.it.cats.pc.gui.tab;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import se.uu.it.cats.pc.gui.BillBoardPanel;

public class BillBoardTab extends JPanel
{
	public BillBoardTab()
	{
		super(new BorderLayout());
		
		add(new BillBoardPanel(), BorderLayout.CENTER);
	}
}
