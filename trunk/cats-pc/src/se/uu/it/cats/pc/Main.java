package se.uu.it.cats.pc;

import se.uu.it.cats.pc.Logger;
import se.uu.it.cats.pc.network.ConnectionHandler;

public class Main
{
	public static void main(String[] args)
	{
		Logger.init();
		//ColorSensor cs = new ColorSensor();
		//cs.run();
		
		Thread t4 = new Thread(new Mouse());
		t4.start();
	}
}
