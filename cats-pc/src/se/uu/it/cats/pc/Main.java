package se.uu.it.cats.pc;

import lejos.pc.comm.NXTCommFactory;
import lejos.pc.comm.NXTConnector;
import lejos.pc.comm.NXTInfo;

public class Main
{
	public static void main(String[] args)
	{
		System.out.println("I am the computer!!!");
		
		// search for any nxt
		NXTConnector conn = new NXTConnector();
		
		System.out.println("Protocol NXTCommFactory.USB:");
		NXTInfo[] info = conn.search(null, null, NXTCommFactory.USB);
		for (NXTInfo i : info)
		{
			System.out.println("Object:"+i+" name:"+i.name);
		}
		
		System.out.println("Protocol NXTCommFactory.BLUETOOTH:");
		info = conn.search(null, null, NXTCommFactory.BLUETOOTH);
		for (NXTInfo i : info)
		{
			System.out.println("Object:"+i+" name:"+i.name);
		}
	}
}
