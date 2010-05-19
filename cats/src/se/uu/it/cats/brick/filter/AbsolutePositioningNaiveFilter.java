package se.uu.it.cats.brick.filter;

import se.uu.it.cats.brick.Clock;
import se.uu.it.cats.brick.storage.BillBoard;

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
	public AbsolutePositioningNaiveFilter(int id, float T, Buffer sensorData,
			Buffer movementData, BillBoard billboard) {
		// Call constructor of super class
		super(id, T, sensorData, movementData, billboard);
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

	public void update() {
		// Get time reference
		currentTime = Clock.timestamp();

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
			billboard.setAbsolutePosition(id, getX(), getY(), getAngle());
		}

		// Increase iteration counter and timer (with full execution time)
		iterationCounter++;
		iterationTime += Clock.timestamp() - currentTime;
		// Update public time
		lastCurrentTime = currentTime;
	}

	public void run() {
		while (true) {
			// update();
			pause((long) (Clock.timestamp() % Tint));
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
