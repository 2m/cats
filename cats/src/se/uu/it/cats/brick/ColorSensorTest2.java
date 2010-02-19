package se.uu.it.cats.brick;

import lejos.nxt.Button;
import lejos.nxt.LightSensor;
import lejos.nxt.SensorPort;

public class ColorSensorTest2
{
	public void run()
	{
		System.out.println("Program started, press button to continue");
		Button.waitForPress();

		//ColorSensor cs = new ColorSensor(SensorPort.S1);

		LightSensor ls = new LightSensor(SensorPort.S2);

		System.out.println("Place sensor 15mm from white surface");
		Button.waitForPress();
		//cs.initWhiteBalance();
		ls.calibrateHigh();


		System.out.println("Place sensor 15mm from black surface");
		Button.waitForPress();
		//cs.initBlackLevel();
		ls.calibrateLow();
		
		int[] surf = new int[5];
		
		System.out.println("Place on black line");
		Button.waitForPress();
		surf[0] = ls.readValue();
		if (surf[0] < 0){
			surf[0] = 0;
		}
		System.out.println("Value: "+String.valueOf(surf[0]));

		System.out.println("Place on surface 1");
		Button.waitForPress();
		surf[1] = ls.readValue();
		System.out.println("Value: "+String.valueOf(surf[1]));
		
		System.out.println("Place on surface 2");
		Button.waitForPress();
		surf[2] = ls.readValue();
		System.out.println("Value: "+String.valueOf(surf[2]));
		
		System.out.println("Place on surface 3");
		Button.waitForPress();
		surf[3] = ls.readValue();
		System.out.println("Value: "+String.valueOf(surf[3]));
		
		System.out.println("Place on surface 4");
		Button.waitForPress();
		surf[4] = ls.readValue();
		System.out.println("Value: "+String.valueOf(surf[4]));
		
		int[] thresh = new int[4];
		thresh[0]=surf[0]+(surf[1]-surf[0])/2+(surf[1]-surf[0])/4;
		System.out.println("thresh   : "+String.valueOf(thresh[0]));
		
		 for (int i=1; i<=3; i++) {
			 thresh[i]=surf[i]+(surf[i+1]-surf[i])/2;
			 System.out.println("thresh: "+String.valueOf(thresh[i]));
		 }
		 Button.waitForPress();
		
		//int[] result = new int[3];
		int lv;
		int newNum = -1;
		for (;;)
		{
			//result = cs.getColor();
			//System.out.println("r:"+String.valueOf(result[0])+" g:"+String.valueOf(result[1])+" b:"+String.valueOf(result[2]));
			lv = ls.readValue();
			if (lv<thresh[0]) {
				newNum = 0;
				System.out.println("Surf: "+String.valueOf(0));
			}
			else if (lv<thresh[1]) {
				System.out.println("Surf: "+String.valueOf(1));
				newNum = 1;
			}
			else if (lv<thresh[2]) {
				System.out.println("Surf: "+String.valueOf(2));
				newNum = 2;
			}
			else if (lv<thresh[3]) {
				System.out.println("Surf: "+String.valueOf(3));
				newNum = 3;
			}
			else {
				System.out.println("Surf: "+String.valueOf(4));
				newNum = 4;
			}
			

		}
	}

}
