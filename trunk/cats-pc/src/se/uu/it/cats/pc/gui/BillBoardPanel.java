package se.uu.it.cats.pc.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import se.uu.it.cats.brick.network.packet.AbsolutePositionUpdate;
import se.uu.it.cats.brick.network.packet.LatestSightingUpdate;
import se.uu.it.cats.pc.network.ConnectionManager;

public class BillBoardPanel extends JPanel
{
	private LatestSighting[] _latestSightings = new LatestSighting[3];
	private AbsolutePosition[] _absolutePositions = new AbsolutePosition[3];
	private MeanAndCovariance[] _meanAndCovariance = new MeanAndCovariance[3];
	
	public BillBoardPanel()
	{
		super(new BorderLayout());
		
		JPanel panel = new JPanel();
		for (int i = 0; i < Area.CAT_COUNT; i++)
		{
			_latestSightings[i] = new LatestSighting(i);
			panel.add(_latestSightings[i]);			
		}
		add(panel, BorderLayout.NORTH);
		
		panel = new JPanel();
		for (int i = 0; i < Area.CAT_COUNT; i++)
		{
			_absolutePositions[i] = new AbsolutePosition(i);
			panel.add(_absolutePositions[i]);			
		}
		add(panel, BorderLayout.CENTER);
		
		panel = new JPanel();
		for (int i = 0; i < Area.CAT_COUNT; i++)
		{
			_meanAndCovariance[i] = new MeanAndCovariance(i);
			panel.add(_meanAndCovariance[i]);			
		}
		add(panel, BorderLayout.SOUTH);
	}
	
	public void repaint()
	{
		super.repaint();
		
		try
		{
			for (LatestSighting ls: _latestSightings)
				ls.repaint();
			
			for (AbsolutePosition ap: _absolutePositions)
				ap.repaint();
			
			for (MeanAndCovariance mac: _meanAndCovariance)
				mac.repaint();
		}
		catch (NullPointerException ex) {}
	}
	
	private class LatestSighting extends JPanel
	{
		private int _id;
		
		private JLabel _x, _y, _theta, _timestamp;
		private JTextField _xInput, _yInput, _thetaInput, _timestampInput;
		
