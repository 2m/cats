package GSim;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import lejos.util.Matrix;
import static java.lang.Math.*;
import static GSim.Matlab.*;



//TODO: Least squares not implemented... currently only using tachometer data and collecting sigthings
public class AbsolutePositioningLeastSquare extends AbsolutePositioningFilter
{
	/** estimated state vector, (n)x(1) Matrix, of the cat 
	 * x, y, vx, vy, orientation in radians, (absCamAngle)*/
	private Matrix xc;
	
	/** measurments */
	private Matrix z;
	
	
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

	
	public AbsolutePositioningLeastSquare(float T, Buffer sensorData, Buffer movementData)		
	{	
		super(T, sensorData, movementData);

		//LandmarkList, true positions of the landmarks are in this static class. HmeasCat accesses the landmark list directly
		numberOfLandmarks = LandmarkList.landmarkX.length;  //number of landmarks
		int nx = 5;  //number of variables in the cats' state vector
		int nz = numberOfLandmarks;  //number of elements in the measurement vector of the cats
		float dt = T;//1f;  //sampling period, must be 1 for now TODO adjust to real dt ????
		//T = 1;
		xc = zeros(nx,1);  //initial estimated state
		z = zeros(nz,1);  //initial estimated state
		
		if (DEBUG){
			debug("Creating AbsolutePositioningLeastSquare object");
			debug("Debug: pos.ls, xc dim: " + xc.getRowDimension() + " x " + xc.getColumnDimension() + ", xc:");
			printM(xc);
			debug("Debug: pos.ls, number of landmarks= " + numberOfLandmarks);
		}
	}
	

	/**
	 * @param x
	 *            Initial x position
	 * @param y
	 *            Initial y position
	 * @param angle
	 *            Initial angle
	 */
	public void initData(float x, float y, float angle) {
		lastCurrentTime = Clock.getTime();
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
		currentTime = Clock.getTime();

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

				sdata = (SightingData) sensorData.pop();


			}
			else 
			{	
				sdata = null; //leave loop
				sensorData.push(sdata);
			}
		}
		double[] R = {0, 0, 0, 0};

		/*//Calculate position from landmark bearings
		double[] xPositionFromBearings = new double[4];
		double[] yPositionFromBearings = new double[4];
		for (int i = 0; i < numberOfLandmarks; i++ )
		{
			if (landmarksSighted[i])
			{		
				R[i] = 1;
				xPositionFromBearings[i] = LandmarkList.landmarkX[i]- cos(z.get(i,0));
				yPositionFromBearings[i] = LandmarkList.landmarkY[i]- sin(z.get(i,0));
				if (DEBUG)
				{
					debug("x pos from bearings to landmark " + i + " = " + xPositionFromBearings[i]);
					debug("y pos from bearings to landmark " + i + " = " + yPositionFromBearings[i]);
				}
			}		
		}	*/

		
		// Update cat velocity and orientation in the measurement matrix
		MovementData mdata = (MovementData) movementData.pop();	
		double xMovementFromTachometer = 0.0;
		double yMovementFromTachometer = 0.0;
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
		double xVelocityFromTachometer = (xMovementFromTachometer) / T; // (lastCurrentTime - currentTime);  // T;
		double yVelocityFromTachometer = (yMovementFromTachometer) / T; //(lastCurrentTime - currentTime);  // T;
		


		
		//Least squares...
		xc.set(0,0, xc.get(0,0) + xMovementFromTachometer);
		xc.set(1,0, xc.get(1,0) + yMovementFromTachometer);
		xc.set(2,0, xVelocityFromTachometer);
		xc.set(3,0, yVelocityFromTachometer);
		xc.set(4,0, orientationFromTachometer);
		
		if (DEBUG){
			debug("x pos, y pos, x vel, y vel, orientation = ");
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
		iterationTime += Clock.getTime() - currentTime;
		// Update public time
		lastCurrentTime = currentTime;
		
		debug("Debug, leaving update at iteration " + iterationCounter + ", current iterationTime= " + (Clock.getTime() - currentTime) );
	}//end of update
	

	
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
	
	
	
}


