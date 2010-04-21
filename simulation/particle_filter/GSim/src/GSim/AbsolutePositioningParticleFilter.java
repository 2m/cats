package GSim;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

public class AbsolutePositioningParticleFilter extends AbsolutePositioningFilter {

	// TODO: Write javadoc
	
	/* Particles */
	private int x[];
	private int y[];
	private int angle[];
	private int[] weights;

	/* Linked list for particles */
	private Particle first;
	private Particle last;

	private final int N;
	private final int Ncut;
	private int mean_x;
	private int mean_y;
	private int mean_angle;
	private boolean zerosum;
	private int currentTime;
	
	private int[] randn_lut;
	private int randn_index;
	private int RANDN_MASK = 255; 
	
	public AbsolutePositioningParticleFilter(int N, float T, Buffer sensorData, Buffer movementData, Arena arena, RealTimeClock rttime, LandmarkList landmarks) {
		super(T, sensorData, movementData, arena, rttime, landmarks);
		this.N = N;
		/* Set up cut-off for survival of the fittest */
		Ncut = (int) (N*(1/4));
	}

	/**
	 * Create and init all particles
	 */
	private void initParticles() {
		// State variables x, y, angle
		for(int i=0;i<N;i++) {
			addParticle(0, 0, 0, 0);
		}
		initParticleData();
		// Generate random numbers
		randn_lut = new int[RANDN_MASK + 1];

	}

	/** Add a particle to the daa structure */
	private addParticle(int x, int y, int angle, int w) {
		if (first==null) {
			first = new Particle(x, y, angle, w, null, null);
		} else {
			Particle ptr = first;
			first = new Particle(x, y, angle, w, ptr, null);
			ptr.previus = first;
		}
	}

	/** Swap two particles in the data structure */
	private swapParticle(Particle a, Particle b) {
		Particle ptr_a1 = a.previous;
		Particle ptr_a2 = a.next;
		Particle ptr_b1 = b.previous;
		Particle ptr_b2 = b.next;
		a.previous = ptr_b1;
		a.next = ptr_b2;
		b.previous = ptr_a1;
		b.next = ptr_a2;
		if (prt_a1==null) {
			// a is first element
			first = b;
		} else {
			ptr_a1.next = b;
		}
		if (prt_a2==null) {
			// a is last element
			last = b;
		} else {
			ptr_a2.previous = b;
		}
		if (prt_b1==null) {
			// b is first element
			first = a;
		} else {
			ptr_b1.next = a;
		}
		if (prt_b2==null) {
			// b is last element
			last = a;
		} else {
			ptr_b2.previous = a;
		}
	}

	/** Move particle node a to the place between b and c. */
	private moveParticle(Particle a, Particle b, Particle c) {
	}

	private quickSort(Particle Ptr1, Particle Ptr2, int N1, int N2) {
		if (N1<Ncut) && (Ncut<N2) {
		int pivot = Ptr2;
		Particle ptr2 = Ptr2.previous;
		Particle ptr1 = Ptr1;
		n2 = N2 - 1;
		n1 = N1;
		int pivotw = pivot.w;
		while(ptr1!=ptr2) {
			if (ptr1.w<pivotw) {
				ptr1 = ptr1.next;
				n1++;
			}else if (ptr2.w>pivotw) {
				ptr2 = ptr2.previous;
				n2--;
			} else if (ptr1.w>pivotw) && (ptr2.w<pivotw) {
				swapParticle(ptr1, ptr2);
			}
			
		}
		moveParticle(pivot, ptr1, ptr2);
		quickSort(Ptr1, ptr1, N1, n1);
		quickSort(ptr2, Ptr2, n2, N2);
	}}
	
	private void initParticleData() {
		// TODO: Add init data (from arena data or other input?)
		Particle ptr = first;
		while (ptr!=null) {
			//ptr.x = 0;
			ptr = ptr.next;
		}
		for(int i=0;i<=RANDN_MASK;i++){
			randn_lut[i] = Fixed.randn();
		}
	}
	
/**
 * Move particles in the direction each particle is facing.
 * @param distance The distance driven
 */
	private void integrateParticles(int distance) {
		for(int i=0;i<N;i++) {
			x[i] = x[i] + Fixed.mul(Fixed.cos(angle[i]), distance);
			y[i] = y[i] + Fixed.mul(Fixed.sin(angle[i]), distance);
		}
	}

	private void turnParticles(int theta){
		for(int i=0;i<N;i++) {
			angle[i] = angle[i] + theta;
			// TODO: Mask high values, depends on circle size
		}
	}
	
	private void compareParticles(int sensorangle) {
		// Compare particles to sensors inputs
		for(int i=0;i<N;i++) {
			int theta = -angle[i] - sensorangle;
			int cos = Fixed.cos(theta);
			int sin = Fixed.sin(theta);
			int z = 0;
			// TODO: Loop through landmarks
			// TODO: Get landmark data
			int toMark_x = 0; // landmark_x-x
			int toMark_y = 0; // landmark_y-y
			int norm = Fixed.norm(toMark_x, toMark_y);
			int v1 = toMark_x * cos - toMark_x * sin;
			//int v2 = toMark_y * cos + toMark_y * sin;
			int a = Fixed.div(v1, norm);
			if (a>z) {
				// Optimise if two landmarks can not be a hit, sort landmark list to get hit faster
				z = a;
			}
			// Penalty function
			weights[i] = ParticleFilter.penalty(z);
			// End landmark loop
		}
			/*mu=sensor angle
		 * u = [cos(0); sin(0)];	% Sensor vector
		*for g1 = 1:N
		*	theta = -p(g1, 3) - mu(g);
		*	rot_p = [cos(theta) -sin(theta); ...
		*		sin(theta) cos(theta)];
		*	% Vector towards landmark in ??
		*	for g2 = 1:no_landmarks
		*		v=rot_p*[real(landmarks(g))-p(g1,1) ;...
		*			imag(landmarks(g)) - p(g1, 2)];
		*		v = v./norm(v);
		*		%a = u'*v;
		*		a = v(1);	% Assumes u=[1; 0]
		*		if (a>z(g1))
		*			z(g1) = a;
		*		end
		*	end
		*end
		**/
}

