package GSim;

/** Parent class for buffers */
public class Buffer {

	public synchronized Object pop() {
		return null;
	}

	public void push(Object value) {
	}

}

/** Buffer node */
final class node {
	public Object value;
	public node next;

	public node(Object value, node next) {
		this.value = value;
		this.next = next;
	}
}