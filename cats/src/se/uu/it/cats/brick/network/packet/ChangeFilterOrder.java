package se.uu.it.cats.brick.network.packet;

public class ChangeFilterOrder extends Packet
{
	public byte _type = 0x11;
	
	public static int LENGTH =
		1		// _type - 1byte
		+ 1;	// _src  - 1byte
	
	public ChangeFilterOrder()
	{		
	}
	
	public void readImpl(byte[] bArr)
	{
		_src = readByte(bArr, 1);
	}

	public byte[] writeImpl()
	{
		byte[] output = new byte[getLength()];
		
		// write byte
		writeByte(_type, output, 0);
		
		// write byte
		writeByte(_src, output, 1);
		
		return output;
	}
	
	public int getLength()
	{
		return LENGTH;
	}
	
	public String toString()
	{
		return "ChangeFilterOrder[_type:"+_type+", _src:"+_src+"]";
	}
	public String toStringMatlab()
	{
		return "E ChangeFilterOrder[_type:"+_type+", _src:"+_src+"]";
	}
}
