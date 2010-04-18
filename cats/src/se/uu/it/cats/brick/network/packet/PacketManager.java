package se.uu.it.cats.brick.network.packet;

import se.uu.it.cats.brick.Clock;
import se.uu.it.cats.brick.Logger;

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
	
	public int checkForCompletePackets(byte[] bArr, int arrayEndIndex)
	{
		// first byte should be packet type
		byte packetType = bArr[0];
		Logger.println("Checking for packets of type:"+packetType);
		
		switch (packetType)
		{
			case 0x00:
			{
				if (arrayEndIndex >= Timestamp.LENGTH)
				{
					Timestamp p = new Timestamp();
					p.readImpl(bArr);
					
					Logger.println("New packet read "+p);
					
					Clock.incommingPacket(p);
					//addToBuffer(p);
					
					return Timestamp.LENGTH;
				}
			}
			case 0x01:
			{
				if (arrayEndIndex >= PFMeasurement.LENGTH)
				{
					PFMeasurement p = new PFMeasurement();
					p.readImpl(bArr);
					
					Logger.println("New packet read "+p);
					
					//addToBuffer(p);
					
					return PFMeasurement.LENGTH;
				}
			}
			case 0x02:
			{
				if (arrayEndIndex >= SimpleMeasurement.LENGTH)
				{
					SimpleMeasurement p = new SimpleMeasurement();
					p.readImpl(bArr);
					
					Logger.println("New packet read "+p);
					
					//addToBuffer(p);
					
					return SimpleMeasurement.LENGTH;
				}
			}
		}
		
		return 0;
	}
	
	public boolean addToBuffer(Packet p)
	{
		int bufId = p.getSource();
		
		if (_buffer[bufId] == null)
			_buffer[bufId] = new PacketBuffer();
		
		return _buffer[bufId].addPacket(p);
	}
}
