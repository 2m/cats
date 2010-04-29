package GSim;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

/** Particle filter for absolute positioning of one cat. */
public class AbsolutePositioningParticleFilter extends
		AbsolutePositioningFilter {

	/** Linked list for particles */
	private Particle first;
	private Particle last;

	private final int N;
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
				first = new PositioningParticle(Fixed.floatToFixed(1.0), Fixed
						.floatToFixed(1.0), 0, 0, null, null);
				last = first;
			} else {
				Particle ptr = first;
				first = new PositioningParticle(Fixed.floatToFixed(1.0), Fixed
						.floatToFixed(1.0), Fixed.floatToFixed(i * 200), Fixed
						.floatToFixed(0), ptr, null);
				ptr.previous = first;
			}
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
		Particle ptr = first;
		PositioningParticle ptr2;
		// System.out.print("Move: " + distance + " - ");
		while (ptr != null) {
			ptr2 = (PositioningParticle) ptr;
			final int a = Fixed.floor(ptr2.angle);
			// System.out.print(a + " ");
			int c = Fixed.cos(a);
			int s = Fixed.sin(a);
			ptr2.x = ptr2.x + Fixed.mul(c, distance);
			ptr2.y = ptr2.y + Fixed.mul(s, distance);
			ptr = ptr.next;
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
		Particle ptr = first;
		PositioningParticle ptr2;
		while (ptr != null) {
			ptr2 = (PositioningParticle) ptr;
			ptr2.angle = ptr2.angle + theta;
			// TODO: Masking needs more work
			ptr2.angle &= Fixed.ANGLE_MASK;
			ptr = ptr.next;
		}
		mean_angle += theta;
		mean_angle &= Fixed.ANGLE_MASK;
	}

	private void compareParticles(int sensorangle, int type) {
		// Compare particles to sensors inputs and sum weights
		int sum_w_tmp = 0;
		Particle ptr = first;
		PositioningParticle ptr2;
		int j = 0;
		while (ptr != null) {
			ptr2 = (PositioningParticle) ptr;
			int theta = Fixed.floor(-ptr2.angle - sensorangle);
			int cos = Fixed.cos(theta);
			int sin = Fixed.sin(theta);
			int z = 0;
			// Loop through landmarks
			for (int i = 0; i < LandmarkList.landmarkX.length; i++) {
				if (type == landmarks[j][2]) {
					int toMark_x = landmarks[j][0] - ptr2.x; // landmark_x-x
					int toMark_y = landmarks[j][1] - ptr2.y; // landmark_y-y
					int norm = Fixed.norm(toMark_x, toMark_y);
					int v1 = toMark_x * cos - toMark_x * sin;
					// int v2 = toMark_y * cos + toMark_y * sin;
					if (norm == 0) {
						System.out.println("Disvision by zero");
						z = 0;
					} else {
						z = Fixed.div(v1, norm);
					}
					/*
					 * int a = Fixed.div(v1, norm); if (a > z) { z = a; }
					 */
					// I assume that a sensor can NOT detect different landmarks
					// at
					// one instant => first landmark found is the correct one.
					if (z > ParticleFilter.CUT[4]) {
						break;
					}
				}
				j++;
				j %= LandmarkList.landmarkX.length;
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
		// Set up co-variance matrix
		int[][] C = new int[2][2];
		C[0][0] = varXX;
		C[0][1] = varXY;
		C[1][0] = varXY;
		C[1][1] = varYY;
		// Weight norm
		int norm = Fixed.floatToFixed(1 / ((float) N));
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
			System.out.print("Quicksort of particles... ");
			quickSort(first, last, 1, N, cut);
			System.out.println("done");
			System.out.println(this.toString());
		}

		Particle ptr = first;
		for (int i = 0; i < cut; i++) {
			ptr.w = norm;
			ptr = ptr.next;
		}

		PositioningParticle ptr2;
		while (ptr != null) {
			ptr2 = (PositioningParticle) ptr;
			int a = nextRandn();
			int b = nextRandn();
			int c = nextRandn();
			System.out.println(Fixed.fixedToFloat(a));
			ptr2.x = mean_x + Fixed.mul(V[0][0], a) + Fixed.mul(V[0][1], b);
			ptr2.y = mean_y + Fixed.mul(V[1][0], a) + Fixed.mul(V[1][1], b);
			ptr2.angle = mean_angle + Fixed.mul(varAngle, c);
			ptr.w = norm;
			ptr = ptr.next;
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
			Particle ptr = first;
			PositioningParticle ptr2;
			while (ptr != null) {
				ptr2 = (PositioningParticle) ptr;
				tmean_x += ptr2.x;
				tmean_y += ptr2.y;
				tmean_a += ptr2.angle;
				ptr = ptr.next;
			}
			norm = Fixed.floatToFixed(1 / ((float) N));
		} else {
			System.out.println("(weighted)");
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
		mean_x = Fixed.mul(tmean_x, norm);
		mean_y = Fixed.mul(tmean_y, norm);
		mean_angle = Fixed.mul(tmean_a, norm);

		// Calculate covariance
		if (!zerosum) {
			int tvarXX = 0, tvarXY = 0, tvarYY = 0, tvarAngle = 0;
			Particle ptr = first;
			PositioningParticle ptr2;
			while (ptr != null) {
				ptr2 = (PositioningParticle) ptr;
				int x = ptr2.x - mean_x;
				int y = ptr2.y - mean_y;
				int xw = Fixed.mul(x, ptr2.w);
				int yw = Fixed.mul(y, ptr2.w);
				tvarXX += Fixed.mul(xw, x);
				tvarXY += Fixed.mul(xw, y);
				tvarYY += Fixed.mul(yw, y);
				tvarAngle += Fixed.mul(ptr2.angle - mean_angle, ptr2.w);
				ptr = ptr.next;
			}
			// Normalise
			varXX = Fixed.mul(tvarXX, norm);
			varXY = Fixed.mul(tvarXY, norm);
			varYY = Fixed.mul(tvarYY, norm);
			varAngle = tvarAngle;
		} else {
			varXX = Fixed.ONE;
			varXY = 0;
			varYY = Fixed.ONE;
			varAngle = Fixed.ONE;
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

		/*
		 * if (varXX < Fixed.floatToFixed(0.0001)) { varXX =
		 * Fixed.floatToFixed(0.0001); } if (varYY < Fixed.floatToFixed(0.0001))
		 * { varYY = Fixed.floatToFixed(0.0001); } if (varAngle <
		 * Fixed.floatToFixed(0.0001)) { varAngle = Fixed.floatToFixed(0.0001);
		 * } if (varXX > Fixed.floatToFixed(2)) { varXX = Fixed.floatToFixed(2);
		 * } if (varYY > Fixed.floatToFixed(2)) { varYY = Fixed.floatToFixed(2);
		 * } if (varAngle > Fixed.floatToFixed(90 * (Fixed.DEGREES / 360))) {
		 * varAngle = Fixed.floatToFixed(90 * (Fixed.DEGREES / 360)); }
		 */
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
		Particle ptr = first;
		PositioningParticle ptr2;
		while (ptr != null) {
			ptr2 = (PositioningParticle) ptr;
			int ix = Actor.e2gX(Fixed.fixedToFloat(ptr2.x));
			int iy = Actor.e2gY(Fixed.fixedToFloat(ptr2.y));
			double iangle = -Fixed.fixedToFloat(ptr2.angle)
					* (2 * Math.PI / Fixed.DEGREES);
			/*
			 * System.out.println(ptr2.angle + ": " +
			 * Fixed.fixedToFloat(ptr2.angle) + ": " + iangle);
			 */
			g2.fillOval((int) ix - (size / 2), (int) iy - (size / 2),
					(int) size, (int) size);
			g2.drawLine((int) ix, (int) iy, (int) (ix + Math.cos(iangle)
					* linelength), (int) (iy + Math.sin(iangle) * linelength));
			ptr = ptr.next;
		}
		// Plot mean
		g2.setColor(Color.blue);
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
				compareParticles(Fixed.mul(Fixed.floatToFixed(sdata.angle),
						Fixed.RADIANS_TO_DEGREES), sdata.type);
				evaluationsSinceResample++;
				sdataUsed = true; // Set flag for popping
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
		Particle ptr = first;
		PositioningParticle ptr2;
		String ret = "[";
		while (ptr != null) {
			ptr2 = (PositioningParticle) ptr;
			ret += ptr2.toString();
			ptr = ptr.next;
			if (ptr != null) {
				ret += ",\n ";
			}
		}
		return ret + "]";
	}

	public void quickSort(Particle Ptr1, Particle Ptr2, int N1, int N2, int Ncut) {
		// System.out.println("QuickSort: start");
		// Sort from highest to lowest, Ptr1 to Ptr2
		// TODO: Use cut off in sorter
		// if ((N1 < Ncut) && (Ncut < N2)) {
		if (Ptr1 == Ptr2) {
			// End case
		} else if (Ptr1.next == Ptr2) {
			// End case
			if (Ptr1.w < Ptr2.w) {
				swapParticle2(Ptr1, Ptr2);
			}
		} else {
			// Extract pivot value
			Particle ptr1 = Ptr1;
			Particle ptr2 = Ptr2;
			Particle pivot = Ptr2.previous;
			Ptr2.previous = pivot.previous;
			pivot.previous.next = Ptr2;
			pivot.next = null;
			pivot.previous = null;

			Particle tmpptr = null;
			int n2 = N2;
			int n1 = N1;
			while (ptr1 != ptr2) {
				// System.out.println("QuickSort: loop " + Ncut);
				if (Ncut >= 20) {
					Ncut = 0;
				}
				if ((ptr1.w < pivot.w) && (ptr2.w > pivot.w)) {
					swapParticle2(ptr1, ptr2);
					/*
					 * swapParticle(ptr1, ptr2); tmpptr = ptr1; ptr1 = ptr2;
					 * ptr2 = tmpptr;
					 */
				}
				if (ptr1.w >= pivot.w) {
					ptr1 = ptr1.next;
					n1++;
				}
				if ((ptr2.w < pivot.w) && (ptr1 != ptr2)) {
					ptr2 = ptr2.previous;
					n2--;
				}
			}
			// Where to insert the pivot?
			if (ptr1.w >= pivot.w) {
				// Insert pivot after ptr1
				ptr2 = ptr1.next;
				n2++;
			} else {
				// Insert pivot before ptr1
				ptr2 = ptr1;
				ptr1 = ptr1.previous;
				n1--;
			}
			// Check if pointers are on the ends of the list
			if (ptr1 == null) {
				// Insert first
				pivot.next = ptr2;
				pivot.previous = null;
				first = pivot;
				ptr2.previous = pivot;
				quickSort(first.next, Ptr2, 2, N2, Ncut + 1);
			} else if (ptr2 == null) {
				// Insert last
				pivot.next = null;
				pivot.previous = ptr1;
				ptr1.next = pivot;
				last = pivot;
				quickSort(Ptr1, last.previous, N1, N - 1, Ncut + 1);
			} else {
				// Insert inside list but might be at Prt1 or Ptr2
				pivot.previous = ptr1;
				pivot.next = ptr2;
				ptr1.next = pivot;
				ptr2.previous = pivot;
				if (Ptr1 != pivot) {
					quickSort(Ptr1, pivot.previous, N1, n1, Ncut + 1);
				}
				if (Ptr2 != pivot) {
					quickSort(pivot.next, Ptr2, n2, N2, Ncut + 1);
				}
			}

		}
		// }
	}

	public void swapParticle2(Particle a, Particle b) {
		// Hack, but this might be faster
		int t = a.x;
		a.x = b.x;
		b.x = t;
		t = a.y;
		a.y = b.y;
		b.y = t;
		t = a.w;
		a.w = b.w;
		b.w = t;
		t = ((PositioningParticle) a).angle;
		((PositioningParticle) a).angle = ((PositioningParticle) b).angle;
		((PositioningParticle) b).angle = t;
	}

	/** Swap two particles in the data structure */
	public void swapParticle(Particle a, Particle b) {
		Particle ptr_a_prev = a.previous;
		Particle ptr_a_next = a.next;
		Particle ptr_b_prev = b.previous;
		Particle ptr_b_next = b.next;

		if (a == first) {
			// a is first element
			first = b;
			b.previous = null;
		} else {
			ptr_a_prev.next = b;
		}
		if (a == last) {
			// a is last element
			last = b;
			b.next = null;
		} else {
			ptr_a_next.previous = b;
		}

		if (b == first) {
			// b is first element
			first = a;
			a.previous = null;
		} else {
			ptr_b_prev.next = a;
		}
		if (b == last) {
			// b is last element
			last = a;
			a.next = null;
		} else {
			ptr_b_next.previous = a;
		}

		a.previous = ptr_b_prev;
		a.next = ptr_b_next;
		b.previous = ptr_a_prev;
		b.next = ptr_a_next;
	}

}
