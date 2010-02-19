package se.uu.it.cats.brick.network;

import se.uu.it.cats.brick.Logger;
import lejos.nxt.comm.BTConnection;
import lejos.nxt.comm.Bluetooth;

public class ConnectionListener implements Runnable
{
	public static volatile boolean _canListen = true;
	
	public void run()
	{
		while (true)
		{
			if (_canListen)
			{
				BTConnection btc = Bluetooth.waitForConnection();
			
				if (btc != null)
				{			
					Logger.println("Received connection from: "+btc.getAddress());
				
					Thread t = new Thread(new ReceiveAndAckData(btc));
					t.start();
					//ReceiveAndAckData raad = new ReceiveAndAckData(btc);
					//raad.run();
				}
				else
				{
					Logger.println("ConnectionListener got NP");
				}
			}
			else
			{
				try
				{
					Thread.sleep(100);					
				}
				catch (InterruptedException ex)
				{
					
				}
			}
		}
	}
}
