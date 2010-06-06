package se.uu.it.cats.brick.network.packet;

public class MoveOrder extends Packet
{
	public byte _type = 0x09;
	
	private float _x;
	private float _y;
	
	public static int LENGTH =
		1		// _type - 1byte
		+ 1		// _src  - 1byte
		+ 4		// _x 	 - 4bytes
		+ 4;	// _y 	 - 4bytes
	
	public MoveOrder()
	{
		
	}
	
	public MoveOrder(float x, float y)
	{
		_x = x;
		_y = y;
	}
	
	public float getX()
	{
		return _x;
	}
	
	public float getY()
	{
		return _y;
	}
	
	public void readImpl(byte[] bArr)
	{
		_src = readByte(bArr, 1);
		
		_x = readFloat(bArr, 2);
		_y = readFloat(bArr, 6);
	}

	public byte[] writeImpl()
	{
		byte[] output = new byte[getLength()];
		
		// write byte
		writeByte(_type, output, 0);
		
		// write byte
		writeByte(_src, output, 1);
		
		writeFloat(_x, output, 2);
		writeFloat(_y, output, 6);
		
		return output;
	}
	
	public int getLength()
	{
		return LENGTH;
	}
	
	public String toString()
	{
		return "MoveOrder[_type:"+_type+", _src:"+_src+"_x:"+_x+", _y:"+_y+"]";
	}

}
