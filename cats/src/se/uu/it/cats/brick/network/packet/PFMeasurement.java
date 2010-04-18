package se.uu.it.cats.brick.network.packet;

public class PFMeasurement extends Packet
{
	public byte _type = 0x01;
	
	private int _id;
	private int _t;
	private float _angle_m;
	private float _x_c;
	private float _y_c;
	
	public static int LENGTH =
		1		// _type - 1byte
		+ 1		// _src  - 1byte
		+ 4		// _id - 1int - 4bytes
		+ 4		// _t - 1int - 4bytes
		+ 4		// _angle_m - 1float - 4bytes
		+ 4		// _x_c - 1float - 4bytes
		+ 4;	// _y_c - 1float - 4bytes
	
	public PFMeasurement()
	{
	}
	
	public PFMeasurement(int id, int t, float angle_m, float x_c, float y_c)
	{
		_id = id;
		_t = t;
		_angle_m = angle_m;
		_x_c = x_c;
		_y_c = y_c;
	}
	
	public void readImpl(byte[] bArr)
	{
		// _type is already defined,
		// no need to read it again from network 
	
		// read byte
		_src = readByte(bArr, 1);
		
		_id = readInt(bArr, 2);
		_t = readInt(bArr, 6);
		
		_angle_m = readFloat(bArr, 10);
		_x_c = readFloat(bArr, 14);
		_y_c = readFloat(bArr, 18);
	}
	
	public byte[] writeImpl()
	{
		byte[] output = new byte[LENGTH];
		
		// write byte
		writeByte(_type, output, 0);
		
		// write byte
		writeByte(_src, output, 1);
		
		writeInt(_id, output, 2);
		writeInt(_t, output, 6);
		
		writeFloat(_angle_m, output, 10);
		writeFloat(_x_c, output, 14);
		writeFloat(_y_c, output, 18);
		
		return output;
	}
	
	public String toString()
	{
		return "PFMeasurement[_type:"+_type+", _src:"+_src+", _id:"+_id+", _t:"+_t+", _angle_m:"+_angle_m+", _x_c:"+_x_c+", _y_c:"+_y_c+"]";
	}
}
