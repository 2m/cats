package GSim;
/** Placeholder for the real time clock on the NXT
 * 
 * @author Fredrik Wahlberg
 * @version $Rev$
 */
public class realTimeClock {
	private long startTime;

	public realTimeClock() {
		startTime = System.currentTimeMillis();
	}

	public int getTime() {
		return (int) ((int) System.currentTimeMillis() - startTime);
	}

	public String toString() {
		return "" + getTime();
	}
}
