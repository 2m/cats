package GSim;

/**
 * Sorted buffer implemented as a linked list
 * 
 * @author Fredrik Wahlberg
 * @version $Rev$
 * 
 */

// FIXME: add getLatestAndFlushBuffer()
public class BufferSorted extends Buffer {
	private Object lockOnLast;

	public BufferSorted() {
		// TODO: check if semaphore needs to be used globaly
		lockOnLast = new Object();
	}

	/**
	 * Add a BufferData object to the buffer
	 * 
	 * @param BufferData
	 *            Any BufferData
	 */
	public void push(ComparableData value) {
		synchronized (lockOnLast) {
			list.insertSorted(value);
		}
	}

	/**
	 * Pop a BufferData object from the buffer
	 * 
	 * @return BufferData oldest BufferData or null
	 */
	public synchronized ComparableData pop() {
		ComparableData ret = list.pop();
		return ret;
	}
	
	/**
	 * Get the oldest BufferData object from the buffer without removing it
	 * 
	 * @return BufferData oldest BufferData or null
	 */
	public synchronized ComparableData top() {
		ComparableData ret = list.top();
		return ret;
	}

	public String toString() {
		return list.toString();
	}
}
