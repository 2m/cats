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
	 * x, y, vx, vy, orientation, (absCamAngle)*/
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
	
	/** Varible for time */
	private int lastCurrentTime, currentTime;

	
	public AbsolutePositioningUKF(float T, Buffer sensorData, Buffer movementData, RealTimeClock rttime)		
	{	
		super(T, sensorData, movementData, rttime);
	/*
	public AbsolutePositioningUKF()		
	{*/		
		//LandmarkList, true positions of the landmarks are in this static class. HmeasCat accesses the landmark list directly
		int n = 1;//TODO use the following: LandmarkList.landmarkX.length;  //number of landmarks
		//int nm=1;  //number of cats, should always be one for a single positioning filter
		int nz=n+3;  //TODO: change back to nz=n+4  //number of elements in the measurement vector of the cats = number of landmarks + 4
		int nx=5;  //TODO: change back to nx=6 //number of variables in the cats' state vector
		
		ufk_filter = new UnscentedKalmanFilter(nx,nz);
	
		float dt=1;  //sampling period, must be 1 for now TODO adjust to matlab real dt ????
		float q = 0.005f;  //std of expected process noise for the cat
		float stddegrees = 2;
		double[][] temp_r = {{stddegrees*(PI/180), pow(10, -2), pow(10, -2), pow(10, -20), pow(10, -20)}};
		Matrix r = new Matrix(temp_r);  //std of expected measurement noise for the catdouble[][] temp_r = {{stddegrees*(Math.PI/180), Math.pow(10, -2), Math.pow(10, -2), Math.pow(10, -20),Math.pow(10, -20)}};
		float k1=dt;  //how much the noise in the wheel tachometers is amplified
		float k2=dt;  //how much the noise in the camera motor tachometers is amplified
		double[][] temp_Q = {{pow(dt, 4)/4.0, 0.0,            pow(dt, 3)/2.0, 0.0,            0.0,            0.0},
							{0.0,             pow(dt, 4)/4.0, 0.0,            pow(dt, 3)/2.0, 0.0,            0.0},
							{pow(dt, 3)/2.0,  0.0,            pow(dt, 2),     0.0,            0.0,            0.0},
							{0.0,             pow(dt, 3)/2.0, 0.0,            pow(dt, 2),     0.0,            0.0},
							{0.0,             0.0,            0.0,            0.0,            k1,             0.0},
							{0.0,             0.0,            0.0,            0.0,            0.0,            k2}};
		Q = new Matrix(temp_Q);  //covariance of process for the cat
		Q.timesEquals(pow(q,2));
		
		R = eye(nz).timesEquals( pow(r.get(1-1,1-1),2) );  //covariance of measurement of the cat
		for (int j=1; j<=nz-n; j++)
		{
			R.set( n+j-1, n+j-1, pow(r.get(1-1, j+1-1),2) );
		}

		f = new FstateCat();  //nonlinear state equations
		h = new HmeasCat();  //measurement equation
		
		P = eye(nz).timesEquals( pow(10,-3) );  //initial state covariance

		//sc = zeros(nx,1);  //initial true state of the cat (basic simulation only)
		xc = zeros(nx,1);  //initial estimated state
		/*
		double[][] temp_s = {{0.0}, {0.0}, {1.0}};;  //initial state of the cats  TODO get initial state from buffer?
		Matrix s = new Matrix(temp_s);  //true state of the cats
		*/
		
		/*	
		global actLandm; %the indices of the landmarks that are seen
		global ra; %std of actual measurement noise 
		global vc; %the velocities of the cats
		global k;  %current time step
		*/
	}
	
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
		lastCurrentTime = rttime.getTime();
		xc.set(0, 0, x);
		xc.set(1, 0, y);	
		xc.set(2, 0, 0);
		xc.set(3, 0, 0);	
		xc.set(4, 0, angle);	
	}
	
	
	/**
	 * Updates the filter without explicitly returning the values
	 */
	public void update() {
		// Get time reference
		currentTime = rttime.getTime();

		// Update landmark angle in the measurement matrix
		SightingData sdata = (SightingData) sensorData.pop();
		while (sdata != null) 
		{
			if (sdata.getComparable() <= currentTime) 
			{
				//Determine which landmark it is
				double absLandmarkAngle =  sdata.angle;
				if (absLandmarkAngle>=0 && absLandmarkAngle<90) //upper right corner
				{
					z.set(3,0,sdata.angle);
				}
				else if (absLandmarkAngle>=90 && absLandmarkAngle<180) //upper left corner
				{
					z.set(1,0,sdata.angle);
				}
				else if (absLandmarkAngle>=180 && absLandmarkAngle<270) //lower left corner
				{
					z.set(0,0,sdata.angle);
				}
				else if (absLandmarkAngle>=270 && absLandmarkAngle<360)  //lower right corner
				{
					z.set(2,0,sdata.angle);
				}
				else System.out.print("ERROR in update!");
			} 
			else 
			{
				sensorData.push(sdata);
				sdata = null;
			}
		}
		
		// Update cat velocity and orientation in the measurement matrix
		MovementData mdata = (MovementData) movementData.pop();	
		double xPositionFromTachometer = xc.get(0, 0);
		double yPositionFromTachometer = xc.get(1, 0);
		double orientationFromTachometer = xc.get(4, 0);
		while (mdata != null) 
		{
			if (mdata.getComparable() <= currentTime) 
			{
				double distance = mdata.dr;
				xPositionFromTachometer += cos(distance);
				yPositionFromTachometer += sin(distance);
				orientationFromTachometer += mdata.dangle;
				
				mdata = (MovementData) movementData.pop();
			}
			else {
				movementData.push(mdata);
				mdata = null;
			}
		}
		double xVelocityFromTachometer = (z.get(0, 0) - xPositionFromTachometer) / (lastCurrentTime - currentTime);
		double yVelocityFromTachometer = (z.get(1, 0) - yPositionFromTachometer) / (lastCurrentTime - currentTime);
		z.set(2, 0, xVelocityFromTachometer);
		z.set(3, 0, yVelocityFromTachometer);	
		z.set(4, 0, orientationFromTachometer);	
		
		//One iteration with UKF
		Matrix[] result = ufk_filter.ukf(f, xc, P, h, z, Q, R);
		xc = result[0];  
		P = result[1];
		//sc = f.eval(sc);  //update process (basic simulation only)
		
		lastCurrentTime = currentTime;	
	}//end of update
	
	/** Poll estimated x position value from filter */
	public float getX() {
		return (float)xc.get(0, 0);
	}

	/** Poll estimated y position value from filter */
	public float getY() {
		return (float)xc.get(0, 1);
	}

	//TODO Rename to getOrientation ??
	/** Poll estimated direction angle value from filter */
	public float getAngle() {
		return (float)xc.get(0, 4);
	}
	
	/*
	public static void main(String args[])
	{
		Buffer sensorData;
		Buffer movementData;
		float T;
		RealTimeClock rttime;
		AbsolutePositioningUKF test = new AbsolutePositioningUKF();
	}*/
	
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

	
		// Plot mean
		g2.setColor(Color.red);
		int ix = Actor.e2gX(getX());
		int iy = Actor.e2gY(getY());
		double iangle = -getAngle();
		g2.fillOval((int) ix - (size / 2), (int) iy - (size / 2), (int) size,
				(int) size);
		g2.drawLine((int) ix, (int) iy, (int) (ix + Math.cos(iangle)
				* linelength), (int) (iy + Math.sin(iangle) * linelength));

		// Reset the transformation matrix
		g2.setTransform(oldTransform);
	}

}
