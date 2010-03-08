package se.uu.it.cats.brick.network;

import javax.bluetooth.RemoteDevice;

import lejos.nxt.comm.BTConnection;

import se.uu.it.cats.brick.Logger;
import se.uu.it.cats.brick.storage.StorageManager;

public class KeepAlive extends LowLevelHandler
{
	private int counter;
	
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
		
		sw.reset();
		while (isAlive())
		{
			byte[] bArr = new byte[255];
			int received = read(bArr);
			
			if (received > 0)
				StorageManager.getInstance().dataInput(bArr[0]);
			
			//Logger.println("Received bytes:"+received);
			counter += received;
			
			if (sw.elapsed() > 3000)
			{
				Logger.println("BW from "+getPeerName()+":"+(counter/((float)3)+"B/s"));
				sw.reset();
				counter = 0;
			}
			
			try { Thread.sleep(100); } catch (Exception ex) {}
		}
		
		ConnectionManager.getInstance().closeConnection(this);
	}
	
	public void sendByte(byte b)
	{
		//Logger.println("S 66 to "+getPeerName());
		write(new byte[] {b});
		
		if (flush() < 0)
			ConnectionManager.getInstance().closeConnection(this);
	}	
}
