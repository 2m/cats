package GSim;

/**
 * Parent class for buffers
 * 
 * @author Fredrik Wahlberg
 * @version $Rev$
 */
public class Buffer {
	protected LinkedList list;

	public Buffer() {
		list = new LinkedList();
	}

	public synchronized ComparableData pop() {
		return null;
	}
	
	public synchronized ComparableData top() {
		return null;
	}

	public void push(ComparableData value) {
	}

	public int getLength() {
		return list.getLength();
	}
}