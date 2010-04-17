package se.uu.it.cats.brick.network.packet;

public class PacketBuffer
{	
	private static final int BUF_SIZE = 32;
	
	private Packet[] _buffer;
	
	private int _readIndex = 0;
	private int _writeIndex = 0;
	
	private boolean _empty = true;
	private boolean _full = false;
	
	public PacketBuffer()
	{
		_buffer = new Packet[BUF_SIZE];
	}
	
	public boolean addPacket(Packet p)
	{
		if (!_full)
		{
			_buffer[_writeIndex] = p;
			_writeIndex = (_writeIndex + 1) % BUF_SIZE;
			
			if (_writeIndex == _readIndex)
				_full = true;
			else _full = false;
		}
		
		return !_full;
	}
}
