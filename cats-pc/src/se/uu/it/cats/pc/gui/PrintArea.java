package se.uu.it.cats.pc.gui;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import java.util.Hashtable;

public class PrintArea extends JPanel implements ChangeListener{ // implements ActionListener
  
	private Area _newArea;
	private Cat[] _cats;
	private Lighthouse[] _lighthouse;
	private Mouse _mouse;
	
	private int _areaHeight;
	private int _areaWidth;
	
	private int _arenaHeight;
	private int _arenaWidth;
	private int arenaHeightFix;
	private int arenaWidthFix;
	
	private int zk = 50; // zoom variable
	
	private int centFix_X;
	private int centFix_Y;
	
	private int entityPosX;
	private int entityPosY;
	private int linelength;
	
	private boolean _showgrid = true;
	private boolean _showSlider = false;
	
	final static float dash1[] = {5.0f};
	final static BasicStroke dashed = new BasicStroke(1.0f,BasicStroke.CAP_BUTT,BasicStroke.JOIN_MITER,10.0f,dash1,0.0f);
	final static BasicStroke dashedBackground = new BasicStroke(1.0f,BasicStroke.CAP_BUTT,BasicStroke.JOIN_MITER,10.0f,dash1,0.0f);
	final static BasicStroke solid = new BasicStroke();		
	
	final static int wBetDashedLines = 60;
	private int wBetDashedLines_zk;
	
	// Slidern
	private JSlider scaleSlider;
	static final int SCALE_MIN = 1;
	static final int SCALE_MAX = 100;
	static final int SCALE_INIT = 50;
	private int SCALE_CURRENT = SCALE_INIT;
  
  public PrintArea(Area newArea, int areaHeight, int areaWidth) {
	_newArea = newArea;
	_areaHeight = areaHeight;
	_areaWidth = areaWidth;
	
	_arenaHeight = _newArea.getArenaHeight();
    _arenaWidth = _newArea.getArenaWidth();

	arenaHeightFix = _arenaHeight;
	arenaWidthFix = _arenaWidth;
	// Size of window
    setPreferredSize(new Dimension(_areaWidth,_areaHeight));
    
	// Bakgrundsf�rgen
    setBackground(Color.white);
    
    // Add a slider with purpose to change scale
    scaleSlider = new JSlider(JSlider.HORIZONTAL, SCALE_MIN, SCALE_MAX, SCALE_INIT);
    scaleSlider.setMinorTickSpacing(10);
    scaleSlider.setMajorTickSpacing(50);
    scaleSlider.setPaintTicks(true);
    scaleSlider.setPaintLabels(true);
    scaleSlider.setBackground(Color.white);
    scaleSlider.addChangeListener(this);

    // Change view of slider
    Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
    JLabel zin = new JLabel("In");
    JLabel zoom = new JLabel("Zoom");
    JLabel zout = new JLabel("Out");
    zin.setForeground(Color.black);
    zoom.setForeground(Color.black);
    zout.setForeground(Color.black);
    labelTable.put( new Integer( 10 ), zout );
    labelTable.put( new Integer( SCALE_MAX/2 ), zoom );
    labelTable.put( new Integer( SCALE_MAX ), zin );

    scaleSlider.setLabelTable( labelTable );

    add(scaleSlider);

    // Initialt osynlig
    scaleSlider.setVisible(false);
  }

