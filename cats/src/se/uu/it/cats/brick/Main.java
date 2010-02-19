package se.uu.it.cats.brick;

import lejos.nxt.Button;
import lejos.nxt.ButtonListener;
import lejos.nxt.comm.RConsole;
import se.uu.it.cats.brick.network.ConnectionListener;
import se.uu.it.cats.brick.network.SendData;

/**
 * Example leJOS Project with an ant build file
 *
 */
public class Main
{	
	public static void main(String[] args) throws InterruptedException
	{
		Logger.init();
		
		//ColorSensorTest cst = new ColorSensorTest();
		//cst.run();
		
		//PilotTest pt = new PilotTest();
		//pt.run();
		
		//NetworkTest nt = new NetworkTest();
		//nt.run();
		
		//ColorSensorTest2 cst2 = new ColorSensorTest2();
		//cst2.run();
		
		Thread listenerThread = new Thread(new ConnectionListener());
		listenerThread.start();
		
		Button.LEFT.addButtonListener(new ButtonListener() {
			public void buttonPressed(Button b) {}
			
			public void buttonReleased(Button b)
			{
				Thread initiatorThread = new Thread(new SendData("cat1"));
				initiatorThread.start();
			}
		});
		
		Button.ENTER.addButtonListener(new ButtonListener() {
			public void buttonPressed(Button b) {}
			
			public void buttonReleased(Button b)
			{
				Thread initiatorThread = new Thread(new SendData("cat2"));
				initiatorThread.start();
			}
		});
		
		Button.RIGHT.addButtonListener(new ButtonListener() {
			public void buttonPressed(Button b) {}
			
			public void buttonReleased(Button b)
			{
				Thread initiatorThread = new Thread(new SendData("cat3"));
				initiatorThread.start();
				//SendData sd = new SendData("cat3");
				//sd.run();
			}
		});
		
		while (!Button.ESCAPE.isPressed())
		{
			Thread.sleep(100);
		}
		
		if (RConsole.isOpen())
			RConsole.close();
	}
}
