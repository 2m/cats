package GSim;

/**
 * FIFO buffer implemented as a linked list
 * 
 * @author Fredrik Wahlberg
 * @version $Rev$
 * 
 */
public class bufferFIFO extends buffer {
	private node first;
	private node last;
	private Object lockOnLast;

	public bufferFIFO() {
		first = null;
		last = null;
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
				last.next = newNode;
				last = newNode;
			} else {
				first = newNode;
				last = newNode;
			}
		}

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

final class node {
	public Object value;
	public node next;

	public node(Object value, node next) {
		this.value = value;
		this.next = next;
	}
}
