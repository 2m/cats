package se.uu.it.cats.pc;

public class Logger
{
	public static void init()
	{
		// blocks
		//RConsole.openUSB(1 * 1000);
	}
	
	public synchronized static void print(String msg)
	{
		System.out.print(msg);
	}
	
	public synchronized static void println(String msg)
	{
		System.out.println(msg);
	}
	
	public synchronized static void print(int i)
	{
		print(String.valueOf(i));
	}
	
	public synchronized static void println(int i)
	{
		println(String.valueOf(i));
	}
	
	public synchronized static void print(Object obj)
	{
		System.out.print(obj.toString());
	}
	
	public synchronized static void println(Object obj)
	{
		System.out.println(obj.toString());
	}
}
