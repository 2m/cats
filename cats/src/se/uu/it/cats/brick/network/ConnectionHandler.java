package se.uu.it.cats.brick.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.bluetooth.RemoteDevice;

import se.uu.it.cats.brick.Logger;

import lejos.nxt.comm.BTConnection;
import lejos.nxt.comm.Bluetooth;
import lejos.util.Stopwatch;

public class ConnectionHandler implements Runnable
{
	public static final RemoteDevice CAT1 = new RemoteDevice("cat1", "00165302CC4E", new byte[] {0, 0, 8, 4});
	public static final RemoteDevice CAT2 = new RemoteDevice("cat2", "0016530E6938", new byte[] {0, 0, 8, 4});
	public static final RemoteDevice CAT3 = new RemoteDevice("cat3", "00165302CDC3", new byte[] {0, 0, 8, 4});
		
	protected BTConnection _btc = null;
	
	protected DataInputStream _dis = null;
	protected DataOutputStream _dos = null;
	
	protected RemoteDevice _peerDevice = null;
	
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
			int result = _btc.write(_outputBuffer, _outBufSize);
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
			_inBufSize = _btc.read(_inputBuffer, _inputBuffer.length);
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

	@Override
	public void run()
	{
		//_btc.close();
	}
}
