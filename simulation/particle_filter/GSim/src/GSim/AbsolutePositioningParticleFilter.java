package GSim;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.util.Random;

/** Particle filter for absolute positioning of one cat using landmarks. */
public class AbsolutePositioningParticleFilter extends
		AbsolutePositioningFilter {

	/** Linked list for particles */
	private LinkedList data;

	/** Number of particles */
	private final int N;
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
	private int varXY;
	private int varAngle;

	/** Sum of weights and flag to tell if it is zero. */
	private int sum_w;
	private boolean zerosum;

	/** Varible for time */
	private int lastCurrentTime, currentTime;

	/** Local list of landmarks in fixed point. */
	private final int[][] landmarks;

	/** Random number lookup table */
	private final int[] randn_lut;
	private int randn_index;
	private final int RANDN_MASK = 511;

	/** Counter and timer too keep track of mean iteration execution time */
	private int iterationCounter = 0;
	private int iterationTime = 0;

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
	public AbsolutePositioningParticleFilter(int N, float T, Buffer sensorData,
			Buffer movementData, RealTimeClock rttime) {
		// Call constructor of super class
		super(T, sensorData, movementData, rttime);
		this.N = N;
		// Pre-calculate particle weight
		Nnorm = Fixed.floatToFixed(1 / ((float) N));
		// Set up a cut off for survival of the fittest
		Ncut = (N >> 2);
		// Make a local landmark list using fixed point integers. First dim of
		// landmarks are the landmark number, the second dim are x, y and type.
		landmarks = new int[4][3];
		for (int i = 0; i < LandmarkList.landmarkX.length; i++) {
			// Save x and y
			landmarks[i][0] = Fixed.floatToFixed(LandmarkList.landmarkX[i]);
			landmarks[i][1] = Fixed.floatToFixed(LandmarkList.landmarkY[i]);
			// Save type
			if (LandmarkList.landmarkC[i]) {
				landmarks[i][2] = LandmarkList.GREEN;
			} else {
				landmarks[i][2] = LandmarkList.RED;
			}
		}
		// Create the linked list in which the particles live
		data = new LinkedList();
		// Create particles
		for (int i = 0; i < N; i++) {
			data.insertSorted(new PositioningParticle(Fixed.floatToFixed(1.0),
					Fixed.floatToFixed(1.0), Fixed.floatToFixed(i * 200), Fixed
							.floatToFixed(i % 10)));
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
		varXY = Fixed.floatToFixed(0.0);
		// std => 3 degrees
		varAngle = Fixed.floatToFixed(Math.pow(3 * (Math.PI / 180), 2));
		// Set this to true so filter assumes all old particle data can be
		// overwritten.
		zerosum = true;
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
		// Bet pointer to first element in data list
		Link link = data.first;
		// Loop through all particles
		while (link != null) {
			PositioningParticle part = (PositioningParticle) link.data;
			// Convert radians to pseudo degrees
			int a = Fixed.round(part.angle * Fixed.RADIANS_TO_DEGREES);
			// Get sin and cos
			int c = Fixed.cos(a);
			int s = Fixed.sin(a);
			// "Drive"
			part.x = part.x + Fixed.mul(c, distance);
			part.y = part.y + Fixed.mul(s, distance);
			link = link.next;
		}
		// Update mean in the same way as above
		int ang = Fixed.round(mean_angle * Fixed.RADIANS_TO_DEGREES);
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
			part.angle = part.angle + theta;
			link = link.next;
		}
		// Update mean
		mean_angle += theta;
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
		// TODO: Verify code against matlab
		// Create temporary weight summation variable
		int sum_w_tmp = 0;
		// Create new data list
		LinkedList newlist = new LinkedList();
		// Pop a particle
		PositioningParticle part = (PositioningParticle) data.popFirst();
		// Loop through all particles
		while (part != null) {
			int theta = Fixed.round(Fixed.mul(-part.angle - sensorangle,
					Fixed.RADIANS_TO_DEGREES));
			int cos = Fixed.cos(theta);
			int sin = Fixed.sin(theta);

			int z = 0;
			// Loop through landmarks
			for (int i = 0; i < LandmarkList.landmarkX.length; i++) {
				int a = 0;
				// Check if the type is right
				if (type == landmarks[i][2]) {
					// Compute a vector from the particle towards the landmark
					// (toMark_x, toMark_y).
					int toMark_x = landmarks[i][0] - part.x;
					int toMark_y = landmarks[i][1] - part.y;
					// Calculate norm of (toMark_x, toMark_y).
					int norm = Fixed.norm(toMark_x, toMark_y);

					float toLandm = (float) Math.atan2(toMark_y, toMark_x);
					float sens = Fixed.fixedToFloat(part.angle);
					float h = (float) ((Math.cos(sens) * Math.cos(toLandm)) + (Math
							.sin(sens) * Math.sin(toLandm)));
					int hf = Fixed.floatToFixed(h);

					// Check for zero distance to landmark
					if (norm == 0) {
						System.out
								.println("Division by zero in compareParticles()");
						a = 0;
					} else {
						int v1 = Fixed.mul(toMark_x, cos)
								- Fixed.mul(toMark_y, sin);
						a = Fixed.div(v1, norm);
					}

					// Check to see if this landmark gives a better hit
					if (a > z) {
						z = a;
					}

					if (hf > z) {
						z = hf;
					}

					/*
					 * if (z > ParticleFilter.CUT[4]) { // Break loop if
					 * comparison is a hit break; }
					 */
				}
			}
			// Run penalty function
			int w = ParticleFilter.penalty(z);
			if (w < 0) {
				// Check for negative values (for debugging).
				w = 0;
			}
			// Multiply weight from this iteration with particle weight
			part.comparable = Fixed.mul(part.comparable, w);
			// System.out.println("Weight: " + Fixed.fixedToFloat(part.w));
			// Sum weights
			sum_w_tmp += part.comparable;
			// Insert particle into new list
			newlist.insertSorted(part);
			// Pop new particle for the next iteration
			part = (PositioningParticle) data.popFirst();
		}

		// Replace the old list with the new sorted list
		data = newlist;
		// Save weight sum
		sum_w = sum_w_tmp;
		// Check if the sum of weights are zero
		zerosum = (sum_w_tmp == 0);

		System.out.println("sum_w=" + Fixed.fixedToFloat(sum_w));
		if (data.length() != N) {
			System.out.println("Particles lost! (count:" + data.length() + ")");
		}
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
		// Set up co-variance matrix
		int[][] C = new int[2][2];
		C[0][0] = varXX;
		C[0][1] = varXY;
		C[1][0] = varXY;
		C[1][1] = varYY;

		// Get transform matrix for new sampling
		int[][] V = ParticleFilter.getTransformFromCovariance(C);

		// Get pointer to first element
		Link link = data.first;

		// Decide on cut off
		if (!zerosum) {
			// Only the worst particles needs to be re-sampled, so some
			// particles can be skipped.
			// TODO: Stop full re-sampling
			/*
			 * for (int i = 0; (i < Ncut) && (link != null); i++) { link.data.w
			 * = Nnorm; link = link.next; }
			 */
		}

		int stdAngle = Fixed.sqrt(varAngle);
		System.out.println("stdAngle: " + Fixed.fixedToFloat(stdAngle)
				* (180 / Math.PI));
		// Loop through all particles
		while (link != null) {
			PositioningParticle part = (PositioningParticle) link.data;
			// Get new random space vector (a, b), sample from 2d Gaussian
			// distribution.
			int a = nextRandn();
			int b = nextRandn();
			// Add mean and transform (a, b) into the new sample space.
			part.x = mean_x + Fixed.mul(V[0][0], a) + Fixed.mul(V[0][1], b);
			part.y = mean_y + Fixed.mul(V[1][0], a) + Fixed.mul(V[1][1], b);
			// Add mean and get random samples for angular values
			part.angle = mean_angle + Fixed.mul(stdAngle, nextRandn());
			// Set norm to standard (all are equal) norm.
			part.comparable = Nnorm;
			link = link.next;
		}
	}

	/**
	 * Calculate means and (co-)variances. Also does checks on all values so
	 * they are within limits.
	 */
	public void calcMean() {
		System.out.print("Calculating mean ");
		// Create local vaiables
		int tmean_x = 0, tmean_y = 0, tmean_a = 0, norm;
		if (zerosum) {
			System.out.println("(ordinary)");
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
			System.out
					.println("(weighted) sum_w: " + Fixed.fixedToFloat(sum_w));
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
		if (zerosum) {
			// No old data should be saved and filter knows nothing about the
			// current tracked states. Uncertainty increases with time (standard
			// deviation increases by 20%).
			varXX *= 1.44;
			varYY *= 1.44;
			varAngle *= 1.44;
			// X and Y can be considered as independent if nothing is known.
			varXY = 0;
		} else {
			// Create local summation variables
			int tvarXX = 0, tvarXY = 0, tvarYY = 0, tvarAngle = 0;
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
				tvarXY += Fixed.mul(xw, y);
				tvarYY += Fixed.mul(yw, y);
				// TODO: Check circle errors in mean
				tvarAngle += Fixed
						.mul(part.angle - mean_angle, part.comparable);
				link = link.next;
			}
			// Normalise the variances
			varXX = Fixed.mul(tvarXX, norm);
			varXY = Fixed.mul(tvarXY, norm);
			varYY = Fixed.mul(tvarYY, norm);
			varAngle = Fixed.mul(tvarAngle, norm);
		}
		// Check x and y means so they keep inside the arena
		if (mean_x < Fixed.floatToFixed(Arena.min_x)) {
			mean_x = Fixed.floatToFixed(Arena.min_x);
		}
		if (mean_x > Fixed.floatToFixed(Arena.max_x)) {
			mean_x = Fixed.floatToFixed(Arena.max_x);
		}
		if (mean_y < Fixed.floatToFixed(Arena.min_y)) {
			mean_y = Fixed.floatToFixed(Arena.min_y);
		}
		if (mean_y > Fixed.floatToFixed(Arena.max_y)) {
			mean_y = Fixed.floatToFixed(Arena.max_y);
		}
		// Check for min variance
		if (varXX < Fixed.floatToFixed(0.0001)) {
			// 0.0001 => std=1cm
			varXX = Fixed.floatToFixed(0.0001);
		}
		if (varYY < Fixed.floatToFixed(0.0001)) {
			varYY = Fixed.floatToFixed(0.0001);
		}
		if (varAngle < 0) {
			// No negative variances in angle
			varAngle = -varAngle;
		}
		if (varAngle < Fixed.floatToFixed(0.0003)) {
			// std approx 1 degree
			varAngle = Fixed.floatToFixed(0.0003);
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

		g2.setColor(Color.green);
		// Plot particles
		Link link = data.first;
		while (link != null) {
			PositioningParticle part = (PositioningParticle) link.data;
			int ix = Actor.e2gX(Fixed.fixedToFloat(part.x));
			int iy = Actor.e2gY(Fixed.fixedToFloat(part.y));
			double iangle = -Fixed.fixedToFloat(part.angle);
			g2.fillOval((int) ix - (size / 2), (int) iy - (size / 2),
					(int) size, (int) size);
			g2.drawLine((int) ix, (int) iy, (int) (ix + Math.cos(iangle)
					* linelength), (int) (iy + Math.sin(iangle) * linelength));
			link = link.next;
		}
		// Plot mean
		g2.setColor(Color.red);
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
		// TODO: Needs revision and clean up

		// Get time reference
		currentTime = rttime.getTime();

		// Make a new sorted list with all data up to time currentTime which is
		// sorted.
		LinkedList list = new LinkedList();
		SightingData sdata = (SightingData) sensorData.pop();
		while (sdata != null) {
			if (sdata.getComparable() <= currentTime) {
				list.insertSorted(sdata);
				sdata = (SightingData) sensorData.pop();
			} else {
				sensorData.push(sdata);
				sdata = null;
			}
		}
		MovementData mdata = (MovementData) movementData.pop();
		// TODO: Remove redundant buffer objects and merge where possible
		while (mdata != null) {
			if (mdata.getComparable() <= currentTime) {
				list.insertSorted(mdata);
				mdata = (MovementData) movementData.pop();
			} else {
				movementData.push(mdata);
				mdata = null;
			}
		}
		System.out.println(list.toString());

		// Counter for number of compares since re-sample
		int evaluationsSinceResample = 0;

		ComparableData bufferdata = list.popFirst();
		while (bufferdata != null) {

			// Read buffers for integration (angles, distance)
			if (bufferdata.isMovementData()) {
				// System.out.println("Integrate");
				mdata = (MovementData) bufferdata;
				if (Math.abs(mdata.dr) > 0.00001) {
					// Move particles
					moveParticles(Fixed.floatToFixed(mdata.dr));
					lastCurrentTime = mdata.comparable;
				}
				if (Math.abs(mdata.dangle) > 0.00001) {
					// Turning angle as fixed
					turnParticles(Fixed.floatToFixed(mdata.dangle));
					lastCurrentTime = mdata.comparable;
				}
			}

			// Compare with landmarks
			if (bufferdata.isSightingData()) {
				sdata = (SightingData) bufferdata;
				compareParticles(Fixed.floatToFixed(sdata.angle), sdata.type);
				// Increase evaluation counter
				evaluationsSinceResample++;
			}

			// Pops new data
			bufferdata = list.popFirst();

			// Re-sample every n:th evaluation or if there is no more data
			if ((evaluationsSinceResample >= 4) || (bufferdata == null)) {
				calcMean();
				reSample();
				evaluationsSinceResample = 0;
			}
		}
		// Increase iteration counter and timer (with full execution time)
		iterationCounter++;
		iterationTime += rttime.getTime() - currentTime;
		// Update public time
		lastCurrentTime = currentTime;
	}

	public void run() {
		// TODO: Implement main loop and thread timer
		/*
		 * while (true) { update(); sleep((long) (rttime.getTime() % T)); }
		 */
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
