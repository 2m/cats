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
	
	/** estimated state vector, (n)x(1) Matrix, of the mouse 
	 * x, y, vx, vy */
	private Matrix states;
	
	/** state covariance */
	private Matrix P;
	
	/** measurement equation */
	private IFunction h;
	
	/** measurments */
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
		float q = 0.005f;  //std of expected process noise for the cat
		float stddegrees = 2;
		std_array = new double[]{stddegrees*(PI/180), pow(10, -2), pow(10, -2), pow(10, -7)};//, pow(10, -20)};
		double[][] r_temp = {std_array};
		r = new Matrix(r_temp);  //std of expected measurement noise for the cat (for bearing angle, x, y, orient., cam.ang respectivly)
		double[][] temp_Q = {{pow(dt, 4)/4.0, 0.0,            pow(dt, 3)/2.0, 0.0            },
							{0.0,             pow(dt, 4)/4.0, 0.0,            pow(dt, 3)/2.0 },
							{pow(dt, 3)/2.0,  0.0,            pow(dt, 2),     0.0            },
							{0.0,             pow(dt, 3)/2.0, 0.0,            pow(dt, 2)     }};
		Q = new Matrix(temp_Q);  //covariance of process for the mouse
		Q.timesEquals(pow(q,2));
		
		R = eye(nz).timesEquals( pow(r.get(1-1,1-1),2) );  //covariance of measurement of the cat
		for (int j=1; j<=nz-numberOfLandmarks; j++)
		{
			R.set( numberOfLandmarks+j-1, numberOfLandmarks+j-1, pow(r.get(1-1, j+1-1),2) );
		}

		states = zeros(nx,1);  //initial estimated state
		measurments = zeros(nz,1);  //initial estimated state
		
		f = new FstateMouse(T);  //nonlinear state equations
		h = new HmeasMouse(billboard);  //measurement equation  //TODO: Needs the position of the cats...
		
		P = eye(nx).timesEquals( pow(10,-3) );  //initial state covariance

	
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
			debug("Debug: tracking.ukf, number of landmarks= " + numberOfLandmarks);
			debug("Array of std of expected measurement= " + std_array[0] +", " + std_array[1] +", " + std_array[2] +", " + std_array[3]);
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
			billboard.setLatestSighting(id, sens.x, sens.y, sens.angle);
		}


		// Get time reference
		currentTime = Clock.timestamp();


		// Compare sensor data to particles
		float[] sightings = billboard.getLatestSightings();
		// Loop through cats in billboard
		for (int i = 1; i <= billboard.getNoCats(); i++) {
			int x = Fixed.floatToFixed(sightings[(i - 1) * 3]);
			int y = Fixed.floatToFixed(sightings[(i - 1) * 3 + 1]);
			int angle = Fixed.floatToFixed(sightings[(i - 1) * 3 + 2]);

		}

		// Calculate mean and (co-)variance, then commit data to billboard


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

