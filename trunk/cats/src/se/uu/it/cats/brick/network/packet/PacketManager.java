package se.uu.it.cats.brick.network.packet;

import se.uu.it.cats.brick.Clock;
import se.uu.it.cats.brick.Logger;
import se.uu.it.cats.brick.network.KeepAlive;

public class PacketManager
{
	private static PacketManager _instanceHolder = new PacketManager();
	
	private static final int BUF_COUNT = 4;
	
	private PacketBuffer[] _buffer;
	
	private PacketManager()
	{
		_buffer = new PacketBuffer[BUF_COUNT];
	}
	
	public static PacketManager getInstance()
	{
		return _instanceHolder;
	}
	
	public Packet checkForCompletePackets(byte[] bArr, int arrayEndIndex)
	{
		Packet p = null;
		
		if (arrayEndIndex == 0)
			// empty array, return
			return null;
		
		// first byte should be packet type
		byte packetType = bArr[0];
		//Logger.println("Checking for packets of type:"+packetType);
		
		// determine which packet do we have by type,
		// an create object from bytes
		switch (packetType)
		{
			case 0x00:
			{
				if (arrayEndIndex >= Timestamp.LENGTH)
				{
					p = new Timestamp();
					p.readImpl(bArr);
					
					Clock.incommingPacket((Timestamp)p);
					//addToBuffer(p);
				}
				break;
			}
			case 0x01:
			{
				if (arrayEndIndex >= PFMeasurement.LENGTH)
				{
					p = new PFMeasurement();
					p.readImpl(bArr);
					
					//addToBuffer(p);
				}
				break;
			}
			case 0x02:
			{
				if (arrayEndIndex >= SimpleMeasurement.LENGTH)
				{
					p = new SimpleMeasurement();
					p.readImpl(bArr);
					
					//addToBuffer(p);
				}
				break;
			}
			case -1:
			{
				if (arrayEndIndex >= CloseConnection.LENGTH)
				{
					p = new CloseConnection();
					p.readImpl(bArr);
					
					//addToBuffer(p);
				}
				break;
			}
		}
		
		if (p != null)
		{
			//Logger.println("New packet read "+p);
		}
		
		return p;
	}
	
	public boolean addToBuffer(Packet p)
	{
		int bufId = p.getSource();
		
		if (_buffer[bufId] == null)
			_buffer[bufId] = new PacketBuffer();
		
		return _buffer[bufId].addPacket(p);
	}
}
