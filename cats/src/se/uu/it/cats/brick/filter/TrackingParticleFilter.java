package se.uu.it.cats.brick.filter;

import java.util.Random;

import se.uu.it.cats.brick.Clock;
import se.uu.it.cats.brick.Logger;
import se.uu.it.cats.brick.Settings;
import se.uu.it.cats.brick.storage.BillBoard;

/** Particle filter for tracking a mouse. */
public class TrackingParticleFilter extends TrackingFilter {

	/** Linked list for particles */
	private LinkedList data;

	/** Number of particles */
	private final int N;
	private final int Nnorm;
	private final int Ncut;

	/** Mean in the x, y, x' and y' directions */
	private int mean_x;
	private int mean_y;
	private int mean_xv;
	private int mean_yv;

	/**
	 * Variance and co-variance (instead of using a full co-variance matrix only
	 * the value needed are saved).
	 */
	private int varXX;
	private int varYY;
	// private int varXY;
	private int varXvXv;
	private int varYvYv;
	// private int varXvYv;

	/** Sum of weights */
	private int sum_w;

	/** Variable for time */
	private int lastCurrentTime, currentTime;// , lastIntegration;

	/** Random number lookup table */
	private final int[] randn_lut;
	private int randn_index;
	private final int RANDN_MASK = 511;

	/** Counter and timer too keep track of mean iteration execution time */
	private int iterationCounter = 0;
	private int iterationTime = 0;

	/** Counter for how many iterations there have been no good particles */
	private int dangerLevel = 0;

	/**
	 * Constructor of the tracking filter.
	 * 
	 * @param N
	 *            Number of particles
	 * @param T
	 *            Period time
	 * @param sensorData
	 *            Buffer with sensor readings
	 * @param rttime
	 *            RealTimeClock
	 * @param billboard
	 *            Shared network data object
	 */
	public TrackingParticleFilter(int id, int N, float T, BillBoard billboard) {
		// Call constructor of super class
		super(id, T, billboard);
		this.N = N;
		// Pre-calculate particle weight
		Nnorm = Fixed.floatToFixed(1 / ((float) N));
		// Set up a cut off for survival of the fittest
		Ncut = (N >> 2);
		// Create the linked list in which the particles live
		data = new LinkedList();
		// Create particles
		for (int i = 0; i < N; i++) {
			data.insertSorted(new TrackingParticle(0, 0, 0, 0, 0));
		}
		reSampleUniformly();
		// Initialise random look up table data
		randn_lut = new int[RANDN_MASK + 1];
		for (int i = 0; i <= RANDN_MASK; i++) {
			randn_lut[i] = Fixed.randn();
		}
	}

