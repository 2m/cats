package GSim;

/**
 * Sorted buffer implemented as a linked list
 * 
 * @author Fredrik Wahlberg
 * @version $Rev$
 * 
 */
public class BufferSorted extends Buffer {
	// TODO: Base this on LinkedList
	private Object lockOnLast;
	private int nonodes = 0;

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
			nonodes++;
		}
		// System.out.println(nonodes
		// + " number of objects buffered in SortedBuffer");
	}

	/**
	 * Pop a BufferData object from the buffer
	 * 
	 * @return BufferData oldest BufferData or null
	 */
	public synchronized ComparableData pop() {
		// TODO: Needs to check pointer last
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
