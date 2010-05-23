package se.uu.it.cats.pc.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.TextArea;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class BillBoardPanel extends JPanel
{
	public BillBoardPanel()
	{
		super(new BorderLayout());
		
		JTextArea textArea = new JTextArea();
		
		for (int i = 0; i < 33; i++)
			textArea.append("0.015  ");
			
		add(new JScrollPane(textArea), BorderLayout.CENTER);
	}
	
	private class UpdatesList extends JPanel
	{
		public UpdatesList()
		{
			JList list = new JList();
		}
	}
}
