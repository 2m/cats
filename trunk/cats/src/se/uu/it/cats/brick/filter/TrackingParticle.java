package se.uu.it.cats.brick.filter;

public class TrackingParticle extends ComparableData {
	public int x;
	public int y;
	public int xv;
	public int yv;

	public TrackingParticle(int x, int y, int xv, int yv, int w) {
		super(w);
		this.x = x;
		this.y = y;
		this.xv = xv;
		this.yv = yv;
	}

	public boolean isTrackingParticle() {
		return true;
	}
}