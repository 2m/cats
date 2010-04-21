package GSim;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

public class AbsolutePositioningParticleFilter extends
		AbsolutePositioningFilter {

	// TODO: Write javadoc

	/* Linked list for particles */
	private Particle first;
	private Particle last;

	private final int N;
	private final int Ncut;
	private int mean_x;
	private int mean_y;
	private int mean_angle;
	private boolean zerosum;
	private int lastCurrentTime, currentTime;
	private int sum_w;
	private final int[][] landmarks;

	private int[] randn_lut;
	private int randn_index;
	private int RANDN_MASK = 255;

	public AbsolutePositioningParticleFilter(int N, float T, Buffer sensorData,
			Buffer movementData, Arena arena, RealTimeClock rttime,
			LandmarkList landmarklist) {
		super(T, sensorData, movementData, arena, rttime, landmarklist);
		this.N = N;
		// Set up cut-off for survival of the fittest
		Ncut = (int) (N * (1 / 4));
		// Make a local landmark list
		landmarks = new int[4][3];
		for (int i = 0; i < landmarklist.landmarkX.length; i++) {
			landmarks[i][0] = Fixed.floatToFixed(landmarklist.landmarkX[i]);
			landmarks[i][1] = Fixed.floatToFixed(landmarklist.landmarkY[i]);
			if (landmarklist.landmarkC[i]) {
				landmarks[i][2] = LandmarkList.GREEN;
			} else {
				landmarks[i][2] = LandmarkList.RED;
			}
		}
	}

	/**
	 * Create and init all particles
	 */
	private void initParticles() {
		// State variables x, y, angle
		for (int i = 0; i < N; i++) {
			addParticle(0, 0, 0, 0);
		}
		initParticleData();
		// Generate random numbers
		randn_lut = new int[RANDN_MASK + 1];

	}

	/** Add a particle to the data structure */
	private void addParticle(int x, int y, int angle, int w) {
		if (first == null) {
			first = new PositioningParticle(x, y, angle, w, null, null);
		} else {
			Particle ptr = first;
			first = new PositioningParticle(x, y, angle, w, ptr, null);
			ptr.previous = first;
		}
	}

	private void initParticleData() {
		// TODO: Add init data (from arena data or other input?)
		Particle ptr = first;
		while (ptr != null) {
			// ptr.x = 0;
			ptr = ptr.next;
		}
		for (int i = 0; i <= RANDN_MASK; i++) {
			randn_lut[i] = Fixed.randn();
		}
	}

	/**
	 * Move particles in the direction each particle is facing.
	 * 
	 * @param distance
	 *            The distance driven
	 */
	private void integrateParticles(int distance) {
		Particle ptr = first;
		PositioningParticle ptr2;
		while (ptr != null) {
			ptr2 = (PositioningParticle) ptr;
			ptr2.x = ptr2.x + Fixed.mul(Fixed.cos(ptr2.angle), distance);
			ptr2.y = ptr2.y + Fixed.mul(Fixed.sin(ptr2.angle), distance);
			ptr = ptr.next;
		}
	}

	private void turnParticles(int theta) {
		Particle ptr = first;
		PositioningParticle ptr2;
		while (ptr != null) {
			ptr2 = (PositioningParticle) ptr;
			ptr2.angle = ptr2.angle + theta;
			// TODO: Mask high values, depends on circle size
			// TODO: Check for high and low values
			ptr = ptr.next;
		}
	}

	private void compareParticles(int sensorangle) {
		// Compare particles to sensors inputs and sum weights
		int sum_w_tmp = 0;
		Particle ptr = first;
		PositioningParticle ptr2;
		while (ptr != null) {
			ptr2 = (PositioningParticle) ptr;
			int theta = -ptr2.angle - sensorangle;
			int cos = Fixed.cos(theta);
			int sin = Fixed.sin(theta);
			int z = 0;
			// TODO: Loop through landmarks
			// TODO: Get landmark data
			int toMark_x = 0; // landmark_x-x
			int toMark_y = 0; // landmark_y-y
			int norm = Fixed.norm(toMark_x, toMark_y);
			int v1 = toMark_x * cos - toMark_x * sin;
			// int v2 = toMark_y * cos + toMark_y * sin;
			int a = Fixed.div(v1, norm);
			if (a > z) {
				// Optimise if two landmarks can not be a hit, sort landmark
				// list to get hit faster
				z = a;
			}
			// Penalty function
			ptr2.w = ParticleFilter.penalty(z);
			// Sum weights
			sum_w_tmp += ptr2.w;
			ptr = ptr.next;
		}
		sum_w = sum_w_tmp;
		zerosum = (sum_w_tmp == 0);
	}

	private int nextRandn() {
		randn_index++;
		randn_index &= RANDN_MASK;
		return randn_lut[randn_index];
	}

	private void reSample() {
		if (zerosum) {
			// If sum of weights are 0 then a full reinit is needed
			initParticleData();
		} else {
			Particle.quickSort(first, last, first, last, 1, N, Ncut);
			// TODO: Use mean and variance to generate new particles
			for (int i = Ncut; i < N; i++) {
				/*
				 * x[i] = mean_x + Fixed.mul(nextRandn(), std_x); y[i] = mean_y
				 * + Fixed.mul(nextRandn(), std_y); angle[i] = mean_angle +
				 * Fixed.mul(nextRandn(), std_angle);
				 */
			}
			// TODO: Reset weights (all particles equaly posible)
			// int norm = Fixed.div(Fixed.ONE, Fixed.intToFixed(N));
			for (int i = 0; i < N; i++) {
				// weights[i] = norm;
			}
		}
	}

	/**
	 * Calculate mean
	 */
	public void calcMean() {
		int tmean_x = 0, tmean_y = 0, tmean_a = 0, norm;
		if (zerosum) {
			// Ordinary mean
			Particle ptr = first;
			PositioningParticle ptr2;
			while (ptr != null) {
				ptr2 = (PositioningParticle) ptr;
				tmean_x += ptr2.x;
				tmean_y += ptr2.y;
				tmean_a += ptr2.angle;
				ptr = ptr.next;
			}
			norm = Fixed.div(Fixed.ONE, Fixed.intToFixed(N));
		} else {

			// Weighted mean
			Particle ptr = first;
			PositioningParticle ptr2;
			while (ptr != null) {
				ptr2 = (PositioningParticle) ptr;
				tmean_x += Fixed.mul(ptr2.x, ptr2.w);
				tmean_y += Fixed.mul(ptr2.y, ptr2.w);
				tmean_a += Fixed.mul(ptr2.angle, ptr2.w);
				ptr = ptr.next;
			}
			norm = Fixed.div(Fixed.ONE, sum_w);
		}
		mean_x = Fixed.intToFixed(Fixed.mul(tmean_x, norm));
		mean_y = Fixed.intToFixed(Fixed.mul(tmean_y, norm));
		mean_angle = Fixed.intToFixed(Fixed.mul(tmean_a, norm));
		// TODO: Calculate covariance
		// TODO: Check means and covariances
	}

	public int getTime() {
		return lastCurrentTime;
	}

	public float getX() {
		return Fixed.fixedToFloat(mean_x);
	}

	public float getY() {
		return Fixed.fixedToFloat(mean_y);
	}

	public float getAngle() {
		return Fixed.fixedToFloat(mean_angle);
	}

	/*
	 * Draw particles (NOT brick material)
	 */
	public void draw(Graphics g) {
		// TODO: Move graphics code from filter
		final int size = 2; // Diameter
		final int linelength = 5;

		Graphics2D g2 = (Graphics2D) g;

		// Save the current tranform
		AffineTransform oldTransform = g2.getTransform();

		// Rotate and translate the actor
		// g2.rotate(iangle, ix, iy);

		g2.setColor(Color.green);
		// Plot particles
		Particle ptr = first;
		PositioningParticle ptr2;
		while (ptr != null) {
			ptr2 = (PositioningParticle) ptr;
			int ix = Actor.e2gX(Fixed.fixedToFloat(ptr2.x));
			int iy = Actor.e2gY(Fixed.fixedToFloat(ptr2.y));
			double iangle = -Fixed.fixedToFloat(ptr2.angle) / 2;
			ptr = ptr.next;

			g2.fillOval((int) ix - (size / 2), (int) iy - (size / 2),
					(int) size, (int) size);
			g2.drawLine((int) ix, (int) iy, (int) (ix + Math.cos(iangle)
					* linelength), (int) (iy + Math.sin(iangle) * linelength));
		}
		// Plot mean
		g2.setColor(Color.blue);
		int ix = Actor.e2gX(getX());
		int iy = Actor.e2gY(getY());
		double iangle = -getAngle() / 2;
		g2.fillOval((int) ix - (size / 2), (int) iy - (size / 2), (int) size,
				(int) size);
		g2.drawLine((int) ix, (int) iy, (int) (ix + Math.cos(iangle)
				* linelength), (int) (iy + Math.sin(iangle) * linelength));

		// Reset the tranformation matrix
		g2.setTransform(oldTransform);
	}

	public void update() {
		currentTime = rttime.getTime();
		{ // Update and evaluation loop
			// TODO: Read buffers for integration (angles, distance)
			// TODO: Compare with landmarks
			// TODO: Resample if needed
		}
		// TODO: Guarantee a resample
		lastCurrentTime = currentTime;
	}

	public void run() {
		// TODO: Implement main loop and thread timer
		/*
		 * while(true){ update() wait }
		 */
	}
}
