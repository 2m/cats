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
		
		Thread t = new Thread(new ConnectionHandler("cat1"));
		t.start();
		
		Thread t2 = new Thread(new ConnectionHandler("cat2"));
		t2.start();
		
		Thread t3 = new Thread(new ConnectionHandler("cat3"));
		t3.start();
		
		Thread t4 = new Thread(new Mouse());
		t4.start();
	}
}
