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
}
