package se.uu.it.cats.brick.network;

import java.io.IOException;

import se.uu.it.cats.brick.Logger;

import lejos.nxt.comm.BTConnection;

public class ReceiveAndAckData extends ConnectionHandler
{
	private static final int NUM_RECVS = 100;
	private static final int PACKET_LENGTH = 10;
	
	public ReceiveAndAckData(BTConnection btc)
	{
		super(btc);
		
		ConnectionListener._canListen = false;
	}
	
	public void run()
	{
		connect();
		
		int readErrCount = 0;
		int flushErrCount = 0;
		int writeErrCount = 0;
		
		boolean success = false;
		
		int data = 0;
		
		for (int i = 0; i < NUM_RECVS; i++)
		{
			for (int j = 0; j < PACKET_LENGTH; j++)
			{
				success = false;
				while (!success)
				{
					try
					{
						data = _dis.readInt();
						success = true;
					}
					catch (IOException ioe)
					{
						Logger.println("RAAD: IO Exception reading bytes, i:"+i+" j:"+j);
						readErrCount++;
						//Button.waitForPress();
				    	//System.exit(1);
					}
				}
			}
			
			success = false;
			while (!success)
			{
				try
				{
					_dos.writeInt(-data);
					success = true;
				}
				catch (IOException ioe)
				{
					Logger.println("RAAD: IO Exception writing bytes, i:"+i);
					writeErrCount++;
					//Button.waitForPress();
			    	//System.exit(1);
				}
			}
			
			success = false;
			while (!success)
			{
				try
				{
					_dos.flush();
					success = true;
				}
				catch (IOException ioe)
				{
					Logger.println("RADD: IO Exception flushing, i:"+i);
					flushErrCount++;
					//Button.waitForPress();
			    	//System.exit(1);
				}
			}
		}
		
		_btc.close();
		Logger.println("Got to the end.");
		Logger.print("readErrCount: "+readErrCount);
		Logger.print(" flushErrCount: "+flushErrCount);
		Logger.println(" readErrCount: "+readErrCount);
		ConnectionListener._canListen = true;
	}
}
