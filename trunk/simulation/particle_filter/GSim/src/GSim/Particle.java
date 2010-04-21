package GSim;

public class Particle {

	public int x;
	public int y;

	public int w;
	public Particle next;
	public Particle previous;

	public Particle(int x, int y, int w, Particle next, Particle previous) {
		this.x = x;
		this.y = y;
		this.w = w;
		this.next = next;
		this.previous = previous;
	}

	/** Move particle node a to the place between b and c. */
	public static void moveParticle(Particle a, Particle b, Particle c) {
		// TODO: Implement move
	}

	public static void quickSort(Particle first, Particle last, Particle Ptr1, Particle Ptr2, int N1, int N2, int Ncut) {
		if ((N1 < Ncut) && (Ncut < N2)) {
			Particle pivot = Ptr2;
			Particle ptr2 = Ptr2.previous;
			Particle ptr1 = Ptr1;
			int n2 = N2 - 1;
			int n1 = N1;
			int pivotw = pivot.w;
			while (ptr1 != ptr2) {
				if (ptr1.w < pivotw) {
					ptr1 = ptr1.next;
					n1++;
				} else if (ptr2.w > pivotw) {
					ptr2 = ptr2.previous;
					n2--;
				} else if ((ptr1.w > pivotw) && (ptr2.w < pivotw)) {
					swapParticle(first, last, ptr1, ptr2);
				}

			}
			//moveParticle(pivot, ptr1, ptr2);
			quickSort(first, last, Ptr1, ptr1, N1, n1, Ncut);
			quickSort(first, last, ptr2, Ptr2, n2, N2, Ncut);
		}
	}

	/** Swap two particles in the data structure */
   public static void swapParticle(Particle first, Particle last, Particle a, Particle b) {
		Particle ptr_a1 = a.previous;
		Particle ptr_a2 = a.next;
		Particle ptr_b1 = b.previous;
		Particle ptr_b2 = b.next;
		a.previous = ptr_b1;
		a.next = ptr_b2;
		b.previous = ptr_a1;
		b.next = ptr_a2;
		if (ptr_a1 == null) {
			// a is first element
			first = b;
		} else {
			ptr_a1.next = b;
		}
		if (ptr_a2 == null) {
			// a is last element
			last = b;
		} else {
			ptr_a2.previous = b;
		}
		if (ptr_b1 == null) {
			// b is first element
			first = a;
		} else {
			ptr_b1.next = a;
		}
		if (ptr_b2 == null) {
			// b is last element
			last = a;
		} else {
			ptr_b2.previous = a;
		}
	}

}