	private int nextRandn() {
		randn_index++;
		randn_index &= RANDN_MASK;
		return randn_lut[randn_index];
	}
	
	private void reSample() {
		if (zerosum){
			// If sum of weights are 0 then a full reinit is needed
			initParticleData();
		}else{
		quickSort(first, last, 1, N);
		// TODO: Use mean and variance to generate new particles
		for(int i=Nlc;i<N;i++) {
			/*x[i] = mean_x + Fixed.mul(nextRandn(), std_x);
			y[i] = mean_y + Fixed.mul(nextRandn(), std_y);
			angle[i] = mean_angle + Fixed.mul(nextRandn(), std_angle);*/
		}
		// TODO: Reset weights (all particles equaly posible)
		int norm = Fixed.div(Fixed.ONE, Fixed.intToFixed(N));
		for(int i=0;i<N;i++) {
			weights[i] = norm;
		}
	}}

	/**
	 *  Calculate mean
	 */	
	public void calcMean(){
		int tmean_x=0, tmean_y=0, tmean_a=0;	// Temporary variables
		int sum_w = 0;
		int norm;
		for(int i=0;i<N;i++) {
			sum_w += weights[i];
		}		
		if (sum_w==0) {
			zerosum = true;
			// Ordinary mean
			for(int i=0;i<N;i++) {
				tmean_x += x[i];
				tmean_y += y[i];
				tmean_a += angle[i];
			}
			norm = Fixed.div(Fixed.ONE, Fixed.intToFixed(N));
		}else{
			zerosum = false;
			// Weighted mean
			for(int i=0;i<N;i++) {
				tmean_x += Fixed.mul(x[i], weights[i]);
				tmean_y += Fixed.mul(y[i], weights[i]);
				// TODO: Look at ways to account for circular vlues in angle
				tmean_a += Fixed.mul(angle[i], weights[i]);
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
		return currentTime;
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
		final int size = 2; // Diameter
		final int linelength = 5;

		Graphics2D g2 = (Graphics2D) g;

		// Save the current tranform
		AffineTransform oldTransform = g2.getTransform();

		// Rotate and translate the actor
		//g2.rotate(iangle, ix, iy);

		g2.setColor(Color.green);
		for(int i=0;i<N;i++) {
			int ix = Actor.e2gX(Fixed.fixedToFloat(x[i]));
			int iy = Actor.e2gY(Fixed.fixedToFloat(y[i]));
			double iangle = -Fixed.fixedToFloat(angle[i]) / 2;
		g2.fillOval((int) ix - (size / 2), (int) iy - (size / 2), (int) size,
				(int) size);
		g2.drawLine((int) ix, (int) iy, (int) (ix + Math.cos(iangle)
					* linelength), (int) (iy + Math.sin(iangle) * linelength));
		}
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
	
	public void run() {
		// TODO: Implement main loop
		// Read time
		currentTime = rttime.getTime();
		// Update and evaluation loop
		// TODO: Read buffers for integration (angles, distance) 
		// TODO: Compare with landmarks
		// TODO: Resample if needed
		// TODO: Guarantee a resample
		// Check timers
		// Wait
	}
}
/*

loop
	% Compare particles to sensors inputs
	landmark_seen = [];
	mu = [];
	re_sample = 0;
	vision_angle = abs(angle((landmarks - cat)*looking'))*(180/pi);
	mu = angle((landmarks - cat)*looking') + randn(size(landmarks))*sensor_std;	% Sensor readings
	% Input from sensor (angle in radians from forward direction)
	% Go through landmarks
	for g = 1:no_landmarks
		% Check if landmark is seen
		if (vision_angle(g) < (vision/2)) || (init_particles)
			re_sample = 1;			% Flag re-sampling
			landmark_seen(end + 1) = g;	% Add landmark to list
			z = ones(N, 1)*(-1);		% Make room
			% --- Filter implementation on robot ---

			u = [cos(0); sin(0)];	% Sensor vector
			for g1 = 1:N
				theta = -p(g1, 3) - mu(g);
				rot_p = [cos(theta) -sin(theta); ...
					sin(theta) cos(theta)];
				% Vector towards landmark in ??
				for g2 = 1:no_landmarks
					v=rot_p*[real(landmarks(g))-p(g1,1) ;...
						imag(landmarks(g)) - p(g1, 2)];
					v = v./norm(v);
					%a = u'*v;
					a = v(1);	% Assumes u=[1; 0]
					if (a>z(g1))
						z(g1) = a;
					end
				end
			end

			% Penalty function
			for g1 = 1:N
				w(g1) = penalty(z(g1));
			end

			% --- end ---

			% Update weights
			p(:, 4) = p(:, 4) .* w;
		end
	end


*/

class Particle {
	public int x;
	public int y;
	public int angle;
	public int w;
	public Particle next;
	public Particle previous;

	public Particle(int x, int y, int angle, int w, Particle next, Particle previous) {
		this.x = x;
		this.y = y;
		this.angle = angle;
		this.w = w;
		this.next = next;
		this.previous = previous;
	}
}
