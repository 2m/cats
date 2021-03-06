package GSim;

import java.util.Random;

public class MotorControl {
	public double x;
	public double y;
	public double angle;
	public Buffer updateBuffer;
	public final double MAX_SPEED = 0.2 * ((double) GSim.timestep / 1000d); // [m/s]*conversion_factor
	private Random rng = new Random();

	public MotorControl() {
		this.x = 10;
		this.y = 10;
		this.angle = 0;
	}

	public MotorControl(double x, double y, double angle, Buffer motorUpdate) {
		this.x = x;
		this.y = y;
		this.angle = angle;
		updateBuffer = motorUpdate;
	}

	public void drive(double distance) {
		if (distance > MAX_SPEED) {
			distance = MAX_SPEED;
		}
		float noiseAmp = 0.5f;// 0.04f;
		float staticNoise = 0.02f;
		setX(getX() + Math.cos(angle) * distance
				* (1 + (rng.nextDouble() - 0.5) * noiseAmp + staticNoise));
		setY(getY() + Math.sin(angle) * distance
				* (1 + (rng.nextDouble() - 0.5) * noiseAmp + staticNoise));
		updateBuffer.push(new MovementData(Clock.timestamp(), (float) distance,
				(float) 0.0));
	}

	public void turn(double turnangle) {
		setAngle(getAngle() + turnangle + (rng.nextDouble() - 0.5) * 3
				* (Math.PI / 180));
		updateBuffer.push(new MovementData(Clock.timestamp(), (float) 0.0,
				(float) turnangle));
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
	 * Make the motor go to a certain position but not faster than MAX_SPEED
	 * 
	 * @param x
	 *            X position to go to
	 * @param y
	 *            Y position to go to
	 */
	public void goTo(double x, double y) {
		double distance = Math.sqrt(Math.pow(getX() - x, 2)
				+ Math.pow(getY() - y, 2));
		if (distance > 0.05) {
			turnTo(x, y);
			// set travel mode
			// turnMode = false;
			drive(distance);
			// set back to turn mode
			// turnMode = true;
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
