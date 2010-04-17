package se.uu.it.cats.brick.network.packet;

import se.uu.it.cats.brick.Logger;

public class Timestamp extends Packet
{
	public byte _type = 0x00;
	
	private int _timestamp;
	
	public static int LENGTH =
		1		// type - 1byte
		+ 1		// src  - 1byte
		+ 4;	// timestamp - 1int - 4bytes
	
	public Timestamp()
	{		
	}
	
	public Timestamp(int timestamp)
	{
		_timestamp = timestamp;
		Logger.println("timestamp:"+_timestamp);
	}
	
	public void readImpl(byte[] bArr)
	{
		// read byte
		_src = bArr[1];
		
		// read int
		_timestamp = (bArr[2] & 0xFF);
		_timestamp = (_timestamp << 8) | (bArr[3] & 0xFF);
		_timestamp = (_timestamp << 8) | (bArr[4] & 0xFF);
		_timestamp = (_timestamp << 8) | (bArr[5] & 0xFF);		
	}
	
	public byte[] writeImpl()
	{
		byte[] output = new byte[LENGTH];
		
		// write byte
		output[0] = _type;
		
		// write byte
		output[1] = _src;
		
		// write int
		output[2] = (byte)(_timestamp >>> 24);
		output[3] = (byte)(_timestamp >>> 16);
		output[4] = (byte)(_timestamp >>> 8);
		output[5] = (byte)(_timestamp);
		
		return output;
	}
	
	public String toString()
	{
		return "Timestamp[_type:"+_type+", _src:"+_src+" _timestamp:"+_timestamp+"]";
	}
}
