package se.uu.it.cats.brick.network;

import javax.bluetooth.RemoteDevice;

import lejos.nxt.comm.BTConnection;

public class LowLevelHandler extends ConnectionHandler
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
	
	protected int read()
    {
		if (_inReturnedUntil >= _inBufSize) _inBufSize = 0;
		if (_inBufSize <= 0)
		{
			_inBufSize = _btc.read(_inputBuffer, _inputBuffer.length, false);
			if (_inBufSize < -1) return _inBufSize;
			if (_inBufSize <= 0) return -1;
			_inReturnedUntil = 0;
		}
		return _inputBuffer[_inReturnedUntil++] & 0xFF;
	}
	
	protected int read(byte[] bArray)
	{
		int i;
		for (i = 0; i < bArray.length; i++)
		{
			byte b = (byte)read();
			if (b <= 0) return i;
			bArray[i] = b;
		}
		return i;
	}
	
	@Override
	public void run()
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void sendByte(byte b)
	{
		// TODO Auto-generated method stub
	}

}
