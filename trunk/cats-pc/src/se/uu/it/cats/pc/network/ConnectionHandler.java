package se.uu.it.cats.pc.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import se.uu.it.cats.brick.network.packet.Packet;
import se.uu.it.cats.pc.Logger;
import se.uu.it.cats.pc.network.packet.PacketManager;

import lejos.pc.comm.NXTConnector;

public class ConnectionHandler implements Runnable
{
	DataOutputStream _dos = null;
	DataInputStream _dis = null;
	
	boolean _alive = false;
	
	String _remoteName;
	
	public ConnectionHandler(String remoteName)
	{
		_remoteName = remoteName;
	}
	
	public boolean connect()
	{
		NXTConnector conn = new NXTConnector();
		boolean connected = conn.connectTo("btspp://"+_remoteName);
		
		if (!connected)
		{
			System.err.println("Failed to connect to any NXT");
			return false;
		}
		
		_dos = conn.getDataOut();
		_dis = conn.getDataIn();
		
		return true;
	}
	
	protected boolean isAlive()
	{
		return _alive;
	}
	
	protected void setAlive(boolean alive)
	{
		_alive = alive;
	}
	
	public void run()
	{
		setAlive(connect());
		
		byte[] bArr = new byte[255];
		int index = 0;
		
		int packetCounter = 0;
		long startTime = System.currentTimeMillis();
		
		while (isAlive())
		{
			try
			{
				// read only 6 bytes or less if the buffer is full
				// this function blocks as long as there is some input available
				// we want to execute immediately as at least one packet is available
				int received = _dis.read(bArr, index, Math.min(6, 255 - index));
				
				index = index + received;
				
				/*Logger.print("Rcvd:"+received+" input buffer:");
				for (int i = 0; i < index; i++)
					Logger.print(bArr[i]+", ");
				Logger.println("of length"+index);*/
				
				Packet p = PacketManager.getInstance().checkForCompletePackets(bArr, index);
				
				while (p != null)
				{
					int bytesRead = p.getLength();
					
					System.arraycopy(bArr, bytesRead, bArr, 0, index - bytesRead);
					index -= bytesRead;
					packetCounter++;
					
					p = PacketManager.getInstance().checkForCompletePackets(bArr, index);
				}
				
				if (index > 255)
					Logger.println("Received data buffer is full.");
			}
			catch (IOException ioe)
			{
				Logger.println("IO Exception reading bytes:");
				Logger.println(ioe.getMessage());
				break;
			}
			catch (Exception ex)
			{
				// connection broken, return from this function gracefully (somehow)
				Logger.println("Exception reading bytes:");
				Logger.println(ex.getMessage());
			}
			
			if (System.currentTimeMillis() - startTime > 3000)
			{
				float currentBw = (float)packetCounter / 3;
				if (currentBw > 0.0)
				{
					//Logger.println("BW from "+getRemoteName()+":"+currentBw+"Pck/s");
				}
				
				packetCounter = 0;
				startTime = System.currentTimeMillis();
			}
		}
		
		/*
		
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
		
		ConnectionManager.getInstance().closeConnection(this);*/
	}
	
	protected String getRemoteName()
	{
		return _remoteName;
	}

}
