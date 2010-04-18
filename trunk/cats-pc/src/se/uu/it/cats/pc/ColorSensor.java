package se.uu.it.cats.pc;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JPanel;

import lejos.pc.comm.NXTComm;
import lejos.pc.comm.NXTCommFactory;
import lejos.pc.comm.NXTConnector;
import lejos.pc.comm.NXTInfo;

public class ColorSensor extends javax.swing.JFrame
{
	public class Threshold
	{
		float[][] rel;
		Col abs;
		Vector<Col>[] order;
		
		public Threshold(float[][] r, Vector<Col>[] o, Col a)
		{
			rel = r;
			order = o;
			abs = a;
		}
		
		public int getThresholdValue(int absValue, int thIndex, int boundIndex)
		{
			if (abs == Col.RED)
			{
				switch (thIndex)
				{
					case 0:
						return (int)(absValue * rel[Col.GREEN.index()][boundIndex]);
					case 1:
						return (int)(absValue * rel[Col.BLUE.index()][boundIndex]);
				}
			}
			else if (abs == Col.GREEN)
			{
				switch (thIndex)
				{
					case 0:
						return (int)(absValue * rel[Col.RED.index()][boundIndex]);
					case 1:
						return (int)(absValue * rel[Col.BLUE.index()][boundIndex]);
				}
			}
			else if (abs == Col.BLUE)
			{
				switch (thIndex)
				{
					case 0:
						return (int)(absValue * rel[Col.RED.index()][boundIndex]);
					case 1:
						return (int)(absValue * rel[Col.GREEN.index()][boundIndex]);
				}
			}
			
			return 0;
		}
		
		public boolean detected(int red, int green, int blue, int threshMarg)
		{
			if (abs == Col.RED)
			{
				if (green < (getThresholdValue(red, 0, 0)) && green > (getThresholdValue(red, 0, 1)) &&
					blue  < (getThresholdValue(red, 1, 0)) && blue  > (getThresholdValue(red, 1, 1)))
				{
					if (order[0].contains(getLowestColor(red, green, blue)) &&
						order[1].contains(getMiddleColor(red, green, blue)) &&
						order[2].contains(getHighestColor(red, green, blue)))
						return true;
				}
			}
			else if (abs == Col.GREEN)
			{
				if (red   < (getThresholdValue(green, 0, 0)) && red   > (getThresholdValue(green, 0, 1)) &&
					blue  < (getThresholdValue(green, 1, 0)) && blue  > (getThresholdValue(green, 1, 1)))
				{
					if (order[0].contains(getLowestColor(red, green, blue)) &&
						order[1].contains(getMiddleColor(red, green, blue)) &&
						order[2].contains(getHighestColor(red, green, blue)))
						return true;
				}
			}
			else if (abs == Col.BLUE)
			{
				if (red   < (getThresholdValue(blue, 0, 0)) && red   > (getThresholdValue(blue, 0, 1)) &&
					green < (getThresholdValue(blue, 1, 0)) && green > (getThresholdValue(blue, 1, 1)))
				{
					if (order[0].contains(getLowestColor(red, green, blue)) &&
						order[1].contains(getMiddleColor(red, green, blue)) &&
						order[2].contains(getHighestColor(red, green, blue)))
						return true;
				}
			}
			
			return false;
		}
		
		public Col getLowestColor(int red, int green, int blue)
		{
			if (red < green && red < blue)
				return Col.RED;
			else if (green < red && green < blue)
				return Col.GREEN;
			else 
				return Col.BLUE;
		}
		
		public Col getMiddleColor(int red, int green, int blue)
		{
			if ((red < green && red > blue) || (red < blue && red > green))
				return Col.RED;
			else if ((green < red && green > blue) || (green < blue && green > red))
				return Col.GREEN;
			else 
				return Col.BLUE;
		}
		
		public Col getHighestColor(int red, int green, int blue)
		{
			if (red > green && red > blue)
				return Col.RED;
			else if (green > red && green > blue)
				return Col.GREEN;
			else 
				return Col.BLUE;
		}
	}
	
	
	public enum Col
	{
		RED(0),
		GREEN(1),
		BLUE(2),
		YELLOW(3);
		
