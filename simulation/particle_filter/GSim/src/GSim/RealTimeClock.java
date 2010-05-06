package GSim;

/**
 * Placeholder for the real time clock on the NXT
 * 
 * @author Fredrik Wahlberg
 * @version $Rev$
 */
public class RealTimeClock {
	private long startTime = 0;
	private long syncOffset = 0;

	public RealTimeClock() {
		startTime = System.currentTimeMillis();
	}

	public void setOffset(int offset) {
		syncOffset += offset;
	}

	public int getTime() {
		return (int) ((int) System.currentTimeMillis() - startTime);
	}

	public String toString() {
		return "" + getTime();
	}
}
