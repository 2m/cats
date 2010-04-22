package GSim;

/**
 * Parent class for buffer data
 * 
 * @author Fredrik Wahlberg
 * @version $Rev$
 */
public class BufferData {
	/** Global time stamp */
	public int timestamp;

	public BufferData(int timestamp) {
		this.timestamp = timestamp;
	}

	/**
	 * Method to compare timestamps
	 * 
	 * @param other
	 *            BufferData object to compare with
	 * @return boolean
	 */
	public boolean earlierThan(BufferData other) {
		return (timestamp > other.timestamp);
	}
}
