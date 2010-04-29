package se.uu.it.cats.brick;

import java.util.Random;

import lejos.nxt.Button;
import lejos.nxt.ButtonListener;
import lejos.nxt.LCD;
import lejos.nxt.Sound;
import lejos.nxt.comm.RConsole;
import lejos.util.Timer;
import lejos.util.TimerListener;
import se.uu.it.cats.brick.filter.SimpleFilter;
import se.uu.it.cats.brick.network.ConnectionListener;
import se.uu.it.cats.brick.network.ConnectionManager;
import se.uu.it.cats.brick.network.packet.PFMeasurement;
import se.uu.it.cats.brick.network.packet.Packet;
import se.uu.it.cats.brick.network.packet.SimpleMeasurement;
import se.uu.it.cats.brick.network.packet.Timestamp;
import se.uu.it.cats.brick.storage.StorageManager;

/**
 * Example leJOS Project with an ant build file
 *
 */
public class Main
{	
	public static void main(String[] args) throws InterruptedException
	{
		Logger.init();
		Clock.init();
		
		//ColorSensorTest cst = new ColorSensorTest();
		//cst.run();
		
		//PilotTest pt = new PilotTest();
		//pt.run();
		
		//NetworkTest nt = new NetworkTest();
		//nt.run();
		
		//ColorSensorTest2 cst2 = new ColorSensorTest2();
		//cst2.run();
		
		Thread listenerThread = new Thread(new ConnectionListener());
		listenerThread.start();
		
		//Run SimpleFilter:
		//Thread filterThread = new Thread(new SimpleFilter());
		//filterThread.start();
		
		//Run MovementPilot:
		float[] c_state = {0, 0, (float) (Math.PI/2)};
		MovementPilot mPilot = new MovementPilot(c_state);
		
		PilotPoll pilotPoll = new PilotPoll(mPilot.pilot, c_state);
		Thread pilotPollThread = new Thread(pilotPoll);
		pilotPollThread.start();
		//try{Thread.sleep(1000);}catch(Exception ex){}
		
		//mPilot.travel(0.3f, 0);
		//mPilot.travel(0, 3.0f); //straight going test
		//System.out.println(pilotPoll.x);
		//System.out.println(pilotPoll.y);
		//System.out.println();
		for (int i=0; i<2; i++){ //turn in square
			mPilot.travel(0.3f, 0);
			System.out.println("COMMAND FINISHED!");
		}
		//for (int i=0; i<10; i++) //motor ops. speed test
		//	mPilot.travel(0, 0.005f);
		
		Button.LEFT.addButtonListener(new ButtonListener() {
			public void buttonPressed(Button b) {}
			
			public void buttonReleased(Button b)
			{
				if (ConnectionManager.getInstance().isAlive(0))
				{
					/*Timer t = new Timer(50, new TimerListener() {
						public void timedOut()
						{
							if (ConnectionManager.getInstance().isAlive(1))
								ConnectionManager.getInstance().getConnection(1).sendByte((byte)0x66);
						}
					});
					t.start();*/
					//byte data = (byte)(StorageManager.getInstance().getData() + 1);
					//StorageManager.getInstance().setData(data);
					//Random r = new Random(Clock.timestamp());
					//PFMeasurement p = new PFMeasurement(r.nextInt(), r.nextInt(), (float)r.nextDouble(), (float)r.nextDouble(), (float)r.nextDouble());
					//Logger.println("Sending:"+p);
					//ConnectionManager.getInstance().sendPacketToAll(p);
					for (int i = 0; i < 100; i++)
					{
						Random r = new Random();			
						ConnectionManager.getInstance().sendPacketToAll(
								new SimpleMeasurement((float)r.nextDouble())
						);
						
						try {Thread.sleep(100);}catch(Exception ex){}
					}
					
					//Clock.syncWith(0);
				}
				else
					ConnectionManager.getInstance().openConnection(0);
			}
		});
		
		Button.ENTER.addButtonListener(new ButtonListener() {
			public void buttonPressed(Button b) {}
			
			public void buttonReleased(Button b)
			{
				if (ConnectionManager.getInstance().isAlive(1))
				{
					/*Timer t = new Timer(50, new TimerListener() {
						public void timedOut()
						{
							if (ConnectionManager.getInstance().isAlive(2))
								ConnectionManager.getInstance().getConnection(2).sendByte((byte)0x66);
						}
					});
					t.start();*/
					//byte data = (byte)(StorageManager.getInstance().getData() + 1);					
					//StorageManager.getInstance().setData(data);
					Clock.syncWith(1);
				}
				else
					ConnectionManager.getInstance().openConnection(1);
			}
		});
		
		Button.RIGHT.addButtonListener(new ButtonListener() {
			public void buttonPressed(Button b) {}
			
			public void buttonReleased(Button b)
			{
				if (ConnectionManager.getInstance().isAlive(2))
				{
					/*Timer t = new Timer(100, new TimerListener() {
						public void timedOut()
						{
							if (ConnectionManager.getInstance().isAlive(3))
								ConnectionManager.getInstance().getConnection(3).sendByte((byte)0x66);
						}
					});
					t.start();*/
					//byte data = (byte)(StorageManager.getInstance().getData() + 1);					
					//StorageManager.getInstance().setData(data);
					//Clock.syncWith(2);
					Random r = new Random(Clock.timestamp());
					PFMeasurement p = new PFMeasurement(r.nextInt(), r.nextInt(), (float)r.nextDouble(), (float)r.nextDouble(), (float)r.nextDouble());
					Logger.println("Sending:"+p);
					ConnectionManager.getInstance().sendPacketToAll(p);
				}
				else
					ConnectionManager.getInstance().openConnection(2);
			}
		});
		
		Button.ESCAPE.addButtonListener(new ButtonListener() {
			public void buttonPressed(Button b) {}
			
			public void buttonReleased(Button b)
			{
				/*if (ConnectionManager.getInstance().isAlive(3))
				{
					//ConnectionManager.getInstance().sendByteTo(4, (byte)0x66);
					//byte data = (byte)(StorageManager.getInstance().getData() + 1);					
					//StorageManager.getInstance().setData(data);
					//ConnectionManager.getInstance().sendPacketToAll(new Timestamp(Clock.timestamp()));
					Clock.syncWith(-1);					
				}*/
				for (int i = 0; i < 100; i++)
				{
					Random r = new Random();			
					ConnectionManager.getInstance().sendPacketToAll(
							new SimpleMeasurement((float)r.nextDouble())
					);
					
					try {Thread.sleep(100);}catch(Exception ex){}
				}

			}
		});
		
		int test = 0;
		while (test == 0)
		{
			/*Random r = new Random();			
			ConnectionManager.getInstance().sendPacketToAll(
					new SimpleMeasurement((float)r.nextDouble())
			);*/
			
			//LCD.drawInt((byte)StorageManager.getInstance().getData(), 2, 2);
			//int milisUntilNextSec = 1000 - (Clock.timestamp() % 1000);
			//Thread.sleep(milisUntilNextSec + 1000);
						
			Thread.sleep(100);
			//Thread.yield();
			//Sound.beep();
		}
		
		if (RConsole.isOpen())
			RConsole.close();
	}
}
