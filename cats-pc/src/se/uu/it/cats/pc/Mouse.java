package se.uu.it.cats.pc;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import se.uu.it.cats.pc.network.ConnectionHandler;

public class Mouse extends javax.swing.JFrame implements Runnable
{
	public static final int SCREEN_WIDTH = 700;
    public static final int SCREEN_HEIGHT = 900;
	
	public static final int WORLD_WIDTH = SCREEN_WIDTH - 7;
    public static final int WORLD_HEIGHT = SCREEN_HEIGHT - 29;    
    
    public static final int BALL_SIZE = 30;
	
	public static float _angles[] = new float[2];
	
	public float[] calcMousePos(float theta_mouse,float theta_tower)
	{                                        
		float xPos_tower = 0.5f;
		float r_mouse = (float) (- xPos_tower*Math.sin(theta_tower) /Math.sin(theta_mouse - theta_tower ));  
		float[] pos = new float[2];                                                                          
		pos[0]=(float) (r_mouse*Math.cos(theta_mouse));                                                      
		pos[1]=(float) (r_mouse*Math.sin(theta_mouse));                                                      
		return pos;                                                                                          
                                                                                                             
	}

	public void run()
	{
		openWindow();
		
		while (true)
		{
			repaint();
			
			try
			{
				Thread.sleep(50);
			}
			catch (Exception ex)
			{
				
			}
		}
	}
	
	private void openWindow()
    {
        setContentPane(new JPanel()
        {
	        protected void paintComponent(Graphics g)
	        {
	                super.paintComponent(g);
	                paintActors(g);
	        }
        });
        
        JButton but1 = new JButton("cat1");
        but1.addActionListener(new ActionListener() {                    
	        public void actionPerformed(ActionEvent e)
	        {
	            //Execute when button is pressed
	        	Thread t = new Thread(new ConnectionHandler("cat1"));
	    		t.start();
	        }
        });
        
        JButton but2 = new JButton("cat2");
        but2.addActionListener(new ActionListener() {                    
	        public void actionPerformed(ActionEvent e)
	        {
	            //Execute when button is pressed
	        	Thread t = new Thread(new ConnectionHandler("cat2"));
	    		t.start();
	        }
        });
        
        JButton but3 = new JButton("cat3");
        but3.addActionListener(new ActionListener() {                    
	        public void actionPerformed(ActionEvent e)
	        {
	            //Execute when button is pressed
	        	Thread t = new Thread(new ConnectionHandler("cat3"));
	    		t.start();
	        }
        });
        
        getContentPane().add(but1);
        getContentPane().add(but2);
        getContentPane().add(but3);
        
        setVisible(true);
        setResizable(false);
        setSize(SCREEN_WIDTH, SCREEN_HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
	
	private void paintActors(Graphics g)
	{
		float[] pos = calcMousePos(_angles[0], _angles[1]);
		
		int xCoord = (int)(pos[0] * 1000);
		int yCoord = WORLD_HEIGHT - (int)(pos[1] * 1000);
		
		//Logger.println("Angle1:"+(_angles[0]*180)/Math.PI+" angle2:"+(_angles[1]*180)/Math.PI+" pos[0]:"+pos[0]+" pos[1]:"+pos[1]+" xCoord:"+xCoord+" yCoord"+yCoord);
		
		// mouse
		g.setColor(Color.red);
		g.fillOval(xCoord - BALL_SIZE / 2, yCoord - BALL_SIZE / 2, BALL_SIZE, BALL_SIZE);
		
		// cat1
		g.setColor(Color.blue);
		g.fillOval(-BALL_SIZE / 2, WORLD_HEIGHT - BALL_SIZE / 2, BALL_SIZE, BALL_SIZE);
		
		// cat2
		g.setColor(Color.blue);
		g.fillOval(500 - BALL_SIZE / 2, WORLD_HEIGHT - BALL_SIZE / 2, BALL_SIZE, BALL_SIZE);
	}
}
