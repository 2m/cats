package se.uu.it.cats.brick.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;

import javax.bluetooth.RemoteDevice;

import lejos.nxt.comm.BTConnection;

public class StreamHandler extends ConnectionHandler
{
	protected DataInputStream _dis = null;
	protected DataOutputStream _dos = null;
	
	public StreamHandler(RemoteDevice device)
	{
		super(device);
	}
	
	public StreamHandler(BTConnection btc)
	{
		super(btc);
	}
	
	public boolean connect()
	{
		if (super.connect())
		{
			_dis = _btc.openDataInputStream();
			_dos = _btc.openDataOutputStream();
			
			return true;
		}
		
		return false;
	}
	
	protected void write(byte b)
	{
		try
		{
			_dos.writeByte(b);
		}
		catch (Exception e)
		{
			
		}
	}
	
	protected int flush()
	{
		try
		{
			_dos.flush();
		}
		catch (Exception e)
		{
			return -1;
		}
		
		return 0;
	}
	
	protected int read()
	{
		try
		{
			return (byte)_dis.readByte();
		}
		catch (Exception e)
		{
			return -1;
		}
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
