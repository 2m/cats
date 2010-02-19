package se.uu.it.cats.brick;

import lejos.nxt.Button;
import lejos.nxt.SensorPort;
import lejos.nxt.addon.ColorSensor;

public class ColorSensorTest2
{
	public void run()
	{
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
		System.out.println("W: "+"r:"+String.valueOf(white[0])+" g:"+String.valueOf(white[1])+" b:"+String.valueOf(white[2]));
		
		System.out.println("Place on red line");
		Button.waitForPress();
		red = cs.getColor();
		System.out.println("R: "+"r:"+String.valueOf(red[0])+" g:"+String.valueOf(red[1])+" b:"+String.valueOf(red[2]));
		
		System.out.println("Place on green line");
		Button.waitForPress();
		green = cs.getColor();
		System.out.println("G: "+"r:"+String.valueOf(green[0])+" g:"+String.valueOf(green[1])+" b:"+String.valueOf(green[2]));
		
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
		rThresh[1]=(int) ((double) 0.77*(white[1]-red[1]));
		rThresh[2]=(int) ((double) 0.77*(white[2]-red[2]));
		
		gThresh[0]=(int) ((double) 0.77*(white[0]-green[0]));
		gThresh[1]=(green[1]- black[0])/2;
		gThresh[2]=(int) ((double) 0.77*(white[2]-green[2]));
		
		bThresh[0]=(int) ((double) 0.77*(white[0]-blue[0]));
		bThresh[1]=(int) ((double) 0.77*(white[1]-blue[1]));
		bThresh[2]=(blue[2]-black[0])/2;
		
		kThresh[0]=(int) ((double) 0.77*(white[0]-black[0]));
		kThresh[1]=(int) ((double) 0.77*(white[1]-black[1]));
		kThresh[2]=(int) ((double) 0.77*(white[2]-black[2]));
		
		System.out.println("rThresh: "+"r:"+String.valueOf(rThresh[0])+" g:"+String.valueOf(rThresh[1])+" b:"+String.valueOf(rThresh[2]));
		System.out.println("gThresh: "+"r:"+String.valueOf(gThresh[0])+" g:"+String.valueOf(gThresh[1])+" b:"+String.valueOf(gThresh[2]));
		System.out.println("bThresh: "+"r:"+String.valueOf(bThresh[0])+" g:"+String.valueOf(bThresh[1])+" b:"+String.valueOf(bThresh[2]));
		System.out.println("kThresh: "+"r:"+String.valueOf(kThresh[0])+" g:"+String.valueOf(kThresh[1])+" b:"+String.valueOf(kThresh[2]));
		
		Button.waitForPress();
		
		int[] cv = new int[3];
		String c = "";
		for (;;)
		{
			cv = cs.getColor();
			
			if (cv[0] > rThresh[0] && cv[1] < rThresh[1] && cv[2] < rThresh[2]) {
				c = "R";
			}
			/*else if (cv[0] < gThresh[0] && cv[1] > gThresh[1] && cv[2] < gThresh[2]) {
				c = "G";
			}*/
			else if (cv[0] < bThresh[0] && cv[1] < bThresh[1] && cv[2] > bThresh[2]) {
				c = "B";
			}
			else if (cv[0] < kThresh[0] && cv[1] < kThresh[1] && cv[2] < kThresh[2]) {
				c = "K";
			}
			else if (cv[0] > rThresh[0] && cv[1] > gThresh[1] && cv[2] > bThresh[2]) {
				c = "W";
			}
			
			System.out.println(c + "|" + "r:"+String.valueOf(cv[0])+" g:"+String.valueOf(cv[1])+" b:"+String.valueOf(cv[2]));
		}
	}

}
