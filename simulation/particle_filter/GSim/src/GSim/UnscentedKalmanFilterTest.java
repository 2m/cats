package GSim;

import lejos.util.Matrix;

/**
 * A class for testing The Unscented Kalman Filter on some basic functions and inputs.
 * @author Edvard
 */
public class UnscentedKalmanFilterTest {
	
	//Instance variables
	public IFunction f_example;
	public IFunction h_example;
	
	/**
	 * Default constructor for
	 */
	public UnscentedKalmanFilterTest(){
		f_example = new FstateExample();	
		h_example = new HmeasExample();
	}
	
	/**
	 * An example state function used in this test
	 * @author Edvard
	 */
	public class FstateExample implements IFunction{
		public Matrix eval(Matrix x){	
			//Basic state function to test the ukf implementation
			//x is a (n)x(1) Matrix		
			Matrix output = new Matrix(x.getRowDimension(),x.getColumnDimension());
			//System.out.println("Debug: In Fstate.eval(), x:");
			//UnscentedKalmanFilter.printM(x);	
			output.set(0, 0, x.get(1,0) );
			output.set(1, 0, x.get(2,0) );
			output.set(2, 0, 0.05 * x.get(0,0) * (x.get(1,0) + x.get(2,0)) );	
			//System.out.println("Debug: In testing.Fstate.eval(), output:");
			//UnscentedKalmanFilter.printM(output);				
			return output; 
		}
	}
	/**
	 * An example measurement function used in this test.
	 * @author Edvard
	 */
	public class HmeasExample implements IFunction{	
		public Matrix eval(Matrix x){
			//Basic measurement function to test the ukf implementation
			//x is a (1)x(1) Matrix
			Matrix output = new Matrix(1,1,x.get(0,0));
			return output; 
		}
	}

	
	public static void main(String args[]) throws Exception
	{
		//Instance of UnscentedKalmanFilterTest, need to access the example state/measurement functions
		UnscentedKalmanFilterTest test = new UnscentedKalmanFilterTest();
		
		//Example values used for testing
		System.out.println("Test started");
		UnscentedKalmanFilter ukf_test = new UnscentedKalmanFilter(3,1);
		System.out.println("Debug: printing ukf object:\n" +ukf_test);
		
		int n=3;      //number of state
		float q=0.1f;    //std of process
		float r=0.1f;    //std of measurement
		Matrix Q = Matrix.identity(n, n);  //covariance of process
		Q = Q.times(Math.pow(q, 2));	
		float R=(float)Math.pow(r, 2);  //covariance of measurement
		IFunction f = test.f_example; //@(x)[x(2);x(3);0.05*x(1)*(x(2)+x(3))];  % nonlinear state equations
		IFunction h = test.h_example;  //h=@(x)x(1);          % measurement equation
		
		double[][] temp_s = {{0.0}, {0.0}, {1.0}};;  //initial state
		/*temp_s[0][0] = 0;
		temp_s[1][0] = 0;
		temp_s[2][0] = 1;*/
		Matrix s = new Matrix(temp_s);
		Matrix x = new Matrix(3,3);  //initial state 
		x=s.copy();//s.plus( Matrix.random(3,1).times(q) );  //initial state with noise
		Matrix P = Matrix.identity(n, n);  //initial state covraiance
		
		//First iteration with UKF
		Matrix z = h.eval(s);  //h.eval(s).plus( Matrix.random(1,1).times(r) );  //measurments		
		Matrix[] result = ukf_test.ukf(f, x, P, h, z, Q, R);
		x = result[0];
		P = result[1];
		s = f.eval(s);  //update process 

		System.out.println("Debug: ukf, x:");
		UnscentedKalmanFilter.printM(x);
		System.out.println("Debug: ukf, P:");
		UnscentedKalmanFilter.printM(P);	
		
		//Verify correctness
		double[][] x_updated_arr = x.getArray();
		double[][] x_correct_arr = {{0.0}, {1.0}, {0.0}};
		System.out.println("Debug: ukfTest, x_correct_arr:" );
		UnscentedKalmanFilter.printM( new Matrix(x_correct_arr) );
		
		for (int i = 0; i<3; i++)
		{
			for (int j = 0; j<1; j++)
			{
				if( Math.abs(x_updated_arr[i][j] - x_correct_arr[i][j]) > 0.01)
				{
					//Assert doesn't seem to work :( */						
					Exception Exception = null ;
					throw Exception ;
				}
			}
		} 
		assert(false);
		double[][] P_updated_arr = P.getArray();
		double[][] P_correct_arr = {{0.019900990099010,  0.0,  0.0},{0.0,  1.010000000000000,  0.0},{0.0,  0.0,  0.012500000000000}}; 
		System.out.println("Debug: ukfTest, P_correct_arr:" );
		UnscentedKalmanFilter.printM( new Matrix(P_correct_arr) );
		for (int i = 0; i<3; i++)
		{
			for (int j = 0; j<3; j++)
			{
				if( Math.abs(P_updated_arr[i][j] - P_correct_arr[i][j]) > 0.01){
					//Assert doesn't seem to work :( */					
					Exception Exception = null ;
					throw Exception ;
				}
			}
		} 
		/*
		//Further iterations with UKF
		for (int i=1; i<20; i++)
		{
			z = h.eval(s);  //measurments		
			result = ukf_test.ukf(f, x, P, h, z, Q, R);
			x = result[0];
			P = result[1];
			s = f.eval(s);  //update process 
		}
		
		System.out.println("Debug: ukf test, x after multiple iterations:");
		UnscentedKalmanFilter.printM(x);
		System.out.println("Debug: ukf test, P after multiple iterations:");
		UnscentedKalmanFilter.printM(P);	*/
		

	}

}
