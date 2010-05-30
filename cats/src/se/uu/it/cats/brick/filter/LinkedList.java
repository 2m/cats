package se.uu.it.cats.brick.filter;

/**
 * Linked list for particles and buffers. List is sorted with highest weight first.
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

	public void insertFirst(ComparableData data) {
		Link newLink = new Link(data);
		if (isEmpty()) {
			first = newLink;
			last = newLink;
		} else {
			newLink.next = first;
			first = newLink;
		}
	}

	public void insertLast(ComparableData dd) {
		Link newLink = new Link(dd);
		if (isEmpty()) {
			first = newLink;
			last = newLink;
		} else {
			last.next = newLink;
			last = newLink;
		}
	}

	/**
	 * Get first element (the one with lowest weight)
	 * 
	 * @return ComparableData
	 */
	public ComparableData pop() {
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
			return ret.data;
		}
	}
	
	public ComparableData top() {
		if (isEmpty()) {
			// List is empty
			return null;
		} else {
			Link ret = first;
			return ret.data;
		}
	}

	public int getLength() {
		if (isEmpty()) {
			// List is empty
			return 0;
		} else {
			int len = 0;
			Link current = first;
			while (current != null) {
				len++;
				current = current.next;
			}
			return len;
		}
	}

	/**
	 * Sorted insert. Elements are sorted according to weights with smallest
	 * (earliest) first.
	 * 
	 * @param data ComparableData
	 */
	public void insertSorted(ComparableData data) {
		//FIXME Check the iequalities, all inequalities changed 2010-05-30 by Edvard
		if (isEmpty()) {
			Link newLink = new Link(data);
			first = newLink;
			last = newLink;
		} else {
			if (first.data.comparable >= data.comparable) {
				insertFirst(data);
			} else if (last.data.comparable < data.comparable) {
				insertLast(data);
			} else {
				// Link should be inserted inside list
				Link current = first.next;
				Link trail = first;
				while (current != null) {
					// Check if current weight is smaller than the particle
					// weight.
					if (data.comparable <= current.data.comparable) {
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
	public ComparableData data; // data item
	public Link next; // next link in list

	public Link(ComparableData d) {
		data = d;
	}

	public String toString() {
		return data.toString();
	}
}
