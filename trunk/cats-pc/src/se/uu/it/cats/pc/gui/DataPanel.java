package se.uu.it.cats.pc.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import java.awt.*;
import java.awt.event.*;

import se.uu.it.cats.brick.network.packet.SettingUpdate;
import se.uu.it.cats.brick.network.packet.SweepOrder;
import se.uu.it.cats.pc.actor.Area;
import se.uu.it.cats.pc.network.ConnectionManager;

public class DataPanel extends JPanel{ // implements ActionListener

	private Area world;
	private int _areaHeight;
	
	private CatInfo[] catInfos = new CatInfo[Area.CAT_COUNT];
	
	private MouseLabel mouseLabel;
	
	public DataPanel(int areaHeight) {
		world = Area.getInstance();
		_areaHeight = areaHeight;
		// Size of window
		setPreferredSize(new Dimension(200,_areaHeight));
		
		// Bakgrundsfärgen
		setBackground(Color.white);
		setBorder(new TitledBorder(
				new LineBorder(Color.gray, 1, true),
				"Information",
				TitledBorder.LEFT,
				TitledBorder.DEFAULT_POSITION)
		);

		setLayout(new GridLayout(4,1,0,5)); //rows, cols, hgap, vgap
		
		for(int i=0;i < world.getCats().length;i++) {			
			catInfos[i] = new CatInfo(i);
			add(catInfos[i]);
		}
		
		JPanel panel = new JPanel();
		panel.setBackground(Color.white);
		JLabel idLabel = new JLabel("Mouse");
		idLabel.setFont(new Font("Monotype Corsiva",1,17));
		panel.add(idLabel);
		
		mouseLabel = new MouseLabel();
		panel.add(mouseLabel);
		
		add(panel);
	}
	
	public void repaint() {
		try {
			for (CatInfo ci: catInfos)
				ci.repaint();
			
			mouseLabel.repaint();
		}
		catch (NullPointerException ex) {
			
		}
	}
	
	private class CatInfo extends JPanel {
		
		private int id;
		
		PositionLabel _positionLabel;
		AngleLabel _angleLabel;
		
		public CatInfo(final int id) {
			this.id = id;
			
			setLayout(new GridLayout(4,1,0,0)); //rows, cols, hgap, vgap
			setBackground(Color.white);
			
			JLabel idLabel = new JLabel("ID: "+world.getCat(id).getName());
			idLabel.setFont(new Font("Monotype Corsiva",1,17));
			add(idLabel);
			
			_positionLabel = new PositionLabel(id);
			add(_positionLabel);
			
			_angleLabel = new AngleLabel(id);
			add(_angleLabel);
			
			ActionListener ae = new ActionListener()
			{
				public void actionPerformed(ActionEvent ae)
				{
					JCheckBox checkBox = (JCheckBox)ae.getSource();
					
					SettingUpdate su = null;
					if (checkBox.isSelected())
						su = new SettingUpdate(SettingUpdate.USE_GUIDE, 1);
					else
						su = new SettingUpdate(SettingUpdate.USE_GUIDE, 0);
					
					ConnectionManager.getInstance().sendPacketTo(id, su);
				}
			};
			
			JPanel panel = new JPanel();
			panel.setBackground(Color.white);			
			JCheckBox checkBox = new JCheckBox("Use guide");
			checkBox.setSelected(false);
			checkBox.setBackground(Color.white);
			checkBox.addActionListener(ae);
			checkBox.setMargin(new Insets(0, 0, 0, 0));
			checkBox.setBorder(new EmptyBorder(new Insets(0, 0, 0, 0)));
			panel.add(checkBox);
			
			JButton sweepButton = new JButton("Sweep");
			sweepButton.setMargin(new Insets(0, 0, 0, 0));
			sweepButton.setBorder(new EmptyBorder(new Insets(0, 5, 0, 5)));
			sweepButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae)
				{
					ConnectionManager.getInstance().sendPacketTo(id, new SweepOrder());
				}
			});
			panel.add(sweepButton);
			
			add(panel);
		}
		
		public void repaint() {
			try {
				_positionLabel.repaint();
				
				_angleLabel.repaint();
			}
			catch (NullPointerException ex) {
				
			}
		}
	}
	
	private class PositionLabel extends JLabel {
		private int id;
		
		public PositionLabel(int id) {
			this.id = id;
		}
		
		public void repaint() {
			setText(String.format("Position: (%07.3f, %07.3f)", world.getCat(id).getFloatX(), world.getCat(id).getFloatY()));
		}
	}
	
	private class AngleLabel extends JLabel {
		private int id = 0;
		
		public AngleLabel(int id) {
			this.id = id;
		}
		
		public void repaint() {
			setText("Angle: "+(int)Math.toDegrees(world.getCat(id).getAngle_c())+", Angle cam: "+(int)Math.toDegrees(world.getCat(id).getAngle_cam()));
		}
	}
	
	private class MouseLabel extends JLabel {
		public void repaint() {
			setText(String.format("Est. Position: (%07.3f, %07.3f)", world.getMouse().getFloatX(), world.getMouse().getFloatY()));
		}
	}
};
