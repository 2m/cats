package se.uu.it.cats.pc.gui;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import java.awt.*;
import java.awt.event.*;

import se.uu.it.cats.pc.network.ConnectionManager;

public class DataPanel extends JPanel{ // implements ActionListener

	private Area world;
	private int _areaHeight;
	
	private PositionLabel[] positionLabels;
	private AngleLabel[] angleLabels;

	public DataPanel(int areaHeight) {
		world = Area.getInstance();
		_areaHeight = areaHeight;
		// Size of window
		//setPreferredSize(new Dimension(200,_areaHeight));

		// Bakgrundsfärgen
		setBackground(Color.white);
		setBorder(new TitledBorder(
				new LineBorder(Color.gray, 1, true),
				"Information",
				TitledBorder.LEFT,
				TitledBorder.DEFAULT_POSITION)
		);

		setLayout(new GridLayout(12,1,0,0)); //rows, cols, hgap, vgap
		
		positionLabels = new PositionLabel[world.getCats().length];
		angleLabels = new AngleLabel[world.getCats().length];
		
		for(int i=0;i < world.getCats().length;i++) {
			JLabel idLabel = new JLabel("ID: "+world.getCat(i).getName());
			idLabel.setFont(new Font("Monotype Corsiva",1,17));
			add(idLabel);
			
			positionLabels[i] = new PositionLabel(i);
			add(positionLabels[i]);
			
			angleLabels[i] = new AngleLabel(i);
			add(angleLabels[i]);
		}
		JLabel idLabel = new JLabel("Mouse");
		idLabel.setFont(new Font("Monotype Corsiva",1,17));
		add(idLabel);
		add(new JLabel("Est. Position: ("+world.getMouse().getX()+","+world.getMouse().getY()+")"));
	}
	
	public void repaint() {
		try {
			for (PositionLabel pl: positionLabels)
				pl.repaint();
			
			for (AngleLabel al: angleLabels)
				al.repaint();
		}
		catch (NullPointerException ex) {
			
		}
	}
	
	private class PositionLabel extends JLabel {
		private int id = 0;
		
		public PositionLabel(int id) {
			this.id = id;
		}
		
		public void repaint() {
			setText("Position: ("+world.getCat(id).getX()+","+world.getCat(id).getY()+")");
		}
	}
	
	private class AngleLabel extends JLabel {
		private int id = 0;
		
		public AngleLabel(int id) {
			this.id = id;
		}
		
		public void repaint() {
			setText("Angle: "+Math.toDegrees(world.getCat(id).getAngle_c())+", Angle cam: "+Math.toDegrees(world.getCat(id).getAngle_cam()));
		}
	}
};
