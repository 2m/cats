package se.uu.it.cats.brick.network;

import javax.bluetooth.RemoteDevice;

import lejos.nxt.comm.BTConnection;
import se.uu.it.cats.brick.Logger;
import se.uu.it.cats.brick.network.packet.Packet;
import se.uu.it.cats.brick.network.packet.PacketManager;
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
		ConnectionListener.setListen(false);
	}
	
	public void run()
	{
		setAlive(connect());
		
		int index = 0;
		
		sw.reset();
		while (isAlive())
		{
			byte[] bArr = new byte[255];
			
			int received = read(bArr, index);
			
			if (received > 0)
			{
				// forward received data to other devices
				byte[] receivedBytes = new byte[received];
				System.arraycopy(bArr, index, receivedBytes, 0, received);
				ConnectionManager.getInstance().sendBytesToAllExcept(receivedBytes, getRemoteName());
				
				index = index + received;
				
				Logger.print("Rcvd:"+received+" input buffer:");			
				for (int i = 0; i < index; i++)
					Logger.print(bArr[i]+", ");
				Logger.println("of length"+index);
				
				int bytesRead = PacketManager.getInstance().checkForCompletePackets(bArr, index);
				
				// if some bytes were used to construct a packet, remove these bytes from the buffer
				if (bytesRead > 0)
				{
					System.arraycopy(bArr, bytesRead, bArr, 0, index - bytesRead);
					index -= bytesRead;
				}
				
				if (index > 255)
					Logger.println("Received data buffer is full.");
			}
			
			counter += received;
			
			if (sw.elapsed() > 3000)
			{
				float currentBw = (float)counter / 3;
				if (currentBw > 0.0)
					Logger.println("BW from "+getRemoteName()+":"+currentBw+"B/s");
				sw.reset();
				counter = 0;
			}
			
			try { Thread.sleep(100); } catch (Exception ex) {}
		}
		
		ConnectionManager.getInstance().closeConnection(this);
	}
}
