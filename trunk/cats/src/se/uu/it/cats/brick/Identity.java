package se.uu.it.cats.brick;

import javax.bluetooth.RemoteDevice;

import lejos.nxt.comm.Bluetooth;

public class Identity
{
	public static final int CAT_COUNT = 3;
	
	public static final RemoteDevice[] _devices = new RemoteDevice[] {
		new RemoteDevice("cat0", "00165302CDC3", new byte[] {0, 0, 8, 4}),
		new RemoteDevice("cat1", "00165302CC4E", new byte[] {0, 0, 8, 4}),
		new RemoteDevice("cat2", "0016530E6938", new byte[] {0, 0, 8, 4}),		
		new RemoteDevice("dongle1", "0015832A3670", new byte[] {0, 0, 8, 4}),
		new RemoteDevice("dongle2", "000C783394E7", new byte[] {0, 0, 8, 4}),
		new RemoteDevice("MartinPC", "002556F9072D", new byte[] {0, 0, 8, 4}),
		new RemoteDevice("ChristianPC", "002243B7BDAA", new byte[] {0, 0, 8, 4})
	};
	
	private static String _name = Bluetooth.getFriendlyName();
	
	public static String getName()
	{
		return _name;
	}
	
	public static int getId()
	{
		return getIdByName(getName());
	}
	
	public static RemoteDevice getDeviceById(int id)
	{
		return _devices[id];
	}
	
	public static RemoteDevice getDeviceByAddress(String address)
	{
		for (int i = 0; i < _devices.length; i++)
			if (_devices[i].getDeviceAddr().equals(address))
				return _devices[i];
		
		return null;
	}
	
	public static int getIdByAddress(String address)
	{
		for (int i = 0; i < _devices.length; i++)
			if (_devices[i].getDeviceAddr().equals(address))
				return i;
		
		return -1;
	}
	
	public static int getIdByName(String name)
	{
		for (int i = 0; i < _devices.length; i++)
			if (_devices[i].getFriendlyName(false).equals(name))
				return i;
		
		return -1;
	}
}
