package se.uu.it.cats.brick;

import se.uu.it.cats.brick.network.ConnectionManager;
import se.uu.it.cats.brick.network.packet.Timestamp;
import lejos.util.Stopwatch;

public class Clock
{
	private static final String SERVER_NAME = "cat0";
	private static final int SERVER_ID = 0;
	
	private static Stopwatch _sw = null;
	private static int _offset = 0;
	
	private static boolean _server = false;	
	
	public static void init()
	{
		_sw = new Stopwatch();
		_sw.reset();
		
		if (Identity.getName().equals(SERVER_NAME))
			_server = true;
	}
	
	public static int timestamp()
	{
		return _sw.elapsed() + _offset;
	}
	
	public static void syncTime()
	{
		if (!_server)
			ConnectionManager.getInstance().sendPacketTo(SERVER_ID, new Timestamp(Clock.timestamp()));
	}
	
	public static void incommingPacket(Timestamp p)
	{
		if (_server)
		{
			// server actions
			p.setDestination(p.getSource());
			p.setServerTime(Clock.timestamp());			
			ConnectionManager.getInstance().sendPacketTo(p.getSource(), p);
		}
		else if (p.getSource() == SERVER_ID && p.getDestination() == Identity.getId())
		{
			// client actions if the packet came from server 
			int lag = (Clock.timestamp() - p.getClientTime()) / 2;
			
			int offsetDelta = p.getServerTime() + lag - Clock.timestamp();
			_offset += offsetDelta;
			//Logger.println("Lag:"+lag+" offsetDelta:"+offsetDelta+" _offset:"+_offset+" p.getClientTime()"+p.getClientTime()+" p.getServerTime()"+p.getServerTime());
		}
	}
}