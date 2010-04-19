package se.uu.it.cats.brick.network.packet;

public abstract class Packet
{
	public byte _type;
	public byte _src;
	
	public static int LENGTH;
	
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
	public abstract int getLength();
	
	public byte readByte(byte[] bArr, int startIndex)
	{
		return bArr[startIndex];
	}
	
	public void writeByte(byte b, byte[] bArr, int startIndex)
	{
		bArr[startIndex] = b;
	}
	
	public int readInt(byte[] bArr, int startIndex)
	{
		int result;
		
		// read integer
		result = (bArr[startIndex] & 0xFF);
		result = (result << 8) | (bArr[startIndex + 1] & 0xFF);
		result = (result << 8) | (bArr[startIndex + 2] & 0xFF);
		result = (result << 8) | (bArr[startIndex + 3] & 0xFF);
		
		return result;
	}
	
	public void writeInt(int i, byte[] bArr, int startIndex)
	{
		bArr[startIndex] = (byte)(i >>> 24);
		bArr[startIndex + 1] = (byte)(i >>> 16);
		bArr[startIndex + 2] = (byte)(i >>> 8);
		bArr[startIndex + 3] = (byte)(i);
	}
	
	public float readFloat(byte[] bArr, int startIndex)
	{
		int i = readInt(bArr, startIndex);
		return Float.intBitsToFloat(i);
	}
	
	public void writeFloat(float f, byte[] bArr, int startIndex)
	{
		writeInt(Float.floatToIntBits(f), bArr, startIndex);
	}
}
