package se.uu.it.cats.pc.gui.tab;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import se.uu.it.cats.pc.gui.BillBoardPanel;

public class BillBoardTab extends JPanel
{
	BillBoardPanel _billBoardPanel = null;
	
	public BillBoardTab()
	{
		super(new BorderLayout());
		
		_billBoardPanel = new BillBoardPanel();
		
		add(_billBoardPanel, BorderLayout.CENTER);
	}
	
	public void repaint()
	{
		super.repaint();
		
		try {
			_billBoardPanel.repaint();
		}
		catch (NullPointerException ex) {
			
		}
	}
}
