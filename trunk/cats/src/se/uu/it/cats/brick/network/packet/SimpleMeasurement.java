package se.uu.it.cats.brick.network.packet;

public class SimpleMeasurement extends Packet {
	
	public byte _type = 0x02;
	
	private byte _id;
	private float _angle;
	private float _camAngle;
	
	public static int LENGTH =
		1		// type - 1byte
		+ 1		// src  - 1byte
		+ 1		// id - 1byte
		+ 4		// angle - 1float - 4bytes
		+ 4;	// camAngle - 1float - 4bytes
	
	public SimpleMeasurement()
	{		
	}
	
	public SimpleMeasurement(int id, float angle, float camAngle)
	{
		_id = (byte)id;
		_angle = angle;
		_camAngle = camAngle;
	}
	
	public int getId()
	{
		return (int)_id;
	}
	
	public float getAngle()
	{
		return _angle;
	}
	
	public float getCamAngle()
	{
		return _camAngle;
	}

	public void readImpl(byte[] bArr)
	{
		// _type is already defined,
		// no need to read it again from network 
	
		// read byte
		_src = readByte(bArr, 1);
		
		// read byte
		_id = readByte(bArr, 2);
		
		// read float
		_angle = readFloat(bArr, 3);
		
		// read float
		_camAngle = readFloat(bArr, 7);
	}

	public byte[] writeImpl()
	{
		byte[] output = new byte[getLength()];
		
		// write byte
		writeByte(_type, output, 0);
		
		// write byte
		writeByte(_src, output, 1);
		
		// write byte
		writeByte(_id, output, 2);
		
		// write float
		writeFloat(_angle, output, 3);
		
		// write float
		writeFloat(_camAngle, output, 7);
		
		return output;
	}
	
	public int getLength()
	{
		return LENGTH;
	}
	
	public String toString()
	{
		return "SimpleMeasurement[_type:"+_type+", _src:"+_src+", _id:"+_id+" _angle:"+_angle+" _camAngle:"+_camAngle+"]";
	}
	public String toStringMatlab()
	{
		return ""+_type+","+_src+","+_id+","+_angle+","+_camAngle;
	}
}
