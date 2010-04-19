package se.uu.it.cats.pc.network.packet;

import se.uu.it.cats.pc.Logger;
import se.uu.it.cats.pc.Mouse;

import se.uu.it.cats.brick.network.packet.PFMeasurement;
import se.uu.it.cats.brick.network.packet.Packet;
import se.uu.it.cats.brick.network.packet.SimpleMeasurement;
import se.uu.it.cats.brick.network.packet.Timestamp;

public class PacketManager
{
	private static PacketManager _instanceHolder = new PacketManager();
	
	private static final int BUF_COUNT = 4;
	
	//private PacketBuffer[] _buffer;
	
	private PacketManager()
	{
		//_buffer = new PacketBuffer[BUF_COUNT];
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
		
		switch (packetType)
		{
			case 0x00:
			{
				if (arrayEndIndex >= Timestamp.LENGTH)
				{
					p = new Timestamp();
					p.readImpl(bArr);
				}
				break;
			}
			case 0x01:
			{
				if (arrayEndIndex >= PFMeasurement.LENGTH)
				{
					p = new PFMeasurement();
					p.readImpl(bArr);
				}
				break;
			}
			case 0x02:
			{
				if (arrayEndIndex >= SimpleMeasurement.LENGTH)
				{
					p = new SimpleMeasurement();
					p.readImpl(bArr);
					
					Mouse._angles[p.getSource()] = ((SimpleMeasurement)p).getAngle();
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
	
	/*public boolean addToBuffer(Packet p)
	{
		int bufId = p.getSource();
		
		if (_buffer[bufId] == null)
			_buffer[bufId] = new PacketBuffer();
		
		return _buffer[bufId].addPacket(p);
	}*/
}
