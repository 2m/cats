package se.uu.it.cats.brick.network;

import java.io.IOException;

import se.uu.it.cats.brick.Logger;

public class SendData extends ConnectionHandler
{
	private static final int NUM_SENDS = 100;
	private static final int PACKET_LENGTH = 10;
	
	public SendData(String name)
	{
		super(name);
	}
	
	public void run()
	{
		connect();
		
		int readErrCount = 0;
		int flushErrCount = 0;
		int writeErrCount = 0;
		
		boolean success = false;
		
		for (int i = 0; i < NUM_SENDS; i++)
		{
			for (int j = 1; j <= PACKET_LENGTH; j++)
			{
				success = false;
				while (!success)
				{
					try
					{
						_dos.writeInt(j * 200);
						success = true;
					}
					catch (IOException ioe)
					{
						Logger.println("SendData: IO Exception writing bytes, i:"+i+" j:"+j);
						writeErrCount++;
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
					_dos.flush();
					success = true;
				}
				catch (IOException ioe)
				{
					Logger.println("SendData: IO Exception flushing, i:"+i);
					flushErrCount++;
					//Button.waitForPress();
			    	//System.exit(1);
				}
			}
			
			success = false;
			while (!success)
			{
				try
				{
					_dis.readInt();					
					success = true;
				}
				catch (IOException ioe)
				{
					Logger.println("SendData: IO Exception reading bytes, i:"+i);
					readErrCount++;
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
	}
}
