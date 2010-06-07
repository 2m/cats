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

/** Geometric filter for absolute positioning of one cat using landmarks. */
public class AbsolutePositioningGeometricFilter extends
		AbsolutePositioningFilter {

	/** Mean in the x, y and angle directions */
	private float mean_x;
	private float mean_y;
	private float mean_angle;

	/** Variable for time */
	private int lastCurrentTime, currentTime;

	/** Counter and timer too keep track of mean iteration execution time */
	private int iterationCounter = 0;
	private int iterationTime = 0;

	private float[] landmarkAngles;
	private int[] landmarkTypes;
	private int[] landmarkNoSightings;
	private boolean positioning = false;

	/**
	 * Constructor of the geometric absolute positioning filter.
	 * 
	 * @param T
	 *            Period time
	 * @param dataBuffer
	 *            Buffer with movement data
	 * @param rttime
	 *            RealTimeClock
	 */
	public AbsolutePositioningGeometricFilter(int id, float T,
			Buffer unifiedBuffer, BillBoard billboard) {
		// Call constructor of super class
		super(id, T, unifiedBuffer, billboard);
		landmarkAngles = new float[Settings.NO_LANDMARKS];
		landmarkTypes = new int[Settings.NO_LANDMARKS];
		landmarkNoSightings = new int[Settings.NO_LANDMARKS];
		resetSightings();
	}

	private void resetSightings() {
		positioning = false;
	}

	private void addLandmark(float angle, int type) {
		final float angleEpsilon = (float) (10 * (Math.PI / 180));
		if (!positioning) {
			for (int i = 0; i < Settings.NO_LANDMARKS; i++) {
				landmarkNoSightings[i] = 0;
			}
			positioning = true;
		}
		// Check if landmark already is in the list
		boolean inserted = false;
		for (int i = 0; (i < Settings.NO_LANDMARKS) && (!inserted); i++) {
			if (landmarkNoSightings[i] > 0) {
				if ((Math.abs(landmarkAngles[i] - angle) < angleEpsilon)
						&& (landmarkTypes[i] == type)) {
					landmarkNoSightings[i]++;
					landmarkAngles[i] = ((landmarkNoSightings[i] - 1)
							* landmarkAngles[i] + angle)
							/ landmarkNoSightings[i];
					inserted = true;
				}
			}
		}
		for (int i = 0; (i < Settings.NO_LANDMARKS) && (!inserted); i++) {
			if (landmarkNoSightings[i] == 0) {
				landmarkNoSightings[i]++;
				landmarkAngles[i] = angle;
				landmarkTypes[i] = type;
				inserted = true;
			}
		}
	}

	private void sortLandmarks() {
		// Bubble sort
		for (int j = 0; j < (Settings.NO_LANDMARKS - 1); j++) {
			for (int i = 0; i < (Settings.NO_LANDMARKS - 1); i++) {
				if (landmarkAngles[i] < landmarkAngles[i + 1]) {
					float a = landmarkAngles[i];
					landmarkAngles[i] = landmarkAngles[i + 1];
					landmarkAngles[i + 1] = a;
					int t = landmarkTypes[i];
					landmarkTypes[i] = landmarkTypes[i + 1];
					landmarkTypes[i + 1] = t;
					int s = landmarkNoSightings[i];
					landmarkNoSightings[i] = landmarkNoSightings[i + 1];
					landmarkNoSightings[i + 1] = s;
				}
			}
		}
	}

	private void shiftLandmarks() {
		// Simple shift
		float a = landmarkAngles[0];
		int t = landmarkTypes[0];
		int s = landmarkNoSightings[0];
		for (int i = 0; i < (Settings.NO_LANDMARKS - 1); i++) {
			landmarkAngles[i] = landmarkAngles[i + 1];
			landmarkTypes[i] = landmarkTypes[i + 1];
			landmarkNoSightings[i] = landmarkNoSightings[i + 1];
		}
		landmarkAngles[Settings.NO_LANDMARKS - 1] = a;
		landmarkTypes[Settings.NO_LANDMARKS - 1] = t;
		landmarkNoSightings[Settings.NO_LANDMARKS - 1] = s;
	}

	private void doGeometricMagic() {
		// TODO: Implement
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

		// Plot mean
		g2.setColor(Color.PINK);

		int ix, iy;
		double iangle;

		ix = Actor.e2gX(getX());
		iy = Actor.e2gY(getY());
		iangle = -getAngle();

		g2.fillOval((int) ix - (size / 2), (int) iy - (size / 2), (int) size,
				(int) size);
		g2.drawLine((int) ix, (int) iy, (int) (ix + Math.cos(iangle)
				* linelength), (int) (iy + Math.sin(iangle) * linelength));

		g2.setColor(Color.GREEN);
		for (int i = 0; i < Settings.NO_LANDMARKS; i++) {
			if (landmarkNoSightings[i] > 0) {
				iangle = -(getAngle() + landmarkAngles[i]);
				g2.drawLine((int) ix, (int) iy, (int) (ix + Math.cos(iangle)
						* linelength * 2), (int) (iy + Math.sin(iangle)
						* linelength * 2));
			}
		}
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
					// Update mean
					MovementData mdata = (MovementData) data;
					if ((Math.abs(mdata.dr) > 0.0005)
							|| (Math.abs(mdata.dangle) > 0.001)) {
						doGeometricMagic();
						mean_x += Math.cos(mean_angle) * mdata.dr;
						mean_y += Math.sin(mean_angle) * mdata.dr;
						mean_angle += mdata.dangle;
						resetSightings();
					}
					lastCurrentTime = mdata.comparable;
				} else if (data.isSightingData()) {
					SightingData sdata = (SightingData) data;
					if (sdata.type == Settings.TYPE_MOUSE) {
						latestSighting[0] = getX();
						latestSighting[1] = getY();
						latestSighting[2] = sdata.angle + getAngle();
						latestSighting[3] = sdata.comparable;
						needToSendSighting = true;
					} else {
						addLandmark(sdata.angle, sdata.type);
					}
				}
				data = unifiedBuffer.pop();
			} else {
				unifiedBuffer.push(data);
				data = null;
			}
		}

		doGeometricMagic();
		billboard.setAbsolutePosition(id, getX(), getY(), getAngle(), Clock
				.timestamp());

		if (needToSendSighting) {
			billboard.setLatestSighting(id, latestSighting[0],
					latestSighting[1], latestSighting[2],
					(int) latestSighting[3]);
			// System.out.println("Cat: "+id+", zMouse: "+Math.toDegrees(latestSighting[2])+" updated to billboard");
		}

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
