package GSim;


public class motorControl {
	public double x;
	public double y;
	public double angle;
	public buffer updateBuffer;
	public realTimeClock clock;
	public final double MAX_SPEED = 0.05;

	// TODO: max acceleration of motor?

	public motorControl() {
		this.x = 10;
		this.y = 10;
		this.angle = 0;
	}

	public motorControl(double x, double y, double angle, buffer motorUpdate, realTimeClock clock) {
		this.x = x;
		this.y = y;
		this.angle = angle;
		updateBuffer = motorUpdate;
		this.clock = clock;
	}

	public void drive(double distance) {
		if (distance > MAX_SPEED) {
			distance = MAX_SPEED;
		}
		setX(getX() + Math.cos(angle) * distance);
		setY(getY() + Math.sin(angle) * distance);
		updateBuffer.push(new movementData(clock.getTime(), distance, 0.0));
	}

	public void turn(double turnangle) {
		setAngle(getAngle() + turnangle);
		updateBuffer.push(new movementData(clock.getTime(), 0.0, turnangle));
	}

	public void setPos(double x, double y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * Turns toward a coordinate
	 * 
	 * @param x
	 *            X position to look at
	 * @param y
	 *            Y position to look at
	 */
	public void turnTo(double x, double y) {
		turn(Math.atan2((y - getY()), (x - getX())) - getAngle());
	}

	/**
	 * Make the motor go to a sertain position but not faster than MAX_SPEED
	 * 
	 * @param x
	 *            X position to go to
	 * @param y
	 *            Y position to go to
	 */
	public void goTo(double x, double y) {
		double distance = Math.sqrt(Math.pow(getX() - x, 2)
				+ Math.pow(getY() - y, 2));
		if (distance > 0) {
			turnTo(x, y);
			drive(distance);
		}
	}

	// Get/Set methods

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public double getAngle() {
		return angle;
	}

	public void setAngle(double angle) {
		this.angle = angle;
	}
}
