package se.uu.it.cats.brick.network.packet;

import se.uu.it.cats.brick.Logger;

public class Timestamp extends Packet
{
	public byte _type = 0x00;
	
	private int _timestamp;
	private int _roundTripTime;
	
	public static int LENGTH =
		1		// _type - 1byte
		+ 1		// _src  - 1byte
		+ 4		// _timestamp - 1int - 4bytes
		+ 4;	// _roundTripTime - 1int - 4bytes
	
	public Timestamp()
	{		
	}
	
	public Timestamp(int timestamp)
	{
		_timestamp = timestamp;
		Logger.println("timestamp:"+_timestamp);
	}
	
	public void setTimestamp(int timestamp)
	{
		_timestamp = timestamp;
	}
	
	public int getTimestamp()
	{
		return _timestamp;
	}
	
	public void setRoundTripTime(int roundTripTime)
	{
		_roundTripTime = roundTripTime;
	}
	
	public int getRoundTripTime()
	{
		return _roundTripTime;
	}	
	
	public void readImpl(byte[] bArr)
	{
		// _type is already defined,
		// no need to read it again from network 
	
		// read byte
		_src = readByte(bArr, 1);
		
		// read int
		_timestamp = readInt(bArr, 2);
		_roundTripTime = readInt(bArr, 6);
	}
	
	public byte[] writeImpl()
	{
		byte[] output = new byte[getLength()];
		
		// write byte
		writeByte(_type, output, 0);
		
		// write byte
		writeByte(_src, output, 1);
		
		// write integer
		writeInt(_timestamp, output, 2);
		writeInt(_roundTripTime, output, 6);
		
		return output;
	}
	
	public int getLength()
	{
		return LENGTH;
	}
	
	public String toString()
	{
		return "Timestamp[_type:"+_type+", _src:"+_src+", _timestamp:"+_timestamp+", _roundTripTime:"+_roundTripTime+"]";
	}
}
