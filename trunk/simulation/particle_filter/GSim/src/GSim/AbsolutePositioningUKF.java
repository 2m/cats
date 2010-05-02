package GSim;

import lejos.util.Matrix;

public class AbsolutePositioningUKF
{
	//Instance variables
	
	
	public Matrix Xc;
	
	private IFunction f;
	private IFunction h;
	private UnscentedKalmanFilterTest ufk_filter;
	
	public AbsolutePositioningUKF()
	{
		
		double[][] temp_s = {{0.0}, {0.0}, {1.0}};;  //initial state of the cats  TODO get initial state from buffer?
		Matrix s = new Matrix(temp_s);  //true state of the cats
		
		//TODO: fix R, covariance of measurement of the cats
		/*
		R=cell(1,nm);  % covariance of measurement of the cats
		for i=1:nm
		    R{1,i} = r(1)^2*eye(nz);            % covariance of measurement
		    for j=1:nz-n
		        R{1,i}(n+j,n+j) = r(j+1)^2;
		    end
		end
		*/
		
		//LandmarkList, true positions of the landmarks are in this static class. HmeasCat accesses the landmark list directly
		int n = LandmarkList.landmarkX.length;  //number of landmarks
		int nm=3;  //number of cats
		int nz=n+4;  //?? number of elements in the measurement vector of the cats = number of landmarks + 4
		int nx=6;  //number of variables in the cats' state vector
		
		UnscentedKalmanFilter ufk_filter = new UnscentedKalmanFilter(nx,nz);
		
		
		
		f = new FstateCat();  //nonlinear state equations
		h = new HmeasCat();  //measurement equation
		
		/*
		
		global actLandm; %the indices of the landmarks that are seen
		global dt; %sampling period, must be 1 for now
		global r;  %std of expected measurement noise 
		global ra; %std of actual measurement noise 
		global vc; %the velocities of the cats
		global k;  %current time step
		global N;  %total number of time steps
		global x;  %state vector of the cats
		global phi; %Angle of circular motion of the cats
		*/
		
		
		

		

		/*
		float q=0.1f;    //std of process
		float r=0.1f;    //std of measurement
		Matrix Q = Matlab.eye(nx);  //covariance of process
		Q = Q.times(Math.pow(q, 2));	
		float R=(float)Math.pow(r, 2);  //covariance of measurement
			
		
		Matrix x = new Matrix(3,3);  //initial state estimate
		x=s.copy();//s.plus( Matrix.random(3,1).times(q) );  //initial state with noise
		Matrix P = Matlab.eye(nx);  //initial state covraiance
		*/
		

		
		
		

	}
	public Matrix update(Matrix xc)
	{
		/*
		Matrix z = h.eval(s);  //h.eval(s).plus( Matrix.random(1,1).times(r) );  //measurments		
		Matrix[] result = ukf_obj.ukf(f, x, P, h, z, Q, R);
		x = result[0];
		P = result[1];
		s = f.eval(s);  //update process 
		*/
		
		return null;
	}

}
