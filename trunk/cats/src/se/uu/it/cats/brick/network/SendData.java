package se.uu.it.cats.brick.network;

import java.util.Random;

import javax.bluetooth.RemoteDevice;

import se.uu.it.cats.brick.Logger;

public class SendData extends ConnectionHandler
{
	private static final int NUM_SENDS = 100;
	private static int PACKET_LENGTH = 5;
	private static final int ACK_LENGTH = 0;
	
	public SendData(RemoteDevice device)
	{
		super(device);
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
		
		Random r = new Random();
		
		byte[] output = new byte[200];
		for (int i = 0; i < output.length; i++)
			output[i] = (byte)r.nextInt();
		
		/*if (PACKET_LENGTH == 0)
			PACKET_LENGTH = 1;
		else
			PACKET_LENGTH = PACKET_LENGTH * 2;*/
		
		// reset the stopwatch
		sw.reset();
		
		for (int i = 0; i < NUM_SENDS; i++)
		{
			for (int j = 1; j <= PACKET_LENGTH; j++)
			{
				_btc.write(output, 200, true);
				for (int k = 0; k < output.length; k++)
					checksum += output[k];
				
				try { Thread.sleep(100); } catch (Exception e) {}
				
				/*success = false;
				while (!success)
				{
					try
					{
						_dos.writeLong(j+1);						
						success = true;
					}
					catch (Exception ioe)
					{
						Logger.println("SendData: IO Exception writing bytes, i:"+i+" j:"+j);						
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
					Logger.println("SendData: IO Exception flushing, i:"+i);					
					flushErrCount++;
					//Button.waitForPress();
			    	//System.exit(1);
				}
			}*/
			
			for (int j = 0; j < ACK_LENGTH; j++)
			{
				byte[] input = new byte[1];
				_btc.read(input, 1);
				/*success = false;
				while (!success)
				{
					try
					{
						//Logger.println("Available:"+_btc.available(0));
						_dis.readLong();
						success = true;
					}
					catch (Exception ioe)
					{
						Logger.println("SendData: IO Exception reading bytes, i:"+i);
						readErrCount++;
						//Button.waitForPress();
				    	//System.exit(1);
					}
				}*/
			}
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
		Logger.print("Sent: "+(NUM_SENDS * (PACKET_LENGTH + ACK_LENGTH * 8))+"B ");
		Logger.println("BW: "+((NUM_SENDS * (PACKET_LENGTH + ACK_LENGTH * 8)) / ((float)elapsed / 1000))+"B/s");
		Logger.println("Checksum:"+String.valueOf(checksum));
	}
}
