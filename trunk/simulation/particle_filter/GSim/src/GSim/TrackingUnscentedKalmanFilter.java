package GSim;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import lejos.util.Matrix;
import static java.lang.Math.*;
import static GSim.Matlab.*;

/** Unscented Kalman filter for tracking (a mouse). */
public class TrackingUnscentedKalmanFilter extends TrackingFilter
{
	//Instance variables
	/** Unscented Kalman Filter */
	private UnscentedKalmanFilter ufk_filter;
	
	/** nonlinear state equations */
	private IFunction f;
	
	/** estimated state vector, (n)x(1) Matrix, of the mouse containing
	 * x, y, vx, vy . (aka 'x'). */
	private Matrix states;
	
	/** state covariance */
	private Matrix P;
	
	/** measurement equation */
	private IFunction h;
	
	/** measurments (aka 'z') */
	private Matrix measurments;
	
	/** covariance of process for the mouse */
	private Matrix Q; 
	
	/** covariance of measurement of the mouse */
	private Matrix R;
	
	/** std of expected measurement noise for the mouse TODO: update comment(for bearing angle, x, y, orient., cam.ang respectivly)*/
	private double[] std_array;
	private Matrix r; //TODO never used ? remove ?
	
	/** Varible for time */
	private int lastCurrentTime, currentTime;
	
	/** Counter and timer too keep track of mean iteration execution time */
	private int iterationCounter = 0;
	private int iterationTime = 0;
	
	/** number of landmarks */
	private int numberOfLandmarks;
	
	private float large = (float)pow(10,10);
	
	/**Toggle debug info*/
	private final boolean DEBUG = true;


	/**
	 * Constructor of the tracking filter.
	 * 
	 * @param T
	 *            Period time
	 * @param sensorData
	 *            Buffer with sensor readings
	 * @param rttime
	 *            RealTimeClock
	 * @param billboard
	 *            Shared network data object
	 */
	public TrackingUnscentedKalmanFilter(int id, float T, Buffer sensorData,
			 BillBoard billboard) 
	{
		// Call constructor of super class
		super(id, T, sensorData, billboard);
		
		int nz = billboard.NUMBER_OF_CATS;  //number of cats
		int nx = 4;  //number of variables in the mouse's state vector
		
		ufk_filter = new UnscentedKalmanFilter(nx,nz); 
		float dt = T;//1f;  //sampling period
		float q = 0.005f;  //std of expected process noise for the mouse
		float stddegrees = 2;
		std_array = new double[]{stddegrees*(PI/180)};
		double[][] r_temp = {std_array};
		r = new Matrix(r_temp);  //std of expected measurement noise for the mouse
		double[][] temp_Q = {{pow(dt, 4)/4.0, 0.0,            pow(dt, 3)/2.0, 0.0            },
							{0.0,             pow(dt, 4)/4.0, 0.0,            pow(dt, 3)/2.0 },
							{pow(dt, 3)/2.0,  0.0,            pow(dt, 2),     0.0            },
							{0.0,             pow(dt, 3)/2.0, 0.0,            pow(dt, 2)     }};
		Q = new Matrix(temp_Q);  //covariance of process for the mouse
		Q.timesEquals(pow(q,2));
		
		R = eye(nz).timesEquals( pow(std_array[0],2) );  //covariance of measurement of the mouse

		states = zeros(nx,1);  //initial estimated state
		measurments = zeros(nz,1);  //initial estimated state
		
		f = new FstateMouse(T);  //nonlinear state equations
		h = new HmeasMouse(billboard);  //measurement equation  
		
		P = eye(nx).timesEquals( pow(10,-3) );  //initial state covariance

	
		initData(1f, 1f, 0.001f, 0.001f);
		
		if (DEBUG){
			debug("Creating TrackingUnscentedKalmanFilter object");
			debug("Debug: tracking.ukf, Q dim: " + Q.getRowDimension() + " x " + Q.getColumnDimension() + ", Q:");
			printM(Q);
			debug("Debug: tracking.ukf, R dim: " + R.getRowDimension() + " x " + R.getColumnDimension() + ", R:");
			printM(R);
			debug("Debug: tracking.ukf, P dim: " + P.getRowDimension() + " x " + P.getColumnDimension() + ", P:");
			printM(P);
			debug("Debug: tracking.ukf, state dim: " + states.getRowDimension() + " states " + states.getColumnDimension() + ", states:");
			printM(states);
			debug("Debug: tracking.ukf, measurments dim: " + measurments.getRowDimension() + " x " + measurments.getColumnDimension() + ", measurments:");
			printM(measurments);
			debug("Array of std of expected measurement= " + std_array[0] );
		}
	}

