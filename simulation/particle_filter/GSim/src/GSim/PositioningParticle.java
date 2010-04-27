package GSim;

public class PositioningParticle extends Particle {
	public int angle;

	public PositioningParticle(int x, int y, int angle, int w, Particle next,
			Particle previous) {
		super(x, y, w, next, previous);
		this.angle = angle;
	}

	public String toString() {
		// TODO: Printing of angle does not work
		return "("
				+ Fixed.fixedToFloat(x)
				+ ", "
				+ Fixed.fixedToFloat(y)
				+ ", "
				+ Fixed
						.fixedToFloat(angle)*(2*Math.PI/Fixed.DEGREES) + ", "
				+ Fixed.fixedToFloat(w) + ")";
	}
}