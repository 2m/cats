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
		
		int packetsSoFar = 0;
		
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
				packetsSoFar += packetCounter[0] + packetCounter[1] + packetCounter[2];
				//float byteBw = (float)(byteCounter[0] + byteCounter[1] + byteCounter[2]) / 3;
				float packetBw = (float)(packetCounter[0] + packetCounter[1] + packetCounter[2]) / 3;
				//Logger.println("Byte bw from "+getRemoteName()+":"+byteBw+"B/s");
				Logger.println("Pck bw from "+getRemoteName()+":"+packetBw+"Pck/s"+" "+(float)packetCounter[0] / 3+" "+(float)packetCounter[1] / 3+" "+(float)packetCounter[2] / 3+" "+packetsSoFar);
				
				sw.reset();
				
				byteCounter[0] = 0; byteCounter[1] = 0; byteCounter[2] = 0;
				packetCounter[0] = 0; packetCounter[1] = 0; packetCounter[2] = 0;
			}
			
			Thread.yield();
			//try { Thread.sleep(100); } catch (Exception ex) {}
		}
		
		Logger.println("KeepAlive: end of run() function.");
		ConnectionManager.getInstance().closeConnection(this);
	}
}
