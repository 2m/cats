package se.uu.it.cats.brick.network;

import java.io.IOException;

import se.uu.it.cats.brick.Logger;

import lejos.nxt.Button;
import lejos.nxt.comm.BTConnection;

public class ReceiveAndAckData extends ConnectionHandler
{
	private static final int NUM_RECVS = 100;
	private static final int PACKET_LENGTH = 200;
	
	public ReceiveAndAckData(BTConnection btc)
	{
		super(btc);
	}
	
	public void run()
	{
		connect();
		
		for (int i = 0; i < NUM_RECVS; i++)
		{
			for (int j = 0; j < PACKET_LENGTH; j++)
			{
				boolean success = false;
				while (!success)
				{
					try
					{
						_dis.readByte();
						success = true;
					}
					catch (IOException ioe)
					{
						Logger.println("RAAD: IO Exception reading bytes, i:"+i+" j:"+j);
						Button.waitForPress();
				    	System.exit(1);
					}
				}
			}
			
			try
			{
				_dos.writeByte(0x00);
			}
			catch (IOException ioe)
			{
				Logger.println("RAAD: IO Exception writing bytes, i:"+i);
				Button.waitForPress();
		    	System.exit(1);
			}
			
			try
			{
				_dos.flush();
			}
			catch (IOException ioe)
			{
				Logger.println("RADD: IO Exception flushing, i:"+i);
				Button.waitForPress();
		    	System.exit(1);
			}
		}
		
		_btc.close();
	}
}
