package se.uu.it.cats.brick;

import lejos.nxt.comm.RConsole;

/*
 * Logging really hurts performace. 
 */
public class Logger
{
	public static void init()
	{
		// blocks
		RConsole.openUSB(1 * 1000);
	}
	
	public synchronized static void print(String msg)
	{
		if (RConsole.isOpen())
		{
			RConsole.print(msg);
		}
		else
		{
			System.out.print(msg);
		}
	}
	
	public static void println(String msg)
	{
		print(msg+"\n");
	}
	
	public static void print(int i)
	{
		print(String.valueOf(i));
	}
	
	public static void println(int i)
	{
		print(String.valueOf(i)+"\n");
	}
}
