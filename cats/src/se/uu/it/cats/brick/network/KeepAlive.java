package se.uu.it.cats.brick.network;

import javax.bluetooth.RemoteDevice;

import lejos.nxt.comm.BTConnection;
import se.uu.it.cats.brick.Logger;
import se.uu.it.cats.brick.network.packet.CloseConnection;
import se.uu.it.cats.brick.network.packet.Packet;
import se.uu.it.cats.brick.network.packet.PacketManager;
import se.uu.it.cats.brick.storage.StorageManager;

public class KeepAlive extends LowLevelHandler
{
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
		
		byte[] bArr = new byte[255];
		int index = 0;
		
		int byteCounter = 0;
		int packetCounter = 0;
		
		sw.reset();
		while (isAlive())
		{
			int received = read(bArr, index);
			
			if (received > 0)
			{
				// forward received data to other devices
				byte[] receivedBytes = new byte[received];
				System.arraycopy(bArr, index, receivedBytes, 0, received);
				ConnectionManager.getInstance().sendBytesToAllExcept(receivedBytes, getRemoteName());
				
				index = index + received;
				
				/*Logger.print("Rcvd:"+received+" input buffer:");
				for (int i = 0; i < index; i++)
					Logger.print(bArr[i]+", ");
				Logger.println("of length"+index);*/
				
				Packet p = PacketManager.getInstance().checkForCompletePackets(bArr, index);
				
				// do not continue if just received closed connection packet
				if (p instanceof CloseConnection)
				{
					setAlive(false);
					p = null;
				}
				
				// if some bytes were used to construct a packet, remove these bytes from the buffer
				// try to construct as many packets as possible				
				while (p != null)
				{
					int bytesRead = p.getLength();
					
					System.arraycopy(bArr, bytesRead, bArr, 0, index - bytesRead);
					index -= bytesRead;
					
					packetCounter++;
					
					p = PacketManager.getInstance().checkForCompletePackets(bArr, index);
				}
				
				if (index > 255)
				{
					//Logger.println("Received data buffer is full.");
				}
			}
			
			byteCounter += received;
			
			if (sw.elapsed() > 3000)
			{
				float byteBw = (float)byteCounter / 3;
				float packetBw = (float)packetCounter / 3;
				if (byteBw > 0.0)
				{
					//Logger.println("Byte bw from "+getRemoteName()+":"+byteBw+"B/s");
					Logger.println("Packet bw from "+getRemoteName()+":"+packetBw+"Pck/s");
				}
				sw.reset();
				byteCounter = 0;
				packetCounter = 0;
			}
			
			Thread.yield();
			//try { Thread.sleep(100); } catch (Exception ex) {}
		}
		
		ConnectionManager.getInstance().closeConnection(this);
	}
}
