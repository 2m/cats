package se.uu.it.cats.brick.filter;

import se.uu.it.cats.brick.Clock;
import se.uu.it.cats.brick.storage.BillBoard;

import lejos.util.KalmanFilter;
import lejos.util.Matrix;
import static java.lang.Math.*;
import static se.uu.it.cats.brick.filter.Matlab.*;

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
	private double catAngle;
	private boolean turnMode = false; //true if turning, false if traveling
	
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
	private Matrix r; //TODO remove and use std_array instead?
	
	/** Varible for time */
	private int lastCurrentTime, currentTime;
	
	/** Counter and timer too keep track of mean iteration execution time */
	private int iterationCounter = 0;
	private int iterationTime = 0;
	
	/** number of landmarks */
	private int numberOfLandmarks;
	
	private float large = (float)pow(10,10);
	
	/**Toggle debug info*/
	private final boolean DEBUG = false;

	public AbsolutePositioningUKF(int id, float T, Buffer unifiedBuffer, BillBoard billboard)		
	{	
		super(id,T, unifiedBuffer, billboard);

		//LandmarkList, true positions of the landmarks are in this static class. HmeasCat accesses the landmark list directly
		numberOfLandmarks = 4;//LandmarkList.landmarkX.length;  //number of landmarks
		int nz = numberOfLandmarks+3;  //number of elements in the measurement vector of the cats
		int nx = 5;  //number of variables in the cats' state vector
		ufk_filter = new UnscentedKalmanFilter(nx,nz);
		float dt = T;// sampling period in seconds, NB: change T, not dt as T is used elsewhere in the code!

		float q = 0.5f;  //std of expected process noise for the cat, NB 0.5 is a ok value @ T = 200-500ms
		float stddegrees = 1f;  //a higher value gives smaller errors but slower conversion, NB 1 is a ok value @ T = 200-500ms
		std_array = new double[]{stddegrees*(PI/180), pow(10, -2), pow(10, -2), pow(10, -7)};//, pow(10, -20)};
		double[][] r_temp = {std_array};
		r = new Matrix(r_temp);  //std of expected measurement noise for the cat (for bearing angle, x, y, orient., cam.ang respectivly)
		float k1 = 1f;  //how much the noise in the wheel tachometers is amplified
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
		catAngle = 0;
		z = zeros(nz,1);  //initial estimated measurements
		
		/*if (DEBUG){
			debug("Creating AbsolutePositioningUKF object for cat " +id);
			debug("Debug: pos.ukf cat " +id + ", Q dim: " + Q.getRowDimension() + " x " + Q.getColumnDimension() + ", Q:");
			printM(Q);
			debug("Debug: pos.ukf cat " +id + ", R dim: " + R.getRowDimension() + " x " + R.getColumnDimension() + ", R:");
			printM(R);
			debug("Debug: pos.ukf cat " +id + ", P dim: " + P.getRowDimension() + " x " + P.getColumnDimension() + ", P:");
			printM(P);
			debug("Debug: pos.ukf cat " +id + ", xc dim: " + xc.getRowDimension() + " x " + xc.getColumnDimension() + ", xc:");
			printM(xc);
			debug("Debug: pos.ukf cat " +id + ", z dim: " + z.getRowDimension() + " x " + z.getColumnDimension() + ", z:");
			printM(z);
			debug("Debug: pos.ukf cat " +id + ", number of landmarks= " + numberOfLandmarks);
			debug("Debug: pos.ukf cat " +id + ",array of std of expected measurement= " + std_array[0] +", " + std_array[1] +", " + std_array[2] +", " + std_array[3]);
		}*/
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
		catAngle = angle;	
	}
	
	/** Poll estimated x position value from filter */
	public float getX() {
		return (float)xc.get(0, 0);
	}

	/** Poll estimated y position value from filter */
	public float getY() {
		return (float)xc.get(1, 0);
	}

	/** Poll estimated direction angle value from filter */
	public float getAngle() {
		return (float) ((float)(catAngle+2*Math.PI) % (2*Math.PI));//xc.get(4, 0);
	}
