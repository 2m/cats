package se.uu.it.cats.brick.network;

public class PacketManager
{
	private static PacketManager _instanceHolder = new PacketManager();
	
	private static final int BUF_SIZE = 256;
	
	private byte[] _buffer;
	
	private int _readIndex = 0;
	private int _writeIndex = 0;
	
	private boolean _empty = true;
	private boolean _full = false;
	
	public PacketManager()
	{
		_buffer = new byte[BUF_SIZE];
	}
	
	public static PacketManager getInstance()
	{
		return _instanceHolder;
	}
	
	public boolean addToBuffer(byte[] buff, int length)
	{
		for (int i = 0; i < length; i++)
			if (!addByte(buff[i]))
				return false;
		
		return true;
	}
	
	public boolean addByte(byte b)
	{
		if (!_full)
		{
			_buffer[_writeIndex] = b;
			_writeIndex = (_writeIndex + 1) % BUF_SIZE;
			
			if (_writeIndex == _readIndex)
				_full = true;
			else _full = false;
		}
		
		return !_full;
	}
}
