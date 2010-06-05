package se.uu.it.cats.pc.gui;

import javax.imageio.ImageIO;
import javax.swing.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.*;

//import GSim.Actor;

import java.util.Hashtable;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

//Images
import java.awt.image.BufferedImage;
import java.awt.Image;
import java.io.File;
import java.io.IOException;

public class PrintArea extends JPanel implements ChangeListener, MouseWheelListener, MouseListener{ // implements ActionListener
  
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
	private int oldEntityPosX;
	private int oldEntityPosY;
	private int linelength;
	private float[] bufferX;
	private float[] bufferY;
	private int posBuffer;
	private int bufferLength;
	
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
	
	//Load image
	BufferedImage image = null;  //Toolkit.getDefaultToolkit().getImage("robot2.png");
	
	private boolean marked = false;
  
  public PrintArea(int areaHeight, int areaWidth) {
	_newArea = Area.getInstance();
	_areaHeight = areaHeight;
	_areaWidth = areaWidth;
	
	_arenaHeight = _newArea.getArenaHeight();
    _arenaWidth = _newArea.getArenaWidth();

	arenaHeightFix = _arenaHeight;
	arenaWidthFix = _arenaWidth;
	// Size of window
    //setPreferredSize(new Dimension(_areaWidth,_areaHeight));
    
	// Bakgrundsfärgen
    setBackground(Color.white);    
    setBorder(BorderFactory.createLineBorder(Color.gray));
    
    // Add a slider with purpose to change scale
    scaleSlider = new JSlider(JSlider.HORIZONTAL, SCALE_MIN, SCALE_MAX, SCALE_INIT);
    scaleSlider.setMinorTickSpacing(10);
    scaleSlider.setMajorTickSpacing(50);
    scaleSlider.setPaintTicks(true);
    scaleSlider.setPaintLabels(true);
    scaleSlider.setBackground(Color.white);
    scaleSlider.addChangeListener(this);
    
    addMouseWheelListener(this);
    addMouseListener(this);

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
    // Slider initialt osynlig
    scaleSlider.setVisible(false);
    
    //Add image
    try {
    	image = ImageIO.read(new File("robot2.png"));
    } catch (IOException e) {
    }
    
    
  }

