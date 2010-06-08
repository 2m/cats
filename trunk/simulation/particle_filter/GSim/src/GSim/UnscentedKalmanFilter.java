package GSim;

import lejos.util.Matrix;
import static java.lang.Math.*;
import static GSim.Matlab.*;

/**
 * The Unscented Kalman Filter 
 * @author Edvard Zak, Nils Tornblom 
 */
public class UnscentedKalmanFilter
{
	//Class constants 
	/* True position and other information like color of the landmarks
	 * are stored in the public static list of landmarks.
	 * The evaluation functions access this variable directly.
	 */
	
	//Instance variables
	/** Number of states. */
	private int L;  
	
	/** Number of measurements*/
	private int m;  
	
	/** Tunable. */
	private double alpha;  
	
	/** Tunable. */
	private double ki;
	
	/** Tunable. */
	private double beta;
	
	/** Scaling factor. */
	private double lambda;
	
	/** Scaling factor. */
	private double c; 
	
	/** Weights for means. */
	private Matrix Wm; 
	
	/** Weights for covariance. */
	private Matrix Wc; 
	
	private final boolean DEBUG = false;
	private final boolean DEBUG_LIGHT = false;
	private final boolean DEBUG2 = false; //ukf.ut()
	private final boolean DEBUG3 = false;  //ukf.sigmas()

	/**Constructor
	 * @param L  number of states
	 * @param m  number of measurements
	 */
	public UnscentedKalmanFilter(int L, int m)
	{
		this.L = L;
		this.m = m;
		alpha=3e-2f;//3.5f;  //1e-3 default
		ki=0;  //default
		beta=2f;//pow(alpha, 2) -0.9f;  //lower bound -2; 10 5 10000 -2* -1 0 1 def:2; default, tunable
		lambda=pow(alpha, 2)*(L+ki)-L;  
		c=L+lambda; 
		Wm = new Matrix(1, (2*L+1), 0.5/c);
		Wm.set(0,0,lambda/c);  
		Wc=Wm.copy();
		Wc.set(0,0, Wc.get(0,0) + 1 - pow(alpha, 2) + beta);          
		c=sqrt(c);	
	}
	

