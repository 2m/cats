package GSim;

/**
 * FIFO buffer implemented as a linked list
 * 
 * @author Fredrik Wahlberg
 * @version $Rev$
 * 
 */
public class BufferFIFO extends Buffer {
	// TODO: Base this on LinkedList
	private BufferNode first;
	private BufferNode last;
	private Object lockOnLast;
	private int nonodes = 0;

	public BufferFIFO() {
		first = null;
		last = null;
		lockOnLast = new Object();
	}

	/**
	 * Add a BufferData object to the buffer.
	 * 
	 * @param BufferData
	 *            Any BufferData
	 */
	public void push(BufferData value) {
		BufferNode newNode = new BufferNode(value, null);

		synchronized (lockOnLast) {
			if (last != null) {
				last.next = newNode;
				last = newNode;
			} else {
				first = newNode;
				last = newNode;
			}
			nonodes++;
		}
		// System.out.println(nonodes + " number of objects buffered in FIFO");
	}

	/**
	 * Pop a BufferData object from the buffer
	 * 
	 * @return BufferData oldest BufferData or null
	 */
	public synchronized BufferData pop() {
		BufferData ret = null;
		if (first != null) {
			BufferNode next = first.next;
			ret = first.value;
			first = next;
			nonodes--;
		}
		return ret;
	}

	public String toString() {
		BufferNode ptr = first;
		String ret = "{";
		while (ptr != null) {
			ret += " " + ptr.value;
			ptr = ptr.next;
		}
		ret += " }";
		return ret;
	}
}