  // Ritar ut allt.
  public void paintComponent(Graphics g) {
	
	super.paintComponent(g);

    Graphics2D g2d = (Graphics2D)g; 

    _areaHeight = getHeight();
    _areaWidth = getWidth();
    
	_cats = _newArea.getCats();
	_lighthouse = _newArea.getLighthouse();
	_mouse = _newArea.getMouse();
	
	_arenaHeight = _newArea.getArenaHeight();
    _arenaWidth = _newArea.getArenaWidth();
	
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
			
			//calculate cats positions to be drawn
			entityPosX = centFix_X+_cats[i].getX()*zk/50;
			entityPosY = centFix_Y-_cats[i].getY()*zk/50;
			
			//Draw camera angles
			linelength = 90;
			g2d.setColor(Color.blue);
			float camAngle = _cats[i].getAngle_cam() + _cats[i].getAngle_c();
			g2d.drawLine( (int) entityPosX, (int) entityPosY, (int) (entityPosX + Math.cos(-(camAngle+43f/360*Math.PI))*linelength), (int) (entityPosY + Math.sin(-(camAngle+43f/360*Math.PI))*linelength));
			g2d.drawLine( (int) entityPosX, (int) entityPosY, (int) (entityPosX + Math.cos(-(camAngle-43f/360*Math.PI))*linelength), (int) (entityPosY + Math.sin(-(camAngle-43f/360*Math.PI))*linelength));
			//System.out.println(_cats[i].getName()+" "+camAngle);
			
			if(_cats[i].isManualOrder()) {
				g2d.setColor(Color.red); 
				g2d.setStroke(dashedBackground);
				g2d.drawLine( (int) entityPosX, (int) entityPosY, (int) (centFix_X+_cats[i].getGoToX()*zk/50), (int) (centFix_Y+_cats[i].getGoToY()*zk/50));
			}
			
			// Print the position buffer (fading old positions)
			bufferX = _cats[i].getBufferX();
			bufferY = _cats[i].getBufferY();
			posBuffer = _cats[i].getPosBuffer();
			bufferLength = _cats[i].getBufferLength();
			for(int j = 0; j<bufferLength;j++) {
				oldEntityPosX = centFix_X+(int)(bufferX[(posBuffer+j) % bufferLength]*100)*zk/50;
				oldEntityPosY = centFix_Y-(int)(bufferY[(posBuffer+j) % bufferLength]*100)*zk/50;
				g2d.setColor(new Color((int) (bufferLength-j)*255/bufferLength, (int) (bufferLength-j)*255/bufferLength,(int) (bufferLength-j)*255/bufferLength));
				g2d.fillOval( (int) oldEntityPosX-4, (int) oldEntityPosY-4, 8, 8);
			}
			
			
			
			// print sightings as long lines of different colors
			g2d.setStroke(new BasicStroke(3)); // width of the lines
			for (int j = 0; j < _cats[i].getSightingCount(); j++) {
				
				if (_cats[i].getSightingDrawCount(j) <= 0)
					continue;
				
				float angle = _cats[i].getSighting(j) + _cats[i].getAngle_c();
				linelength = 100;
				
				Color[] colors = new Color[] {Color.red, Color.magenta, Color.blue, Color.green, Color.yellow};				
				g2d.setColor(colors[j]);
				
				g2d.drawLine( (int) entityPosX, (int) entityPosY, (int) (entityPosX + Math.cos(-(angle))*linelength), (int) (entityPosY + Math.sin(-(angle))*linelength));
				
				_cats[i].decreaseSightingDrawnCount(j);
			}
			
			//Draw cat-direction
			linelength = 20;
			g2d.setColor(Color.black);
			g2d.setStroke(new BasicStroke(1));
			g2d.drawLine( (int) entityPosX, (int) entityPosY, (int) (entityPosX + Math.cos(-(_cats[i].getAngle_c()))*linelength), (int) (entityPosY + Math.sin(-(_cats[i].getAngle_c()))*linelength));
			
			
			// draw the cat
			g2d.setColor(Color.black); // Black cats
			//Set cat yellow if marked
			if(_cats[i].isMarked()) {
				g2d.setColor(Color.yellow); // Black cats
			}
			g2d.fillOval( (int) entityPosX-5, (int) entityPosY-5, 10, 10);
			
			//Streckat test
			g2d.setStroke(dashedBackground);
			//g2d.draw(new Rectangle((int) entityPosX-5,(int) entityPosY-5,20,20));
			g2d.setStroke(solid);
			//Draw cat ids
			g2d.setColor(Color.black);
			g2d.drawString(_cats[i].getName(), entityPosX-10f, entityPosY-10f);
		}
    }
	
	
	//Print lighthouses
	for (int i = 0; i < _lighthouse.length; i++) {
		if (_lighthouse[i] != null) {
			g2d.setColor(Color.green); 
			if(i==3) { g2d.setColor(Color.blue); }
			g2d.fillOval( (int) centFix_X+(_lighthouse[i].getX()*zk/50-15), (int) centFix_Y-(_lighthouse[i].getY()*zk/50+15), 30, 30);
			g2d.setColor(Color.black); 
			g2d.drawString(_lighthouse[i].getLighthouseName(), (int) centFix_X+(_lighthouse[i].getX()*zk/50-15)-10, (int) centFix_Y-(_lighthouse[i].getY()*zk/50+15)-10f);

		}
    } 
	
	//Print mouse
	g2d.setColor(Color.red);
	g2d.fillOval( (int) centFix_X+_mouse.getX()*zk/50-8, (int) centFix_Y-_mouse.getY()*zk/50-8, 16, 16);
  
	//Print cat-image
	//g2d.rotate(Math.PI);  // Rotate the image by 1 radian.
    //g2d.drawImage(image, 30, 30, ImageObserver observer);
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
	      setScale(scaleSlider.getValue());
	  }
  }
  
  public void mouseWheelMoved(MouseWheelEvent e) {
	  setScale(getScale() + (e.getWheelRotation() * 3));
	  scaleSlider.setValue(getScale());
  }
  
  public void setScale(int newScale)
  {
	  if (newScale < 100 && newScale > 0)
	  {
		  SCALE_CURRENT = newScale;
		  zk = newScale;
		  //System.out.println(newScale);
	  }	  
  }
  
  public int getScale()
  {
	  return zk;
  }
  
  public void mousePressed(MouseEvent e) {
		// saySomething("Mouse pressed", e);
	}

	public void mouseReleased(MouseEvent e) {
		// saySomething("Mouse released", e);
	}

	public void mouseEntered(MouseEvent e) {
		// saySomething("Mouse entered", e);
	}

	public void mouseExited(MouseEvent e) {
		// saySomething("Mouse exited", e);
	}
	
	public void mouseClicked(MouseEvent e) {
		centFix_X = getWidth()/2-_arenaWidth/2*zk/50;
		centFix_Y = getHeight()/2+_arenaHeight/2*zk/50;
		if (marked) {
			for (int i = 0; i < _newArea.getCats().length; i++) {
				if (_newArea.getCats()[i].isMarked()) {
					_newArea.getCats()[i].goTo(((e.getX()-centFix_X)*50/zk), ((e.getY()-centFix_Y)*50/zk));
					System.out.println(((e.getX()-centFix_X)*50/zk) + " "+((e.getY()-centFix_Y)*50/zk));
					//System.out.println((e.getX()) + " "+(e.getY()));
					_newArea.getCats()[i].setMarked(false);
					_newArea.getCats()[i].setManualOrder(true);
				}
			}
			marked = false;
		} else {
			
			
			double dist, mindist = _arenaWidth * _arenaWidth + _arenaHeight
					* _arenaHeight;
			int j = 0;
			for (int i = 0; i < _newArea.getCats().length; i++) {
				entityPosX = centFix_X+_cats[i].getX()*zk/50;
				entityPosY = centFix_Y-_cats[i].getY()*zk/50;
				
				dist = Math.pow(entityPosX - e.getX(), 2)
						+ Math.pow(entityPosY - e.getY(), 2);
				if (dist < mindist) {
					mindist = dist;
					j = i;
				}
			}
			_cats[j].setMarked(true);
			marked = true;
		}
	}

};
