package GSim;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

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
	private int[] landmarkNoSightings;
	private boolean positioning = false;

	final float maxPositionCorrection = 0.01f;
	final float maxAngleCorrection = (float) (0.5 * (Math.PI / 180));
	final float maxAngle = (float) (140 * Math.PI / 180);

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
		landmarkNoSightings = new int[Settings.NO_LANDMARKS];
		resetSightings();
	}

	private void resetSightings() {
		if (positioning) {
			for (int i = 0; i < Settings.NO_LANDMARKS; i++) {
				landmarkNoSightings[i] = 0;
			}
			positioning = false;
		}
	}

	private int noSeenLandmarks() {
		int ret = 0;
		for (int i = 0; i < Settings.NO_LANDMARKS; i++) {
			if (landmarkNoSightings[i] > 0) {
				ret++;
			}
		}
		return ret;
	}

	private void addLandmark(float angle, int type) {
		final float angleEpsilon = (float) (10 * (Math.PI / 180));
		positioning = true;
		for (int i = 0; i < Settings.NO_LANDMARKS; i++) {
			if (Settings.LANDMARK_COLOR[i] == type) {

				if (Math.abs(Math.atan2(Settings.LANDMARK_POSITION[i][1]
						- getY(), Settings.LANDMARK_POSITION[i][0] - getX())
						- getAngle() - angle) < (90 * (Math.PI / 180))) {

					// Check if this landmark has been seen
					if (landmarkNoSightings[i] > 0) {
						// Check if new landmark is close enough to the existing
						if (Math.abs(landmarkAngles[i] - angle) < angleEpsilon) {
							landmarkNoSightings[i]++;
							landmarkAngles[i] = ((landmarkNoSightings[i] - 1)
									* landmarkAngles[i] + angle)
									/ landmarkNoSightings[i];
						}
					} else {
						landmarkNoSightings[i]++;
						landmarkAngles[i] = angle;
					}
				}

			}
		}
	}

	/*
	 * private void sortLandmarks() { // Bubble sort for (int j = 0; j <
	 * (Settings.NO_LANDMARKS - 1); j++) { for (int i = 0; i <
	 * (Settings.NO_LANDMARKS - 1); i++) { if (landmarkAngles[i] <
	 * landmarkAngles[i + 1]) { float a = landmarkAngles[i]; landmarkAngles[i] =
	 * landmarkAngles[i + 1]; landmarkAngles[i + 1] = a; int t =
	 * landmarkTypes[i]; landmarkTypes[i] = landmarkTypes[i + 1];
	 * landmarkTypes[i + 1] = t; int s = landmarkNoSightings[i];
	 * landmarkNoSightings[i] = landmarkNoSightings[i + 1];
	 * landmarkNoSightings[i + 1] = s; } } } }
	 */

	/*
	 * private void shiftLandmarks() { // Simple shift float a =
	 * landmarkAngles[0]; int t = landmarkTypes[0]; int s =
	 * landmarkNoSightings[0]; for (int i = 0; i < (Settings.NO_LANDMARKS - 1);
	 * i++) { landmarkAngles[i] = landmarkAngles[i + 1]; landmarkTypes[i] =
	 * landmarkTypes[i + 1]; landmarkNoSightings[i] = landmarkNoSightings[i +
	 * 1]; } landmarkAngles[Settings.NO_LANDMARKS - 1] = a;
	 * landmarkTypes[Settings.NO_LANDMARKS - 1] = t;
	 * landmarkNoSightings[Settings.NO_LANDMARKS - 1] = s; }
	 */

	private void doGeometricMagic() {
		if (positioning) {
			if (noSeenLandmarks() >= 3) {
				// List is now sorted with unique landmark first
				float[] north = null, west = null, south = null, east = null;
				float[] phi = new float[Settings.NO_LANDMARKS];
				// North
				if ((landmarkNoSightings[3] > 0)
						&& (landmarkNoSightings[1] > 0)) {
					phi[0] = (float) Math
							.abs((landmarkAngles[3] - landmarkAngles[1])
									% (2 * Math.PI));
					if (phi[0] > Math.PI) {
						phi[0] = (float) (2 * Math.PI - phi[0]);
					}
					if (phi[0] < maxAngle) {
						north = findCircle(phi[0], 1);
					}
				}
				// West
				if ((landmarkNoSightings[1] > 0)
						&& (landmarkNoSightings[0] > 0)) {
					phi[1] = (float) Math
							.abs((landmarkAngles[1] - landmarkAngles[0])
									% (2 * Math.PI));
					if (phi[1] > Math.PI) {
						phi[1] = (float) (2 * Math.PI - phi[1]);
					}
					if (phi[1] < maxAngle) {
						west = findCircle(phi[1], 2);
					}

				}
				// South
				if ((landmarkNoSightings[0] > 0)
						&& (landmarkNoSightings[2] > 0)) {
					phi[2] = (float) Math
							.abs((landmarkAngles[0] - landmarkAngles[2])
									% (2 * Math.PI));
					if (phi[2] > Math.PI) {
						phi[2] = (float) (2 * Math.PI - phi[2]);
					}
					if (phi[2] < maxAngle) {
						south = findCircle(phi[2], 3);
					}

				}
				// East
				if ((landmarkNoSightings[2] > 0)
						&& (landmarkNoSightings[3] > 0)) {
					phi[3] = (float) Math
							.abs((landmarkAngles[2] - landmarkAngles[3])
									% (2 * Math.PI));
					if (phi[3] > Math.PI) {
						phi[3] = (float) (2 * Math.PI - phi[3]);
					}
					if (phi[3] < maxAngle) {
						east = findCircle(phi[3], 4);
					}

				}
				// TODO: Use the ones with the smallest angle

				float[] P = null;

				if ((north != null) && (east != null)) {
					P = findClosestIntersection(north, east);
				} else if ((east != null) && (south != null)) {
					P = findClosestIntersection(east, south);
				} else if ((south != null) && (west != null)) {
					P = findClosestIntersection(south, west);
				} else if ((west != null) && (north != null)) {
					P = findClosestIntersection(west, north);
				}

				if (P != null) {
					if ((Settings.ARENA_MIN_X <= P[0])
							&& (P[0] <= Settings.ARENA_MAX_X)
							&& (Settings.ARENA_MIN_Y <= P[1])
							&& (P[1] <= Settings.ARENA_MAX_Y)) {
						float diff = P[0] - mean_x;
						if (Math.abs(diff) < maxPositionCorrection) {
							mean_x += diff;
						} else {
							mean_x += maxPositionCorrection * Math.signum(diff);
						}
						diff = P[1] - mean_y;
						if (Math.abs(diff) < maxPositionCorrection) {
							mean_y += diff;
						} else {
							mean_y += maxPositionCorrection * Math.signum(diff);
						}
					}
					if (noSeenLandmarks() >= 1) {

						float diff = 0;
						int denominator = 0;
						for (int i = 0; i < Settings.NO_LANDMARKS; i++) {
							if (landmarkNoSightings[i] > 0) {
								diff += (float) (Math.atan2(
										Settings.LANDMARK_POSITION[i][1]
												- mean_y,
										Settings.LANDMARK_POSITION[i][0]
												- mean_x) - landmarkAngles[i])
										- mean_angle;
								denominator++;
							}

						}
						if (denominator > 0) {
							diff = diff / denominator;
							if (Math.abs(diff) < maxAngleCorrection
									* denominator) {
								mean_angle += diff;
							} else {
								mean_angle += maxAngleCorrection * denominator
										* Math.signum(diff);
							}
						}
					}
				}
			}
		}
	}

	private float[] findClosestIntersection(float[] c1, float[] c2) {
		float d = (float) Math.sqrt((c1[0] - c2[0]) * (c1[0] - c2[0])
				+ (c1[1] - c2[1]) * (c1[1] - c2[1]));

		if (d > (c1[2] + c2[2])) {
			// circles are separate
			Logger.println("circles are separate");
		} else if (d < Math.abs(c1[2] - c2[2])) {
			// one circle is inside the other
			Logger.println("one circle is inside the other");
		} else if ((d == 0) && (c1[2] == c2[2])) {
			// circles coincide
			Logger.println("circles coincide");
		}

		float a = (c1[2] * c1[2] - c2[2] * c2[2] + d * d) / (2 * d);
		float h = (float) Math.sqrt(c1[2] * c1[2] - a * a);

		// v=(P1-P0)/d;
		float[] v = new float[2];
		v[0] = (c2[0] - c1[0]) / d;
		v[1] = (c2[1] - c1[1]) / d;
		// u=[v(2) -v(1)];
		float[] u = new float[2];
		u[0] = v[1];
		u[1] = -v[0];
		// P0 = [x1 y1];
		// P1 = [x2 y2];
		// P2 = P0 + a*v + h*u;
		float[] P1 = new float[2];
		P1[0] = c1[0] + a * v[0] + h * u[0];
		P1[1] = c1[1] + a * v[1] + h * u[1];
		// P3 = P0 + a*v - h*u;
		float[] P2 = new float[2];
		P2[0] = c1[0] + a * v[0] - h * u[0];
		P2[1] = c1[1] + a * v[1] - h * u[1];

		float d1 = (getX() - P1[0]) * (getX() - P1[0]) + (getY() - P1[1])
				* (getY() - P1[1]);
		float d2 = (getX() - P2[0]) * (getX() - P2[0]) + (getY() - P2[1])
				* (getY() - P2[1]);

		if ((d1 < d2) && (Settings.ARENA_MIN_X <= P1[0])
				&& (P1[0] <= Settings.ARENA_MAX_X)
				&& (Settings.ARENA_MIN_Y <= P1[1])
				&& (P1[1] <= Settings.ARENA_MAX_Y)) {
			return P1;
		} else {
			return P2;
		}
	}

	private float[] findCircle(float angle, int direction) {
		// direction: 1 north, 2 west, 3 south, 4 east
		float sidex = Settings.ARENA_MAX_X - Settings.ARENA_MIN_X;
		float sidey = Settings.ARENA_MAX_Y - Settings.ARENA_MIN_Y;
		float[] ret = new float[3];
		// North
		if (direction == 1) {
			float x = sidex / 2;
			float r = (float) (x / Math.sin(angle));
			float y = (float) (sidey - x / Math.tan(angle));
			ret[0] = x;
			ret[1] = y;
			ret[2] = r;
		}
		// West
		else if (direction == 2) {
			float y = sidey / 2;
			float r = (float) (y / Math.sin(angle));
			float x = (float) (y / Math.tan(angle));
			ret[0] = x;
			ret[1] = y;
			ret[2] = r;
		}
		// South
		else if (direction == 3) {
			float x = sidex / 2;
			float r = (float) (x / Math.sin(angle));
			float y = (float) (x / Math.tan(angle));
			ret[0] = x;
			ret[1] = y;
			ret[2] = r;
		}
		// East
		else if (direction == 4) {
			float y = sidey / 2;
			float r = (float) (y / Math.sin(angle));
			float x = (float) (sidex - y / Math.tan(angle));
			ret[0] = x;
			ret[1] = y;
			ret[2] = r;
		}
		return ret;
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
						resetSightings();
						mean_x += Math.cos(mean_angle) * mdata.dr;
						mean_y += Math.sin(mean_angle) * mdata.dr;
						mean_angle += mdata.dangle;
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