	private void reSampleUniformly() {
		float dr = 0.15f;
		Random rn = new Random();
		Link link = data.first;
		// Loop through all particles
		while (link != null) {
			TrackingParticle part = (TrackingParticle) link.data;
			float x = (float) (dr + rn.nextDouble()
					* (Settings.ARENA_MAX_X - Settings.ARENA_MIN_X - 2 * dr));
			float y = (float) (dr + rn.nextDouble()
					* (Settings.ARENA_MAX_Y - Settings.ARENA_MIN_Y - 2 * dr));
			part.x = Fixed.floatToFixed(x);
			part.y = Fixed.floatToFixed(y);
			part.comparable = Fixed.ONE;
			link = link.next;
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
	 * @param xv
	 *            Initial x velocity
	 * @param yv
	 *            Initial y velocity
	 */
	public void initData(float x, float y, float xv, float yv) {
		// Set means for x, y, x' and y'
		mean_x = Fixed.floatToFixed(x);
		mean_y = Fixed.floatToFixed(y);
		mean_xv = Fixed.floatToFixed(xv);
		mean_yv = Fixed.floatToFixed(yv);
		// Set (co-)variance variables
		// std => 5cm
		varXX = Fixed.floatToFixed(0.0025);
		varYY = Fixed.floatToFixed(0.0025);
		varXvXv = Fixed.floatToFixed(0.0025);
		varYvYv = Fixed.floatToFixed(0.0025);
		// No co-variance
		/*
		 * varXY = Fixed.floatToFixed(0.0); varXvYv = Fixed.floatToFixed(0.0);
		 */
		// Set this to 0 so filter assumes all old particle data can be
		// overwritten.
		sum_w = 0;
		// Re-sample so particles get the new data
		reSample();
	}

	/**
	 * Move particles in the direction each particle is facing.
	 * 
	 * @param dt
	 *            time to integrate
	 */
	private void integrateParticles(int dt) {
		// Set pointer to first element in data list
		Link link = data.first;
		// Loop through all particles
		while (link != null) {
			TrackingParticle part = (TrackingParticle) link.data;
			// Integrate
			part.x = part.x + Fixed.mul(part.xv, dt);
			part.y = part.y + Fixed.mul(part.yv, dt);
			link = link.next;
		}
		// Update mean in the same way as above
		mean_x = mean_x + Fixed.mul(mean_xv, dt);
		mean_y = mean_y + Fixed.mul(mean_yv, dt);
	}

	// Compare particles to sensor input and sum weights.
	private void compareParticles(int x, int y, int angle) {
		// Create temporary weight summation variable
		int sum_w_tmp = 0;
		// Counts div by zero for logging
		int div_by_zero = 0;
		// Create new data list
		LinkedList newlist = new LinkedList();
		// Pop a particle
		TrackingParticle part = (TrackingParticle) data.pop();
		// Loop through all particles
		while (part != null) {
			// Get rotation angle
			int theta = Fixed
					.round(Fixed.mul(-angle, Fixed.RADIANS_TO_DEGREES));
			int cos = Fixed.cos(theta);
			int sin = Fixed.sin(theta);

			int z = 0;

			int toMouse_x = part.x - x;
			int toMouse_y = part.y - y;
			// Calculate norm of (toMouse_x, toMouse_y).
			int norm = Fixed.norm(toMouse_x, toMouse_y);

			// Check for zero distance to mouse
			if (norm == 0) {
				div_by_zero++;
			} else {
				// Normalise vector toward the mouse
				int toMouse_x_norm = Fixed.div(toMouse_x, norm);
				int toMouse_y_norm = Fixed.div(toMouse_y, norm);
				// rot_p = [cos(theta) -sin(theta); sin(theta)
				// cos(theta)];
				// v=rot_p*toMark
				// After this rotation the landmark vector should point
				// to (1, 0) if the particle has the correct values.
				z = Fixed.mul(toMouse_x_norm, cos)
						+ Fixed.mul(toMouse_y_norm, -sin);
				// Inner product between vectors u and v =>
				// cos(angle_diff)
				// z = Fixed.mul(v1, u1); + Fixed.mul(v2, u2); //u = (1, 0)
			}
			// Run penalty function
			int w = ParticleFilter.penalty(z);
			if (w < 0) {
				// Check for negative values (for debugging).
				w = 0;
				Logger.println("Weight smaller than zero!");
			}
			// Multiply weight from this iteration with particle weight
			part.comparable = Fixed.mul(part.comparable, w);
			// Sum weights
			sum_w_tmp += part.comparable;
			// Insert particle into new list
			newlist.insertSorted(part);
			// Pop new particle for the next iteration
			part = (TrackingParticle) data.pop();
		}

		// Replace the old list with the new sorted list
		data = newlist;
		// Save weight sum
		sum_w = sum_w_tmp;
		// Print to logger if there was a division by zero
		if (div_by_zero != 0) {
			Logger.println(div_by_zero + " instances of division by zero in "
					+ "TrackingParticleFilter.compareParticles()");
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
		// ret = {m_x, m_y, m'_x, m'_y, xx, xy, yy, x'x', x'y', y'y'}

		// Decide on cut off
		int cut;
		if (sum_w != 0) {
			// Only the worst particles needs to be re-sampled
			cut = Ncut;
			dangerLevel = 0;
		} else {
			cut = N;
			dangerLevel++;
		}

		/*
		 * int[][] C1 = new int[2][2]; int[][] C2 = new int[2][2];
		 */

		// Set up co-variance matrices
		/*
		 * C1[0][0] = varXX; C1[0][1] = varXY; C1[1][0] = varXY; C1[1][1] =
		 * varYY; C2[0][0] = varXvXv; C2[0][1] = varXvYv; C2[1][0] = varXvYv;
		 * C2[1][1] = varYvYv;
		 */

		// Get transform matrices for new sampling
		/*
		 * int[][] V1 = ParticleFilter.getTransformFromCovariance(C1); int[][]
		 * V2 = ParticleFilter.getTransformFromCovariance(C2);
		 */
		if (dangerLevel >= 8) {
			this.reSampleUniformly();
		} else {
			int[][] V1 = new int[2][2];
			int[][] V2 = new int[2][2];
			V1[0][0] = Fixed.sqrt(varXX);
			V1[0][1] = 0;
			V1[1][0] = 0;
			V1[1][1] = Fixed.sqrt(varYY);
			V2[0][0] = Fixed.sqrt(varXvXv);
			V2[0][1] = 0;
			V2[1][0] = 0;
			V2[1][1] = Fixed.sqrt(varYvYv);

			// Get pointer to first element
			Link link = data.first;

			for (int i = 0; (i < cut) && (link != null); i++) {
				TrackingParticle part = (TrackingParticle) link.data;
				// Get new random space vector (a, b), sample from 2d Gaussian
				// distribution.
				int a = nextRandn();
				int b = nextRandn();
				int c = nextRandn();
				int d = nextRandn();
				// Add mean and transform the vectors (a, b) and (c, d) into the
				// new
				// sample space.
				part.x = mean_x + Fixed.mul(V1[0][0], a);
				// + Fixed.mul(V1[0][1], b);
				part.y = mean_y // + Fixed.mul(V1[1][0], a)
						+ Fixed.mul(V1[1][1], b);
				part.xv = mean_xv + Fixed.mul(V2[0][0], c);
				// + Fixed.mul(V2[0][1], d);
				part.yv = mean_yv // + Fixed.mul(V2[1][0], c)
						+ Fixed.mul(V2[1][1], d);
				// Set norm to standard (all are equal) norm.
				part.comparable = Fixed.ONE;
				link = link.next;
			}

			// All remaining particles (if any) have their weights reset
			while (link != null) {
				link.data.comparable = Fixed.ONE;
				link = link.next;
			}
		}
	}

	/**
	 * Calculate means and (co-)variances. Also does checks on all values so
	 * they are within limits.
	 */
	public void calcMean() {
		// Logger.print(id + ": Calculating mean ");
		// Create local variables
		int tmean_x = 0, tmean_y = 0, tmean_xv = 0, tmean_yv = 0, norm;
		// int mx = 0, my = 0;
		// Unweighed means
		if (sum_w == 0) {
			// Calculate an ordinary mean
			Link link = data.first;
			// Loop through all particle
			while (link != null) {
				TrackingParticle part = (TrackingParticle) link.data;
				tmean_x += part.x;
				tmean_y += part.y;
				tmean_xv += part.xv;
				tmean_yv += part.yv;
				link = link.next;
			}
			// Set normalisation constant to 1/N since all particles have the
			// same weight.
			norm = Nnorm;
			// mx = tmean_x;
			// my = tmean_y;
		} else {
			// Calculate a weighted mean
			Link link = data.first;
			// This should be equal to tmean_x=sum(x.*w) ...
			while (link != null) {
				// Loop through all particle
				TrackingParticle part = (TrackingParticle) link.data;
				// Add each value multiplied by the weight to the summation
				// variable
				tmean_x += Fixed.mul(part.x, part.comparable);
				tmean_y += Fixed.mul(part.y, part.comparable);
				tmean_xv += Fixed.mul(part.xv, part.comparable);
				tmean_yv += Fixed.mul(part.yv, part.comparable);
				// mx += part.x;
				// my += part.y;
				link = link.next;
			}
			// Set normalisation constant as the inverse of the sum of all
			// particle weights, equal to 1/sum(w).
			norm = Fixed.div(Fixed.ONE, sum_w);
		}

		// Normalise means and set the instance variables for means
		mean_x = Fixed.mul(tmean_x, norm);
		mean_y = Fixed.mul(tmean_y, norm);
		mean_xv = Fixed.mul(tmean_xv, norm);
		mean_yv = Fixed.mul(tmean_yv, norm);
		// mx = Fixed.mul(mx, Nnorm);
		// my = Fixed.mul(my, Nnorm);
		// Calculate (co-)variances
		if (sum_w == 0) {
			// No old data should be saved and filter knows nothing about the
			// current tracked states. Uncertainty increases with time (standard
			// deviation increases by 100%/40%).
			varXX *= 4;
			varYY *= 4;
			varXvXv *= 2;
			varYvYv *= 2;
			// X and Y can be considered as independent if nothing is known.
			/*
			 * varXY = 0; varXvYv = 0;
			 */
		} else {
			// Create local summation variables
			int tvarXX = 0, tvarXY = 0, tvarYY = 0, tvarXvXv = 0, tvarXvYv = 0, tvarYvYv = 0;
			// Loop through particles
			// Should something like sum(w(x-meanx)(x-meanx)) ..
			Link link = data.first;
			while (link != null) {
				TrackingParticle part = (TrackingParticle) link.data;
				int x = part.x - mean_x;
				int y = part.y - mean_y;
				// int x = part.x - mx;
				// int y = part.y - my;
				int xv = part.xv - mean_xv;
				int yv = part.yv - mean_yv;
				int xw = Fixed.mul(x, part.comparable);
				int yw = Fixed.mul(y, part.comparable);
				int xvw = Fixed.mul(xv, part.comparable);
				int yvw = Fixed.mul(yv, part.comparable);
				tvarXX += Fixed.mul(xw, x);
				tvarXY += Fixed.mul(xw, y);
				tvarYY += Fixed.mul(yw, y);
				tvarXvXv += Fixed.mul(xvw, xv);
				tvarXvYv += Fixed.mul(xvw, yv);
				tvarYvYv += Fixed.mul(yvw, yv);
				link = link.next;
			}
			// Normalise the variances
			varXX = Fixed.mul(tvarXX, norm);
			// varXY = Fixed.mul(tvarXY, norm);
			varYY = Fixed.mul(tvarYY, norm);
			varXvXv = Fixed.mul(tvarXvXv, norm);
			// varXvYv = Fixed.mul(tvarXvYv, norm);
			varYvYv = Fixed.mul(tvarYvYv, norm);
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
		// Check x and y velocities (always lower than 20 cm/s)
		int vc = Fixed.floatToFixed(0.2);
		if (mean_xv > vc) {
			mean_xv = vc;
		}
		if (mean_xv > vc) {
			mean_xv = vc;
		}
		if (mean_yv < -vc) {
			mean_yv = -vc;
		}
		if (mean_yv < -vc) {
			mean_yv = -vc;
		}
		// Check for min variance (0.0025 => std=5cm)
		if (varXX < Fixed.floatToFixed(0.0025)) {
			varXX = Fixed.floatToFixed(0.0025);
		}
		if (varYY < Fixed.floatToFixed(0.0025)) {
			varYY = Fixed.floatToFixed(0.0025);
		}
		// Check for max variance
		if (varXX > Fixed.ONE) {
			varXX = Fixed.ONE;
		}
		if (varYY > Fixed.ONE) {
			varYY = Fixed.ONE;
		}
		// Limit position co-variance
		/*
		 * int maxCoVar = (int) (.5 * Fixed.min(varXX, varYY)); if (varXY >
		 * maxCoVar) { varXY = maxCoVar; } if (varXY < -maxCoVar) { varXY =
		 * maxCoVar; }
		 */
		// Limit velocity variance
		/*
		 * maxCoVar = (int) (.5 * Fixed.min(varXvXv, varYvYv)); if (varXvYv >
		 * maxCoVar) { varXvYv = maxCoVar; } if (varXvYv < -maxCoVar) { varXvYv
		 * = maxCoVar; }
		 */
		if (varXvXv < Fixed.floatToFixed(0.0001)) {
			varXvXv = Fixed.floatToFixed(0.0001);
		}
		if (varYvYv < Fixed.floatToFixed(0.0001)) {
			varYvYv = Fixed.floatToFixed(0.0001);
		}
		/*
		 * varXY = 0; varXvYv = 0;
		 */
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
	 * Returns estimated x velocity.
	 * 
	 * @return x velocity in meters as a float
	 */
	public float getXv() {
		return Fixed.fixedToFloat(mean_xv);
	}

	/**
	 * Returns estimated y velocity.
	 * 
	 * @return y velocity in meters as a float
	 */
	public float getYv() {
		return Fixed.fixedToFloat(mean_yv);
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
		// Download mean and co-variance data
		float[] net = billboard.getMeanAndCovariance();
		// Update the local mean and co-variance
		sum_w = Fixed.floatToFixed(net[10]);
		if (sum_w == 0) {
			varXX = Fixed.ONE;
			// varXY = 0;
			varYY = Fixed.ONE;
		} else {
			mean_x = Fixed.floatToFixed(net[0]);
			mean_y = Fixed.floatToFixed(net[1]);
			mean_xv = Fixed.floatToFixed(net[2]);
			mean_yv = Fixed.floatToFixed(net[3]);
			// Set up co-variance matrices
			varXX = Fixed.floatToFixed(net[4]);
			// varXY = Fixed.floatToFixed(net[5]);
			varYY = Fixed.floatToFixed(net[6]);
		}
		varXvXv = Fixed.floatToFixed(net[7]);
		// varXvYv = Fixed.floatToFixed(net[8]);
		varYvYv = Fixed.floatToFixed(net[9]);

		// Re-sample
		reSample();

		// Get time reference
		currentTime = Clock.timestamp();

		// Integrate particles
		// TODO: Integration should be done with respect to time diff between
		// sightings
		integrateParticles(Fixed.floatToFixed(T));

		// Compare sensor data to particles
		float[] sightings = billboard.getLatestSightings();
		// Loop through cats in billboard
		for (int i = 0; i < billboard.getNoCats(); i++) {
			int x = Fixed.floatToFixed(sightings[i * 4]);
			int y = Fixed.floatToFixed(sightings[i * 4 + 1]);
			int angle = Fixed.floatToFixed(sightings[i * 4 + 2]);
			if ((sightings[i * 4] >= 0)
					&& (sightings[i * 4 + 3] <= currentTime)) {
				compareParticles(x, y, angle);
			}
		}

		// Calculate mean and (co-)variance, then commit data to billboard
		calcMean();
		/*
		 * billboard.setMeanAndCovariance(id, Fixed.fixedToFloat(mean_x), Fixed
		 * .fixedToFloat(mean_y), Fixed.fixedToFloat(mean_xv), Fixed
		 * .fixedToFloat(mean_yv), Fixed.fixedToFloat(varXX), Fixed
		 * .fixedToFloat(varXY), Fixed.fixedToFloat(varYY), Fixed
		 * .fixedToFloat(varXvXv), Fixed.fixedToFloat(varXvYv), Fixed
		 * .fixedToFloat(varYvYv), Fixed.fixedToFloat(sum_w));
		 */
		billboard.setMeanAndCovariance(id, Fixed.fixedToFloat(mean_x), Fixed
				.fixedToFloat(mean_y), Fixed.fixedToFloat(mean_xv), Fixed
				.fixedToFloat(mean_yv), Fixed.fixedToFloat(varXX), Fixed
				.fixedToFloat(0), Fixed.fixedToFloat(varYY), Fixed
				.fixedToFloat(varXvXv), Fixed.fixedToFloat(0), Fixed
				.fixedToFloat(varYvYv), Fixed.fixedToFloat(sum_w));

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
			TrackingParticle part = (TrackingParticle) link.data;
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
