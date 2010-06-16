package se.uu.it.cats.brick.network.packet;

public class SettingUpdate extends Packet
{
	public static final int USE_GUIDE = 0;
	public static final int USE_KALMAN = 1;
	
	public byte _type = 0x01;
	
	private int _setting;
	private int _value;
	
	public static int LENGTH =
		1		// _type - 1byte
		+ 1		// _src  - 1byte
		+ 4		// _setting - 1int - 4bytes
		+ 4;	// _value - 1int - 4bytes		
	
	public SettingUpdate()
	{
	}
	
	public SettingUpdate(int setting, int value)
	{
		_setting = setting;
		_value = value;
	}
	
	public int getSetting()
	{
		return _setting;
	}
	
	public int getValue()
	{
		return _value;
	}
	
	public void readImpl(byte[] bArr)
	{
		// _type is already defined,
		// no need to read it again from network 
	
		// read byte
		_src = readByte(bArr, 1);
		
		_setting = readInt(bArr, 2);
		_value = readInt(bArr, 6);
	}
	
	public byte[] writeImpl()
	{
		byte[] output = new byte[getLength()];
		
		// write byte
		writeByte(_type, output, 0);
		
		// write byte
		writeByte(_src, output, 1);
		
		writeInt(_setting, output, 2);
		writeInt(_value, output, 6);
		
		return output;
	}
	
	public int getLength()
	{
		return LENGTH;
	}
	
	public String toString()
	{
		return "SettingUpdate[_type:"+_type+", _src:"+_src+", _setting:"+_setting+", _value:"+_value+"]";
	}
	public String toStringMatlab()
	{
		return "E SettingUpdate[_type:"+_type+", _src:"+_src+", _setting:"+_setting+", _value:"+_value+"]";
	}
}
