package GSim;

import lejos.util.Matrix;

/**
 * The Unscented Kalman Filter 
 * @author Edvard
 */
public class UnscentedKalmanFilter implements IUnscentedKalmanFilter
{
	//Instance variables
	/** Number of states. */
	private int L;  
	
	/** Number of measurements*/
	private int m;  //
	
	/** Tunable. */
	private float alpha;  
	
	/** Tunable. */
	private float ki;
	
	/** Tunable. */
	private float beta;
	
	/** Scaling factor. */
	private float lambda;
	
	/** Scaling factor. */
	private float c; 
	
	/** Weights for means. */
	private Matrix Wm; 
	
	/** Weights for covariance. */
	private Matrix Wc; 

	/**Constructor
	 * @param L  number of states
	 * @param m  number of measurements
	 */
	public UnscentedKalmanFilter(int L, int m)
	{
		this.L = L;
		this.m = m;
		alpha=3.5f;  //1e-3 default
		ki=0;  //default
		beta=(float)Math.pow(alpha, 2) -0.9f;  //lower bound -2; 10 5 10000 -2* -1 0 1 def:2; default, tunable
	
		lambda=(float)Math.pow(alpha, 2)*(L+ki)-L;  
		c=L+lambda; 
		Wm = new Matrix(1, (2*L+1), 0.5/c);
		Wm.set(0,0,lambda/c);  
		Wc=Wm.copy();
		Wc.set(0,0, Wc.get(0,0) + 1 - Math.pow(alpha, 2) + beta);          
		c=(float)Math.sqrt(c);
		//The code above (in the constructor) has been compared with the original matlab and should be ok			
	}
	

    /* (non-Javadoc)
     * @see IUnscentedKalmanFilter#ukf()
     */
	public Matrix[] ukf(IFunction f, Matrix x, Matrix P, IFunction h, Matrix z, Matrix Q, float R)
	{
		//L=x.getColumnDimension();  //Currently not needed //TODO check that its the right dimension (column/row)
		//m=z.getColumnDimension(); 
		
		Matrix X = sigmas(x,P,c);  //sigma points around x, NB: c has been set in the constructor
		ut(f,X,Wm,Wc,L,Q);  //unscented transformation of process
		
		/*
		[x1,X1,P1,X2]=ut(fstate,X,Wm,Wc,L,Q);          %unscented transformation of process
		% X1=sigmas(x1,P1,c);                         %sigma points around x1
		% X2=X1-x1(:,ones(1,size(X1,2)));             %deviation of X1
		[z1,Z1,P2,Z2]=ut(hmeas,X1,Wm,Wc,m,R);       %unscented transformation of measurments
		P12=X2*diag(Wc)*Z2';                        %transformed cross-covariance
		K=P12/P2;           %old: P12*inv(P2);

		Ktemp=K;
		if nargin>7
		    for i=1:length(Kzero)
		        Ktemp(:,Kzero(i))=0;
		    end
		    x=x1+Ktemp*(z-z1);                              %state update
		    P=P1;
		else
		    x=x1+K*(z-z1);                              %state update
		    P=P1-K*P12';                                %covariance update
		end
		*/
		
		return null;
	}
	
	
	/**TODO implement
	 * Unscented Transformation
	 * @param f  nonlinear map
	 * @param X  sigma points
	 * @param m  weights for mean
	 * @param Wc  weights for covariance
	 * @param n  number of outputs of f
	 * @param R  additive covariance
	 * @return y: transformed mean, Y: transformed sampling points, P: transformed covariance, Y1: transformed deviations 
	 */
	private Object ut(IFunction f, Matrix X, Matrix Wm, Matrix Wc, int n, Matrix R)
	{
		int L = X.getColumnDimension();
		Matrix y = new Matrix(n,1,0);
		Matrix Y = new Matrix(n,L,0);
		
		System.out.println("Debug: ut, X:");
		printM(X);
		//System.out.println("Debug: ut, y:");
		//printM(y);
		//System.out.println("Debug: ut, Y:");
		//printM(Y);
		

		for (int k=0; k<L; k++)
		{
			
			//for all columns in X, compute fstate for the given row vector and put the result in Y
			Matrix row_in_X = X.getMatrix(0, X.getRowDimension()-1, k, k);
			//System.out.println("Debug: ut, row_in_X:");
			//printM(row_in_X);
			Y.setMatrix(0, Y.getRowDimension()-1, k, k, f.eval(row_in_X) );
			//System.out.println("Debug: ut, Y:");
			//printM(Y);
			Matrix row_in_Y = Y.getMatrix(0, Y.getRowDimension()-1, k, k);
			//System.out.println("Debug: ut, row_in_Y:");
			//printM(row_in_Y);
			y.setMatrix( 0, y.getRowDimension()-1, 0, 0, ( row_in_Y.times(Wm.get(0, k)) ).plus(y)  );
			//System.out.println("Debug: ut, y:");
			//printM(y);
		}
		System.out.println("Debug: ut, y:");
		printM(y);
		System.out.println("Debug: ut, Y:");
		printM(Y);
		
		Matrix Y1 = Y.minus(  y.times( new Matrix(1,Y.getColumnDimension(),1) )  );
		System.out.println("Debug: ut, Y1:");
		printM(Y1);	
		
		Matrix Wc_diag = Matrix.identity(Wc.getColumnDimension(), Wc.getColumnDimension());
		//TODO creat diagonal and calculate P
		
		
		/*
		L=size(X,2);
		y=zeros(n,1);
		Y=zeros(n,L);
		for k=1:L                   
		    Y(:,k)=f(X(:,k));       
		    y=y+Wm(k)*Y(:,k);       
		end
		Y1=Y-y(:,ones(1,L));
		P=Y1*diag(Wc)*Y1'+R;   
		*/
		
		return null;
	}
	