	/**
	 * UKF, Unscented Kalman Filter, for nonlinear dynamic systems. 
	 * [x, P] = ukf(f,x,P,h,z,Q,R) returns state estimate x and state covariance P 
	 * for nonlinear dynamic system (for simplicity, noises are assumed as additive):
	 *            x_k+1 = f(x_k) + w_k
	 *            z_k   = h(x_k) + v_k
	 * where w ~ N(0,Q) meaning w is gaussian noise with covariance Q and 
	 *       v ~ N(0,R) meaning v is gaussian noise with covariance R.         
	 * @param f  function handle for f(x), nonlinear state equations
	 * @param x  "a priori" state estimate
	 * @param P  "a priori" estimated state covariance
	 * @param h  fanction handle for h(x), measurement equation
	 * @param z  current measurement
	 * @param Q  process noise covariance
	 * @param R  measurement noise covariance
	 * @return  "a posteriori" state estimate and P: "a posteriori" state covariance
	 */
	public Matrix[] run_ukf(IFunction f, Matrix[] x_and_P, IFunction h, Matrix z, Matrix Q, Matrix R)
	{
		if (DEBUG ||DEBUG_LIGHT)
		{	System.out.println("Entering ukf with the following parameters:");	
			System.out.println("Debug: ukf, x dim= " + x_and_P[0].getRowDimension() + " x " + x_and_P[0].getColumnDimension() + ", x= ");
			printM(x_and_P[0]);
			System.out.println("Debug: ukf, P dim= " + x_and_P[1].getRowDimension() + " x " + x_and_P[1].getColumnDimension() + ", P= ");
			printM(x_and_P[1]);
			System.out.println("Debug: ukf, z dim= " + z.getRowDimension() + " x " + z.getColumnDimension() + ", z= ");
			printM(z);
			System.out.println("Debug: ukf, Q dim= " + Q.getRowDimension() + " x " + Q.getColumnDimension() + ", Q= ");
			printM(Q);	
			System.out.println("Debug: ukf, R dim= " + R.getRowDimension() + " x " + R.getColumnDimension() + ", R= ");	
			printM(R);	
		}
		if (DEBUG)
		{
			System.out.println("Debug: ukf, Wm dim= " + Wm.getRowDimension() + " x " + Wm.getColumnDimension() + ", Wm= ");
			printM(Wm);
			System.out.println("Debug: ukf, Wc dim= " + Wc.getRowDimension() + " x " + Wc.getColumnDimension() + ", Wc= ");
			printM(Wc);
			System.out.println("Starting calculations");
		}
		if (DEBUG ||DEBUG_LIGHT)
		{
			if (x_and_P[0].getRowDimension() != L || x_and_P[0].getColumnDimension() != 1) System.out.println("WARNING: The dimension of the state vector (matrix) x is incorrect! Expected dim = " + L +" x 1" );
			if (x_and_P[1].getRowDimension() != L || x_and_P[1].getColumnDimension() != L) System.out.println("WARNING: The dimension of the state covariance matrix P is incorrect! Expected dim = " + L +" x " + L);	
			if (Q.getRowDimension() != L || Q.getColumnDimension() != L) System.out.println("WARNING: The dimension of the covariance of process matrix Q is incorrect! Expected dim = " + L +" x " + L);	
			if (z.getRowDimension() != m || z.getColumnDimension() != 1) System.out.println("WARNING: The dimension of the measurement vector (matrix) z is incorrect! Expected dim = " + m +" x 1");
			if (R.getRowDimension() != m || R.getColumnDimension() != m) System.out.println("WARNING: The dimension of the covariance of measurement matrix P is incorrect! Expected dim = " + m +" x " + m);
		}
	
		Matrix X = sigmas(x_and_P[0],x_and_P[1],c);  //sigma points around x, NB: c has been set in the constructor
		if (DEBUG)
		{
			System.out.println("Debug: ukf, X dim= " + X.getRowDimension() + " x " + X.getColumnDimension() + ", X (after sigmas() )= ");
			printM(X);
		}

		Matrix[] ut_f_matrices= ut(f,X,Wm,Wc,L,Q);  //unscented transformation of process
		Matrix x1 = ut_f_matrices[0];
		Matrix X1 = ut_f_matrices[1];
		Matrix P1 = ut_f_matrices[2];
		Matrix X2 = ut_f_matrices[3];
		if (DEBUG)
		{
			System.out.println("Debug: ukf, x1 dim= " + x1.getRowDimension() + " x " + x1.getColumnDimension() + ", x1= ");
			printM(x1);
			System.out.println("Debug: ukf, X1 dim= " + X1.getRowDimension() + " x " + X1.getColumnDimension() + ", X1= ");
			printM(X1);
			System.out.println("Debug: ukf, P1 dim= " + P1.getRowDimension() + " x " + P1.getColumnDimension() + ", P1= ");
			printM(P1);
			System.out.println("Debug: ukf, X2 dim= " + X2.getRowDimension() + " x " + X2.getColumnDimension() + ", X2= ");
			printM(X2);
		}
		
		//Not used, X1=sigmas(x1,P1,c);  //sigma points around x1
		//Not used, X2=X1.minus(  x1.times( ones(1,X1.getColumnDimension()) )  );  //deviation of X1
		Matrix[] ut_h_matrices =ut(h,X1,Wm,Wc,m, R);  //unscented transformation of measurments
		Matrix z1 = ut_h_matrices[0];
		Matrix Z1 = ut_h_matrices[1];
		Matrix P2 = ut_h_matrices[2];
		Matrix Z2 = ut_h_matrices[3];
		if (DEBUG)
		{
			System.out.println("Debug: ukf, z1 dim= " + z1.getRowDimension() + " x " + z1.getColumnDimension() + ", z1= ");
			printM(z1);
			System.out.println("Debug: ukf, Z1 dim= " + Z1.getRowDimension() + " x " + Z1.getColumnDimension() + ", Z1= ");
			printM(Z1);
			System.out.println("Debug: ukf, P2 dim= " + P2.getRowDimension() + " x " + P2.getColumnDimension() + ", P2= ");
			printM(P2);
			System.out.println("Debug: ukf, Z2 dim= " + Z2.getRowDimension() + " x " + Z2.getColumnDimension() + ", Z2= ");
			printM(Z2);
		}
		
		Matrix P12 = ( X2.times(diagFromColumn(Wc)) ).times(Z2.transpose());  //transformed cross-covariance
		if (DEBUG)
		{
			System.out.println("Debug: ukf, P12 dim= " + P12.getRowDimension() + " x " + P12.getColumnDimension() + ", P12= ");
			printM(P12);
		}
		//Matrix K = P12.arrayRightDivide(P2);
		//Matrix K = P12.solve(P2); //TODO correct?? //.arrayLeftDivide(P2);  //old: P12*inv(P2);
		//Matrix K = P2.solve(P12);
		//Matrix K = P12.arrayLeftDivide(P2);
		
		//Before: P12.times(P2.inverse());
		//This should be faster (matrix inverse is slow and numerically less stable):
		Matrix K = P2.transpose().solve(P12.transpose()).transpose();
		//can be used since: A/B is equivalent to (B'\A')'
		
		if (DEBUG)
		{
			System.out.println("Debug: ukf, K dim= " + K.getRowDimension() + " x " + K.getColumnDimension() + ", K= ");
			printM(K);
		}
		
		x_and_P[0] = x1.plus( K.times(z.minus(z1)) );  //state update
		x_and_P[1] = P1.minus( K.times(P12.transpose()) );  //covariance update
	    //Matrix[] output = {x_updated, P_updated};
	    if (DEBUG ||DEBUG_LIGHT)
		{
			System.out.println("Leaving ukf with the following results:");
			System.out.println("Debug: ukf, x_updated dim= " + x_and_P[0].getRowDimension() + " x " + x_and_P[0].getColumnDimension() + ", x_updated= ");
			printM(x_and_P[0]);
			System.out.println("Debug: ukf, P_updated dim= " + x_and_P[1].getRowDimension() + " x " + x_and_P[1].getColumnDimension() + ", P_updated= ");
			printM(x_and_P[1]);		
		}
		return x_and_P;
		
		
		/*
		Original matlab code:
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
	}//end of ukf()
	
	
	/**
	 * Unscented Transformation
	 * @param f  nonlinear map
	 * @param X  sigma points
	 * @param Wm  weights for mean
	 * @param Wc  weights for covariance
	 * @param n  number of outputs of f
	 * @param R  additive covariance
	 * @return y: transformed mean, Y: transformed sampling points, P: transformed covariance, Y1: transformed deviations 
	 */
	private Matrix[] ut(IFunction func, Matrix X, Matrix Wm, Matrix Wc, int n, Matrix R)
	{
		int L = X.getColumnDimension();
		Matrix y = zeros(n,1);
		Matrix Y = zeros(n,L);
		
		//System.out.println("Debug: ut, X:");
		//printM(X);
		//System.out.println("Debug: ut, y:");
		//printM(y);
		//System.out.println("Debug: ut, Y:");
		//printM(Y);
		
		int X_row_dim = X.getRowDimension();
		int Y_row_dim = Y.getRowDimension();
		int y_row_dim = y.getRowDimension();
		Matrix row_in_X;
		for (int k=0; k<L; k++)
		{	
			//for all columns in X, compute fstate for the given row vector and put the result in Y
			row_in_X = X.getMatrix(0, X_row_dim-1, k, k);
			/*if (DEBUG2)
			{
				System.out.println("Debug: ukf.ut, row_in_X =");
				printM(row_in_X);s
			}*/
			Y.setMatrix(0, Y_row_dim-1, k, k, func.eval(row_in_X) );
			/*if (DEBUG2)
			{
				System.out.println("Debug: ukf.ut, Y (after function evaluation) =");
				printM(Y);
			}*/
			//Matrix row_in_Y = Y.getMatrix(0, Y.getRowDimension()-1, k, k);
			//System.out.println("Debug: ut, row_in_Y:");
			//printM(row_in_Y);
			y.setMatrix( 0, y_row_dim-1, 0, 0, ( (Y.getMatrix(0, Y_row_dim-1, k, k)).times(Wm.get(0, k)) ).plus(y)  );
			//System.out.println("Debug: ut, y:");
			//printM(y);
		}
		
		//System.out.println("Debug: ut, y:");
		//printM(y);
		//System.out.println("Debug: ut, Y:");
		//printM(Y);
		
		Matrix Y1 = Y.minus(  y.times( Matlab.ones(1,Y.getColumnDimension()) )  );
		//System.out.println("Debug: ut, Y1:");
		//printM(Y1);	
		
		Matrix P = Y1.times(diagFromColumn(Wc));
		P = P.times(Y1.transpose());
		P.plusEquals(R);
		//System.out.println("Debug: ut, P:");
		//printM(P);	
		
		//create output matrix array
		Matrix[] output = {y,Y,P,Y1};
		
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
		
		return output;
	}//end of ut()
	
	/**
	 * Sigma points around reference point
	 * @param x  reference point  
	 * @param P  covariance  
	 * @param c  coefficient
	 * @return Sigma points
	 */
	private Matrix sigmas(Matrix x, Matrix P, double c)
	{
		if (DEBUG3)
		{
			System.out.println("Debug: ukf.sigmas, c = " + c);
		}
		
		Matrix A = new Matrix( Cholesky.cholesky( P.getArray() ) );
		//A = (A.times(c)).transpose();  //Incorrect, not transposed...
		A = A.times(c);
		A.transpose();
		if (DEBUG3)
		{
			System.out.println("Debug: ukf.sigmas, A:");
			printM(A);
		}

		
		int n = x.getRowDimension();
		//System.out.println("Debug: sigmas, x:");
		//printM(x);
		
		//Create Y
		Matrix Y = new Matrix(n, n, 1);
		for (int j=0; j<n; j++)  //columns
		{
			Y.setMatrix(0, n-1, j, j, x);
		}
		if (DEBUG3)
		{
			System.out.println("Debug: ukf.sigmas, Y:");
			printM(Y);
		}

		//Create X
		Matrix X = new Matrix(n,(1+n+n));
		
		X.setMatrix(0, n-1, 0, 0, x);

		Matrix Y_plus_A = Y.plus(A);		
		X.setMatrix(0, n-1, 1, n, Y_plus_A);
		
		Matrix Y_minus_A = Y.minus(A);
		X.setMatrix(0, n-1, n+1, n+n, Y_minus_A);
		
		//System.out.println("Debug: sigmas, X:");
		//printM(X);
		
		return X;
	}//end of sigmas()
	
		
	/**
	 * Prints a filter object
	 */
	public String toString()
	{

		String s ="";
		s = " L = " + L + "\n m = " + m + "\n";
		s += " alpha = " + alpha + "\n ki = " + ki + "\n beta = " + beta +
		"\n lambda = " + lambda + "\n c = " + c + "\n Wm = " + Matlab.MatrixToString(Wm) + " Wc = " + Matlab.MatrixToString(Wc);

		return s;	
	}	

}//end of class

/*int X_row_dim = X.getRowDimension();
int Y_row_dim = Y.getRowDimension();
int y_row_dim = y.getRowDimension();
Matrix row_in_X = zeros(X_row_dim,1);
Matrix row_in_Y = zeros(Y_row_dim,1);
for (int k=0; k<L; k++)
{	
	//for all columns in X, compute fstate for the given row vector and put the result in Y
	row_in_X.setMatrix(0, X_row_dim-1, 0, 0, X.getMatrix(0, X_row_dim-1, k, k) );
	if (DEBUG2)
	{
		System.out.println("Debug: ukf.ut, row_in_X =");
		printM(row_in_X);
	}
	Y.setMatrix(0, Y_row_dim-1, k, k, func.eval(row_in_X) );
	if (DEBUG2)
	{
		System.out.println("Debug: ukf.ut, Y (after function evaluation) =");
		printM(Y);
	}
	//row_in_Y.setMatrix(0, Y_row_dim-1, 0, 0, Y.getMatrix(0, Y_row_dim-1, k, k) );
	//System.out.println("Debug: ut, row_in_Y:");
	//printM(row_in_Y);
	y.setMatrix( 0, y_row_dim-1, 0, 0, ( (Y.getMatrix(0, Y_row_dim-1, k, k)).times(Wm.get(0, k)) ).plus(y)  );
	//System.out.println("Debug: ut, y:");
	//printM(y);
}*/