	/**
	 * Set initial values. The input data is considered relatively certain.
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
	public void initData(float x, float y, float xv, float yv) 
	{	
		lastCurrentTime = Clock.timestamp();
		states.set(0, 0, x);
		states.set(1, 0, y);	
		states.set(2, 0, xv);
		states.set(3, 0, yv);	
	}

	/**
	 * Returns time of the last update of the filter 
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
		return (float)states.get(0, 0);
	}

	/**
	 * Returns estimated y position.
	 * 
	 * @return y in meters as a float
	 */
	public float getY() {
		return (float)states.get(1, 0);
	}

	/**
	 * Returns estimated x velocity.
	 * 
	 * @return x velocity in meters as a float
	 */
	public float getXv() {
		return (float)states.get(2, 0);
	}

	/**
	 * Returns estimated y velocity.
	 * 
	 * @return y velocity in meters as a float
	 */
	public float getYv() {
		return (float)states.get(3, 0);
	}

	/**
	 * Returns the mean iteration execution time in seconds.
	 * 
	 * @return time in seconds
	 */
	public float getExecutionTime() {
		return ((((float) iterationTime) / 1000) / ((float) iterationCounter));
	}

	/**
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
		// g2.rotate(iangle, ix, iy)

		// Plot mean
		g2.setColor(Color.pink);
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
			// System.out.println(id + ": Sighting: " + sens);
			billboard.setLatestSighting(id, sens.x, sens.y, sens.angle, sens.comparable);
		}
		

		// Get time reference
		currentTime = Clock.timestamp();


		// Compare sensor data to particles
		float[] sightings = billboard.getLatestSightings();
		R.set(0,0, large);
		R.set(1,1, large);
		R.set(2,2, large);
		// Loop through cats in billboard
		for (int i = 1; i <= billboard.getNoCats(); i++) {
			
			//TODO: if sighting is newer then lastCurrentTime
			R.set(i-1, i-1, pow(std_array[0],2) );
			measurments.set(i-1, 0, sightings[(i - 1) * 4 + 2] );
		}
				
		//One iteration with UKF
		Matrix[] result = ufk_filter.ukf(f, states, P, h, measurments, Q, R);
		states = result[0]; 
		P = result[1];
		if (DEBUG)
		{
			debug("Debug: tracking.ukf, P dim: " + P.getRowDimension() + " x " + P.getColumnDimension() + ", P:");
			printM(P);
			debug("Debug: tracking.ukf, states dim: " + states.getRowDimension() + " x " + states.getColumnDimension() + ", states:");
			printM(states);
		}
		
		// Check x and y so they keep inside the arena and also set velocity in that direction to zero if outside the arena
		if (states.get(0, 0) < Arena.min_x) {
			states.set(0, 0, Arena.min_x);
		}
		if (states.get(0, 0) > Arena.max_x) {
			states.set(0, 0, Arena.max_x);
		}
		if (states.get(1, 0) < Arena.min_y) {
			states.set(1, 0, Arena.min_y);
		}
		if (states.get(1, 0) > Arena.max_y) {
			states.set(1, 0, Arena.max_y);
		}

		// Commit data to billboard ??
		billboard.setMeanAndCovariance(id, (float)states.get(0, 0), (float)states.get(1, 0), (float)states.get(2, 0), (float)states.get(3, 0),
				0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f);

		// Increase iteration counter and timer (with full execution time)
		iterationCounter++;
		iterationTime += Clock.timestamp() - currentTime;
		// Update public time
		lastCurrentTime = currentTime;
	}

	public void run() {
		while (true) {
			// update();
			pause((long) (Clock.timestamp() % Tint));
		}

	}
	
	private void debug(Object info){
		if (DEBUG) System.out.println(info);
	}

}//End of class
