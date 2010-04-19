package se.uu.it.cats.brick;

import lejos.nxt.comm.Bluetooth;

public class Identity
{
	private static String _name = Bluetooth.getFriendlyName();
	
	public static String getName()
	{
		return _name;
	}
}
