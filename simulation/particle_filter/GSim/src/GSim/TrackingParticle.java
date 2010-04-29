package GSim;

public class TrackingParticle extends Particle {
	public int xv;
	public int yv;

	public TrackingParticle(int x, int y, int xv, int yv, int w) {
		super(x, y, w);
		this.xv = xv;
		this.yv = yv;
	}

}