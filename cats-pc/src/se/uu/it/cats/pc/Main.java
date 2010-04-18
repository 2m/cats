package se.uu.it.cats.pc;

import se.uu.it.cats.pc.network.ConnectionHandler;

public class Main
{
	public static void main(String[] args)
	{
		//ColorSensor cs = new ColorSensor();
		//cs.run();
		
		Thread t = new Thread(new ConnectionHandler());
		t.start();
	}
}
