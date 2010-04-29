package GSim;

public class PositioningParticle extends Particle {
	public int angle;

	public PositioningParticle(int x, int y, int angle, int w) {
		super(x, y, w);
		this.angle = angle;
	}

	public String toString() {
		// TODO: Printing of positioning particles
		return "(" + Fixed.fixedToFloat(x) + ", " + Fixed.fixedToFloat(y)
				+ ", " + Fixed.fixedToFloat(angle)
				* (2 * Math.PI / Fixed.DEGREES) + ", " + w + ")";
	}
}