package GSim;

public class PositioningParticle extends ComparableData {
	public int angle;
	public int x;
	public int y;

	/**
	 * 
	 * @param x
	 *            x value in meters
	 * @param y
	 *            y value in meters
	 * @param angle
	 *            value in radians
	 * @param w
	 *            weight
	 */
	public PositioningParticle(int x, int y, int angle, int w) {
		super(w);
		this.x = x;
		this.y = y;
		this.angle = angle;
	}

	public boolean isPositioningParticle() {
		return true;
	}

	public String toString() {
		return "(" + Fixed.fixedToFloat(x) + ", " + Fixed.fixedToFloat(y)
				+ ", " + Fixed.fixedToFloat(angle) * (180 / Math.PI) + ", "
				+ comparable + ")";
	}
}