		final int val;
		
		Col(int c)
		{
			this.val = c;
		}
		
		int index()
		{
			return val;
		}
	}
	
	public class Threshold2
	{
		Col abs; // color with highest readings
		
		int[][] bound;
		
		public Threshold2(int[][] b, Col a)
		{
			bound = b;
			abs = a;
		}
		
		public int getThresholdValue(int absValue, int thIndex)
		{
			double slopePos;
			
			slopePos = (double)(bound[abs.index()][0] - absValue) / (double)(bound[abs.index()][0] - bound[abs.index()][1]);
			
			/*if (thIndex == 0)
				System.out.println(bound[abs.index()][0] - absValue);*/
			
			if (abs == Col.RED)
			{
				switch (thIndex)
				{
					case 0:
						return (int)(bound[Col.GREEN.index()][0] - slopePos * (bound[Col.GREEN.index()][0] - bound[Col.GREEN.index()][1]));						
					case 1:
						return (int)(bound[Col.BLUE.index()][0] - slopePos * (bound[Col.BLUE.index()][0] - bound[Col.BLUE.index()][1]));
				}
			}
			else if (abs == Col.GREEN)
			{
				switch (thIndex)
				{
					case 0:
						return (int)(bound[Col.RED.index()][0] - slopePos * (bound[Col.RED.index()][0] - bound[Col.RED.index()][1]));						
					case 1:
						return (int)(bound[Col.BLUE.index()][0] - slopePos * (bound[Col.BLUE.index()][0] - bound[Col.BLUE.index()][1]));
				}
			}
			else if (abs == Col.BLUE)
			{
				switch (thIndex)
				{
					case 0:
						return (int)(bound[Col.RED.index()][0] - slopePos * (bound[Col.RED.index()][0] - bound[Col.RED.index()][1]));						
					case 1:
						return (int)(bound[Col.GREEN.index()][0] - slopePos * (bound[Col.GREEN.index()][0] - bound[Col.GREEN.index()][1]));
				}
			}
			
			return 0;
		}
		
		public boolean detected(int red, int green, int blue, int threshMarg)
		{
			if (abs == Col.RED)
			{
				if (green < (getThresholdValue(red, 0) + threshMarg) && green > (getThresholdValue(red, 0) - threshMarg) &&
					blue  < (getThresholdValue(red, 1) + threshMarg) && blue  > (getThresholdValue(red, 1) - threshMarg) &&
					red   < bound[Col.RED.index()][0] + ABS_MARGIN   && red   > bound[Col.RED.index()][1] - ABS_MARGIN)
				{
					return true;
				}
			}
			else if (abs == Col.GREEN)
			{
				if (red   < (getThresholdValue(green, 0) + threshMarg) && red   > (getThresholdValue(green, 0) - threshMarg) &&
					blue  < (getThresholdValue(green, 1) + threshMarg) && blue  > (getThresholdValue(green, 1) - threshMarg) &&
					green < bound[Col.GREEN.index()][0] + ABS_MARGIN   && green > bound[Col.GREEN.index()][1] - ABS_MARGIN)
				{
					return true;
				}
			}
			else if (abs == Col.BLUE)
			{
				if (red   < (getThresholdValue(blue, 0) + threshMarg) && red   > (getThresholdValue(blue, 0) - threshMarg) &&
					green < (getThresholdValue(blue, 1) + threshMarg) && green > (getThresholdValue(blue, 1) - threshMarg) &&
					blue  < bound[Col.BLUE.index()][0] + ABS_MARGIN   && blue  > bound[Col.BLUE.index()][1] - ABS_MARGIN)
				{
					return true;
				}
			}
			
			return false;
		}
	}
	
	public static final int WORLD_WIDTH = 1500;
    public static final int WORLD_HEIGHT = 800;
    public static final int WOLRD_HEIGHT_OFF = 30;
    
    public static final int MAX_VAL = 320;
    
