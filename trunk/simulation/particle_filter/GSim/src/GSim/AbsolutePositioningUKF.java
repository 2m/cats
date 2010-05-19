package GSim;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import lejos.util.Matrix;
import static java.lang.Math.*;
import static GSim.Matlab.*;

public class AbsolutePositioningUKF extends AbsolutePositioningFilter
{
	//Instance variables
	/** Unscented Kalman Filter */
	private UnscentedKalmanFilter ufk_filter;
	
	/** nonlinear state equations */
	private IFunction f;
	
	/** estimated state vector, (n)x(1) Matrix, of the cat 
	 * x, y, vx, vy, orientation in radians, (absCamAngle)*/
	private Matrix xc;
	
	///** true state of the cat (basic simulation only)*/
	//private Matrix sc;
	
	/** state covariance */
	private Matrix P;
	
	/** measurement equation */
	private IFunction h;
	
	/** measurments */
	private Matrix z;
	
	/** covariance of process for the cat */
	private Matrix Q; 
	
	/** covariance of measurement of the cat */
	private Matrix R;
	
	/** std of expected measurement noise for the cat (for bearing angle, x, y, orient., cam.ang respectivly)*/
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

	
	public AbsolutePositioningUKF(int id,float T, Buffer sensorData, Buffer movementData,BillBoard billboard)		
	{	
		super(id,T, sensorData, movementData, billboard);

		//LandmarkList, true positions of the landmarks are in this static class. HmeasCat accesses the landmark list directly
		numberOfLandmarks = LandmarkList.landmarkX.length;  //number of landmarks
		int nz = numberOfLandmarks+3;  //TODO: change back to nz=n+4  //number of elements in the measurement vector of the cats = number of landmarks + 4
		int nx = 5;  //TODO: change back to nx=6 //number of variables in the cats' state vector
		ufk_filter = new UnscentedKalmanFilter(nx,nz);
		float dt = T;//1f;  //sampling period in seconds
		//T = 1;
		float q = 0.005f;  //std of expected process noise for the cat
		float stddegrees = 2;
		std_array = new double[]{stddegrees*(PI/180), pow(10, -2), pow(10, -2), pow(10, -7)};//, pow(10, -20)};
		double[][] r_temp = {std_array};
		r = new Matrix(r_temp);  //std of expected measurement noise for the cat (for bearing angle, x, y, orient., cam.ang respectivly)
		float k1 = dt;  //how much the noise in the wheel tachometers is amplified
		/*float k2 = dt;  //how much the noise in the camera motor tachometers is amplified
		double[][] temp_Q = {{pow(dt, 4)/4.0, 0.0,            pow(dt, 3)/2.0, 0.0,            0.0,            0.0},
							{0.0,             pow(dt, 4)/4.0, 0.0,            pow(dt, 3)/2.0, 0.0,            0.0},
							{pow(dt, 3)/2.0,  0.0,            pow(dt, 2),     0.0,            0.0,            0.0},
							{0.0,             pow(dt, 3)/2.0, 0.0,            pow(dt, 2),     0.0,            0.0},
							{0.0,             0.0,            0.0,            0.0,            k1,             0.0},
							{0.0,             0.0,            0.0,            0.0,            0.0,            k2}};*/
		double[][] temp_Q = {{pow(dt, 4)/4.0, 0.0,            pow(dt, 3)/2.0, 0.0,            0.0 },
							{0.0,             pow(dt, 4)/4.0, 0.0,            pow(dt, 3)/2.0, 0.0 },
							{pow(dt, 3)/2.0,  0.0,            pow(dt, 2),     0.0,            0.0 },
							{0.0,             pow(dt, 3)/2.0, 0.0,            pow(dt, 2),     0.0 },
							{0.0,             0.0,            0.0,            0.0,            k1 }};
		Q = new Matrix(temp_Q);  //covariance of process for the cat
		Q.timesEquals(pow(q,2));
		
		R = eye(nz).timesEquals( pow(r.get(1-1,1-1),2) );  //covariance of measurement of the cat
		for (int j=1; j<=nz-numberOfLandmarks; j++)
		{
			R.set( numberOfLandmarks+j-1, numberOfLandmarks+j-1, pow(r.get(1-1, j+1-1),2) );
		}

		f = new FstateCat(T);  //nonlinear state equations
		h = new HmeasCat();  //measurement equation
		
		P = eye(nx).timesEquals( pow(10,-3) );  //initial state covariance


		//sc = zeros(nx,1);  //initial true state of the cat (basic simulation only)
		xc = zeros(nx,1);  //initial estimated state
		z = zeros(nz,1);  //initial estimated state
		
		if (DEBUG){
			debug("Creating AbsolutePositioningUKF object");
			debug("Debug: pos.ukf, Q dim: " + Q.getRowDimension() + " x " + Q.getColumnDimension() + ", Q:");
			printM(Q);
			debug("Debug: pos.ukf, R dim: " + R.getRowDimension() + " x " + R.getColumnDimension() + ", R:");
			printM(R);
			debug("Debug: pos.ukf, P dim: " + P.getRowDimension() + " x " + P.getColumnDimension() + ", P:");
			printM(P);
			debug("Debug: pos.ukf, xc dim: " + xc.getRowDimension() + " x " + xc.getColumnDimension() + ", xc:");
			printM(xc);
			debug("Debug: pos.ukf, z dim: " + z.getRowDimension() + " x " + z.getColumnDimension() + ", z:");
			printM(z);
			debug("Debug: pos.ukf, number of landmarks= " + numberOfLandmarks);
			debug("Array of std of expected measurement= " + std_array[0] +", " + std_array[1] +", " + std_array[2] +", " + std_array[3]);
		}
	}//End of constructor
	
