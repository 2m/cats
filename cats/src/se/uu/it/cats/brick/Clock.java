package se.uu.it.cats.brick;

import lejos.util.Stopwatch;

public class Clock
{
	private static Stopwatch _sw = null;
	
	public static void init()
	{
		_sw = new Stopwatch();
		_sw.reset();
	}
	
	public static int timestamp()
	{
		return _sw.elapsed();
	}
}
