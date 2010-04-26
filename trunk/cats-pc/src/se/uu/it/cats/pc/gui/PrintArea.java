package se.uu.it.cats.pc.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import java.util.Hashtable;

public class PrintArea extends JPanel{ // implements ActionListener
  
	private Area _newArea;
	private Cat[] _cats;
	private Lighthouse[] _lighthouse;
	private Mouse _mouse;
	
	private int _areaHeight;
	private int _areaWidth;
	private int _arenaHeight;
	private int _arenaWidth;
	
	private int centFix_X;
	private int centFix_Y;
	
	private int entityPosX;
	private int entityPosY;
	private int linelength;
	
	final static float dash1[] = {5.0f};
	final static BasicStroke dashed = new BasicStroke(1.0f,BasicStroke.CAP_BUTT,BasicStroke.JOIN_MITER,10.0f,dash1,0.0f);
	final static BasicStroke dashedBackground = new BasicStroke(1.0f,BasicStroke.CAP_BUTT,BasicStroke.JOIN_MITER,10.0f,dash1,0.0f);
	final static BasicStroke solid = new BasicStroke();		
	
	final static int wBetDashedLines = 90;
  
  public PrintArea(Area newArea, int areaHeight, int areaWidth) {
	_newArea = newArea;
	_areaHeight = areaHeight;
	_areaWidth = areaWidth;
	_arenaHeight = _newArea.getArenaHeight();
    _arenaWidth = _newArea.getArenaWidth();
	// Size of window
    setPreferredSize(new Dimension(_areaWidth,_areaHeight));
    
	// Bakgrundsfärgen
    setBackground(Color.white);

  }

  // Ritar ut allt.
  public void paintComponent(Graphics g) {
	
	super.paintComponent(g);

    Graphics2D g2d = (Graphics2D)g; // Skapa den grafiska miljön i ett objekt för uppritning olika former

	_cats = _newArea.getCats();
	_lighthouse = _newArea.getLighthouse();
	_mouse = _newArea.getMouse();
	
    //Draw lines
	g2d.setColor(Color.gray);
	g2d.drawLine(_areaWidth/2, 0, _areaWidth/2, _areaHeight); //Vertical center
	g2d.drawLine(0, _areaHeight/2, _areaWidth, _areaHeight/2); //Horizontal center
	g2d.setStroke(dashedBackground);
	for(int i = 0; i*wBetDashedLines < _areaWidth/2; i++) {
		g2d.drawLine(_areaWidth/2-i*wBetDashedLines, 0, _areaWidth/2-i*wBetDashedLines, _areaHeight);
		g2d.drawLine(_areaWidth/2+i*wBetDashedLines, 0, _areaWidth/2+i*wBetDashedLines, _areaHeight);
	}
	for(int i = 0; i*wBetDashedLines < _areaHeight/2; i++) {
		g2d.drawLine(0, _areaHeight/2+i*wBetDashedLines, _areaWidth, _areaHeight/2+i*wBetDashedLines);
		g2d.drawLine(0, _areaHeight/2-i*wBetDashedLines, _areaWidth, _areaHeight/2-i*wBetDashedLines);
	}
	g2d.setStroke(solid);	

	// g.drawOval( (int) ((constant.posJord().getX()-constant.radieJord())/constant.skala()) , (int) ((constant.posJord().getY()-constant.radieJord())/constant.skala()), (int) (2*constant.radieJord()/constant.skala()), (int) (2*constant.radieJord()/constant.skala()));

	centFix_X = _areaWidth/2-_arenaWidth/2;
	centFix_Y = _areaHeight/2+_arenaHeight/2;
	
	//Print cats
    int size = Math.min(getWidth(), getHeight());
	for (int i = 0; i < _cats.length; i++) {
		if (_cats[i] != null) {
			// Print cat as a black oval
			//g2d.fillOval( (int) cats[i].getX(), (int) cats[i].getY(), 2, 2);
			g2d.setColor(Color.black);
			//Draw cat-direction
			linelength = 20;
			g2d.drawLine( (int) entityPosX, (int) entityPosY, (int) (entityPosX + Math.cos(_cats[i].getAngle_c())*linelength), (int) (entityPosY + Math.sin(_cats[i].getAngle_c())*linelength));
			//Draw camera.direction
			linelength = 90;
			g2d.setColor(Color.blue);
			
			g2d.drawLine( (int) entityPosX, (int) entityPosY, (int) (entityPosX + Math.cos(_cats[i].getAngle_cam()+43f/360*Math.PI)*linelength), (int) (entityPosY + Math.sin(_cats[i].getAngle_cam()+43f/360*Math.PI)*linelength));
			g2d.drawLine( (int) entityPosX, (int) entityPosY, (int) (entityPosX + Math.cos(_cats[i].getAngle_cam()-43f/360*Math.PI)*linelength), (int) (entityPosY + Math.sin(_cats[i].getAngle_cam()-43f/360*Math.PI)*linelength));

			g2d.setColor(Color.black); // Black cats
			
			//Draw the cats
			entityPosX = centFix_X+_cats[i].getX();
			entityPosY = centFix_Y-_cats[i].getY();
			
			g2d.fillOval( (int) entityPosX-5, (int) entityPosY-5, 10, 10);
			//Streckat test
			g2d.setStroke(dashedBackground);
			g2d.draw(new Rectangle((int) entityPosX-5,(int) entityPosY-5,20,20));
			g2d.setStroke(solid);	
		}
    }
	
	//Print lighthouses
	for (int i = 0; i < _lighthouse.length; i++) {
		if (_lighthouse[i] != null) {
			g2d.setColor(Color.green); 
			if(i==3) { g2d.setColor(Color.blue); }
			g2d.fillOval( (int) centFix_X+_lighthouse[i].getX()-15, (int) centFix_Y-_lighthouse[i].getY()-15, 30, 30);
		}
    }
  }
};