//	
//	/** Only calculate angles to landmarks*/
//	public double[] landmarkAngleEval(Matrix xc){		
//		//NB: All -1 in the indices are used to indicate the shift form the first array index in matlab = 1 to java's = 0.
//		double[] angles = new double[numberOfLandmarks];
//		//System.out.println("Debug, HmeasCat: n = " + n);
//		for (int i = 0; i < numberOfLandmarks; i++)
//		{
//			angles[i] = Math.atan2(LandmarkList.landmarkY[i] - xc.get(1,0), LandmarkList.landmarkX[i] - xc.get(0,0));
//			angles[i] = (angles[i]+2*Math.PI)%(2*Math.PI);
//		}
//		//System.out.println("angles: " + angles.toString());
//		return angles;
//	}
//	
//	/**
//	 * Returns time of the last update of the filter (this includes minor
//	 * updates).
//	 * 
//	 * @return time in milliseconds
//	 */
//	public int getTime() {
//		return lastCurrentTime;
//	}
//	
//	/**
//	 * Returns the mean iteration execution time in seconds.
//	 * 
//	 * @return time in seconds
//	 */
//	public float getExecutionTime() {
//		return ((((float) iterationTime) / 1000) / ((float) iterationCounter));
//	}
//	
//	/**
//	 * Updates the filter without explicitly returning the values
//	 */
//	public void update() {
//		// Get time reference
//		currentTime = Clock.timestamp();
//		iterationCounter++;
//		float turnEpsilon = 0.00001f;
//		float travelEpsilon = 0.0000001f;
//		
//		boolean[] landmarksSighted = new boolean[numberOfLandmarks];
//		debug("Debug cat " +id + ", entering update at iteration" + iterationCounter +", current time= " + currentTime + ", number of landmarks= " + landmarksSighted.length);
//		for (int i = 0; i<numberOfLandmarks-1; i++)
//		{
//			landmarksSighted[i] = false; //default, no sighting
//		}
//		double xMovementFromTachometer = 0.0;//xc.get(0, 0);
//		double yMovementFromTachometer = 0.0;//xc.get(1, 0);
//		//double orientationFromTachometer = xc.get(4, 0);
//		
//		ComparableData data = unifiedBuffer.pop();
//		while(data != null)
//		{
//			if (data.getComparable() <= currentTime)
//			{
//				//check if the second oldest (=newer) data also is valid, 
//				// if so use that data instead
//				/*if ( (newerSdata != null) && (newerSdata.getComparable() <= currentTime) ) 
//				{
//					sdata = (SightingData) sensorData.pop();
//				}*/
//				
//				if (data.isSightingData()) {
//					// Compare with landmarks or mouse data
//					SightingData sdata = (SightingData) data;
//					if (sdata.type == LandmarkList.MOUSE) {
//						//send mouse sighting to billboard so the tracking filter can use it
//						billboard.setLatestSighting(id, getX(), getY(),
//								sdata.angle + getAngle(), sdata.comparable);
//					} 
//					else 
//					{
//						//Determine which landmark it is
//						double absLandmarkAngle = (sdata.angle + getAngle() + 2.0*PI) % (2.0*PI); //+4.0*PI old
//						if (absLandmarkAngle>=0 && absLandmarkAngle<PI/2.0) //upper right corner
//						{
//							debug("Cat " +id + " sighting landmark 3 (upper right corner) with absLandmarkAngle = " + toDegrees(absLandmarkAngle) + " , rel. = " + toDegrees(sdata.angle) + " , orient. = " + toDegrees(xc.get(4, 0)) + ", timestamp = " + sdata.comparable );
//
//							z.set(3,0,absLandmarkAngle);
//							landmarksSighted[3] = true;
//
//						}
//						else if (absLandmarkAngle>=PI/2.0 && absLandmarkAngle<PI) //upper left corner
//						{
//							debug("Cat " +id + " sighting landmark 1 (upper left corner) with absLandmarkAngle = " + toDegrees(absLandmarkAngle) + " , rel. = " + toDegrees(sdata.angle) + " , orient. = " + toDegrees(xc.get(4, 0)) + ", timestamp = " + sdata.comparable );
//
//							z.set(1,0,absLandmarkAngle);
//							landmarksSighted[1] = true;
//
//						}
//						else if (absLandmarkAngle>=PI && absLandmarkAngle<3.0*PI/2.0) //lower left corner
//						{
//							debug("Cat " +id + " sighting landmark 0 (lower left corner) with absLandmarkAngle = " + toDegrees(absLandmarkAngle) + " , rel. = " + toDegrees(sdata.angle) + " , orient. = " + toDegrees(xc.get(4, 0)) + ", timestamp = " + sdata.comparable );
//
//							z.set(0,0,absLandmarkAngle);
//							landmarksSighted[0] = true;
//
//						}
//						else if (absLandmarkAngle>=3.0*PI/2.0 && absLandmarkAngle<2*PI)  //lower right corner
//						{
//							debug("Cat " +id + " sighting landmark 2 (lower right corner) with absLandmarkAngle = " + toDegrees(absLandmarkAngle) + " , rel. = " + toDegrees(sdata.angle) + " , orient. = " + toDegrees(xc.get(4, 0)) + ", timestamp = " + sdata.comparable );
//
//							z.set(2,0,absLandmarkAngle);
//							landmarksSighted[2] = true;
//
//						}
//						else System.out.println("CAT " +id + "ERROR in update! absLandmarkAngle in radians = " + absLandmarkAngle + " and in degrees = "+ toDegrees(absLandmarkAngle) );
//						
//						//sdata = null; //leave loop
//						//System.out.println("Cat: " + id + " z:...");
//						//printM(z);
//						//System.out.println("absLandmarkAngle: "+absLandmarkAngle);
//					}
//				}
//				else if (data.isMovementData()) {
//					MovementData mdata = (MovementData) data;
//					System.out.println("Cat: "+id+", dr: "+mdata.dr+", angle: "+mdata.dangle);
//					// Update cat velocity and orientation in the measurement matrix
//					/*if (turnMode && mdata.dangle > turnEpsilon)
//						catAngle += mdata.dangle;
//					else if (turnMode && mdata.dangle <= turnEpsilon)
//					{
//						unifiedBuffer.push(data);
//						data = null;
//						turnMode = false;
//					}
//					else if(!turnMode && mdata.dangle < turnEpsilon)
//					{
//						xMovementFromTachometer += mdata.dr*cos(catAngle);
//						yMovementFromTachometer += mdata.dr*sin(catAngle);
//					}
//					else if(!turnMode && mdata.dr < travelEpsilon)
//					{
//						unifiedBuffer.push(data);
//						data = null;
//						turnMode = true;
//					}
//					else if(!turnMode && mdata.dangle > turnEpsilon)
//					{
//						unifiedBuffer.push(data);
//						data = null;
//						turnMode = true;
//					}
//					else
//						System.out.println("Error in turn mode!");
//					 */		
//					catAngle += mdata.dangle;
//					xMovementFromTachometer += mdata.dr*cos(catAngle);
//					yMovementFromTachometer += mdata.dr*sin(catAngle);
//					
//					System.out.println("					Cat: "+id+", turnMode: "+turnMode);
//				}
//				// Pops new data
//				data = unifiedBuffer.pop();
//			} //end time-if
//			else 
//			{
//				unifiedBuffer.push(data);
//				data = null;
//			}
//		} //end while
//		
//		// Update cat velocity and orientation in the measurement matrix
//		double xVelocityFromTachometer = xMovementFromTachometer / T; // ((lastCurrentTime - currentTime)*1000);  // T;
//		double yVelocityFromTachometer = yMovementFromTachometer / T; // ((lastCurrentTime - currentTime)*1000);  // T;
//		z.set(numberOfLandmarks-1 +1, 0, xVelocityFromTachometer);
//		z.set(numberOfLandmarks-1 +2, 0, yVelocityFromTachometer);	
//		//z.set(numberOfLandmarks-1 +3, 0, orientationFromTachometer);
//		
//		//Default if no sighting
//		R.set(0,0, large);
//		R.set(1,1, large);
//		R.set(2,2, large);
//		R.set(3,3, large);
//		for (int i = 0; i < numberOfLandmarks; i++ )
//		{
//			if (landmarksSighted[i])
//			{		
//				R.set(i, i, pow(std_array[0],2) );
//			}		
//		}
//		if (DEBUG)
//		{
//			debug("Debug cat " +id + ": pos.ukf, z dim: " + z.getRowDimension() + " x " + z.getColumnDimension() + ", z:");
//			printM(z);
//		}
//		
//		//Calculate the difference between the cat current cat orinetation 
//		//and the one calculated from the cat position
//		double[] landmarkAngles = new double[numberOfLandmarks];
//		landmarkAngles = landmarkAngleEval(xc);
//		double[] orinetError = new double[numberOfLandmarks];
//		double orinetErrorSum = 0;
//		int landmarksSeen = 0;
//		boolean seesLandmark = false;
//		
//		//System.out.println("z rows: " + z.getRowDimension());
//		for (int i = 0; i < numberOfLandmarks; i++)
//		{
//			if (landmarksSighted[i])
//			{
//				orinetError[i] = z.get(i, 0) - landmarkAngles[i];
//				orinetErrorSum += orinetError[i];
//				landmarksSeen++;
//				seesLandmark = true;
//			}
//		}
//		double orinetErrorMean = 0;
//		if (seesLandmark)
//			orinetErrorMean  = orinetErrorSum/landmarksSeen;
//		
//		z.set(numberOfLandmarks-1+3, 0, orinetErrorMean);
//		
//		//One iteration with UKF
//		Matrix[] result = ufk_filter.ukf(f, xc, P, h, z, Q, R);
//		xc = result[0]; 
//		P = result[1];
//		
//		//Correct the cat orientation
//		catAngle -= orinetErrorMean;
//		catAngle = (catAngle+2*Math.PI)%(2*Math.PI);
//		
//		//Send updated position and orientation to billboard so the tracking filter can use it
//		billboard.setAbsolutePosition(id, getX(), getY(),
//				getAngle(), getTime());
//		
//		if (DEBUG)
//		{
//			debug("Debug cat " +id + ": pos.ukf, P dim: " + P.getRowDimension() + " x " + P.getColumnDimension() + ", P:");
//			printM(P);
//			debug("Debug cat " +id + ": pos.ukf, xc dim: " + xc.getRowDimension() + " x " + xc.getColumnDimension() + ", xc:");
//			printM(xc);
//		}
//		
//		//System.out.println("Cat: " + id + "xc: ");
//		//printM(xc);
//		/*
//		// Check x and y so they keep inside the arena and also set velocity in that direction to zero if outside the arena
//		if (xc.get(0, 0) < Arena.min_x) {
//			xc.set(0, 0, Arena.min_x);
//			//xc.set(3, 0, 0);
//		}
//		if (xc.get(0, 0) > Arena.max_x) {
//			xc.set(0, 0, Arena.max_x);
//			//xc.set(3, 0, 0);
//		}
//		if (xc.get(1, 0) < Arena.min_y) {
//			xc.set(1, 0, Arena.min_y);
//			//xc.set(4, 0, 0);
//		}
//		if (xc.get(1, 0) > Arena.max_y) {
//			xc.set(1, 0, Arena.max_y);
//			//xc.set(4, 0, 0);
//		}*/
//		
//		// Increase iteration timer (with full execution time)
//		
//		iterationTime += Clock.timestamp() - currentTime;
//		// Update public time
//		lastCurrentTime = currentTime;
//		
//		debug("Debug cat " +id + ", leaving update at iteration " + iterationCounter + ", current iterationTime= " + (Clock.timestamp() - currentTime) );
//	}//end of update
//	
//	/**
//	 * Draw particles (NOT brick material)
//	 */
//	/*
//	public void draw(Graphics g) {
//		// TODO: Remove graphics code from filter
//		final int size = 4; // Diameter
//		final int linelength = 8;
//		final int raylength = 1000;
//
//		Graphics2D g2 = (Graphics2D) g;
//
//		// Save the current tranform
//		AffineTransform oldTransform = g2.getTransform();
//
//		// Rotate and translate the actor
//		// g2.rotate(iangle, ix, iy);
//
//		// Plot position
//		g2.setColor(Color.red);
//		int ix = Actor.e2gX(getX());
//		int iy = Actor.e2gY(getY());
//		//debug("Debug, in abs.pos.ukf draw: ix= " + ix + ", iy= "+ iy);
//
//		double iangle = -getAngle();
//		g2.fillOval((int) ix - (size / 2), (int) iy - (size / 2), (int) size,
//				(int) size);
//		g2.drawLine((int) ix, (int) iy, (int) (ix + Math.cos(iangle)
//				* linelength), (int) (iy + Math.sin(iangle) * linelength));
//		//Draw landmark sightings as rays from the estimated cat position
//		switch (id){
//		case 0:
//			g2.setColor(Color.magenta);
//			break;
//		case 1:
//			g2.setColor(Color.green);
//			break;
//		case 2:
//			g2.setColor(Color.blue);
//			break;
//		}
//		for (int i = 0; i < numberOfLandmarks; i++ )
//		{
//			//if(landmarksSighted[i])
//			//{
//				g2.drawLine((int) ix, (int) iy, (int) (ix + Math.cos(-z.get(i, 0))
//						* raylength), (int) (iy + Math.sin(-z.get(i, 0)) * raylength));
//			//}
//		}
//		
//		// Reset the transformation matrix
//		g2.setTransform(oldTransform);
//	}*/
//	
//	public void run() {
//		/*
//		 * while (true) { update(); pause((long) (Clock.getTime() % Tint)); }
//		 */
//	}
//	
//	private void debug(Object info){
//		if (DEBUG) System.out.println(info);
//		
//	}	
}//End of class


