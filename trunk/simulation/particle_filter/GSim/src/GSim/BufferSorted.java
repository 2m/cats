package GSim;

/**
 * Sorted buffer implemented as a linked list
 * 
 * @author Fredrik Wahlberg
 * @version $Rev$
 * 
 */
public class BufferSorted extends Buffer {
	private node first;
	private node last;
	private Object lockOnLast;
	private int nonodes = 0;

	public BufferSorted() {
		first = null;
		last = null;
		// TODO: check if semaphore needs to be used globaly
		lockOnLast = new Object();
	}

	/**
	 * Add an object to the buffer
	 * 
	 * @param Object
	 *            Any Object
	 */
	public void push(Object value) {
		node newNode = new node(value, null);

		synchronized (lockOnLast) {
			if (last != null) {
				// TODO: Sorted insert
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
	 * Pop an Object from the buffer
	 * 
	 * @return Object oldest Object or null
	 */
	public synchronized Object pop() {
		node next = first.next;
		Object ret = null;

		if (first != null) {
			ret = first.value;
			first = next;
		}
		nonodes--;
		return ret;
	}

	public String toString() {
		node ptr = first;
		String ret = "{";
		while (ptr != null) {
			ret += " " + ptr.value;
			ptr = ptr.next;
		}
		ret += " }";
		return ret;
	}
}
