package se.uu.it.cats.brick.filter;

import se.uu.it.cats.brick.Clock;
import se.uu.it.cats.brick.Logger;
import se.uu.it.cats.brick.Settings;
import se.uu.it.cats.brick.storage.BillBoard;

/** Particle filter for absolute positioning of one cat using landmarks. */
public class AbsolutePositioningParticleFilter extends
		AbsolutePositioningFilter {

	/** Linked list for particles */
	private LinkedList data;

	/** Number of particles */
	private final int Nnorm;
	private final int Ncut;

	/** Mean in the x, y and angle directions */
	private int mean_x;
	private int mean_y;
	private int mean_angle;

	/**
	 * Variance and co-variance (instead of using a full co-variance matrix only
	 * the value needed are saved).
	 */
	private int varXX;
	private int varYY;
	// private int varXY;
	private int varAngle;

	/** Sum of weights */
	private int sum_w;

	/** Variable for time */
	private int lastCurrentTime, currentTime;

	/** Local list of landmarks in fixed point. */
	private final int[][] landmarks;

	/** Random number lookup table */
	private final int[] randn_lut;
	private int randn_index;
	private final int RANDN_MASK = 2048 - 1;

	/** Counter and timer too keep track of mean iteration execution time */
	private int iterationCounter = 0;
	private int iterationTime = 0;

	private float lastLandmarkSighting;

	/** Counter for number of compares since re-sample */
	int evaluationsSinceResample = 0;

	/**
	 * Constructor of the absolute positioning particle filter.
	 * 
	 * @param N
	 *            Number of particles
	 * @param T
	 *            Period time
	 * @param sensorData
	 *            Buffer with sensor readings
	 * @param movementData
	 *            Buffer with movement data
	 * @param rttime
	 *            RealTimeClock
	 */
	public AbsolutePositioningParticleFilter(int id, int N, float T,
			Buffer unifiedData, BillBoard billboard) {
		// Call constructor of super class
		super(id, T, unifiedData, billboard);
		// Pre-calculate particle weight
		Nnorm = Fixed.floatToFixed(1 / ((float) N));
		// Set up a cut off for survival of the fittest
		Ncut = (N >> 3);
		// Make a local landmark list using fixed point integers. First dim of
		// landmarks are the landmark number, the second dim are x, y and type.
		landmarks = new int[4][3];
		for (int i = 0; i < Settings.LANDMARK_POSITION.length; i++) {
			// Save x and y
			landmarks[i][0] = Fixed
					.floatToFixed(Settings.LANDMARK_POSITION[i][0]);
			landmarks[i][1] = Fixed
					.floatToFixed(Settings.LANDMARK_POSITION[i][1]);
			// Save type
			landmarks[i][2] = Settings.LANDMARK_COLOR[i];
		}
		// Create the linked list in which the particles live
		data = new LinkedList();
		// Create particles
		for (int i = 0; i < N; i++) {
			data.insertSorted(new PositioningParticle(0, 0, 0, 0));
		}
		// Initialise random look up table data
		randn_lut = new int[RANDN_MASK + 1];
		for (int i = 0; i <= RANDN_MASK; i++) {
			randn_lut[i] = Fixed.randn();
		}
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
		mean_x = Fixed.floatToFixed(x);
		mean_y = Fixed.floatToFixed(y);
		mean_angle = Fixed.floatToFixed(angle);
		// Set (co-)variance variables
		// std => 5cm
		varXX = Fixed.floatToFixed(0.0025);
		varYY = Fixed.floatToFixed(0.0025);
		// No co-variance
		// varXY = Fixed.floatToFixed(0.0);
		// Set variance of the angle (std = 3 degrees)
		varAngle = Fixed.floatToFixed(Math.pow(3 * (Math.PI / 180), 2));
		// Set this to 0 so filter assumes all old particle data can be
		// overwritten.
		sum_w = 0;

		billboard
				.setAbsolutePosition(id, getX(), getY(), getAngle(), getTime());

		// Re-sample so particles get the new data
		reSample();
	}

	/**
	 * Move particles in the direction each particle is facing.
	 * 
	 * @param distance
	 *            The distance driven
	 */
	private void moveParticles(int distance) {
		// Set pointer to first element in data list
		Link link = data.first;
		// Loop through all particles
		while (link != null) {
			PositioningParticle part = (PositioningParticle) link.data;
			// Convert radians to pseudo degrees
			int a = Fixed
					.round(Fixed.mul(part.angle, Fixed.RADIANS_TO_DEGREES));
			// Get sin and cos
			int c = Fixed.cos(a);
			int s = Fixed.sin(a);
			// "Drive"
			part.x = part.x + Fixed.mul(c, distance);
			part.y = part.y + Fixed.mul(s, distance);
			link = link.next;
		}
		// Update mean in the same way as above
		int ang = Fixed.round(Fixed.mul(mean_angle, Fixed.RADIANS_TO_DEGREES));
		mean_x += Fixed.mul(Fixed.cos(ang), distance);
		mean_y += Fixed.mul(Fixed.sin(ang), distance);
	}

	/**
	 * Turn particles (i.e. add theta to all angle values).
	 * 
	 * @param theta
	 *            Angle to turn
	 */
	private void turnParticles(int theta) {
		Link link = data.first;
		// Loop through all particles
		while (link != null) {
			PositioningParticle part = (PositioningParticle) link.data;
			// Update each particles angle value
			part.angle = ((part.angle + theta) % (Fixed.PI << 1));
			link = link.next;
		}
		// Update mean
		mean_angle = ((mean_angle + theta) % (Fixed.PI << 1));
	}

	/**
	 * Compare particles to sensor input and sum weights.
	 * 
	 * @param sensorangle
	 *            The angle of the sensor reading in radians as a fixed point
	 *            integer.
	 * @param type
	 *            The type of the seen landmark
	 */
	private void compareParticles(int sensorangle, int type) {
		// Create temporary weight summation variable
		int sum_w_tmp = 0;
		// Create new data list
		LinkedList newlist = new LinkedList();
		// Pop a particle
		PositioningParticle part = (PositioningParticle) data.pop();
		// Loop through all particles
		while (part != null) {
			// Get rotation angle

			int theta = Fixed.round(Fixed.mul(-part.angle - sensorangle,
					Fixed.RADIANS_TO_DEGREES));
			int cos = Fixed.cos(theta);
			int sin = Fixed.sin(theta);

			/*
			 * double angle = Fixed.fixedToFloat(-part.angle - sensorangle); int
			 * cos = Fixed.floatToFixed(Math.cos(angle)); int sin =
			 * Fixed.floatToFixed(Math.sin(angle));
			 */

			// u = (1, 0)
			int u1 = Fixed.ONE;
			int u2 = 0;

			int z = 0;
			// Loop through landmarks
			for (int i = 0; i < Settings.LANDMARK_POSITION.length; i++) {
				int a = 0;
				// Check if the type is right
				if (type == landmarks[i][2]) {
					// Compute a vector from the particle towards the landmark
					// (toMark_x, toMark_y).
					int toMark_x = landmarks[i][0] - part.x;
					int toMark_y = landmarks[i][1] - part.y;
					// Calculate norm of (toMark_x, toMark_y).
					int norm = Fixed.norm(toMark_x, toMark_y);

					/*
					 * float toLandm = (float) Math.atan2(toMark_y, toMark_x);
					 * float sens = Fixed.fixedToFloat(sensorangle); float
					 * partangle = Fixed.fixedToFloat(part.angle); float cmpang
					 * = sens + partangle; float h = (float) ((Math.cos(cmpang)
					 * * Math.cos(toLandm)) + (Math .sin(cmpang) *
					 * Math.sin(toLandm))); float h = (float) ((Math.cos(0) *
					 * Math .cos(toLandm - cmpang)) + (Math.sin(0) * Math
					 * .sin(toLandm - cmpang))); int hf = Fixed.floatToFixed(h);
					 */

					// Check for zero distance to landmark
					if (norm == 0) {
						// System.out.println("Division by zero in AbsolutePositioningParticleFilter.compareParticles()");
						a = 0;
					} else {
						int toMark_x_norm = Fixed.div(toMark_x, norm);
						int toMark_y_norm = Fixed.div(toMark_y, norm);
						// rot_p = [cos(theta) -sin(theta); sin(theta)
						// cos(theta)];
						// v=rot_p*toMark
						// After this rotation the landmark vector should point
						// to
						// (1, 0) if the particle has the correct values.
						int v1 = Fixed.mul(toMark_x_norm, cos)
								+ Fixed.mul(toMark_y_norm, -sin);
						int v2 = Fixed.mul(toMark_x_norm, sin)
								+ Fixed.mul(toMark_y_norm, cos);
						// Inner product between vectors u and v =>
						// cos(angle_diff)
						a = Fixed.mul(v1, u1) + Fixed.mul(v2, u2);
					}

					// Check to see if this landmark gives a better hit
					if (a > z) {
						z = a;
					}
					// hf is approx the same as a
					/*
					 * if (hf > z) { z = hf; }
					 */

				}
			}
			// Run penalty function
			int w = ParticleFilter.penalty(z);
			if (w < 0) {
				// Check for negative values (for debugging).
				w = 0;
			}
			if (w > Fixed.ONE) {
				// Check for negative values (for debugging).
				Logger.println("w > Fixed.ONE");
				w = Fixed.ONE;
			}
			// Multiply weight from this iteration with particle weight
			part.comparable = Fixed.mul(part.comparable, w);
			// System.out.println("Weight: " + Fixed.fixedToFloat(part.w));
			// Sum weights
			sum_w_tmp += part.comparable;
			// Insert particle into new list
			newlist.insertSorted(part);
			// Pop new particle for the next iteration
			part = (PositioningParticle) data.pop();
		}

		// Replace the old list with the new sorted list
		data = newlist;
		// Save weight sum
		sum_w = sum_w_tmp;
		// System.out.println("sum_w=" + Fixed.fixedToFloat(sum_w));
	}

	/**
	 * Get next random value from the look up table.
	 * 
	 * @return Gaussian random number as fixed point integer
	 */
	private int nextRandn() {
		// Increase index
		randn_index++;
		randn_index &= RANDN_MASK;
		// Return value
		return randn_lut[randn_index];
	}

	/**
	 * Re-sample particles
	 */
	private void reSample() {

		varXX = Fixed.floatToFixed(0.0025);
		varYY = Fixed.floatToFixed(0.0025);
		varAngle = Fixed.floatToFixed(Math.pow(3 * (Math.PI / 180), 2));

		// Set up co-variance matrix
		/*
		 * int[][] C = new int[2][2]; C[0][0] = varXX; C[0][1] = varXY; C[1][0]
		 * = varXY; C[1][1] = varYY;
		 */

		// Get transform matrix for new sampling
		// int[][] V = ParticleFilter.getTransformFromCovariance(C);

		int[][] V = new int[2][2];
		V[0][0] = Fixed.sqrt(varXX);
		V[1][1] = Fixed.sqrt(varYY);

		// Get pointer to first element
		Link link = data.first;

		// Decide on cut off
		// if (sum_w != 0) {
		// Only the worst particles needs to be re-sampled, so some
		// particles can be skipped.
		// }

		int stdAngle = Fixed.sqrt(varAngle);
		for (int i = 0; (i < Ncut) && (link != null); i++) {
			PositioningParticle part = (PositioningParticle) link.data;
			// Get new random space vector (a, b), sample from 2d Gaussian
			// distribution.
			int a = nextRandn();
			int b = nextRandn();
			// Add mean and transform (a, b) into the new sample space.
			part.x = mean_x + Fixed.mul(V[0][0], a);// + Fixed.mul(V[0][1], b);
			part.y = mean_y // + Fixed.mul(V[1][0], a)
					+ Fixed.mul(V[1][1], b);
			// Add mean and get random samples for angular values
			part.angle = mean_angle + Fixed.mul(stdAngle, nextRandn());
			// Set norm to standard (all are equal) norm.
			part.comparable = Fixed.ONE;
			link = link.next;
		}

		// Loop through remaining particles
		while (link != null) {
			link.data.comparable = Fixed.ONE;
			link = link.next;
		}
	}

	/**
	 * Calculate means and (co-)variances. Also does checks on all values so
	 * they are within limits.
	 */
	public void calcMean() {
		// System.out.print("Calculating mean ");
		// Create local vaiables
		int tmean_x = 0, tmean_y = 0, tmean_a = 0, norm;
		if (sum_w == 0) {
			// System.out.println("(ordinary)");
			// Calculate an ordinary mean
			Link link = data.first;
			// Loop through all particle
			while (link != null) {
				PositioningParticle part = (PositioningParticle) link.data;
				tmean_x += part.x;
				tmean_y += part.y;
				tmean_a += part.angle;
				link = link.next;
			}
			// Set normalisation constant to 1/N since all particles have the
			// same weight.
			norm = Nnorm;
		} else {
			// System.out.println("(weighted) sum_w: " +
			// Fixed.fixedToFloat(sum_w));
			// Calculate a weighted mean
			Link link = data.first;
			// This should be equal to tmean_x=sum(x.*w) ...
			while (link != null) {
				// Loop through all particle
				PositioningParticle part = (PositioningParticle) link.data;
				// Add each value multiplied by the weight to the summation
				// variable
				tmean_x += Fixed.mul(part.x, part.comparable);
				tmean_y += Fixed.mul(part.y, part.comparable);
				tmean_a += Fixed.mul(part.angle, part.comparable);
				link = link.next;
			}
			// Set normalisation constant as the inverse of the sum of all
			// particle weights, equal to 1/sum(w).
			norm = Fixed.div(Fixed.ONE, sum_w);
		}

		// Normalise means and set the instance variables for means
		mean_x = Fixed.mul(tmean_x, norm);
		mean_y = Fixed.mul(tmean_y, norm);
		mean_angle = Fixed.mul(tmean_a, norm);

		// Make sure the angle mean is in the range [0:2*pi]
		while (mean_angle < 0) {
			mean_angle += (Fixed.PI << 1);

		}
		while (mean_angle > (Fixed.PI << 1)) {
			mean_angle -= (Fixed.PI << 1);
		}

		// Calculate (co-)variances
		if (sum_w == 0) {
			// No old data should be saved and filter knows nothing about the
			// current tracked states. Uncertainty increases with time (standard
			// deviation increases by 41%).
			varXX *= 2;
			varYY *= 2;
			varAngle *= 2;
			// X and Y can be considered as independent if nothing is known.
			// varXY = 0;
		} else {
			// Create local summation variables
			int tvarXX = 0, tvarYY = 0, tvarAngle = 0;// , tvarXY = 0;
			// Loop through particles
			// Should something like sum(w(x-meanx)(x-meanx)) ..
			Link link = data.first;
			while (link != null) {
				PositioningParticle part = (PositioningParticle) link.data;
				int x = part.x - mean_x;
				int y = part.y - mean_y;
				int xw = Fixed.mul(x, part.comparable);
				int yw = Fixed.mul(y, part.comparable);
				tvarXX += Fixed.mul(xw, x);
				// tvarXY += Fixed.mul(xw, y);
				tvarYY += Fixed.mul(yw, y);
				tvarAngle += Fixed
						.mul(part.angle - mean_angle, part.comparable);
				link = link.next;
			}
			// Normalise the variances
			varXX = Fixed.mul(tvarXX, norm);
			// varXY = Fixed.mul(tvarXY, norm);
			varYY = Fixed.mul(tvarYY, norm);
			varAngle = Fixed.mul(tvarAngle, norm);
		}
		// Check x and y means so they keep inside the arena
		if (mean_x < Fixed.floatToFixed(Settings.ARENA_MIN_X)) {
			mean_x = Fixed.floatToFixed(Settings.ARENA_MIN_X);
		}
		if (mean_x > Fixed.floatToFixed(Settings.ARENA_MAX_X)) {
			mean_x = Fixed.floatToFixed(Settings.ARENA_MAX_X);
		}
		if (mean_y < Fixed.floatToFixed(Settings.ARENA_MIN_Y)) {
			mean_y = Fixed.floatToFixed(Settings.ARENA_MIN_Y);
		}
		if (mean_y > Fixed.floatToFixed(Settings.ARENA_MAX_Y)) {
			mean_y = Fixed.floatToFixed(Settings.ARENA_MAX_Y);
		}
		// Check for min variance
		if (varXX < Fixed.floatToFixed(0.0025)) {
			// 0.0001 => std=1cm
			varXX = Fixed.floatToFixed(0.0025);
		}
		if (varYY < Fixed.floatToFixed(0.0025)) {
			varYY = Fixed.floatToFixed(0.0025);
		}
		if (varAngle < 0) {
			// No negative variances in angle
			varAngle = -varAngle;
		}
		if (varAngle < Fixed.floatToFixed(0.0027416)) {
			// std approx 2 degree
			varAngle = Fixed.floatToFixed(0.0027416);
		}
		// Check for max variance
		if (varXX > Fixed.HALF) {
			varXX = Fixed.HALF;
		}
		if (varYY > Fixed.HALF) {
			varYY = Fixed.HALF;
		}
		if (varAngle > (Fixed.PI >> 1)) {
			// std is approx 71 degrees
			varAngle = (Fixed.PI >> 1);
		}
		// varXY = 0;
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
		return Fixed.fixedToFloat(mean_x);
	}

	/**
	 * Returns estimated y position.
	 * 
	 * @return y in meters as a float
	 */
	public float getY() {
		return Fixed.fixedToFloat(mean_y);
	}

	/**
	 * Returns estimated angular position.
	 * 
	 * @return angle in radians as a float
	 */
	public float getAngle() {
		return Fixed.fixedToFloat(mean_angle);
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

		ComparableData data = unifiedBuffer.pop();
		while (data != null) {
			// Check if the data is older than cut off
			if (data.getComparable() <= currentTime) {
				// Read buffers for integration (angles, distance)
				if (data.isMovementData()) {
					MovementData mdata = (MovementData) data;
					if (Math.abs(mdata.dr) >= 0.001) {
						// Move particles
						moveParticles(Fixed.floatToFixed(mdata.dr));
						lastCurrentTime = mdata.comparable;
						billboard.setAbsolutePosition(id, getX(), getY(),
								getAngle(), getTime());
					}
					if (Math.abs(mdata.dangle) > (0.1 * (Math.PI / 180f))) {
						// Turning angle as fixed
						turnParticles(Fixed.floatToFixed(mdata.dangle));
						lastLandmarkSighting -= mdata.dangle;
						lastCurrentTime = mdata.comparable;
						billboard.setAbsolutePosition(id, getX(), getY(),
								getAngle(), getTime());
					}
				} else if (data.isSightingData()) {
					// Compare with landmarks or mouse data
					SightingData sdata = (SightingData) data;
					if (sdata.type == Settings.TYPE_MOUSE) {
						billboard.setLatestSighting(id, getX(), getY(),
								sdata.angle + getAngle(), sdata.comparable);
					} else {
						// if (Math.abs((lastLandmarkSighting - sdata.angle)
						// % Math.PI * 2) > (10 * (Math.PI / 180))) {
						// Compare sensor data to particles
						compareParticles(Fixed.floatToFixed(sdata.angle),
								sdata.type);
						// lastLandmarkSighting = sdata.angle;
						// Increase evaluation counter
						evaluationsSinceResample++;
						// }
					}
				}

				// Pops new data
				data = unifiedBuffer.pop();
			} else {
				unifiedBuffer.push(data);
				data = null;
			}

			// Re-sample every n:th evaluation or if there is no more data and
			// an evaluation has been performed.
			if (evaluationsSinceResample >= 3) {
				calcMean();
				reSample();
				evaluationsSinceResample = 0;
			}
		}

		if (evaluationsSinceResample > 0) {
			calcMean();
		}

		// Increase iteration counter and timer (with full execution time)
		iterationCounter++;
		iterationTime += Clock.timestamp() - currentTime;
		// Update public time
		lastCurrentTime = currentTime;
	}

	/**
	 * Returns the particles as a String for printing.
	 * 
	 * @return String with particle data
	 */
	public String toString() {
		String ret = "[";
		// Loop through all particles
		Link link = data.first;
		while (link != null) {
			PositioningParticle part = (PositioningParticle) link.data;
			// Append particle data to return String
			ret += part.toString();
			link = link.next;
			// Only append newline if there is more to print
			if (link != null) {
				ret += ",\n ";
			}
		}
		return ret + "]";
	}
}
