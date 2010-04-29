package GSim;

/**
 * Linked list for particles. List is sorded with highest weight first.
 * 
 * @author Fredrik Wahlberg
 * 
 */
public class LinkedList {
	/** Pointer to first element. */
	public Link first;
	/** Pointer to last element. Needed for insertion as last element */
	public Link last;

	public LinkedList() {
		first = null;
		last = null;
	}

	public boolean isEmpty() {
		return (first == null);
	}

	public void insertFirst(Particle data) {
		Link newLink = new Link(data);
		if (isEmpty()) {
			first = newLink;
			last = newLink;
		} else {
			newLink.next = first;
			first = newLink;
		}
	}

	public void insertLast(Particle dd) {
		Link newLink = new Link(dd);
		if (isEmpty()) {
			first = newLink;
			last = newLink;
		} else {
			last.next = newLink;
			last = newLink;
		}
	}

	public Link popFirst() {
		if (isEmpty()) {
			// List is empty
			return null;
		} else {
			Link ret = first;
			if (first == last) {
				// Only one element in list
				first = null;
				last = null;
			} else {
				// Many elements in list
				first = first.next;
			}
			return ret;
		}
	}

	public void insertSorted(Particle data) {
		if (isEmpty()) {
			Link newLink = new Link(data);
			first = newLink;
			last = newLink;
		} else {
			if (first.data.w < data.w) {
				insertFirst(data);
			} else if (last.data.w > data.w) {
				insertLast(data);
			} else {
				// Link should be inserted inside list
				Link current = first.next;
				Link trail = first;
				while (current != null) {
					// Check if current weight is smaller than the particle
					// weight.
					if (current.data.w < data.w) {
						break;
					}
					// Step forward in list
					trail = current;
					current = current.next;
				}
				Link newLink = new Link(data);
				newLink.next = current;
				trail.next = newLink;
			}
		}

	}

	public String toString() {
		String ret = "[";
		Link current = first;
		// start at beginning
		while (current != null) {
			ret += current.toString();
			current = current.next;
			if (current != null) {
				ret += "\n ";
			}
		}
		ret += "]";
		return ret;
	}

}

class Link {
	public Particle data; // data item
	public Link next; // next link in list

	public Link(Particle d) {
		data = d;
	}

	public String toString() {
		return data.toString();
	}
}
