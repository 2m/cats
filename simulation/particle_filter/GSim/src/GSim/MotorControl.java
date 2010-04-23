package GSim;

import java.util.Random;

public class MotorControl {
	public double x;
	public double y;
	public double angle;
	public Buffer updateBuffer;
	public RealTimeClock clock;
	public final double MAX_SPEED = 0.01;
	private Random rng = new Random();

	public MotorControl() {
		this.x = 10;
		this.y = 10;
		this.angle = 0;
	}

	public MotorControl(double x, double y, double angle, Buffer motorUpdate,
			RealTimeClock clock) {
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
		// TODO: Set more realistic noise (needs measurements)
		setX(getX() + Math.cos(angle) * distance
				* (1 + rng.nextDouble() / 50 + 0.01)); // +-1% noise
		setY(getY() + Math.sin(angle) * distance
				* (1 + rng.nextDouble() / 50 + 0.01));
		updateBuffer.push(new MovementData(clock.getTime(), distance, 0.0));
	}

	public void turn(double turnangle) {
		// TODO: Set more realistic noise
		setAngle(getAngle() + turnangle * (1 + rng.nextDouble() / 50 + 0.01)); // +-1%
																				// noise
		updateBuffer.push(new MovementData(clock.getTime(), 0.0, turnangle));
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

	private void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	private void setY(double y) {
		this.y = y;
	}

	public double getAngle() {
		return angle;
	}

	private void setAngle(double angle) {
		this.angle = angle;
	}
}