		public LatestSighting(int id)
		{
			super(new BorderLayout());
			
			_id = id;
			
			// GridLayout(int rows, int cols, int hgap, int vgap)
			JPanel panel = new JPanel(new GridLayout(4, 3, 10, 0));
			
			setBorder(new TitledBorder(
					new LineBorder(Color.gray, 1, false),
					"cat"+ _id + " LatestSighting",
					TitledBorder.LEFT,
					TitledBorder.DEFAULT_POSITION)
			);
			
			panel.add(new JLabel("x"));
			_x = new JLabel("1");
			panel.add(_x);
			_xInput = new JTextField("0");
			panel.add(_xInput);
			
			panel.add(new JLabel("y"));
			_y = new JLabel("2");
			panel.add(_y);
			_yInput = new JTextField("0");
			panel.add(_yInput);
			
			panel.add(new JLabel("theta"));
			_theta = new JLabel("3");
			panel.add(_theta);
			_thetaInput = new JTextField("0");
			panel.add(_thetaInput);
			
			panel.add(new JLabel("timestamp"));
			_timestamp = new JLabel("4");
			panel.add(_timestamp);
			_timestampInput = new JTextField("0");
			panel.add(_timestampInput);
			
			add(panel, BorderLayout.NORTH);
			
			panel = new JPanel();
			JButton sendButton = new JButton("Send to all");
			sendButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent ae)
				{
					float x = Float.parseFloat(_xInput.getText());
					float y = Float.parseFloat(_yInput.getText());
					float theta = Float.parseFloat(_thetaInput.getText());
					int timestamp = Integer.parseInt(_timestampInput.getText());
					
					LatestSightingUpdate lsu = new LatestSightingUpdate(x, y, theta, timestamp);
					lsu.setSource(_id);
					ConnectionManager.getInstance().relayPacketToAll(lsu);
				}
			});
			panel.add(sendButton);
			
			add(panel, BorderLayout.SOUTH);
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
	
	private class AbsolutePosition extends JPanel
	{
		private int _id;
		
		private JLabel _x, _y, _theta, _timestamp;
		private JTextField _xInput, _yInput, _thetaInput, _timestampInput;
		
		public AbsolutePosition(int id)
		{
			super(new BorderLayout());
			
			_id = id;
			
			// GridLayout(int rows, int cols, int hgap, int vgap)
			JPanel panel = new JPanel(new GridLayout(4, 3, 10, 0));
			
			setBorder(new TitledBorder(
					new LineBorder(Color.gray, 1, false),
					"cat"+ _id + " AbsolutePosition",
					TitledBorder.LEFT,
					TitledBorder.DEFAULT_POSITION)
			);
			
			panel.add(new JLabel("x"));
			_x = new JLabel("1");
			panel.add(_x);
			_xInput = new JTextField("0");
			panel.add(_xInput);
			
			panel.add(new JLabel("y"));
			_y = new JLabel("2");
			panel.add(_y);
			_yInput = new JTextField("0");
			panel.add(_yInput);
			
			panel.add(new JLabel("theta"));
			_theta = new JLabel("3");
			panel.add(_theta);
			_thetaInput = new JTextField("0");
			panel.add(_thetaInput);
			
			panel.add(new JLabel("timestamp"));
			_timestamp = new JLabel("4");
			panel.add(_timestamp);
			_timestampInput = new JTextField("0");
			panel.add(_timestampInput);
			
			add(panel, BorderLayout.NORTH);
			
			panel = new JPanel();
			JButton sendButton = new JButton("Send to all");
			sendButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent ae)
				{
					float x = Float.parseFloat(_xInput.getText());
					float y = Float.parseFloat(_yInput.getText());
					float theta = Float.parseFloat(_thetaInput.getText());
					int timestamp = Integer.parseInt(_timestampInput.getText());
					
					AbsolutePositionUpdate lsu = new AbsolutePositionUpdate(x, y, theta, timestamp);
					lsu.setSource(_id);
					ConnectionManager.getInstance().relayPacketToAll(lsu);
				}
			});
			panel.add(sendButton);
			
			add(panel, BorderLayout.SOUTH);
		}
		
		public void repaint()
		{
			try
			{
				super.repaint();
				
				_x.setText(String.format("%07.3f", BillBoard.getInstance().getAbsolutePositionX(_id)));
				_y.setText(String.format("%07.3f", BillBoard.getInstance().getAbsolutePositionY(_id)));
				_theta.setText(String.format("%07.3f", BillBoard.getInstance().getAbsolutePositionTheta(_id)));
				_timestamp.setText(String.format("%07.3f", BillBoard.getInstance().getAbsolutePositionTimestamp(_id)));
			}
			catch (NullPointerException ex) {}
		}		
	}
	
	private class MeanAndCovariance extends JPanel
	{
		private int _id;
		
		private JLabel _meanX, _meanY, _meanXv, _meanYv,
		_varXX, _varXY, _varYY, _varXvXv, _varXvYv, _varYvYv, _weight, _timestamp;		
		
		public MeanAndCovariance(int id)
		{
			super(new BorderLayout());
			
			_id = id;
			
			// GridLayout(int rows, int cols, int hgap, int vgap)
			JPanel panel = new JPanel(new GridLayout(12, 2, 10, 0));
			
			setBorder(new TitledBorder(
					new LineBorder(Color.gray, 1, false),
					"cat"+ _id + " MeanAndCov",
					TitledBorder.LEFT,
					TitledBorder.DEFAULT_POSITION)
			);
			
			panel.add(new JLabel("Mean X"));
			_meanX = new JLabel("1");
			panel.add(_meanX);
			
			panel.add(new JLabel("Mean Y"));
			_meanY = new JLabel("2");
			panel.add(_meanY);
			
			panel.add(new JLabel("Mean Xv"));
			_meanXv = new JLabel("3");
			panel.add(_meanXv);
			
			panel.add(new JLabel("Mean Yv"));
			_meanYv = new JLabel("4");
			panel.add(_meanYv);
			
			panel.add(new JLabel("Var XX"));
			_varXX = new JLabel("4");
			panel.add(_varXX);
			
			panel.add(new JLabel("Var XY"));
			_varXY = new JLabel("4");
			panel.add(_varXY);
			
			panel.add(new JLabel("Var YY"));
			_varYY = new JLabel("4");
			panel.add(_varYY);
			
			panel.add(new JLabel("Var XvXv"));
			_varXvXv = new JLabel("4");
			panel.add(_varXvXv);
			
			panel.add(new JLabel("Var XvYv"));
			_varXvYv = new JLabel("4");
			panel.add(_varXvYv);
			
			panel.add(new JLabel("Var YvYv"));
			_varYvYv = new JLabel("4");
			panel.add(_varYvYv);
			
			panel.add(new JLabel("Weight"));
			_weight = new JLabel("4");
			panel.add(_weight);
			
			panel.add(new JLabel("Timestamp"));
			_timestamp = new JLabel("4");
			panel.add(_timestamp);
			
			add(panel, BorderLayout.NORTH);
		}
		
		public void repaint()
		{
			try
			{
				super.repaint();
				
				_meanX.setText(String.format("%07.3f", BillBoard.getInstance().getMeanX(_id)));
				_meanY.setText(String.format("%07.3f", BillBoard.getInstance().getMeanY(_id)));
				_meanXv.setText(String.format("%07.3f", BillBoard.getInstance().getMeanXv(_id)));
				_meanYv.setText(String.format("%07.3f", BillBoard.getInstance().getMeanYv(_id)));
				
				_varXX.setText(String.format("%07.3f", BillBoard.getInstance().getVarXX(_id)));
				_varXY.setText(String.format("%07.3f", BillBoard.getInstance().getVarXY(_id)));
				_varYY.setText(String.format("%07.3f", BillBoard.getInstance().getVarYY(_id)));
				
				_varXvXv.setText(String.format("%07.3f", BillBoard.getInstance().getVarXvXv(_id)));
				_varXvYv.setText(String.format("%07.3f", BillBoard.getInstance().getVarXvYv(_id)));
				_varYvYv.setText(String.format("%07.3f", BillBoard.getInstance().getVarYvYv(_id)));
				
				_weight.setText(String.format("%07.3f", BillBoard.getInstance().getWeight(_id)));
				_timestamp.setText(String.format("%07.3f", BillBoard.getInstance().getDataTimestamp(_id)));
			}
			catch (NullPointerException ex) {}
		}		
	}
}
