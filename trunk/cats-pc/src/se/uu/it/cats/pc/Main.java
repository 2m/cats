package se.uu.it.cats.pc;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import lejos.pc.comm.*;

public class Main
{
	public static void main(String[] args)
	{
		System.out.println("I am the computer!!!");
		
		NXTConnector conn = new NXTConnector();
		
		/*System.out.println("Protocol NXTCommFactory.BLUETOOTH:");
		NXTInfo[] info = conn.search(null, null, NXTCommFactory.BLUETOOTH);
		for (NXTInfo nxti : info)
		{
			if (nxti.name.equals("cat1"))
			{
				connected = conn.connectTo(nxti, NXTComm.PACKET);
			}
		}*/
		
		NXTInfo nxt = new NXTInfo(NXTCommFactory.BLUETOOTH, "cat1", "00165302CC4E");
		boolean connected = conn.connectTo(nxt, NXTComm.PACKET);
		
		if (!connected)
		{
			System.err.println("Failed to connect to any NXT");
			System.exit(1);
		}
		
		DataOutputStream dos = conn.getDataOut();
		DataInputStream dis = conn.getDataIn();
		
		long sum = 0;
		long min = 9999;
		int minId = -1;
		long max = 0;
		int maxId = -1;
		
		for (int i = 0; i < 100; i++)
		{
			try
			{
				long t0 = System.currentTimeMillis();
				for (int j = 0; j < 200; j++)
					dos.writeByte(0x00);
				dos.flush();
				dis.readByte();
				long t1 = System.currentTimeMillis();
				
				long delta = t1 - t0;
				
				sum += delta;
				
				if (delta > max)
				{
					max = delta;
					maxId = i;
				}
				
				if (delta < min)
				{
					min = delta;
					minId = i;
				}
			}
			catch (IOException ioe)
			{
				System.out.println("IO Exception writing bytes:");
				System.out.println(ioe.getMessage());
				break;
			}
		}
		
		try
		{
			dis.close();
			dos.close();
			conn.close();
		}
		catch (IOException ioe)
		{
			System.out.println("IOException closing connection:");
			System.out.println(ioe.getMessage());
		}
		
		System.out.println("Average time: "+(sum / 100)+"ms");
		System.out.println("Average bw: "+(200 * 1024 / ((double)sum / 100) / 1000)+"kB/s");
		System.out.println("Max time: "+max+"ms ("+maxId+"), min time: "+min+"ms("+minId+")");
	}
}
