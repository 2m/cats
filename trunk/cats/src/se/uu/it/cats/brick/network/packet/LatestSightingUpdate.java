package se.uu.it.cats.brick.network.packet;

public class LatestSightingUpdate extends Packet
{
	public byte _type = 0x03;
	
	private float _x;
	private float _y;
	private float _theta;	
	
	public static int LENGTH =
		1		// _type - 1byte
		+ 1		// _src  - 1byte
		+ 4		// _x - 1float - 4bytes
		+ 4		// _y - 1float - 4bytes
		+ 4;	// _theta - 1float - 4bytes
	
	public LatestSightingUpdate()
	{		
	}
	
	public LatestSightingUpdate(float x, float y, float theta)
	{
		_x = x;
		_y = y;
		_theta = theta;
	}
	
	public float getX()
	{
		return _x;
	}
	
	public float getY()
	{
		return _y;
	}
	
	public float getTheta()
	{
		return _theta;
	}
	
	public void readImpl(byte[] bArr)
	{
		// _type is already defined,
		// no need to read it again from network 
	
		// read byte
		_src = readByte(bArr, 1);
		
		// read float
		_x = readFloat(bArr, 2);
		_y = readFloat(bArr, 6);
		_theta = readFloat(bArr, 10);
	}
	
	public byte[] writeImpl()
	{
		byte[] output = new byte[getLength()];
		
		// write byte
		writeByte(_type, output, 0);
		
		// write byte
		writeByte(_src, output, 1);
		
		// write float
		writeFloat(_x, output, 2);
		writeFloat(_y, output, 6);
		writeFloat(_theta, output, 10);
		
		return output;
	}
	
	public int getLength()
	{
		return LENGTH;
	}
	
	public String toString()
	{
		return "LatestSightingUpdate[_type:"+_type+", _src:"+_src+", _x:"+_x+", _y:"+_y+", _theta:"+_theta+"]";
	}
}