	/**
	 * Sigma points around reference point
	 * @param x  reference point  
	 * @param P  covariance  
	 * @param c  coefficient
	 * @return Sigma points
	 */
	private Matrix sigmas(Matrix x, Matrix P, float c)
	{
		Matrix A = new Matrix( Cholesky.cholesky( P.getArray() ) );
		A = (A.times(c)).transpose();
		System.out.println("Debug: sigmas, A:");
		printM(A);
		
		int n = x.getRowDimension();
		System.out.println("Debug: sigmas, x:");
		printM(x);
		
		//Create Y
		Matrix Y = new Matrix(n, n, 1);
		for (int j=0; j<n; j++)  //columns
		{
			Y.setMatrix(0, n-1, j, j, x);
		}
		System.out.println("Debug: sigmas, Y:");
		printM(Y);

		//Create X
		Matrix X = new Matrix(n,(1+n+n));
		
		X.setMatrix(0, n-1, 0, 0, x);

		Matrix Y_plus_A = Y.plus(A);		
		X.setMatrix(0, n-1, 1, n, Y_plus_A);
		
		Matrix Y_minus_A = Y.minus(A);
		X.setMatrix(0, n-1, n+1, n+n, Y_minus_A);
		
		System.out.println("Debug: sigmas, X:");
		printM(X);
		
		return X;
	}

	public static void main(String args[])
	{
		//Example, for testing
		System.out.println("Test started");
		UnscentedKalmanFilter ukf_test = new UnscentedKalmanFilter(3,1);
		System.out.println("Debug: printing ukf object:\n" +ukf_test);
		
		int n=3;      //number of state
		float q=0.1f;    //std of process
		float r=0.1f;    //std of measurement
		Matrix Q = Matrix.identity(n, n);  //covariance of process
		Q = Q.times(Math.pow(q, 2));	
		float R=(float)Math.pow(r, 2);  //covariance of measurement
		IFunction f = new Fstate(); //@(x)[x(2);x(3);0.05*x(1)*(x(2)+x(3))];  % nonlinear state equations
		IFunction h = new Hmeas();  //h=@(x)x(1);                               % measurement equation
		
		double[][] temp_s = new double[3][1];  //initial state
		temp_s[0][0] = 0;
		temp_s[1][0] = 0;
		temp_s[2][0] = 1;
		Matrix s = new Matrix(temp_s);
		Matrix x = new Matrix(3,3);  //initial state 
		x=s.copy();//s.plus( Matrix.random(3,1).times(q) );  //initial state with noise
		Matrix P = Matrix.identity(n, n);  //initial state covraiance
		
		Matrix z = h.eval(s).plus( Matrix.random(1,1).times(r) );  //measurments		
		Matrix[] result = ukf_test.ukf(f, x, P, h, z, Q, R);
	}
	
	
	/**
	 * Prints a filter object
	 */
	public String toString()
	{

		String s ="";
		s = " L = " + L + "\n m = " + m + "\n";
		s += " alpha = " + alpha + "\n ki = " + ki + "\n beta = " + beta +
		"\n lambda = " + lambda + "\n c = " + c + "\n Wm = " + MatrixToString(Wm) + " Wc = " + MatrixToString(Wc);

		return s;	
	}	
	
	/**
	 * Converts a matrix to a string ready to be printed
	 * The values are rounded to floats (from doubles).
	 * @param m  input matrix
	 * @return a string representation of the matrix
	 */
	public static String MatrixToString(Matrix m)
	{
		double[][] print_m = m.getArray();
		String s = "";
		for (int i=0; i<m.getRowDimension(); i++)
		{
			for (int j=0; j<m.getColumnDimension(); j++)
			{
				s += (float)print_m[i][j] + "  ";
			}
			s += "\n";
		}
		return s;
	}
	
	/**
	 * Prints a matrix.
	 * @param m input matrix
	 */
	public static void printM(Matrix m){
		System.out.println(MatrixToString(m));
	}

}



