package se.uu.it.cats.brick;

import lejos.nxt.Button;
import lejos.nxt.ButtonListener;
import lejos.nxt.LCD;
import lejos.nxt.comm.RConsole;
import lejos.util.Timer;
import lejos.util.TimerListener;
import se.uu.it.cats.brick.network.ConnectionListener;
import se.uu.it.cats.brick.network.ConnectionManager;
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
					byte data = (byte)(StorageManager.getInstance().getData() + 1);
					StorageManager.getInstance().setData(data);
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
					byte data = (byte)(StorageManager.getInstance().getData() + 1);					
					StorageManager.getInstance().setData(data);
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
					byte data = (byte)(StorageManager.getInstance().getData() + 1);					
					StorageManager.getInstance().setData(data);
				}
				else
					ConnectionManager.getInstance().openConnection(2);
			}
		});
		
		Button.ESCAPE.addButtonListener(new ButtonListener() {
			public void buttonPressed(Button b) {}
			
			public void buttonReleased(Button b)
			{
				if (ConnectionManager.getInstance().isAlive(3))
				{
					//ConnectionManager.getInstance().sendByteTo(4, (byte)0x66);
					byte data = (byte)(StorageManager.getInstance().getData() + 1);					
					StorageManager.getInstance().setData(data);
				}

			}
		});
		
		int test = 0;
		while (test == 0)
		{
			LCD.drawInt((byte)StorageManager.getInstance().getData(), 2, 2);
			Thread.sleep(100);
		}
		
		if (RConsole.isOpen())
			RConsole.close();
	}
}
