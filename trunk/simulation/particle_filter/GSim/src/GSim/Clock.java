package GSim;

/**
 * Placeholder for the real time clock on the NXT
 * 
 * @author Fredrik Wahlberg
 * @version $Rev$
 */
public class Clock {
	private static long startTime = 0;

	public static void init() {
		startTime = System.currentTimeMillis();
	}
	
	public static int timestamp() {
		return (int) ((int) System.currentTimeMillis() - startTime);
	}
}
