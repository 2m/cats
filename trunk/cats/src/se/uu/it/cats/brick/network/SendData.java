package se.uu.it.cats.brick.network;

import java.io.IOException;

import se.uu.it.cats.brick.Logger;

import lejos.nxt.Button;

public class SendData extends ConnectionHandler
{
	private static final int NUM_SENDS = 100;
	private static final int PACKET_LENGTH = 1;
	
	public SendData(String name)
	{
		super(name);
	}
	
	public void run()
	{
		connect();
		
		for (int i = 0; i < NUM_SENDS; i++)
		{
			for (int j = 0; j < PACKET_LENGTH; j++)
			{
				try
				{
					_dos.writeByte(0x00);
				}
				catch (IOException ioe)
				{
					Logger.println("SendData: IO Exception writing bytes, i:"+i+" j:"+j);
					Button.waitForPress();
			    	System.exit(1);
				}
			}
			
			try
			{
				_dos.flush();
			}
			catch (IOException ioe)
			{
				Logger.println("SendData: IO Exception flushing, i:"+i);
				Button.waitForPress();
		    	System.exit(1);
			}
			
			try
			{
				_dis.readByte();
			}
			catch (IOException ioe)
			{
				Logger.println("SendData: IO Exception reading bytes, i:"+i);
				Button.waitForPress();
		    	System.exit(1);
			}
		}
		
		_btc.close();
	}
}