    public static final float THRESHOLD_MARGIN_REL = 0.05f;
    public static final int THRESHOLD_MARGIN_MIN = 15;
    
    public static final int ABS_MARGIN = 15;
    
    int[] values;
    
    int current = 0;
    
    int mouseY = 0;
    
    Threshold[] ths = null;
    boolean[] activeThs = null;
    
    @SuppressWarnings("unchecked")
	public ColorSensor()
    {
    	values = new int[WORLD_WIDTH];
    	
    	// red
    	Vector<Col> o1 = new Vector<Col>();
    	o1.add(Col.RED);
    	o1.add(Col.GREEN);
    	o1.add(Col.BLUE);
    	
    	Vector<Col> o2 = new Vector<Col>();
    	o2.add(Col.RED);
    	o2.add(Col.GREEN);
    	o2.add(Col.BLUE);
    	
    	Vector<Col> o3 = new Vector<Col>();
    	o3.add(Col.RED);
    	o3.add(Col.GREEN);
    	o3.add(Col.BLUE);
    	
    	ths = new Threshold[4];
    	ths[0] = new Threshold(
    			new float[][] {
    					{0f, 0f},
    					{0.91f, 0.2f},
    					{0.85f, 0.2f}
    			},
    			new Vector[] {o1, o2, o3},
    			Col.RED
    	);
    	
    	// green
    	o1 = new Vector<Col>();
    	o1.add(Col.RED);
    	o1.add(Col.BLUE);
    	
    	o2 = new Vector<Col>();
    	o2.add(Col.RED);
    	o2.add(Col.BLUE);
    	
    	o3 = new Vector<Col>();    	
    	o3.add(Col.GREEN);
    	
    	ths[1] = new Threshold(
    			new float[][] {
    					{1f, 0.2f},
    					{0f, 0f},
    					{1f, 0.2f}
    			},
    			new Vector[] {o1, o2, o3},
    			Col.GREEN
    	);
    	
    	// blue
    	o1 = new Vector<Col>();
    	o1.add(Col.RED);
    	
    	o2 = new Vector<Col>();
    	o2.add(Col.GREEN);
    	
    	o3 = new Vector<Col>();
    	o3.add(Col.BLUE);
    	
    	ths[2] = new Threshold(
    			new float[][] {
    					{1f, 0.05f},
    					{1f, 0.05f},
    					{0f, 0f}
    			},
    			new Vector[] {o1, o2, o3},
    			Col.BLUE
    	);
    	
    	// yellow
    	o1 = new Vector<Col>();
    	o1.add(Col.RED);
    	o1.add(Col.GREEN);
    	o1.add(Col.BLUE);
    	
    	o2 = new Vector<Col>();
    	o2.add(Col.RED);
    	o2.add(Col.GREEN);
    	o2.add(Col.BLUE);
    	
    	o3 = new Vector<Col>();
    	o3.add(Col.RED);
    	o3.add(Col.GREEN);
    	o3.add(Col.BLUE);
    	
    	ths[3] = new Threshold(
				new float[][] {
						{0.934f, 0.87f},
						{0f, 0f},
						{0.80f, 0.2f}
				},
				new Vector[] {o1, o2, o3},
				Col.GREEN
    	);
    	
    	/*ths = new Threshold[4];
    	
    	// threshold for red color
    	ths[0] = new Threshold(new int[][] {
    			{208, 176},
    			{151, 76},
    			{129, 60}
    	}, Col.RED);
    	
    	// threshold for green color
    	ths[1] = new Threshold(new int[][] {
    			{144, 17},
    			{165, 53},
    			{136, 26}
    	}, Col.GREEN);
    	
    	// threshold for blue color
    	ths[2] = new Threshold(new int[][] {
    			{82, 58},
    			{104, 76},
    			{117, 58}
    	}, Col.BLUE);
    	
    	// threshold for yellow color
    	ths[3] = new Threshold(new int[][] {
    			{219, 166},
    			{232, 193},
    			{97, 56}
    	}, Col.GREEN);*/
    	
    	activeThs = new boolean[4];
    }
    
