package GSim;

import lejos.util.Matrix;

public class CatFilterUKF
{
	//Instance variables
	
	public Matrix Xc;
	
	private IFunction f;
	private IFunction h;
	private UnscentedKalmanFilterTest filter;
	
	public CatFilterUKF()
	{
		filter = new UnscentedKalmanFilterTest();
		f = new FstateCat();  //nonlinear state equations
		h = new HmeasCat();  //measurement equation
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
