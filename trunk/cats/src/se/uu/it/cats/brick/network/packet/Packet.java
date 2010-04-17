package se.uu.it.cats.brick.network.packet;

public abstract class Packet
{
	public byte _type;
	public byte _src;
	
	public Packet()
	{		
	}
	
	public void setSource(int src)
	{
		_src = (byte)src;
	}
	
	public int getSource()
	{
		return (int)_src;
	}
	
	public abstract void readImpl(byte[] bArr);	
	public abstract byte[] writeImpl();
}