    public int invScale(int val)
    {
    	return WORLD_HEIGHT - (val * WORLD_HEIGHT) / MAX_VAL;
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
        
        this.addMouseListener(new MouseListener() 
        {
        	public void mouseReleased(MouseEvent e)
        	{
        		mouseY = e.getY();
        	}

			@Override
			public void mouseClicked(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseEntered(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mouseExited(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void mousePressed(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}
        });
        
        this.addKeyListener(new KeyListener() {

			@Override
			public void keyPressed(KeyEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void keyReleased(KeyEvent arg0) {
				// TODO Auto-generated method stub
				switch (arg0.getKeyChar())
				{
					case 'r':
						activeThs[0] = !activeThs[0];
						break;
					case 'g':
						activeThs[1] = !activeThs[1];
						break;
					case 'b':
						activeThs[2] = !activeThs[2];
						break;
					case 'y':
						activeThs[3] = !activeThs[3];
						break;
				}
			}

			@Override
			public void keyTyped(KeyEvent arg0) {
				// TODO Auto-generated method stub
				
			}
        	
        });
        
        setVisible(true);
        setResizable(false);
        setSize(WORLD_WIDTH, WORLD_HEIGHT + WOLRD_HEIGHT_OFF);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

	private void paintActors(Graphics g)
	{
        // TODO: Add loop for actors.draw
        //t.draw(g);
		int lastR = 0, lastG = 0, lastB = 0, lastI = 0;
		
		for (int i = 0; i < WORLD_WIDTH; i++)
		{
			int color = values[(current + i) % WORLD_WIDTH];
			
			// decoding colors from network
			int blue = color >> 20;
			int green = (color & 0x000FFC00) >> 10;
			int red = (color & 0x000003FF);
			
			/*int blue = color >> 16;
			int green = (color & 0x0000FF00) >> 8;
			int red = (color & 0x000000FF);*/
			
			Graphics2D g2 = (Graphics2D)g;
			
			// rect of current color
			g2.setColor(new Color(red * 255 / 1023, green * 255 / 1023, blue * 255 / 1023));
			g2.fill(new Rectangle(30, 10, 50, 50));
			
			// rect of current color maximized
			int maxValue = red;
			if (green > maxValue) maxValue = green;
			if (blue > maxValue) maxValue = blue;
			if (maxValue != 0)
				g2.setColor(new Color(red * 255 / maxValue, green * 255 / maxValue, blue * 255 / maxValue));
			g2.fill(new Rectangle(90, 10, 50, 50));
			
			boolean[] detected = new boolean[4];
			
			for (int j = 0; j < ths.length; j++)
			{
				detected[j] = false;
				
				int abs = 0;
				Color absColor = null, rel1Color = null, rel2Color = null;
				if (ths[j].abs == Col.RED)
				{
					abs = red;
					
					rel1Color = new Color(200, 255, 200);
					rel2Color = new Color(200, 200, 255);
				}
				else if (ths[j].abs == Col.GREEN)
				{
					abs = green;
					
					rel1Color = new Color(255, 200, 200);
					rel2Color = new Color(200, 200, 255);
				}
				else if (ths[j].abs == Col.BLUE)
				{
					abs = blue;					
					
					rel1Color = new Color(255, 200, 200);
					rel2Color = new Color(200, 255, 200);
				}
				
				switch (j)
				{
					// red
					case 0:
						absColor = new Color(255, 100, 100);
						break;
					// green
					case 1:
						absColor = new Color(100, 255, 100);
						break;
					// blue
					case 2:
						absColor = new Color(100, 100, 255);
						break;
					// yellow
					case 3:
						absColor = new Color(255, 255, 100);
						break;
				}
				
				int threshMarg = THRESHOLD_MARGIN_MIN;
				//if (abs * THRESHOLD_MARGIN_REL > threshMarg)
					//threshMarg = (int)(abs * THRESHOLD_MARGIN_REL);
				
				if (ths[j].detected(red, green, blue, threshMarg))
				{
					g2.setColor(absColor);
					g2.drawLine(i, j * 3, i, (j + 1) * 3);
				}
				
				if (!activeThs[j])
					continue;
				
				// drawing borders
				g2.setColor(absColor);
				g2.drawLine(i, invScale(ths[j].getThresholdValue(abs, 0, 0)), i, invScale(ths[j].getThresholdValue(abs, 0, 0)));
				g2.drawLine(i, invScale(ths[j].getThresholdValue(abs, 0, 1)), i, invScale(ths[j].getThresholdValue(abs, 0, 1)));
				
				g2.drawLine(i, invScale(ths[j].getThresholdValue(abs, 1, 0)), i, invScale(ths[j].getThresholdValue(abs, 1, 0)));
				g2.drawLine(i, invScale(ths[j].getThresholdValue(abs, 1, 1)), i, invScale(ths[j].getThresholdValue(abs, 1, 1)));
				
				if (i % ths.length == j)
				{
					// drawing the first threshold
					g2.setColor(rel1Color);
					g2.drawLine(i, invScale(ths[j].getThresholdValue(abs, 0, 0)), i, invScale(ths[j].getThresholdValue(abs, 0, 1)));
					
					// drawing the second threshold
					g2.setColor(rel2Color);
					g2.drawLine(i, invScale(ths[j].getThresholdValue(abs, 1, 0)), i, invScale(ths[j].getThresholdValue(abs, 1, 1)));
				}
			}
			
			red = invScale(red);
			green = invScale(green);
			blue = invScale(blue);
			
			g2.setColor(Color.red);
			g2.fillOval(i, red, 2, 2);
			g2.drawLine(lastI, lastR, i, red);
			
			g2.setColor(Color.green);
			g2.fillOval(i, green, 2, 2);
			g2.drawLine(lastI, lastG, i, green);
			
			g2.setColor(Color.blue);
			g2.fillOval(i, blue, 2, 2);
			g2.drawLine(lastI, lastB, i, blue);
			
			lastR = red;
			lastG = green;
			lastB = blue;
			lastI = i;
			
			// mouse clicked coordinates
			g2.drawString(String.valueOf((int)(((double)(WORLD_HEIGHT - mouseY + WOLRD_HEIGHT_OFF) / WORLD_HEIGHT) * MAX_VAL)), 10, 30);
		}
	}
	
	private boolean sleep(long millis)
	{
        if (Thread.interrupted())
                return false;
        try {
                Thread.sleep(millis);
        } catch (InterruptedException e) {
                return false;
        }
       return true;
	}
	
	public void run()
	{
        openWindow();
        
        NXTConnector conn = new NXTConnector();
		
        boolean connected = false;
        
		NXTInfo[] info = conn.search(null, null, NXTCommFactory.USB);		
		for (int i = 0; i < info.length; i++)
			connected = conn.connectTo(info[0], NXTComm.PACKET);
		
		/*NXTInfo nxt = new NXTInfo(NXTCommFactory.BLUETOOTH, "cat1", "00165302CC4E");
		boolean connected = conn.connectTo(nxt, NXTComm.PACKET);*/
		
		if (!connected)
		{
			System.err.println("Failed to connect to any NXT");
			System.exit(1);
		}
		
		DataInputStream dis = conn.getDataIn();
        
        while (true)
        {
	        // TODO: Call run() or similar in cat
        	try
			{
        		int data = dis.readInt();
        		int b = data >> 20;
    			int g = (data & 0x000FFC00) >> 10;
    			int r = (data & 0x000003FF);
        		/*int b = data >> 16;
    			int g = (data & 0x0000FF00) >> 8;
    			int r = (data & 0x000000FF);*/
				values[current] = data;
				current = (current + 1) % WORLD_WIDTH;
				//System.out.println("R:"+r+" G:"+g+" B:"+b);
			}
        	catch (IOException ioe)
        	{
        		System.out.println("IO Exception reading bytes:");
        	}
	        repaint();
	        //sleep(0); // Wait before new update
        }
	}
}
