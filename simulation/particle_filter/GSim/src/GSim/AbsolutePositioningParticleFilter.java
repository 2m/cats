package GSim;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

/** Particle filter for absolute positioning of one cat. */
public class AbsolutePositioningParticleFilter extends
		AbsolutePositioningFilter {

	/** Linked list for particles */
	private LinkedList data;

	private final int N;
	private final int Nnorm;
	private final int Ncut;

	private int mean_x;
	private int mean_y;
	private int mean_angle;
	private int varXX = Fixed.floatToFixed(0.0001);
	private int varYY = Fixed.floatToFixed(0.0001);
	private int varXY = Fixed.floatToFixed(0.0);
	private int varAngle = Fixed
			.floatToFixed(3 * (2 * Math.PI / Fixed.DEGREES));
	private boolean zerosum;
	private int lastCurrentTime, currentTime;
	private int sum_w;
	private final int[][] landmarks;

	private final int[] randn_lut;
	private int randn_index;
	private final int RANDN_MASK = 511;

	public AbsolutePositioningParticleFilter(int N, float T, Buffer sensorData,
			Buffer movementData, RealTimeClock rttime) {
		super(T, sensorData, movementData, rttime);
		this.N = N;
		Nnorm = Fixed.floatToFixed(1 / ((float) N));
		Ncut = (N >> 2);
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
		data = new LinkedList();
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
	 * Set initial data means and then re-sample.
	 * 
	 * @param x
	 * @param y
	 * @param angle
	 */
	public void initData(float x, float y, float angle) {
		// Set means for x, y and angle
		mean_x = Fixed.floatToFixed(x);
		mean_y = Fixed.floatToFixed(y);
		mean_angle = Fixed.floatToFixed(angle);
		varXX = Fixed.floatToFixed(0.0001);
		varYY = Fixed.floatToFixed(0.0001);
		varXY = Fixed.floatToFixed(0.0);
		varAngle = Fixed.floatToFixed(3 * (2 * Math.PI / Fixed.DEGREES));
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
		Link link = data.first;
		// System.out.print("Move: " + distance + " - ");
		while (link != null) {
			PositioningParticle part = (PositioningParticle) link.data;
			final int a = Fixed.floor(part.angle);
			// System.out.print(a + " ");
			int c = Fixed.cos(a);
			int s = Fixed.sin(a);
			part.x = part.x + Fixed.mul(c, distance);
			part.y = part.y + Fixed.mul(s, distance);
			link = link.next;
		}
		// System.out.println("");
		mean_x += Fixed.mul(Fixed.cos(Fixed.floor(mean_angle)), distance);
		mean_y += Fixed.mul(Fixed.sin(Fixed.floor(mean_angle)), distance);
	}

	/**
	 * Turn particles
	 * 
	 * @param theta
	 *            Angle to turn
	 */
	private void turnParticles(int theta) {
		Link link = data.first;
		while (link != null) {
			PositioningParticle part = (PositioningParticle) link.data;
			part.angle = part.angle + theta;
			part.angle &= Fixed.ANGLE_MASK;
			link = link.next;
		}
		mean_angle += theta;
		mean_angle &= Fixed.ANGLE_MASK;
	}

	private void compareParticles(float sensorangle, int type) {
		// Compare particles to sensors inputs and sum weights
		int sum_w_tmp = 0;
		LinkedList newlist = new LinkedList();
		PositioningParticle part = (PositioningParticle) data.popFirst();
		while (part != null) {
			// TODO: Check strange convergence, if theta is changed the particle
			// still converge but in other places.
			int theta = Fixed.round(-part.angle
					- Fixed.mul(Fixed.floatToFixed(sensorangle),
							Fixed.RADIANS_TO_DEGREES));
			int cos = Fixed.cos(theta);
			int sin = Fixed.sin(theta);
			int z = 0;
			int a;
			// Loop through landmarks
			for (int i = 0; i < LandmarkList.landmarkX.length; i++) {
				if (type == landmarks[i][2]) {
					int toMark_x = landmarks[i][0] - part.x; // landmark_x-x
					int toMark_y = landmarks[i][1] - part.y; // landmark_y-y
					int norm = Fixed.norm(toMark_x, toMark_y);
					if (norm == 0) {
						// Zero distance to landmark
						System.out
								.println("Disvision by zero in compareParticles()");
						a = 0;
					} else {
						int v1 = Fixed.mul(toMark_x, cos)
								- Fixed.mul(toMark_y, sin);
						a = Fixed.div(v1, norm);
					}
					if (a > z) {
						z = a;
					}
					/*
					 * if (z > ParticleFilter.CUT[4]) { // Break loop if
					 * comparison is a hit break; }
					 */
				}
			}
			// Penalty function
			int w = ParticleFilter.penalty(z);
			part.w = w;
			// Sum weights
			sum_w_tmp += w;
			// Insert particle into new list
			newlist.insertSorted(part);
			part = (PositioningParticle) data.popFirst();
		}
		// TODO: free(data)
		data = newlist;
		// System.out.println(toString());
		sum_w = sum_w_tmp;
		zerosum = (sum_w_tmp == 0);
		if (data.length() != N) {
			System.out.println("Particles lost! (count:" + data.length() + ")");
		}
	}

	private int nextRandn() {
		randn_index++;
		randn_index &= RANDN_MASK;
		return randn_lut[randn_index];
	}

	private void reSample() {
		// Set up co-variance matrix
		int[][] C = new int[2][2];
		C[0][0] = varXX;
		C[0][1] = varXY;
		C[1][0] = varXY;
		C[1][1] = varYY;
		// Weight norm
		int norm = Nnorm;
		// Get transform matrix
		int[][] V = ParticleFilter.getTransformFromCovariance(C);
		// Decide on cut off
		int cut = 0;
		if (zerosum) {
			// If sum of weights are 0 then a full reinit is needed
			cut = 0;
		} else {
			// Only re-sample the worst particles
			cut = Ncut;
			// TODO: Stop full re-sampling
			cut = 0;
		}

		Link link = data.first;
		for (int i = 0; (i < cut) && (link != null); i++) {
			link.data.w = norm;
			link = link.next;
		}

		while (link != null) {
			PositioningParticle part = (PositioningParticle) link.data;
			int a = nextRandn();
			int b = nextRandn();
			part.x = mean_x + Fixed.mul(V[0][0], a) + Fixed.mul(V[0][1], b);
			part.y = mean_y + Fixed.mul(V[1][0], a) + Fixed.mul(V[1][1], b);
			int c = nextRandn();
			part.angle = mean_angle + Fixed.mul(varAngle, c);
			part.w = norm;
			link = link.next;
		}
	}

	/**
	 * Calculate mean
	 */
	public void calcMean() {
		System.out.print("Calculating mean ");
		int tmean_x = 0, tmean_y = 0, tmean_a = 0, norm;
		if (zerosum) {
			System.out.println("(ordinary)");
			// Ordinary mean
			Link link = data.first;
			while (link != null) {
				PositioningParticle part = (PositioningParticle) link.data;
				tmean_x += part.x;
				tmean_y += part.y;
				tmean_a += part.angle;
				link = link.next;
			}
			norm = Nnorm;
		} else {
			System.out.println("(weighted) sum_w: " + sum_w);
			// Weighted mean
			Link link = data.first;
			while (link != null) {
				// TODO: Does this actually calculate the weighted mean
				PositioningParticle part = (PositioningParticle) link.data;
				tmean_x += Fixed.mul(part.x, part.w);
				tmean_y += Fixed.mul(part.y, part.w);
				// TODO: Check angle mean
				tmean_a += Fixed.mul(part.angle, part.w);
				link = link.next;
			}
			norm = Fixed.div(Fixed.ONE, sum_w);
		}
		mean_x = Fixed.mul(tmean_x, norm);
		mean_y = Fixed.mul(tmean_y, norm);
		mean_angle = Fixed.mul(tmean_a, norm);

		// Calculate covariance
		if (zerosum) {
			varXX = Fixed.HALF;
			varXY = 0;
			varYY = Fixed.HALF;
			varAngle = Fixed.ONE * Fixed.QUARTER_CIRCLE;
		} else {
			int tvarXX = 0, tvarXY = 0, tvarYY = 0, tvarAngle = 0;
			Link link = data.first;
			while (link != null) {
				PositioningParticle part = (PositioningParticle) link.data;
				int x = part.x - mean_x;
				int y = part.y - mean_y;
				int xw = Fixed.mul(x, part.w);
				int yw = Fixed.mul(y, part.w);
				tvarXX += Fixed.mul(xw, x);
				tvarXY += Fixed.mul(xw, y);
				tvarYY += Fixed.mul(yw, y);
				// TODO: Check circle errors in mean
				tvarAngle += Fixed.mul(part.angle - mean_angle, part.w);
				link = link.next;
			}
			// Normalise
			varXX = Fixed.mul(tvarXX, norm);
			varXY = Fixed.mul(tvarXY, norm);
			varYY = Fixed.mul(tvarYY, norm);
			// TODO: Returns zero, why?
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
		if (varYY > Fixed.floatToFixed(2)) {
			varYY = Fixed.floatToFixed(2);
		}
		if (varAngle > Fixed.floatToFixed(90 * (Fixed.DEGREES / 360))) {
			varAngle = Fixed.floatToFixed(90 * (Fixed.DEGREES / 360));
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
			double iangle = -Fixed.fixedToFloat(part.angle)
					* (2 * Math.PI / Fixed.DEGREES);
			/*
			 * System.out.println(ptr2.angle + ": " +
			 * Fixed.fixedToFloat(ptr2.angle) + ": " + iangle);
			 */
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
		double iangle = -getAngle() * (2 * Math.PI / Fixed.DEGREES);
		g2.fillOval((int) ix - (size / 2), (int) iy - (size / 2), (int) size,
				(int) size);
		g2.drawLine((int) ix, (int) iy, (int) (ix + Math.cos(iangle)
				* linelength), (int) (iy + Math.sin(iangle) * linelength));

		// Reset the transformation matrix
		g2.setTransform(oldTransform);
	}

	public void update() {
		// System.out.println("Update");
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
					movementData.push(mdata);
				} else {
					mtime = mdata.timestamp;
				}
			}
			// Read buffers for integration (angles, distance)
			if ((mdata != null) && ((sdata == null) || (mtime <= stime))) {
				// System.out.println("Integrate");
				if (Math.abs(mdata.dr) > 0.00001) {
					// Move particles
					moveParticles(Fixed.floatToFixed(mdata.dr));
					lastCurrentTime = mdata.timestamp;
				}
				if (Math.abs(mdata.dangle) > 0.00001) {
					// Turning angle as fixed
					turnParticles(Fixed.mul(Fixed.floatToFixed(-mdata.dangle),
							Fixed.RADIANS_TO_DEGREES));
					lastCurrentTime = mdata.timestamp;
				}
				// Set flag for popping
				mdataUsed = true;
			}

			// Compare with landmarks
			if ((sdata != null) && ((mdata == null) || (stime < mtime))) {
				compareParticles(sdata.angle, sdata.type);
				evaluationsSinceResample++;
				// Set flag for popping
				sdataUsed = true;
			}
			// Re-sample every n:th evaluation
			if (evaluationsSinceResample >= 3) {
				calcMean();
				reSample();
				evaluationsSinceResample = 0;
			}
		}
		if (evaluationsSinceResample > 0) {
			calcMean();
			reSample();
			evaluationsSinceResample = 0;
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
		String ret = "[";
		Link link = data.first;
		while (link != null) {
			PositioningParticle part = (PositioningParticle) link.data;
			ret += part.toString();
			link = link.next;
			if (link != null) {
				ret += ",\n ";
			}
		}
		return ret + "]";
	}
}
