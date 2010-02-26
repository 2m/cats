package se.uu.it.cats.brick.network;

import javax.bluetooth.RemoteDevice;

import lejos.nxt.comm.BTConnection;

import se.uu.it.cats.brick.Logger;

public class KeepAlive extends ConnectionHandler
{
	public KeepAlive(RemoteDevice device)
	{
		super(device);
	}
	
	public KeepAlive(BTConnection btc)
	{
		super(btc);		
		ConnectionListener._canListen = false;
	}
	
	public void run()
	{
		setAlive(connect());
		
		while (isAlive())
		{
			int b = read();
			if (b >= 0)
				Logger.println("Received "+b+" from "+getPeerName());
			try { Thread.sleep(100); } catch (Exception ex) {}
		}
		
		ConnectionManager.getInstance().closeConnection(this);
	}
	
	public void sendByte()
	{
		Logger.println("Sent 66 to "+getPeerName());
		write((byte)66);
		
		if (flush() < 0)
			ConnectionManager.getInstance().closeConnection(this);
	}	
}
