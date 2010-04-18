package se.uu.it.cats.brick.network.packet;

public class SimpleMeasurement extends Packet {
	
	public byte _type = 0x02;
	private float _angle;
	
	public static int LENGTH =
		1		// type - 1byte
		+ 1		// src  - 1byte
		+ 4;	// angle - 1float - 4bytes
	
	public SimpleMeasurement()
	{		
	}
	
	public SimpleMeasurement(float angle)
	{
		_angle = angle;
	}

	public void readImpl(byte[] bArr)
	{
		// _type is already defined,
		// no need to read it again from network 
	
		// read byte
		_src = readByte(bArr, 1);
		
		// read int
		_angle = readFloat(bArr, 2);
	}

	public byte[] writeImpl()
	{
		byte[] output = new byte[LENGTH];
		
		// write byte
		writeByte(_type, output, 0);
		
		// write byte
		writeByte(_src, output, 1);
		
		// write integer
		writeFloat(_angle, output, 2);
		
		return output;
	}

}
