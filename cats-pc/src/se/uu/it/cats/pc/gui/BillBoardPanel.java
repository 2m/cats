package se.uu.it.cats.pc.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.TextArea;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

public class BillBoardPanel extends JPanel
{
	private LatestSighting[] _latestSightings = new LatestSighting[3]; 
	
	public BillBoardPanel()
	{
		//super(new BorderLayout());
		
		for (int i = 0; i < Area.CAT_COUNT; i++)
		{
			_latestSightings[i] = new LatestSighting(i);
			add(_latestSightings[i]);			
		}
	}
	
	public void repaint()
	{
		super.repaint();
		
		try
		{
			for (LatestSighting ls: _latestSightings)
				ls.repaint();
		}
		catch (NullPointerException ex) {}
	}
	
	private class LatestSighting extends JPanel
	{
		private int _id;
		
		private JLabel _x, _y, _theta, _timestamp;
		
		public LatestSighting(int id)
		{
			// GridLayout(int rows, int cols, int hgap, int vgap) 
			super(new GridLayout(4, 2, 10, 0));
			
			_id = id;
			
			setBorder(new TitledBorder(
					new LineBorder(Color.gray, 1, false),
					"cat"+ _id + " LatestSighting",
					TitledBorder.LEFT,
					TitledBorder.DEFAULT_POSITION)
			);
			
			add(new JLabel("x"));
			_x = new JLabel("1");
			add(_x);
			
			add(new JLabel("y"));
			_y = new JLabel("2");
			add(_y);
			
			add(new JLabel("theta"));
			_theta = new JLabel("3");
			add(_theta);
			
			add(new JLabel("timestamp"));
			_timestamp = new JLabel("4");
			add(_timestamp);
		}
		
		public void repaint()
		{
			try
			{
				super.repaint();
				
				_x.setText(String.format("%07.3f", BillBoard.getInstance().getLatestSightingX(_id)));
				_y.setText(String.format("%07.3f", BillBoard.getInstance().getLatestSightingY(_id)));
				_theta.setText(String.format("%07.3f", BillBoard.getInstance().getLatestSightingTheta(_id)));
				_timestamp.setText(String.format("%07.3f", BillBoard.getInstance().getLatestSightingTimestamp(_id)));
			}
			catch (NullPointerException ex) {}
		}
		
	}
}
