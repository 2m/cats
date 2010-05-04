package GSim;

/**
 * FIFO buffer implemented as a linked list
 * 
 * @author Fredrik Wahlberg
 * @version $Rev$
 * 
 */
public class BufferFIFO extends Buffer {
	private Object lockOnLast;
	private int nonodes = 0;

	public BufferFIFO() {
		super();
		lockOnLast = new Object();
	}

	/**
	 * Add a BufferData object to the buffer.
	 * 
	 * @param BufferData
	 *            Any BufferData
	 */
	public void push(ComparableData value) {

		synchronized (lockOnLast) {
			if (value != null) {
				list.insertLast(value);
				nonodes++;
			}
		}
	}

	/**
	 * Pop a BufferData object from the buffer
	 * 
	 * @return BufferData oldest BufferData or null
	 */
	public synchronized ComparableData pop() {
		ComparableData ret = list.popFirst();
		if (ret != null) {
			nonodes--;
		}
		return ret;
	}

	public String toString() {
		return list.toString();
	}
}
