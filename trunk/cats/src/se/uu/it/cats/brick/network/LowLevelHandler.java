package se.uu.it.cats.brick.network;

import javax.bluetooth.RemoteDevice;

import se.uu.it.cats.brick.Clock;
import se.uu.it.cats.brick.Logger;
import se.uu.it.cats.brick.network.packet.Packet;
import se.uu.it.cats.brick.network.packet.SimpleMeasurement;

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
	
	protected void write(byte b) throws Exception
	{
		if (_outBufSize == _outputBuffer.length)
		{
    		flush();
    	}
		_outputBuffer[_outBufSize] = b;
		_outBufSize++;
	}
	
	protected void write(byte[] bArray) throws Exception
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
			
			if (_inBufSize == -2) throw new LostData();
			if (_inBufSize == -1) throw new ConnectionClosed();
			else if (_inBufSize == 0) throw new EmptyBuffer();
			else if (_inBufSize < 0) throw new Exception("Lowlevel.read() error: "+_inBufSize);			
			
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
			catch (EmptyBuffer ex)
			{
				return bytesRead;
			}
			catch (ConnectionClosed ex)
			{
				Logger.println("LowLevelHandler: ConnectionClosed exception.");
				setAlive(false);
				return bytesRead;
			}
			catch (LostData ex)
			{
				Logger.println("LowLevelHandler: LostData exception.");
				return bytesRead;
			}
			catch (Exception ex)
			{
				setAlive(false);
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
		try
		{
			write(new byte[] {b});
		}
		catch (Exception ex)
		{
			Logger.println("Error while writing byte: "+ex.toString());
			setAlive(false);
		}
		
		int result = flush();		
		if (result < 0)
		{
			Logger.println("Error while flushing byte: "+result);
			setAlive(false);
		}
	}
	
	public void sendBytes(byte[] bArr)
	{
		try
		{
			write(bArr);
		}
		catch (Exception ex)
		{
			Logger.println("Error while writing bytes: "+ex.toString());
			setAlive(false);
		}
		
		/*Logger.print("Sending bytes to "+getRemoteName()+":");		
		for (int i = 0; i < bArr.length; i++)
			Logger.print(bArr[i]+", ");
		Logger.println("of length"+bArr.length);*/
		
		int result = flush();		
		if (result < 0)
		{
			Logger.println("Error while flushing bytes:"+result);
			setAlive(false);
		}
	}
	
	public void sendPacket(Packet p)
	{
		p.setSource(getLocalId());
		
		/*Logger.print("Sending packet:");
		byte[] output = p.writeImpl();
		for (int i = 0; i < output.length; i++)
			Logger.print(output[i]+", ");
		Logger.println("of length"+output.length);*/
		
		try
		{
			write(p.writeImpl());
		}
		catch (Exception ex)
		{
			Logger.println("Error while writing packetbytes: "+ex.toString());
			setAlive(false);
		}
		
		int result = flush();		
		if (result < 0)
		{
			Logger.println("Error while flushing packet: "+result);
			//setAlive(false);
		}
	}
	
	private class EmptyBuffer extends Exception
	{
	}
	
	private class ConnectionClosed extends Exception
	{		
	}
	
	private class LostData extends Exception
	{		
	}
}