  // Ritar ut allt.
  public void paintComponent(Graphics g) {
	
	super.paintComponent(g);

    Graphics2D g2d = (Graphics2D)g; 

	_cats = _newArea.getCats();
	_lighthouse = _newArea.getLighthouse();
	_mouse = _newArea.getMouse();
	
	wBetDashedLines_zk = wBetDashedLines*zk/50; //Adjusting grid for zoom
    //Draw lines
	if(_showgrid){
		//g2d.setColor(Color.gray);
		g2d.setColor(new Color(0f,0f,0f,0.2f));
		g2d.drawLine(_areaWidth/2, 0, _areaWidth/2, _areaHeight); //Vertical center
		g2d.drawLine(0, _areaHeight/2, _areaWidth, _areaHeight/2); //Horizontal center
		g2d.setStroke(dashedBackground);
		for(int i = 0; i*wBetDashedLines_zk < _areaWidth/2; i++) {
			g2d.drawLine(_areaWidth/2-i*wBetDashedLines_zk, 0, _areaWidth/2-i*wBetDashedLines_zk, _areaHeight);
			g2d.drawLine(_areaWidth/2+i*wBetDashedLines_zk, 0, _areaWidth/2+i*wBetDashedLines_zk, _areaHeight);
		}
		for(int i = 0; i*wBetDashedLines_zk < _areaHeight/2; i++) {
			g2d.drawLine(0, _areaHeight/2+i*wBetDashedLines_zk, _areaWidth, _areaHeight/2+i*wBetDashedLines_zk);
			g2d.drawLine(0, _areaHeight/2-i*wBetDashedLines_zk, _areaWidth, _areaHeight/2-i*wBetDashedLines_zk);
		}
	}
	g2d.setStroke(solid);	

	// g.drawOval( (int) ((constant.posJord().getX()-constant.radieJord())/constant.skala()) , (int) ((constant.posJord().getY()-constant.radieJord())/constant.skala()), (int) (2*constant.radieJord()/constant.skala()), (int) (2*constant.radieJord()/constant.skala()));

	centFix_X = _areaWidth/2-_arenaWidth/2*zk/50;
	centFix_Y = _areaHeight/2+_arenaHeight/2*zk/50;
	
	//Print cats
    int size = Math.min(getWidth(), getHeight());
	for (int i = 0; i < _cats.length; i++) {
		if (_cats[i] != null) {
			// Print cat as a black oval
			//g2d.fillOval( (int) cats[i].getX(), (int) cats[i].getY(), 2, 2);
			g2d.setColor(Color.black);
			//Draw cat-direction
			linelength = 20;
			g2d.drawLine( (int) entityPosX, (int) entityPosY, (int) (entityPosX + Math.cos(-(_cats[i].getAngle_c()))*linelength), (int) (entityPosY + Math.sin(-(_cats[i].getAngle_c()))*linelength));
			//Draw camera.direction
			linelength = 90;
			g2d.setColor(Color.blue);
			
			g2d.drawLine( (int) entityPosX, (int) entityPosY, (int) (entityPosX + Math.cos(-(_cats[i].getAngle_cam()+43f/360*Math.PI))*linelength), (int) (entityPosY + Math.sin(-(_cats[i].getAngle_cam()+43f/360*Math.PI))*linelength));
			g2d.drawLine( (int) entityPosX, (int) entityPosY, (int) (entityPosX + Math.cos(-(_cats[i].getAngle_cam()-43f/360*Math.PI))*linelength), (int) (entityPosY + Math.sin(-(_cats[i].getAngle_cam()-43f/360*Math.PI))*linelength));

			g2d.setColor(Color.black); // Black cats
			
			//Draw the cats
			entityPosX = centFix_X+_cats[i].getX()*zk/50;
			entityPosY = centFix_Y-_cats[i].getY()*zk/50;
			
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
			g2d.fillOval( (int) centFix_X+(_lighthouse[i].getX()*zk/50-15), (int) centFix_Y-(_lighthouse[i].getY()*zk/50+15), 30, 30);
		}
    }
	
	//Print mouse
	g2d.setColor(Color.red);
	g2d.fillOval( (int) centFix_X+_mouse.getX()*zk/50-8, (int) centFix_Y-_mouse.getY()*zk/50-8, 16, 16);
  }
  
  public void showGrid(boolean showgrid){
	  _showgrid = showgrid;
	  
  }
  
  public void showSlider(boolean showSlider){
	  _showSlider = showSlider;
	  scaleSlider.setVisible(_showSlider);
  }
  
  public void stateChanged(ChangeEvent ce) {
	  if (ce.getSource() == scaleSlider) {
	      SCALE_CURRENT = scaleSlider.getValue();
	      //_arenaHeight = (int) arenaHeightFix*SCALE_CURRENT/50;
	      zk = SCALE_CURRENT;
	      System.out.println(SCALE_CURRENT);
	  }
  }
};