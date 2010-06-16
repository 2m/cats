package se.uu.it.cats.brick.network.packet;

/*
 * When the connection is closed normally or even when
 * the dongle is removed from the PC, a robot receives two
 * bytes -1 -17 which can be interpeted as a packet with a type
 * -1 and with with a source value of -17.
 */
public class CloseConnection extends Packet
{
	public byte _type = -1;
	
	public static int LENGTH =
		1		// _type - 1byte
		+ 1;		// _src  - 1byte
	
	public CloseConnection()
	{		
	}
	
	public void readImpl(byte[] bArr)
	{
		// will always be -17
		_src = readByte(bArr, 1);
	}

	public byte[] writeImpl()
	{
		byte[] output = new byte[getLength()];
		
		// write byte
		writeByte(_type, output, 0);
		
		// write byte
		writeByte((byte)-17, output, 1);
		
		return output;
	}
	
	public int getLength()
	{
		return LENGTH;
	}
	
	public String toString()
	{
		return "CloseConnection[_type:"+_type+", _src:"+_src+"]";
	}
	public String toStringMatlab()
	{
		return "Err: toStringMatlab() CloseConnection[]";
	}

}
