package se.uu.it.cats.brick.network;

import se.uu.it.cats.brick.Logger;

import lejos.nxt.comm.BTConnection;

public class ReceiveAndAckData extends LowLevelHandler
{
	private static final int NUM_SENDS = 100;
	private static int PACKET_LENGTH = 128;
	private static final int ACK_LENGTH = 0;
	
	public ReceiveAndAckData(BTConnection btc)
	{
		super(btc);		
		ConnectionListener._canListen = false;
	}
	
	public void run()
	{
		if (!connect())
			return;
		
		int elapsed = sw.elapsed();
		//Logger.println("Connected in: "+elapsed+"ms");
		
		int readErrCount = 0;
		int flushErrCount = 0;
		int writeErrCount = 0;
		
		boolean success = false;
		long checksum = 0;
		
		/*if (PACKET_LENGTH == 0)
			PACKET_LENGTH = 1;
		else
			PACKET_LENGTH = PACKET_LENGTH * 2;*/
		
		long data = 0;
		byte[] input = new byte[8];
		
		// reset the stopwatch
		sw.reset();
		
		for (int i = 0; i < NUM_SENDS; i++)
		{
			for (int j = 0; j < PACKET_LENGTH; j++)
			{
				//if (available() > 0)
					read(input);
				
				for (int k = 0; k < input.length; k++)
					checksum += input[k];
				
				//try { Thread.sleep(100); } catch (Exception e) {}
				
				/*success = false;
				while (!success)
				{
					try
					{
						//Logger.println("Available:"+_btc.available(0));
						data = _dis.readLong();
						success = true;
					}
					catch (Exception ioe)
					{
						Logger.println("RAAD: IO Exception reading bytes, i:"+i+" j:"+j);
						readErrCount++;
						//Button.waitForPress();
				    	//System.exit(1);
					}
				}*/
			}
			
			for (int j = 0; j < ACK_LENGTH; j++)
			{
				_btc.write(input, 1);
				/*success = false;
				while (!success)
				{
					try
					{
						_dos.writeLong(-data);
						success = true;
					}
					catch (Exception ioe)
					{
						Logger.println("RAAD: IO Exception writing bytes, i:"+i);
						writeErrCount++;
						//Button.waitForPress();
				    	//System.exit(1);
					}
				}*/
			}
			
			/*success = false;
			while (!success)
			{
				try
				{
					_dos.flush();
					success = true;
				}
				catch (Exception ioe)
				{
					Logger.println("RADD: IO Exception flushing, i:"+i);
					flushErrCount++;
					//Button.waitForPress();
			    	//System.exit(1);
				}
			}*/
		}
		
		elapsed = sw.elapsed();
		
		_btc.close();
		Logger.println("Got to the end.");
		Logger.print("readErrCount: "+readErrCount);
		Logger.print(" flushErrCount: "+flushErrCount);
		Logger.println(" readErrCount: "+writeErrCount);
		Logger.print("PL: "+(PACKET_LENGTH * 8)+"B ");
		Logger.print("Took: "+elapsed+"ms ");
		Logger.print("Rountrip: "+(elapsed / NUM_SENDS)+"ms ");
		Logger.print("Sent: "+(NUM_SENDS * (PACKET_LENGTH + ACK_LENGTH) * 8)+"B ");
		Logger.println("BW: "+((NUM_SENDS * (PACKET_LENGTH + ACK_LENGTH) * 8) / ((float)elapsed / 1000))+"B/s");
		Logger.println("Checksum:"+String.valueOf(checksum));
		
		ConnectionListener._canListen = true;		
	}
	
	public void sendByte()
	{
		// stub
	}
}
