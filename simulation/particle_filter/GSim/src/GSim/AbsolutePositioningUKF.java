package GSim;

import lejos.util.Matrix;
import static java.lang.Math.*;
import static GSim.Matlab.*;

public class AbsolutePositioningUKF //extends AbsolutePositioningFilter
{
	//Instance variables
	
	
	private UnscentedKalmanFilterTest ufk_filter;
	private IFunction f;
	private Matrix xc;
	private Matrix[] P;
	private IFunction h;
	private Matrix z;
	private Matrix Q;
	private Matrix[] R;

	
	/*
	public AbsolutePositioningUKF(float T, Buffer sensorData, Buffer movementData, RealTimeClock rttime)		
	{	
		super(T, sensorData, movementData, rttime);
	*/
	public AbsolutePositioningUKF()		
	{		
		//LandmarkList, true positions of the landmarks are in this static class. HmeasCat accesses the landmark list directly
		int n = 1;//TODO use the following: LandmarkList.landmarkX.length;  //number of landmarks
		int nm=3;  //number of cats
		int nz=n+4;  //?? number of elements in the measurement vector of the cats = number of landmarks + 4
		int nx=6;  //number of variables in the cats' state vector
		
		UnscentedKalmanFilter ufk_filter = new UnscentedKalmanFilter(nx,nz);
	
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
		
		R = new Matrix[nm];  //covariance of measurement of the cats
		for (int i=1; i<=nm; i++)
		{
			R[i-1] = eye(nz).timesEquals( pow(r.get(1-1,1-1),2) );  // covariance of measurement of the cats
	
			for (int j=1; j<=nz-n; j++)
			{
				R[i-1].set( n+j-1, n+j-1, pow(r.get(1-1, j+1-1),2) );
			}
		}
		
		f = new FstateCat();  //nonlinear state equations
		h = new HmeasCat();  //measurement equation
		
		P = new Matrix[nm];  //initial state covariance
		for (int i=1; i<=nm; i++)
		{
			P[i-1] = eye(nz).timesEquals( pow(10,-3) );
		}
		
		Matrix s = zeros(nx,nm);  //true state of the cats, each cats state is a column
		xc = s.copy();  //initial state
		/*
		double[][] temp_s = {{0.0}, {0.0}, {1.0}};;  //initial state of the cats  TODO get initial state from buffer?
		Matrix s = new Matrix(temp_s);  //true state of the cats
		*/
		

		/*
		
		global actLandm; %the indices of the landmarks that are seen
		global ra; %std of actual measurement noise 
		global vc; %the velocities of the cats
		global k;  %current time step
		global N;  %total number of time steps
		global x;  %state vector of the cats
		global phi; %Angle of circular motion of the cats
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
		/*
		// Set means for x, y and angle
		mean_x = x;
		mean_y = y;
		mean_angle = angle)
		// Set (co-)variance variables
		// std => 5cm
		varXX = Fixed.floatToFixed(0.0025);
		varYY = Fixed.floatToFixed(0.0025);
		// No co-variance
		varXY = Fixed.floatToFixed(0.0);
		// std => 3 degrees
		varAngle = Fixed.floatToFixed(Math.pow(3 * (Math.PI / 180), 2));
		*/
	}
	
	
	public Matrix update(Matrix xc)
	{/*
		
		//One iteration with UKF
		Matrix z = h.eval(s);  //h.eval(s).plus( Matrix.random(1,1).times(r) );  //measurments		
		Matrix[] result = ukf(f, xc, P, h, z, Q, R);
		x = result[0];  
		P = result[1];
		s = f.eval(s);  //update process 
		
		*/
		return null;
	}
	
	/**
	 * Updates the filter without explicitly returning the values
	 */
	public void update()
	{

	}
	
	/** Poll estimated x position value from filter */
	public float getX() {
		return (float) 0.0;
	}

	/** Poll estimated y position value from filter */
	public float getY() {
		return (float) 0.0;
	}

	/** Poll estimated direction angle value from filter */
	public float getAngle() {
		return (float) 0.0;
	}
	
	public static void main(String args[])
	{
		Buffer sensorData;
		Buffer movementData;
		float T;
		RealTimeClock rttime;
		AbsolutePositioningUKF test = new AbsolutePositioningUKF();
	}

}
