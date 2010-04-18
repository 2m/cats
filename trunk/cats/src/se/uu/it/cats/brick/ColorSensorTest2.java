package se.uu.it.cats.brick;

import lejos.nxt.Button;
import lejos.nxt.SensorPort;
import lejos.nxt.addon.ColorSensor;
import lejos.util.Stopwatch;

public class ColorSensorTest2
{
	public void run()
	{
		Stopwatch clock = new Stopwatch();
		
		System.out.println("Program started, press button to continue");
		Button.waitForPress();

		ColorSensor cs = new ColorSensor(SensorPort.S1);

		System.out.println("Place sensor 15mm from ''white'' surface");
		Button.waitForPress();
		cs.initWhiteBalance();

		System.out.println("Place sensor 15mm from black surface");
		Button.waitForPress();
		cs.initBlackLevel();
		
		int[] black = new int[3];
		int[] white = new int[3];
		int[] red = new int[3];
		int[] green = new int[3];
		int[] blue = new int[3];
		
		System.out.println("Place on black line");
		Button.waitForPress();
		black = cs.getColor();
		System.out.println("K: "+"r:"+String.valueOf(black[0])+" g:"+String.valueOf(black[1])+" b:"+String.valueOf(black[2]));

		System.out.println("Place on white surface");
		Button.waitForPress();
		white = cs.getColor();
		white[0] = 46;
		white[1] = 46;
		white[2] = 46;
		System.out.println("W: "+"r:"+String.valueOf(white[0])+" g:"+String.valueOf(white[1])+" b:"+String.valueOf(white[2]));
		
		System.out.println("Place on red line");
		Button.waitForPress();
		red = cs.getColor();
		System.out.println("R: "+"r:"+String.valueOf(red[0])+" g:"+String.valueOf(red[1])+" b:"+String.valueOf(red[2]));
		
		//System.out.println("Place on green line");
		//Button.waitForPress();
		//green = cs.getColor();
		green[0] =0;
		green[1]=255;
		green[2]=0;      
		//System.out.println("G: "+"r:"+String.valueOf(green[0])+" g:"+String.valueOf(green[1])+" b:"+String.valueOf(green[2]));
		
		System.out.println("Place on blue line");
		Button.waitForPress();
		blue = cs.getColor();
		System.out.println("B: "+"r:"+String.valueOf(blue[0])+" g:"+String.valueOf(blue[1])+" b:"+String.valueOf(blue[2]));
		
		Button.waitForPress();
		
		int[] rThresh = new int[3];
		int[] gThresh = new int[3];
		int[] bThresh = new int[3];
		int[] kThresh = new int[3];
		
		rThresh[0]=(red[0]-black[0])/2;
		rThresh[1]=(int) ((double) 0.5*(white[1]-red[1]));
		rThresh[2]=(int) ((double) 0.5*(white[2]-red[2]));
		/*rThresh[0]=26;
		rThresh[1]=30;
		rThresh[2]=43;*/
		
		gThresh[0]=(int) ((double) 0.5*(white[0]-green[0]));
		gThresh[1]=(green[1]- black[0])/2;
		gThresh[2]=(int) ((double) 0.5*(white[2]-green[2]));
		/*gThresh[0]=40;
		gThresh[1]=23;
		gThresh[2]=43;*/
		
		bThresh[0]=(int) ((double) 0.5*(white[0]-blue[0]));
		bThresh[1]=(int) ((double) 0.5*(white[1]-blue[1]));
		bThresh[2]=(blue[2]-black[0])/2;
		/*bThresh[0]=43;
		bThresh[1]=37;
		bThresh[2]=32;*/
		
		kThresh[0]=(int) ((double) 0.5*(white[0]-black[0]));
		kThresh[1]=(int) ((double) 0.5*(white[1]-black[1]));
		kThresh[2]=(int) ((double) 0.5*(white[2]-black[2]));
		/*kThresh[0]=44;
		kThresh[1]=38;
		kThresh[2]=44;*/
		
		System.out.println("rThresh: "+"r:"+String.valueOf(rThresh[0])+" g:"+String.valueOf(rThresh[1])+" b:"+String.valueOf(rThresh[2]));
		System.out.println("gThresh: "+"r:"+String.valueOf(gThresh[0])+" g:"+String.valueOf(gThresh[1])+" b:"+String.valueOf(gThresh[2]));
		System.out.println("bThresh: "+"r:"+String.valueOf(bThresh[0])+" g:"+String.valueOf(bThresh[1])+" b:"+String.valueOf(bThresh[2]));
		System.out.println("kThresh: "+"r:"+String.valueOf(kThresh[0])+" g:"+String.valueOf(kThresh[1])+" b:"+String.valueOf(kThresh[2]));
		
		Button.waitForPress();
		
		int[] cv = new int[3];
		String c = "";
		String tempc1 = "";
		String tempc2 = "";
		String tempc3 = "";
		String lastc = "";
		String oldc1 = "";
		String oldc2 = "";
		String oldc3 = "";
		double x = 0;
		double y = 0;
		double xest = 0;
		double yest = 0;
		double dx = 1;
		double dy = 1;
		double vx = 0;
		double vy = 0;
		//time stamps, change from int
		int tx= 0; 
		int ty= 0;
		int lasttx= 0;
		int lastty= 0;
		int dtx = 100000000; //should be large
		int dty = 100000000;
		boolean found = false;
		boolean nogreen = true;
		for (;;)
		{
			cv = cs.getColor();
			
			if (cv[0] > 35 && cv[1] > 35 && cv[2] > 35) {
				c = "W";
			}
			else if (cv[0] > rThresh[0] && cv[1] < rThresh[1] && cv[2] < rThresh[2]) {
				c = "R";
			}
			/*else if (cv[0] < gThresh[0] && cv[1] > gThresh[1] && cv[2] < gThresh[2]) {
				c = "G";
			}*/
			else if (cv[0] < kThresh[0] && cv[1] < kThresh[1] && cv[2] < kThresh[2]) {
				c = "K";
			}
			else if (cv[0] < bThresh[0] && cv[1] < bThresh[1] && cv[2] > bThresh[2]) {
				c = "B";
			}

			
			//System.out.println(c + "|" + "r:"+String.valueOf(cv[0])+" g:"+String.valueOf(cv[1])+" b:"+String.valueOf(cv[2]));
			System.out.println("c:" + c + " "+ oldc1 + " "+ oldc2 + " "+ oldc3);
			
			if (!lastc.equals(c)) {
				System.out.println("TRANSITION");
				tempc1 = oldc1;
				oldc1 = lastc;
				
				tempc2 = oldc2;
				oldc2 = tempc1;
				
				oldc3 = tempc2;
			}
			
			if (oldc3.equals("W") && oldc2.equals("K") && oldc1.equals("R") && c.equals("W")) {
				x = x + dx;
				tx = clock.elapsed();
				found = true;
				lastc = "";
				oldc1 = "";
				oldc2 = "";
				oldc3 = "";
			}
/*			if (lastc.equals("R") && c.equals("K")) {
				x = x - dx;
				tx = clock.elapsed();
				found = true;
			}
			if (lastc.equals("K") && c.equals("B")) {
				y = y + dy;
				ty = clock.elapsed();
				found = true;
			}
			if (lastc.equals("B") && c.equals("K")) {
				y = y - dy;
				ty = clock.elapsed();
				found = true;
			}*/
			if (c.equals("G") && nogreen) {
				if (vx > 0)
					x = x + dx;
				if (vx < 0)
					x = x - dx;
				if (vy > 0)
					y = y + dy;
				if (vy < 0)
					y = y - dy;
				//if vx or vy = 0, trouble!
				//also if direction change
				//have to use tachometer data
				tx = clock.elapsed();
				ty = clock.elapsed();
				nogreen = false;
				found = true;
			}
			if (c.equals("W"))
				nogreen = true;
			
			if (found) {
				dtx = tx - lasttx;
				dty = ty - lastty;
				vx = dx/dtx;
				vy = dy/dty;
				lasttx = tx;
				lastty = ty;
			}
			found = false;
			
			
			xest = x + vx*(clock.elapsed() - tx);
			yest = y + vy*(clock.elapsed() - ty);
			
			lastc = c;
			
			//covert x & y to fewer digits
			System.out.println("x:"+String.valueOf(x)+" y:"+String.valueOf(y));
			//System.out.println("xest:"+String.valueOf(xest)+"yest:"+String.valueOf(yest));
			//System.out.println();

		
		}
	}

}
