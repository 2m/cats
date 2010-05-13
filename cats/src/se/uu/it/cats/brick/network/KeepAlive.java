package se.uu.it.cats.brick.network;

import javax.bluetooth.RemoteDevice;

import lejos.nxt.comm.BTConnection;
import se.uu.it.cats.brick.Logger;
import se.uu.it.cats.brick.network.packet.CloseConnection;
import se.uu.it.cats.brick.network.packet.Packet;
import se.uu.it.cats.brick.network.packet.PacketManager;
import se.uu.it.cats.brick.network.packet.SimpleMeasurement;
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
	}
	
	public void run()
	{
		setAlive(connect());
		
		byte[] bArr = new byte[255];
		int index = 0;
		
		int byteCounter[] = new int[ConnectionManager.MAX_OUTBOUND_CONN];
		int packetCounter[] = new int[ConnectionManager.MAX_OUTBOUND_CONN];
		
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
					Logger.println("CloseConnection packet received.");
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
					
					packetCounter[p.getSource()]++;
					byteCounter[p.getSource()] += bytesRead;
					
					p = PacketManager.getInstance().checkForCompletePackets(bArr, index);
				}
				
				if (index > 255)
				{
					Logger.println("Received data buffer is full.");
				}
			}
						
			if (sw.elapsed() > 3000)
			{
				int packetsSoFar = 0;
				int bwSoFar = 0;
				for (int i = 0; i < packetCounter.length; i++)
				{
					packetsSoFar += packetCounter[i];
					packetCounter[i] = 0;
					
					bwSoFar += byteCounter[i];
					byteCounter[i] = 0;
				}
				
				float packetBw = (float)packetsSoFar / 3;
				float byteBw = (float)bwSoFar / 3;				
				
				if (packetBw != 0)
					Logger.println("Pck bw from "+getRemoteName()+":"+packetBw+"Pck/s");
				
				sw.reset();
			}
			
			Thread.yield();
			//try { Thread.sleep(100); } catch (Exception ex) {}
		}
		
		Logger.println("KeepAlive: end of run() function.");
		ConnectionManager.getInstance().closeConnection(this);
	}
}