	/**
	 * Set initial data and run filter once ? and then re-sample. 
	 * 
	 * @param x
	 *            Initial x position
	 * @param y
	 *            Initial y position
	 * @param angle
	 *            Initial angle
	 */
	public void initData(float x, float y, float angle) {
		lastCurrentTime = Clock.timestamp();
		xc.set(0, 0, x);
		xc.set(1, 0, y);	
		xc.set(2, 0, 0);
		xc.set(3, 0, 0);	
		xc.set(4, 0, angle);	
	}
	
	/** Poll estimated x position value from filter */
	public float getX() {
		return (float)xc.get(0, 0);
	}

	/** Poll estimated y position value from filter */
	public float getY() {
		return (float)xc.get(1, 0);
	}

	//TODO Rename to getOrientation ??
	/** Poll estimated direction angle value from filter */
	public float getAngle() {
		return (float)xc.get(4, 0);
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
	 * Returns the mean iteration execution time in seconds.
	 * 
	 * @return time in seconds
	 */
	public float getExecutionTime() {
		return ((((float) iterationTime) / 1000) / ((float) iterationCounter));
	}
	
	/**
	 * Updates the filter without explicitly returning the values
	 */
	public void update() {
		// Get time reference
		currentTime = Clock.timestamp();

		// Update landmark angle in the measurement matrix	
		SightingData sdata = (SightingData) sensorData.pop();
		boolean[] landmarksSighted = new boolean[numberOfLandmarks];
		debug("Debug, entering update: current time= " + currentTime + ", number of landmarks= " +landmarksSighted.length);
		for (boolean landmark: landmarksSighted)
		{
			landmark = false;
		}
		while(sdata != null)
		{
			if (sdata.getComparable() <= currentTime)
			{
				//check if the second oldest (=newer) data also is valid, 
				// if so use that data instead
				SightingData newerSdata = (SightingData) sensorData.top();
				if ( (newerSdata != null) && (newerSdata.getComparable() <= currentTime) ) 
				{
					sdata = (SightingData) sensorData.pop();
				}
				else
				{		
					debug("Current length of buffer = " + sensorData.getLength());
					
					//Determine which landmark it is
					double absLandmarkAngle = (sdata.angle + xc.get(4, 0) +4.0*PI) % (2.0*PI); 
					if (absLandmarkAngle>=0 && absLandmarkAngle<PI/2.0) //pper right corner
					{
						z.set(3,0,absLandmarkAngle);
						landmarksSighted[3] = true;
						debug("Sighting landmark 3 (upper right corner) with absLandmarkAngle = " + toDegrees(absLandmarkAngle) + " , relative = " + toDegrees(sdata.angle) + " , orientation = " + toDegrees(xc.get(4, 0)));
					}
					else if (absLandmarkAngle>=PI/2.0 && absLandmarkAngle<PI) //upper left corner
					{
						z.set(1,0,absLandmarkAngle);
						landmarksSighted[1] = true;
						debug("Sighting landmark 1 (upper left corner) with absLandmarkAngle = " + toDegrees(absLandmarkAngle) + " , relative = " + toDegrees(sdata.angle) + " , orientation = " + toDegrees(xc.get(4, 0)));
					}
					else if (absLandmarkAngle>=PI && absLandmarkAngle<3.0*PI/2.0) //lower left corner
					{
						z.set(0,0,absLandmarkAngle);
						landmarksSighted[0] = true;
						debug("Sighting landmark 0 (lower left corner) with absLandmarkAngle = " + toDegrees(absLandmarkAngle) + " , relative = " + toDegrees(sdata.angle) + " , orientation = " + toDegrees(xc.get(4, 0)));
					}
					else if (absLandmarkAngle>=3.0*PI/2.0 && absLandmarkAngle<2*PI)  //lower right corner
					{
						z.set(2,0,absLandmarkAngle);
						landmarksSighted[2] = true;
						debug("Sighting landmark 2 (lower right corner) with absLandmarkAngle = " + toDegrees(absLandmarkAngle) + " , relative = " + toDegrees(sdata.angle) + " , orientation = " + toDegrees(xc.get(4, 0)));
					}
					else System.out.println("ERROR in update! absLandmarkAngle in radians = " + absLandmarkAngle + " and in degrees = "+ toDegrees(absLandmarkAngle) );
					
					sdata = null; //leave loop
				}
			} 
			else 
			{
				if (sdata != null)
				{
					sensorData.push(sdata);
				}
			}
		}	
		//Default if no sighting
		R.set(0,0, large);
		R.set(1,1, large);
		R.set(2,2, large);
		R.set(3,3, large);
		
		for (int i = 0; i < numberOfLandmarks; i++ )
		{
			if (landmarksSighted[i])
			{		
				R.set(i, i, pow(std_array[0],2) );
			}		
		}	
		
		// Update cat velocity and orientation in the measurement matrix
		MovementData mdata = (MovementData) movementData.pop();	
		double xMovementFromTachometer = 0.0;//xc.get(0, 0);
		double yMovementFromTachometer = 0.0;//xc.get(1, 0);
		double orientationFromTachometer = xc.get(4, 0);
		while (mdata != null) 
		{
			if (mdata.getComparable() <= currentTime) 
			{
				orientationFromTachometer += mdata.dangle;	
				double distance = mdata.dr;
				xMovementFromTachometer += distance*cos(orientationFromTachometer);
				yMovementFromTachometer += distance*sin(orientationFromTachometer);

				mdata = (MovementData) movementData.pop();
			}
			else {
				movementData.push(mdata);
				mdata = null;
			}
		}
		double xVelocityFromTachometer = (xMovementFromTachometer) / T; // ((lastCurrentTime - currentTime)*1000);  // T;
		double yVelocityFromTachometer = (yMovementFromTachometer) / T; // ((lastCurrentTime - currentTime)*1000);  // T;
		z.set(numberOfLandmarks-1 +1, 0, xVelocityFromTachometer);
		z.set(numberOfLandmarks-1 +2, 0, yVelocityFromTachometer);	
		z.set(numberOfLandmarks-1 +3, 0, orientationFromTachometer);	
		
		//One iteration with UKF
		Matrix[] result = ufk_filter.ukf(f, xc, P, h, z, Q, R);
		xc = result[0]; 
		P = result[1];
		
		if (DEBUG)
		{
			debug("Debug: pos.ukf, P dim: " + P.getRowDimension() + " x " + P.getColumnDimension() + ", P:");
			printM(P);
			debug("Debug: pos.ukf, xc dim: " + xc.getRowDimension() + " x " + xc.getColumnDimension() + ", xc:");
			printM(xc);
		}
		
		
		// Check x and y so they keep inside the arena and also set velocity in that direction to zero if outside the arena
		if (xc.get(0, 0) < Arena.min_x) {
			xc.set(0, 0, Arena.min_x);
			//xc.set(3, 0, 0);
		}
		if (xc.get(0, 0) > Arena.max_x) {
			xc.set(0, 0, Arena.max_x);
			//xc.set(3, 0, 0);
		}
		if (xc.get(1, 0) < Arena.min_y) {
			xc.set(1, 0, Arena.min_y);
			//xc.set(4, 0, 0);
		}
		if (xc.get(1, 0) > Arena.max_y) {
			xc.set(1, 0, Arena.max_y);
			//xc.set(4, 0, 0);
		}
		
		// Increase iteration counter and timer (with full execution time)
		iterationCounter++;
		iterationTime += Clock.timestamp() - currentTime;
		// Update public time
		lastCurrentTime = currentTime;
		
		debug("Debug, leaving update at iteration " + iterationCounter + ", current iterationTime= " + (Clock.timestamp() - currentTime) );
	}//end of update
	
	/**
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

		// Plot position
		g2.setColor(Color.red);
		int ix = Actor.e2gX(getX());
		int iy = Actor.e2gY(getY());
		//debug("Debug, in abs.pos.ukf draw: ix= " + ix + ", iy= "+ iy);

		double iangle = -getAngle();
		g2.fillOval((int) ix - (size / 2), (int) iy - (size / 2), (int) size,
				(int) size);
		g2.drawLine((int) ix, (int) iy, (int) (ix + Math.cos(iangle)
				* linelength), (int) (iy + Math.sin(iangle) * linelength));

		// Reset the transformation matrix
		g2.setTransform(oldTransform);
	}
	
	public void run() {
		/*
		 * while (true) { update(); pause((long) (Clock.getTime() % Tint)); }
		 */
	}
	
	private void debug(Object info){
		if (DEBUG) System.out.println(info);
		
	}	
}//End of class

