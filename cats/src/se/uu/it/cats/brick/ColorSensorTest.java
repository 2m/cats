package se.uu.it.cats.brick;

import lejos.nxt.Button;
import lejos.nxt.SensorPort;
import lejos.nxt.addon.ColorSensor;

public class ColorSensorTest
{
	public void run()
	{
		System.out.println("Program started, press button to continue");
		Button.waitForPress();
		
		ColorSensor cs = new ColorSensor(SensorPort.S1);
		
		System.out.println("Place sensor 15mm from white surface");
		Button.waitForPress();
		cs.initWhiteBalance();
		
		System.out.println("Place sensor 15mm from black surface");
		Button.waitForPress();
		cs.initBlackLevel();
		
		int[] result = new int[3];
		for (;;)
		{
			result = cs.getColor();
			System.out.println("r:"+String.valueOf(result[0])+" g:"+String.valueOf(result[1])+" b:"+String.valueOf(result[2]));
		}
	}
}
