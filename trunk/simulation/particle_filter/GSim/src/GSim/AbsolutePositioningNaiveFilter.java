package GSim;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

/** Naive filter for absolute positioning of one cat using landmarks. */
public class AbsolutePositioningNaiveFilter extends AbsolutePositioningFilter {

	/** Mean in the x, y and angle directions */
	private float mean_x;
	private float mean_y;
	private float mean_angle;

	/** Varible for time */
	private int lastCurrentTime, currentTime;

	/** Counter and timer too keep track of mean iteration execution time */
	private int iterationCounter = 0;
	private int iterationTime = 0;

	/**
	 * Constructor of the naive absolute positioning filter.
	 * 
	 * @param T
	 *            Period time
	 * @param sensorData
	 *            Buffer with sensor readings
	 * @param movementData
	 *            Buffer with movement data
	 * @param rttime
	 *            RealTimeClock
	 */
	public AbsolutePositioningNaiveFilter(float T, Buffer sensorData,
			Buffer movementData) {
		// Call constructor of super class
		super(T, sensorData, movementData);
	}

	/**
	 * Set initial data means and then re-sample. The input data is considered
	 * relatively certain so the co-variance matrix will be set as small.
	 * 
	 * @param x
	 *            Initial x position
	 * @param y
	 *            Initial y position
	 * @param angle
	 *            Initial angle
	 */
	public void initData(float x, float y, float angle) {
		// Set means for x, y and angle
		mean_x = x;
		mean_y = y;
		mean_angle = angle;
	}

	/**
	 * Returns time of the last update of the filter (this includes minor
	 * updates).
	 * 
	 * @return time in milliseconds
	 */
	public int getTime() {
		return lastCurrentTime;
	}

	/**
	 * Returns estimated x position.
	 * 
	 * @return x in meters as a float
	 */
	public float getX() {
		return mean_x;
	}

	/**
	 * Returns estimated y position.
	 * 
	 * @return y in meters as a float
	 */
	public float getY() {
		return mean_y;
	}

	/**
	 * Returns estimated angular position.
	 * 
	 * @return angle in radians as a float
	 */
	public float getAngle() {
		return mean_angle;
	}

	/**
	 * Returns the mean iteration execution time in seconds.
	 * 
	 * @return time in seconds
	 */
	public float getExecutionTime() {
		return ((((float) iterationTime) / 1000) / ((float) iterationCounter));
	}

	/*
	 * Draw particles (NOT brick material)
	 */
	public void draw(Graphics g) {
		// TODO: Remove graphics code from filter
		final int size = 4; // Diameter
		final int linelength = 8;

		Graphics2D g2 = (Graphics2D) g;

		// Save the current tranform
		AffineTransform oldTransform = g2.getTransform();

		// Rotate and translate the actor
		// g2.rotate(iangle, ix, iy);

		// Plot mean
		g2.setColor(Color.LIGHT_GRAY);
		int ix = Actor.e2gX(getX());
		int iy = Actor.e2gY(getY());
		double iangle = -getAngle();
		g2.fillOval((int) ix - (size / 2), (int) iy - (size / 2), (int) size,
				(int) size);
		g2.drawLine((int) ix, (int) iy, (int) (ix + Math.cos(iangle)
				* linelength), (int) (iy + Math.sin(iangle) * linelength));

		// Reset the transformation matrix
		g2.setTransform(oldTransform);
	}

	public void update() {
		// Get time reference
		currentTime = Clock.getTime();

		SightingData sdata = (SightingData) sensorData.pop();
		while (sdata != null) {
			if (sdata.getComparable() > currentTime) {
				sensorData.push(sdata);
				sdata = null;
			}
		}
		MovementData mdata = (MovementData) movementData.pop();
		while (mdata != null) {
			if (mdata.getComparable() <= currentTime) {
				// Update mean
				mean_x += Math.cos(mean_angle) * mdata.dr;
				mean_y += Math.sin(mean_angle) * mdata.dr;
				mean_angle += mdata.dangle;
				mdata = (MovementData) movementData.pop();
			} else {
				movementData.push(mdata);
				mdata = null;
			}
		}

		// Increase iteration counter and timer (with full execution time)
		iterationCounter++;
		iterationTime += Clock.getTime() - currentTime;
		// Update public time
		lastCurrentTime = currentTime;
	}

	public void run() {
		while (true) {
			// update();
			pause((long) (Clock.getTime() % Tint));
		}

	}

	/**
	 * Returns the particles as a String for printing.
	 * 
	 * @return String with particle data
	 */
	public String toString() {
		return "(" + mean_x + ", " + mean_y + ", " + mean_angle + ")";
	}
}
