package GSim;

public class DoublyLinkedList {
	private Link first;
	private Link last;

	public DoublyLinkedList() {
		first = null;
		last = null;
	}

	public boolean isEmpty() {
		return first == null;
	}

	public void insertFirst(Particle dd) {
		Link newLink = new Link(dd);

		if (isEmpty())
			last = newLink;
		else
			first.previous = newLink;
		newLink.next = first;
		first = newLink;
	}

	public void insertLast(Particle dd) {
		Link newLink = new Link(dd);
		if (isEmpty())
			first = newLink;
		else {
			last.next = newLink;
			newLink.previous = last;
		}
		last = newLink;
	}

	/*
	 * public Link deleteFirst() { Link temp = first; if (first.next == null)
	 * last = null; else first.next.previous = null; first = first.next; return
	 * temp; }
	 * 
	 * public Link deleteLast() { Link temp = last; if (first.next == null)
	 * first = null; else last.previous.next = null; last = last.previous;
	 * return temp; }
	 * 
	 * public boolean insertAfter(long key, long dd) { Link current = first;
	 * while (current.dData != key) { current = current.next; if (current ==
	 * null) return false; // cannot find it } Link newLink = new Link(dd); //
	 * make new link
	 * 
	 * if (current == last) // if last link, { newLink.next = null; last =
	 * newLink; } else // not last link, { newLink.next = current.next;
	 * 
	 * current.next.previous = newLink; } newLink.previous = current;
	 * current.next = newLink; return true; // found it, insert }
	 * 
	 * public Link deleteKey(long key) { Link current = first; while
	 * (current.dData != key) { current = current.next; if (current == null)
	 * return null; // cannot find it } if (current == first) // found it; first
	 * item? first = current.next; else current.previous.next = current.next;
	 * 
	 * if (current == last) // last item? last = current.previous; else // not
	 * last current.next.previous = current.previous; return current; // return
	 * value }
	 * 
	 * public String toString() { System.out.print("List (first to last): ");
	 * Link current = first; // start at beginning while (current != null) //
	 * until end of list, { current.toString(); current = current.next; // move
	 * to next link } System.out.println(""); }
	 */

}

class Link {
	public Particle data; // data item
	public Link next; // next link in list
	public Link previous; // previous link in list

	public Link(Particle d) {
		data = d;
	}

	public String toString() {
		return data.toString();
	}
}
