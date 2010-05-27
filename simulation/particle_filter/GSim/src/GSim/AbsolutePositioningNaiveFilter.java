package GSim;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

/*import se.uu.it.cats.brick.Clock;
 import se.uu.it.cats.brick.filter.ComparableData;
 import se.uu.it.cats.brick.filter.LandmarkList;
 import se.uu.it.cats.brick.filter.MovementData;
 import se.uu.it.cats.brick.filter.SightingData;*/

/** Naive filter for absolute positioning of one cat using landmarks. */
public class AbsolutePositioningNaiveFilter extends AbsolutePositioningFilter {

	/** Mean in the x, y and angle directions */
	private float mean_x;
	private float mean_y;
	private float mean_angle;

	/** Variable for time */
	private int lastCurrentTime, currentTime;

	/** Counter and timer too keep track of mean iteration execution time */
	private int iterationCounter = 0;
	private int iterationTime = 0;

	/**
	 * Constructor of the naive absolute positioning filter.
	 * 
	 * @param T
	 *            Period time
	 * @param dataBuffer
	 *            Buffer with movement data
	 * @param rttime
	 *            RealTimeClock
	 */
	public AbsolutePositioningNaiveFilter(int id, float T,
			Buffer unifiedBuffer, BillBoard billboard) {
		// Call constructor of super class
		super(id, T, unifiedBuffer, billboard);
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
		lastCurrentTime = Clock.timestamp();
		billboard
				.setAbsolutePosition(id, getX(), getY(), getAngle(), getTime());
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
		final int size = 10; // Diameter
		final int linelength = 20;

		Graphics2D g2 = (Graphics2D) g;

		// Save the current transform
		AffineTransform oldTransform = g2.getTransform();

		// Plot mean
		g2.setColor(Color.PINK);

		int ix, iy;
		double iangle;
		/*
		 * float[] position = billboard.getAbsolutePositions(); * ix =
		 * Actor.e2gX(position[id * 4 + 0]); iy = Actor.e2gY(position[id * 4 +
		 * 1]); iangle = -position[id * 4 + 2];
		 */
		ix = Actor.e2gX(getX());
		iy = Actor.e2gY(getY());
		iangle = -getAngle();

		g2.fillOval((int) ix - (size / 2), (int) iy - (size / 2), (int) size,
				(int) size);
		g2.drawLine((int) ix, (int) iy, (int) (ix + Math.cos(iangle)
				* linelength), (int) (iy + Math.sin(iangle) * linelength));

		// Reset the transformation matrix
		g2.setTransform(oldTransform);
	}

	public void update() {

		float latestSighting[] = new float[4];
		boolean needToSendSighting = false;

		// Get time reference
		currentTime = Clock.timestamp();

		ComparableData data = unifiedBuffer.pop();
		while (data != null) {
			// Use data if it is older than currentTime
			if (data.getComparable() <= currentTime) {
				if (data.isMovementData()) {
					// System.out.println("isMovementData");
					// Update mean
					MovementData mdata = (MovementData) data;
					mean_x += Math.cos(mean_angle) * mdata.dr;
					mean_y += Math.sin(mean_angle) * mdata.dr;
					mean_angle += mdata.dangle;
					lastCurrentTime = mdata.comparable;
					/*
					 * billboard.setAbsolutePosition(id, getX(), getY(),
					 * getAngle(), getTime());
					 */
				} else if (data.isSightingData()) {
					// System.out.println("isSightingData");
					SightingData sdata = (SightingData) data;
					if (sdata.type == LandmarkList.MOUSE) {
						// System.out.println("isMouse");
						latestSighting[0] = getX();
						latestSighting[1] = getY();
						latestSighting[2] = sdata.angle + getAngle();
						latestSighting[3] = sdata.comparable;
						needToSendSighting = true;
					}
				}
				data = unifiedBuffer.pop();
			} else {
				unifiedBuffer.push(data);
				data = null;
			}
		}

		billboard.setAbsolutePosition(id, getX(), getY(), getAngle(), Clock
				.timestamp());

		if (needToSendSighting)
			billboard.setLatestSighting(id, latestSighting[0],
					latestSighting[1], latestSighting[2],
					(int) latestSighting[3]);

		// Increase iteration counter and timer (with full execution time)
		iterationCounter++;
		iterationTime += Clock.timestamp() - currentTime;
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
