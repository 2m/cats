package se.uu.it.cats.brick.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;

import javax.bluetooth.RemoteDevice;

import se.uu.it.cats.brick.Logger;

import lejos.nxt.comm.BTConnection;
import lejos.nxt.comm.Bluetooth;
import lejos.util.Stopwatch;

public abstract class ConnectionHandler implements Runnable
{
	protected BTConnection _btc = null;
	
	protected DataInputStream _dis = null;
	protected DataOutputStream _dos = null;
	
	protected RemoteDevice _peerDevice = null;
	
	protected boolean _alive = false;
	
	protected Stopwatch sw = null;
	
	private byte[] _outputBuffer;
	private int _outBufSize = 0;
	
	private byte[] _inputBuffer;
	private int _inBufSize = 0;
	private int _inReturnedUntil = 0;
	
	public ConnectionHandler()
	{
		_outputBuffer = new byte[256];
		_inputBuffer = new byte[256];
	}
	
	public ConnectionHandler(RemoteDevice device)
	{
		this();
		_peerDevice = device;
	}
	
	public ConnectionHandler(BTConnection btc)
	{
		this();		
		_btc = btc;
		_peerDevice = ConnectionManager.getInstance().getDeviceByAddress(_btc.getAddress());
	}
	
	protected boolean connect()
	{
		sw = new Stopwatch();
		sw.reset();
		
		if (_btc == null)
		{
			_btc = Bluetooth.connect(_peerDevice);
			
		    if (_btc == null)
		    {
		    	Logger.println("Connect fail to: "+_peerDevice.getFriendlyName(false));
		    	return false;
		    }
		}
		
		_dis = _btc.openDataInputStream();
		_dos = _btc.openDataOutputStream();
		
		return true;
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
		/*while (available() <= 0)
		{
			try { Thread.sleep(10); } catch (Exception e) {}
		}*/
		
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
	
	protected int available()
    {
       if (_inReturnedUntil >= _inBufSize) _inBufSize = 0;
       if (_inBufSize == 0) {
    	   _inReturnedUntil = 0;
    	   _inBufSize = _btc.read(_inputBuffer, _inputBuffer.length, false);
           if (_inBufSize < -1) return -1;
           if (_inBufSize < 0) _inBufSize = 0;
       }
       return _inBufSize - _inReturnedUntil;
    }
	
	protected void read(byte[] bArray)
	{
		for (int i = 0; i < bArray.length; i++)
			bArray[i] = (byte)read();
	}
	
	protected void close()
	{
		if (_btc != null)
			_btc.close();
		_alive = false;
	}
	
	protected boolean isAlive()
	{
		return _alive;
	}
	
	protected void setAlive(boolean alive)
	{
		_alive = alive;
	}

	@Override
	public void run()
	{
		//_btc.close();
	}
	
	public abstract void sendByte();
	
	protected String getPeerName()
	{
		return _peerDevice.getFriendlyName(false);
	}
}
