package GSim;


public class PositioningParticle extends Particle {
	public int angle;

	public PositioningParticle(int x, int y, int angle, int w, Particle next, Particle previous) {
		super( x, y, w, next, previous);
		this.angle = angle;
	}

}