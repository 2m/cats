package GSim;

public class TrackingParticle extends Particle {
	public int xv;
	public int yv;

	public TrackingParticle(int x, int y, int xv, int yv, int w, Particle next,
			Particle previous) {
		super(x, y, w, next, previous);
		this.xv = xv;
		this.yv = yv;
	}

}