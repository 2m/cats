package se.uu.it.cats.brick.network;

import se.uu.it.cats.brick.Logger;
import lejos.nxt.comm.BTConnection;
import lejos.nxt.comm.Bluetooth;

public class ConnectionListener implements Runnable
{
	public void run()
	{
		while (true)
		{
			BTConnection btc = Bluetooth.waitForConnection();
			
			if (btc != null)
			{			
				Logger.println("Received connection from: "+btc.getAddress());
			
				Thread t = new Thread(new ReceiveAndAckData(btc));
				t.start();
			}
			else
			{
				Logger.println("ConnectionListener got NP");
			}
		}
	}
}
