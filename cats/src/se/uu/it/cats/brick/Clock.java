package se.uu.it.cats.brick;

import se.uu.it.cats.brick.network.ConnectionManager;
import se.uu.it.cats.brick.network.packet.Timestamp;
import lejos.util.Stopwatch;

public class Clock
{
	private static Stopwatch _sw = null;
	private static int _offset;
	
	private static boolean _master = false;
	
	public static void init()
	{
		_sw = new Stopwatch();
		_sw.reset();
	}
	
	public static int timestamp()
	{
		return _sw.elapsed() + _offset;
	}
	
	public static void syncWith(int i)
	{
		_master = true;
		ConnectionManager.getInstance().sendPacketTo(i, new Timestamp(Clock.timestamp()));
	}
	
	public static void incommingPacket(Timestamp p)
	{
		if (!_master)
		{
			p.setRoundTripTime(Clock.timestamp());
			ConnectionManager.getInstance().sendPacketTo(p.getSource(), p);
		}
		else
		{
			int lag = (p.getTimestamp() - Clock.timestamp()) - 2;
			
			_offset = p.getRoundTripTime() - (Clock.timestamp() - lag);
			Logger.println("Received second packet. Lag is:"+lag);
			
			_master = false;
		}
	}
}
