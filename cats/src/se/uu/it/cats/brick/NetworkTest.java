package se.uu.it.cats.brick;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import lejos.nxt.LCD;
import lejos.nxt.comm.BTConnection;
import lejos.nxt.comm.Bluetooth;
import lejos.util.Stopwatch;

public class NetworkTest
{
	public void run()
	{
		try
		{
			// 16 symbols max to one line of lcd
			LCD.drawString("Waiting for conn", 0, 0);
			LCD.refresh();
			
			// blocks until connection accepted
			BTConnection btc = Bluetooth.waitForConnection();
			
			Stopwatch sw = new Stopwatch();
			
			LCD.drawString("Connected", 0, 1);
			LCD.refresh();
			
			DataInputStream dis = btc.openDataInputStream();
			DataOutputStream dos = btc.openDataOutputStream();
			
			int count = 0;
			boolean connectionOpen = true;
			
			byte b = 0x00;
			
			while (connectionOpen)
			{
				try
				{
					for (int j = 0; j < 200; j++)
						b = dis.readByte();
					
					dos.writeByte(b);
					dos.flush();
					
					count ++;
					
					//if (count >= 100)
					//	connectionOpen = false;
				}
				catch (IOException e)
				{
					connectionOpen = false;
				}
				
				//LCD.drawInt(n, 7, 0, 3);
				//LCD.refresh();
				//dos.writeInt(-n);
				//dos.flush();
			}
			
			int elapsed = sw.elapsed();
			
			LCD.drawString("ms: "+elapsed+" s: "+elapsed/1000, 0, 3);
			
			Thread.sleep(100); // wait for data to drain
			LCD.drawString("Closing conn", 0, 2);
			LCD.refresh();
			btc.close();
			//LCD.clear();
			
			while (true)
			{
				Thread.sleep(100);
			}
		}
		catch (Exception ex)
		{
			System.out.println(ex.getMessage());
		}
	}
}
