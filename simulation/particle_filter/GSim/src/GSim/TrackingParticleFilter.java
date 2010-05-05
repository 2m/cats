package GSim;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

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
	private int varXY;
	private int varXvXv;
	private int varYvYv;
	private int varXvYv;

	/** Sum of weights and flag to tell if it is zero. */
	private int sum_w;
	private boolean zerosum;

	/** Varible for time */
	private int lastCurrentTime, currentTime;

	/** Random number lookup table */
	private final int[] randn_lut;
	private int randn_index;
	private final int RANDN_MASK = 511;

	/** Counter and timer too keep track of mean iteration execution time */
	private int iterationCounter = 0;
	private int iterationTime = 0;

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
	public TrackingParticleFilter(int id, int N, float T, Buffer sensorData,
			RealTimeClock rttime, BillBoard billboard) {
		// Call constructor of super class
		super(id, T, sensorData, rttime, billboard);
		this.N = N;
		// Pre-calculate particle weight
		Nnorm = Fixed.floatToFixed(1 / ((float) N));
		// Set up a cut off for survival of the fittest
		Ncut = (N >> 1);
		// Create the linked list in which the particles live
		data = new LinkedList();
		// Create particles
		for (int i = 0; i < N; i++) {
			data.insertSorted(new TrackingParticle(Fixed.floatToFixed(1.0),
					Fixed.floatToFixed(1.0), Fixed.floatToFixed(0.0), Fixed
							.floatToFixed(0.0), Fixed.floatToFixed(i % 10)));
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
		varXY = Fixed.floatToFixed(0.0);
		varXvYv = Fixed.floatToFixed(0.0);
		// Set this to true so filter assumes all old particle data can be
		// overwritten.
		zerosum = true;
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
		// Create new data list
		LinkedList newlist = new LinkedList();
		// Pop a particle
		TrackingParticle part = (TrackingParticle) data.popFirst();
		// Loop through all particles
		while (part != null) {
			// Get rotation angle
			int theta = Fixed
					.round(Fixed.mul(-angle, Fixed.RADIANS_TO_DEGREES));
			// int cos = Fixed.cos(theta);// OK
			// int sin = Fixed.sin(theta);// Check this
			double anglef = Fixed.fixedToFloat(-angle);
			int cos = Fixed.floatToFixed(Math.cos(anglef));
			int sin = Fixed.floatToFixed(Math.sin(anglef));

			// u = (1, 0)
			int u1 = Fixed.ONE;
			int u2 = 0;

			int z = 0;

			int toMouse_x = part.x - x;
			int toMouse_y = part.y - y;
			// Calculate norm of (toMouse_x, toMouse_y).
			int norm = Fixed.norm(toMouse_x, toMouse_y);

			float toMouse = (float) Math.atan2(toMouse_y, toMouse_x);
			float sens = Fixed.fixedToFloat(angle);
			float h = (float) ((Math.cos(sens) * Math.cos(toMouse)) + (Math
					.sin(sens) * Math.sin(toMouse)));
			int hf = Fixed.floatToFixed(h);

			// Check for zero distance to landmark
			if (norm == 0) {
				System.out.println("Division by zero in compareParticles()");
			} else {
				int toMouse_x_norm = Fixed.div(toMouse_x, norm);
				int toMouse_y_norm = Fixed.div(toMouse_y, norm);
				// rot_p = [cos(theta) -sin(theta); sin(theta)
				// cos(theta)];
				// v=rot_p*toMark
				// After this rotation the landmark vector should point
				// to
				// (1, 0) if the particle has the correct values.
				int v1 = Fixed.mul(toMouse_x_norm, cos)
						+ Fixed.mul(toMouse_y_norm, -sin);
				int v2 = Fixed.mul(toMouse_x_norm, sin)
						+ Fixed.mul(toMouse_y_norm, cos);
				// Inner product between vectors u and v =>
				// cos(angle_diff)
				z = Fixed.mul(v1, u1) + Fixed.mul(v2, u2);

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
			part = (TrackingParticle) data.popFirst();
		}

		// Replace the old list with the new sorted list
		data = newlist;
		// Save weight sum
		sum_w = sum_w_tmp;
		// Check if the sum of weights are zero
		zerosum = (sum_w_tmp == 0);

		// System.out.println("sum_w=" + Fixed.fixedToFloat(sum_w));
		if (data.getLength() != N) {
			System.out.println("Particles lost! (count:" + data.getLength()
					+ ")");
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
		float[] net = billboard.getMeanAndCoveriance();
		// ret = {m_x, m_y, m'_x, m'_y, xx, xy, yy, x'x', x'y', y'y'}
		int[][] C1 = new int[2][2];
		int[][] C2 = new int[2][2];
		if (net[10] == 0) {
			C1[0][0] = Fixed.ONE;
			C1[0][1] = 0;
			C1[1][0] = 0;
			C1[1][1] = Fixed.ONE;
		} else {
			mean_x = Fixed.floatToFixed(net[0]);
			mean_y = Fixed.floatToFixed(net[1]);
			mean_xv = Fixed.floatToFixed(net[2]);
			mean_yv = Fixed.floatToFixed(net[3]);
			// Set up co-variance matrices
			C1[0][0] = Fixed.floatToFixed(net[4]);
			C1[0][1] = Fixed.floatToFixed(net[5]);
			C1[1][0] = Fixed.floatToFixed(net[5]);
			C1[1][1] = Fixed.floatToFixed(net[6]);
		}
		C2[0][0] = Fixed.floatToFixed(net[7]);
		C2[0][1] = Fixed.floatToFixed(net[8]);
		C2[1][0] = Fixed.floatToFixed(net[8]);
		C2[1][1] = Fixed.floatToFixed(net[9]);

		// Get transform matrices for new sampling
		int[][] V1 = ParticleFilter.getTransformFromCovariance(C1);
		int[][] V2 = ParticleFilter.getTransformFromCovariance(C2);

		// Get pointer to first element
		Link link = data.first;

		// Decide on cut off
		if (!zerosum) {
			// Only the worst particles needs to be re-sampled, so some
			// particles can be skipped.

			for (int i = 0; (i < Ncut) && (link != null); i++) {
				link.data.comparable = Nnorm;
				link = link.next;
			}

		}

		// Loop through all particles
		while (link != null) {
			TrackingParticle part = (TrackingParticle) link.data;
			// Get new random space vector (a, b), sample from 2d Gaussian
			// distribution.
			int a = nextRandn();
			int b = nextRandn();
			int c = nextRandn();
			int d = nextRandn();
			// Add mean and transform the vectors (a, b) and (c, d) into the new
			// sample space.
			part.x = mean_x + Fixed.mul(V1[0][0], a) + Fixed.mul(V1[0][1], b);
			part.y = mean_y + Fixed.mul(V1[1][0], a) + Fixed.mul(V1[1][1], b);
			part.xv = mean_xv + Fixed.mul(V2[0][0], c) + Fixed.mul(V2[0][1], d);
			part.yv = mean_yv + Fixed.mul(V2[1][0], c) + Fixed.mul(V2[1][1], d);
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
		System.out.print(id + ": Calculating mean ");
		// Create local vaiables
		int tmean_x = 0, tmean_y = 0, tmean_xv = 0, tmean_yv = 0, norm;
		int mx = 0, my = 0; // Unweighted means
		if (zerosum) {
			System.out.println("(ordinary)");
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
			mx = tmean_x;
			my = tmean_y;
		} else {
			System.out
					.println("(weighted) sum_w: " + Fixed.fixedToFloat(sum_w));
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
				mx += part.x;
				my += part.y;
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
		mx = Fixed.mul(mx, Nnorm);
		my = Fixed.mul(my, Nnorm);
		// Calculate (co-)variances
		if (zerosum) {
			// No old data should be saved and filter knows nothing about the
			// current tracked states. Uncertainty increases with time (standard
			// deviation increases by 20%).
			varXX *= 1.44;
			varYY *= 1.44;
			varXvXv *= 1.44;
			varYvYv *= 1.44;
			// X and Y can be considered as independent if nothing is known.
			varXY = 0;
			varXvYv = 0;
		} else {
			// Create local summation variables
			int tvarXX = 0, tvarXY = 0, tvarYY = 0, tvarXvXv = 0, tvarXvYv = 0, tvarYvYv = 0;
			// Loop through particles
			// Should something like sum(w(x-meanx)(x-meanx)) ..
			Link link = data.first;
			while (link != null) {
				TrackingParticle part = (TrackingParticle) link.data;
				int x = part.x - mx;
				int y = part.y - my;
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
			varXY = Fixed.mul(tvarXY, norm);
			varYY = Fixed.mul(tvarYY, norm);
			varXvXv = Fixed.mul(tvarXvXv, norm);
			varXvYv = Fixed.mul(tvarXvYv, norm);
			varYvYv = Fixed.mul(tvarYvYv, norm);
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
		// Check x and y velocities (always lower than 10 cm/s)
		// TODO: Add limits to velocity
		int vc = Fixed.floatToFixed(0.1);
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
		// Check for min variance
		if (varXX < Fixed.floatToFixed(0.0025)) {
			// 0.0001 => std=1cm
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
		// Limit velocity variance
		int maxCoVar = (int) (.1 * Fixed.min(varXX, varYY));
		if (varXvYv > maxCoVar) {
			varXvYv = (int) (0.9 * maxCoVar);
		}
		if (varXvYv < -maxCoVar) {
			varXvYv = (int) (0.9 * -maxCoVar);
		}
		if (varXvXv < Fixed.floatToFixed(0.0001)) {
			varXvXv = Fixed.floatToFixed(0.0001);
		}
		if (varYvYv < Fixed.floatToFixed(0.0001)) {
			varYvYv = Fixed.floatToFixed(0.0001);
		}
		varXY = 0;
		varXvYv = 0;
		billboard.setMeanAndCoveriance(id, Fixed.fixedToFloat(mean_x), Fixed
				.fixedToFloat(mean_y), Fixed.fixedToFloat(mean_xv), Fixed
				.fixedToFloat(mean_yv), Fixed.fixedToFloat(varXX), Fixed
				.fixedToFloat(varXY), Fixed.fixedToFloat(varYY), Fixed
				.fixedToFloat(varXvXv), Fixed.fixedToFloat(varXvYv), Fixed
				.fixedToFloat(varYvYv), Fixed.fixedToFloat(sum_w));
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

	/*
	 * Draw particles (NOT brick material)
	 */
	public void draw(Graphics g) {
		// TODO: Remove graphics code from filter
		final int size = 4; // Diameter
		int linelength;

		Graphics2D g2 = (Graphics2D) g;

		// Save the current tranform
		AffineTransform oldTransform = g2.getTransform();

		// Rotate and translate the actor
		// g2.rotate(iangle, ix, iy);

		g2.setColor(Color.green);
		// Plot particles
		Link link = data.first;
		while (link != null) {
			TrackingParticle part = (TrackingParticle) link.data;
			int ix = Actor.e2gX(Fixed.fixedToFloat(part.x));
			int iy = Actor.e2gY(Fixed.fixedToFloat(part.y));
			float xv = Fixed.fixedToFloat(part.xv);
			float yv = Fixed.fixedToFloat(part.yv);
			linelength = (int) Fixed.fixedToFloat(Fixed.norm(part.xv, part.yv));
			double iangle = -Math.atan2(yv, xv);
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
		double iangle = -Math.atan2(getYv(), getXv());
		linelength = (int) Math.sqrt(Math.pow(getX(), 2) + Math.pow(getY(), 2));
		g2.fillOval((int) ix - (size / 2), (int) iy - (size / 2), (int) size,
				(int) size);
		g2.drawLine((int) ix, (int) iy, (int) (ix + Math.cos(iangle)
				* linelength), (int) (iy + Math.sin(iangle) * linelength));

		// Reset the transformation matrix
		g2.setTransform(oldTransform);
	}

	public void update() {
		// Get latest sighting
		SightingData sens = null;
		SightingData sens2 = (SightingData) sensorData.pop();
		while (sens2 != null) {
			sens = sens2;
			sens2 = (SightingData) sensorData.pop();
		}
		// Push latest sighting to billboard
		if (sens != null) {
			System.out.println(id + ": Sighting: " + sens);
			billboard.setLatestSighting(id, sens.x, sens.y, sens.angle);
		}

		// Re-sample (loads values from billboard)
		reSample();

		// Get time reference
		currentTime = rttime.getTime();

		// Integrate particles
		integrateParticles(Fixed.floatToFixed(T));

		// Compare sensor data to particles
		float[] sightings = billboard.getLatestSightings();
		// Loop through cats in billboard
		for (int i = 1; i <= billboard.getNoCats(); i++) {
			int x = Fixed.floatToFixed(sightings[(i - 1) * 3]);
			int y = Fixed.floatToFixed(sightings[(i - 1) * 3 + 1]);
			int angle = Fixed.floatToFixed(sightings[(i - 1) * 3 + 2]);
			if (x >= 0) {
				compareParticles(x, y, angle);
			}
		}

		// Calculate mean and (co-)variance, then commit data to billboard
		calcMean();

		// Increase iteration counter and timer (with full execution time)
		iterationCounter++;
		iterationTime += rttime.getTime() - currentTime;
		// Update public time
		lastCurrentTime = currentTime;
	}

	public void run() {
		// TODO: Implement main loop and thread timer
		/*
		 * while (true) { update(); sleep((long) (rttime.getTime() % Tint)); }
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
