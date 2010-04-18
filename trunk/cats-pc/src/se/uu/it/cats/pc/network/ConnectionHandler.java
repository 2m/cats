package se.uu.it.cats.pc.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import lejos.pc.comm.NXTConnector;

public class ConnectionHandler implements Runnable
{
	DataOutputStream _dos = null;
	DataInputStream _dis = null;
	
	boolean _alive = false;
	
	public boolean connect()
	{
		NXTConnector conn = new NXTConnector();
		boolean connected = conn.connectTo("btspp://");
		
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
		
		while (isAlive())
		{
			try
			{			
				System.out.println("Received " + _dis.readByte());
			}
			catch (IOException ioe)
			{
				System.out.println("IO Exception reading bytes:");
				System.out.println(ioe.getMessage());
				break;
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

}
