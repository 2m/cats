package GSim;

/**
 * Parent class for buffers
 * 
 * @author Fredrik Wahlberg
 * @version $Rev$
 */
public class Buffer {

	public synchronized BufferData pop() {
		return null;
	}

	public void push(BufferData value) {
	}

}

/** Buffer node */
final class BufferNode {
	public BufferData value;
	public BufferNode next;

	public BufferNode(BufferData value, BufferNode next) {
		this.value = value;
		this.next = next;
	}
}