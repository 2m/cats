package se.uu.it.cats.brick.network;

import javax.bluetooth.RemoteDevice;

import se.uu.it.cats.brick.Logger;
import se.uu.it.cats.brick.network.packet.Packet;

import lejos.nxt.comm.BTConnection;

public abstract class LowLevelHandler extends ConnectionHandler
{
	private byte[] _outputBuffer;
	private int _outBufSize = 0;
	
	private byte[] _inputBuffer;
	private int _inBufSize = 0;
	private int _inReturnedUntil = 0;
	
	public LowLevelHandler(RemoteDevice device)
	{
		super(device);
		
		_outputBuffer = new byte[256];
		_inputBuffer = new byte[256];
	}
	
	public LowLevelHandler(BTConnection btc)
	{
		super(btc);
		
		_outputBuffer = new byte[256];
		_inputBuffer = new byte[256];
	}
	
	protected void write(byte b)
	{		
		if (_outBufSize == _outputBuffer.length)
		{
    		flush();
    	}
		_outputBuffer[_outBufSize] = b;
		_outBufSize++;
	}
	
	protected void write(byte[] bArray)
	{
		for (int i = 0; i < bArray.length; i++)
			write(bArray[i]);
	}
	
	protected int flush()
	{
		if (_outBufSize > 0)
		{
			int result = _btc.write(_outputBuffer, _outBufSize, false);
			if (result < 0) return result;
			_outBufSize = 0;
		}
		
		return 0;
	}
	
	protected int read() throws Exception
    {
		if (_inReturnedUntil >= _inBufSize) _inBufSize = 0;
		if (_inBufSize <= 0)
		{
			_inBufSize = _btc.read(_inputBuffer, _inputBuffer.length, false);
			
			if (_inBufSize < 0) throw new ReadError("Lowlevel.read() error: "+_inBufSize);
			else if (_inBufSize == 0) throw new EmptyBuffer();
			
			_inReturnedUntil = 0;
		}
		return _inputBuffer[_inReturnedUntil++] & 0xFF;
	}
	
	protected int read(byte[] bArray, int sourceIndex)
	{
		int i, bytesRead = 0;
		for (i = sourceIndex; i < bArray.length - sourceIndex; i++)
		{
			byte b;
			try
			{
				b = (byte)read();
			}
			catch (ReadError ex)
			{
				Logger.println(ex.getMessage());
				return bytesRead;
			}
			catch (Exception ex)
			{				
				return bytesRead;
			}
			bArray[i] = b;
			bytesRead++;
		}
		return bytesRead;
	}
	
	protected int read(byte[] bArray)
	{
		return read(bArray, 0);
	}
	
	public void sendByte(byte b)
	{
		//Logger.println("S 66 to "+getPeerName());
		write(new byte[] {b});
		
		if (flush() < 0)
			ConnectionManager.getInstance().closeConnection(this);
	}
	
	public void sendPacket(Packet p)
	{
		p.setSource(getLocalId());
		
		Logger.print("Sending packet:");
		byte[] output = p.writeImpl();
		for (int i = 0; i < output.length; i++)
			Logger.print(output[i]+", ");
		Logger.println("of length"+output.length);
		
		write(p.writeImpl());
		
		if (flush() < 0)
			ConnectionManager.getInstance().closeConnection(this);
	}
	
	private class EmptyBuffer extends Exception
	{
	}
	
	private class ReadError extends Exception
	{
		public ReadError(String str)
		{
			super(str);
		}
	}
}
