package se.uu.it.cats.brick;

import lejos.nxt.comm.RConsole;

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
	
	public synchronized static void print(int i)
	{
		if (RConsole.isOpen())
		{
			RConsole.print(String.valueOf(i));
		}
		else
		{
			System.out.print(String.valueOf(i));
		}
	}
	
	public synchronized static void println(String msg)
	{
		if (RConsole.isOpen())
		{
			RConsole.println(msg);
		}
		else
		{
			System.out.println(msg);
		}
	}
	
	public synchronized static void println(int i)
	{
		if (RConsole.isOpen())
		{
			RConsole.println(String.valueOf(i));
		}
		else
		{
			System.out.println(String.valueOf(i));
		}
	}
}
