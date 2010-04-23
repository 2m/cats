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
	private int varXX = Fixed.floatToFixed(0.01);
	private int varYY = Fixed.floatToFixed(0.01);
	private int varXY = Fixed.floatToFixed(0.0);
	private int varAngle = Fixed.floatToFixed(3 * (Math.PI / 180));
	private boolean zerosum;
	private int lastCurrentTime, currentTime;
	private int sum_w;
	private final int[][] landmarks;

	private final int[] randn_lut;
	private int randn_index;
	private final int RANDN_MASK = 255;

	public AbsolutePositioningParticleFilter(int N, float T, Buffer sensorData,
			Buffer movementData, RealTimeClock rttime) {
		super(T, sensorData, movementData, rttime);
		this.N = N;
		// Set up cut-off for survival of the fittest
		Ncut = (int) (N * (1 / 4));
		// Make a local landmark list
		landmarks = new int[4][3];
		for (int i = 0; i < LandmarkList.landmarkX.length; i++) {
			landmarks[i][0] = Fixed.floatToFixed(LandmarkList.landmarkX[i]);
			landmarks[i][1] = Fixed.floatToFixed(LandmarkList.landmarkY[i]);
			if (LandmarkList.landmarkC[i]) {
				landmarks[i][2] = LandmarkList.GREEN;
			} else {
				landmarks[i][2] = LandmarkList.RED;
			}
		}
		// Create particles
		for (int i = 0; i < N; i++) {
			if (first == null) {
				first = new PositioningParticle(0, 0, 0, 0, null, null);
			} else {
				Particle ptr = first;
				first = new PositioningParticle(0, 0, 0, 0, ptr, null);
				ptr.previous = first;
			}
		}
		// Init random data
		randn_lut = new int[RANDN_MASK + 1];
		for (int i = 0; i <= RANDN_MASK; i++) {
			randn_lut[i] = Fixed.randn();
		}
	}

	/**
	 * Set initial data in all particles
	 * 
	 * @param x
	 * @param y
	 * @param angle
	 */
	public void initData(float x, float y, float angle) {
		// State variables x, y, angle
		mean_x = Fixed.floatToFixed(x);
		mean_y = Fixed.floatToFixed(y);
		mean_angle = Fixed.floatToFixed(angle);
		reSample();
	}

	private void initParticleData() {
		// TODO: Add init data (from arena data or other input?)
		Particle ptr = first;
		while (ptr != null) {
			// ptr.x = 0;
			ptr = ptr.next;
		}
	}

	/**
	 * Move particles in the direction each particle is facing.
	 * 
	 * @param distance
	 *            The distance driven
	 */
	private void moveParticles(int distance) {
		Particle ptr = first;
		PositioningParticle ptr2;
		while (ptr != null) {
			ptr2 = (PositioningParticle) ptr;
			ptr2.x = ptr2.x + Fixed.mul(Fixed.cos(ptr2.angle), distance);
			ptr2.y = ptr2.y + Fixed.mul(Fixed.sin(ptr2.angle), distance);
			ptr = ptr.next;
		}
		mean_x += Fixed.mul(Fixed.cos(mean_angle), distance);
		mean_y += Fixed.mul(Fixed.sin(mean_angle), distance);
	}

	/**
	 * Turn particles
	 * 
	 * @param theta
	 *            Angle to turn
	 */
	private void turnParticles(int theta) {
		// TODO: Mask high values, depends on circle size
		// TODO: Check for high and low values
		Particle ptr = first;
		PositioningParticle ptr2;
		while (ptr != null) {
			ptr2 = (PositioningParticle) ptr;
			ptr2.angle = ptr2.angle + theta;
			ptr = ptr.next;
		}
		mean_angle += theta;
	}

	private void compareParticles(int sensorangle, int type) {
		// Compare particles to sensors inputs and sum weights
		int sum_w_tmp = 0;
		Particle ptr = first;
		PositioningParticle ptr2;
		int j = 0;
		while (ptr != null) {
			ptr2 = (PositioningParticle) ptr;
			int theta = -ptr2.angle - sensorangle;
			// TODO: Check angle
			int cos = Fixed.cos(theta);
			int sin = Fixed.sin(theta);
			int z = 0;
			// Loop through landmarks
			for (int i = 0; i < LandmarkList.landmarkX.length; i++) {
				if (type == landmarks[j][2]) {
					// TODO: Get landmark data
					int toMark_x = landmarks[j][0] - ptr2.x; // landmark_x-x
					int toMark_y = landmarks[j][1] - ptr2.y; // landmark_y-y
					int norm = Fixed.norm(toMark_x, toMark_y);
					int v1 = toMark_x * cos - toMark_x * sin;
					// int v2 = toMark_y * cos + toMark_y * sin;
					z = Fixed.div(v1, norm);
					/*
					 * int a = Fixed.div(v1, norm); if (a > z) { z = a; }
					 */
					// TODO: Check if sensor can detect different landmarks at
					// one instant
					if (z > ParticleFilter.CUT[5]) {
						break;
					}
				}
				j++;
				j &= LandmarkList.landmarkX.length;
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
		// Calculate covariance
		{
			int tvarXX = 0, tvarXY = 0, tvarYY = 0, tvarAngle = 0;
			Particle ptr = first;
			PositioningParticle ptr2;
			while (ptr != null) {
				ptr2 = (PositioningParticle) ptr;
				int xw = Fixed.mul(ptr2.x, ptr2.w);
				int yw = Fixed.mul(ptr2.y, ptr2.w);
				tvarXX += Fixed.mul(xw, ptr2.x);
				tvarXY += Fixed.mul(xw, ptr2.y);
				tvarYY += Fixed.mul(yw, ptr2.y);
				tvarAngle += Fixed.mul(ptr2.angle, ptr2.w);
				ptr = ptr.next;
			}
			varXX = tvarXX;
			varXY = tvarXY;
			varYY = tvarYY;
			varAngle = tvarAngle;
		}
		// Check means and covariances
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
		// Check for max variance
		if (varXX < Fixed.floatToFixed(0.0001)) {
			varXX = Fixed.floatToFixed(0.0001);
		}
		if (varYY < Fixed.floatToFixed(0.0001)) {
			varYY = Fixed.floatToFixed(0.0001);
		}
		if (varAngle < Fixed.floatToFixed(0.0001)) {
			varAngle = Fixed.floatToFixed(0.0001);
		}
		if (varXX > Fixed.floatToFixed(2)) {
			varXX = Fixed.floatToFixed(2);
		}
		if (varXY > Fixed.floatToFixed(2)) {
			varXY = Fixed.floatToFixed(2);
		}
		if (varYY > Fixed.floatToFixed(2)) {
			varYY = Fixed.floatToFixed(2);
		}
		if (varAngle > Fixed.floatToFixed(90*(Math.PI/180))) {
			varAngle = Fixed.floatToFixed(90*(Math.PI/180));
		}
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
		// Get time reference
		currentTime = rttime.getTime();
		int evaluationsSinceResample = 0;
		int mtime = currentTime + 1;
		int stime = currentTime + 1;
		boolean sdataUsed = false, mdataUsed = false;
		SightingData sdata = (SightingData) sensorData.pop();
		MovementData mdata = (MovementData) movementData.pop();
		while ((sdata != null) || (mdata != null)) {
			// Pops new data, pushes it back if it is too recent
			if (sdataUsed) {
				sdata = (SightingData) sensorData.pop();
				stime = currentTime + 1;
				sdataUsed = false;
			}
			if (sdata != null) {
				if (sdata.timestamp > currentTime) {
					sensorData.push(sdata);
				} else {
					stime = sdata.timestamp;
				}
			}
			if (mdataUsed) {
				mdata = (MovementData) movementData.pop();
				mtime = currentTime + 1;
				mdataUsed = false;
			}
			if (mdata != null) {
				if (mdata.timestamp > currentTime) {
					// TODO: Motor must be sorted buffer
					movementData.push(mdata);
				} else {
					mtime = mdata.timestamp;
				}
			}

			// TODO: Read buffers for integration (angles, distance)
			if ((mdata != null) && ((sdata == null) || (mtime <= stime))) {
				if (Math.abs(mdata.dr) > 0.00001) {
					moveParticles(Fixed.floatToFixed(mdata.dr));
					lastCurrentTime = mdata.timestamp;
				}
				if (Math.abs(mdata.dangle) > 0.00001) {
					// TODO: Turning angle as fixed, how does it work
					turnParticles(Fixed.floatToFixed(mdata.dangle));
					lastCurrentTime = mdata.timestamp;
				}
				mdataUsed = true; // Set flag for popping
			}

			// TODO: Compare with landmarks
			if ((sdata != null) && ((mdata == null) || (stime < mtime))) {
				compareParticles(Fixed.floatToFixed(sdata.angle), sdata.type);
				evaluationsSinceResample++;
				sdataUsed = true; // Set flag for popping
			}
			// Re-sample every third evaluation
			if (evaluationsSinceResample >= 3) {
				calcMean();
				reSample();
				evaluationsSinceResample = 0;
			}
		}
		if (evaluationsSinceResample > 0) {
			calcMean();
			reSample();
		}
		lastCurrentTime = currentTime;
	}

	public void run() {
		// TODO: Implement main loop and thread timer
		/*
		 * while(true){ update() wait(T) }
		 */
	}

	public String toString() {
		Particle ptr = first;
		PositioningParticle ptr2;
		String ret = "[";
		while (ptr != null) {
			ptr2 = (PositioningParticle) ptr;
			ret += ptr2.toString();
			ptr = ptr.next;
		}
		return ret + "]";
	}
}
