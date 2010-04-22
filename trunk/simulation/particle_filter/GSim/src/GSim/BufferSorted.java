package GSim;

/**
 * Sorted buffer implemented as a linked list
 * 
 * @author Fredrik Wahlberg
 * @version $Rev$
 * 
 */
public class BufferSorted extends Buffer {
	private BufferNode first;
	private BufferNode last;
	private Object lockOnLast;
	private int nonodes = 0;

	public BufferSorted() {
		first = null;
		last = null;
		// TODO: check if semaphore needs to be used globaly
		lockOnLast = new Object();
	}

	/**
	 * Add a BufferData object to the buffer
	 * 
	 * @param BufferData
	 *            Any BufferData
	 */
	public void push(BufferData value) {
		BufferNode newNode = new BufferNode(value, null);

		synchronized (lockOnLast) {
			if (first == null) {
				first = newNode;
				last = newNode;
			} else {
				if (!first.value.earlierThan(newNode.value)) {
					newNode.next = first;
					first = newNode;
				} else {
					BufferNode lastptr = first;
					BufferNode ptr = first.next;
					while ((ptr.value.earlierThan(newNode.value))
							&& (ptr.next != null)) {
						// Will exit on node that should be pushed back or on
						// last node
						lastptr = ptr;
						ptr = ptr.next;
					}
					if (ptr.next == null) {
						// New node inserted last
						last.next = newNode;
						last = newNode;
					} else {
						newNode.next = ptr;
						lastptr.next = newNode;
					}
				}
			}
			nonodes++;
		}
		//System.out.println(nonodes
		//		+ " number of objects buffered in SortedBuffer");
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
