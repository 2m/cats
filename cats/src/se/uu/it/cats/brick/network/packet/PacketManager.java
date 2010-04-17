package se.uu.it.cats.brick.network.packet;

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
				// check if there is enough data in the buffer
				// for this kind of packet
				Logger.println("arrayEndIndex:"+arrayEndIndex+" Timestamp.LENGTH:"+Timestamp.LENGTH);
				
				if (arrayEndIndex >= Timestamp.LENGTH)
				{
					Packet p = new Timestamp();
					p.readImpl(bArr);
					
					Logger.println("New packet read "+p);
					
					addToBuffer(p);
					
					return Timestamp.LENGTH;
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
