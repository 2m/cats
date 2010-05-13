package se.uu.it.cats.brick.network.packet;

import se.uu.it.cats.brick.Logger;

public class Timestamp extends Packet
{
	public byte _type = 0x00;
	
	private byte _dst;
	private int _clientTime;
	private int _serverTime;
	
	public static int LENGTH =
		1		// _type - 1byte
		+ 1		// _src  - 1byte
		+ 1		// _dst  - 1byte
		+ 4		// _clientTime - 1int - 4bytes
		+ 4;	// _serverTime - 1int - 4bytes
	
	public Timestamp()
	{		
	}
	
	public Timestamp(int clientTime)
	{
		_clientTime = clientTime;
		Logger.println("_clientTime:"+_clientTime);
	}
	
	public void setDestination(int dst)
	{
		_dst = (byte)dst;
	}
	
	public int getDestination()
	{
		return (int)_dst;
	}
	
	public void setClientTime(int clientTime)
	{
		_clientTime = clientTime;
	}
	
	public int getClientTime()
	{
		return _clientTime;
	}
	
	public void setServerTime(int serverTime)
	{
		_serverTime = serverTime;
	}
	
	public int getServerTime()
	{
		return _serverTime;
	}	
	
	public void readImpl(byte[] bArr)
	{
		// _type is already defined,
		// no need to read it again from network 
	
		// read byte
		_src = readByte(bArr, 1);
		_dst = readByte(bArr, 2);
		
		// read int
		_clientTime = readInt(bArr, 3);
		_serverTime = readInt(bArr, 7);
	}
	
	public byte[] writeImpl()
	{
		byte[] output = new byte[getLength()];
		
		// write byte
		writeByte(_type, output, 0);
		
		// write byte
		writeByte(_src, output, 1);
		
		writeByte(_dst, output, 2);
		
		// write integer
		writeInt(_clientTime, output, 3);
		writeInt(_serverTime, output, 7);
		
		return output;
	}
	
	public int getLength()
	{
		return LENGTH;
	}
	
	public String toString()
	{
		return "Timestamp[_type:"+_type+", _src:"+_src+", _dst:"+_dst+" _clientTime:"+_clientTime+", _serverTime:"+_serverTime+"]";
	}
}
