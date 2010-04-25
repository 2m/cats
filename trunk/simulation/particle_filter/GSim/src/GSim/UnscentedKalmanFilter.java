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
		beta=Math.pow(alpha, 2) -0.9f;  //lower bound -2; 10 5 10000 -2* -1 0 1 def:2; default, tunable
	
		lambda=Math.pow(alpha, 2)*(L+ki)-L;  
		c=L+lambda; 
		Wm = new Matrix(1, (2*L+1), 0.5/c);
		Wm.set(0,0,lambda/c);  
		Wc=Wm.copy();
		Wc.set(0,0, Wc.get(0,0) + 1 - Math.pow(alpha, 2) + beta);          
		c=Math.sqrt(c);
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
		Matrix[] ut_f_matrices= ut(f,X,Wm,Wc,L,Q);  //unscented transformation of process
		Matrix x1 = ut_f_matrices[0];
		Matrix X1 = ut_f_matrices[1];
		Matrix P1 = ut_f_matrices[2];
		Matrix X2 = ut_f_matrices[3];
		/*System.out.println("Debug: ukf, x1:");
		printM(x1);
		System.out.println("Debug: ukf, X1:");
		printM(X1);
		System.out.println("Debug: ukf, P1:");
		printM(P1);
		System.out.println("Debug: ukf, X2:");
		printM(X2);*/
		
		//Not used, X1=sigmas(x1,P1,c);  //sigma points around x1
		//Not used, X2=X1.minus(  x1.times( ones(1,X1.getColumnDimension()) )  );  //deviation of X1
		Matrix[] ut_h_matrices =ut(h,X1,Wm,Wc,m, new Matrix(1,1,(double)R) );  //unscented transformation of measurments
		Matrix z1 = ut_h_matrices[0];
		Matrix Z1 = ut_h_matrices[1];
		Matrix P2 = ut_h_matrices[2];
		Matrix Z2 = ut_h_matrices[3];
		/*System.out.println("Debug: ukf, z1:");
		printM(z1);
		System.out.println("Debug: ukf, Z1:");
		printM(Z1);
		System.out.println("Debug: ukf, P2:");
		printM(P2);
		System.out.println("Debug: ukf, Z2:");
		printM(Z2);*/
		Matrix P12 = ( X2.times(Matlab.diag(Wc)) ).times(Z2.transpose());  //transformed cross-covariance
		//System.out.println("Debug: ukf, P12:");
		//printM(P12);
		//Matrix K = P12.arrayRightDivide(P2);
		//Matrix K = P12.solve(P2); //TODO correct?? //.arrayLeftDivide(P2);  //old: P12*inv(P2);
		//Matrix K = P2.solve(P12);
		//Matrix K = P12.arrayLeftDivide(P2);
		Matrix K = P12.times(P2.inverse());
		//System.out.println("Debug: ukf, K:");
		//printM(K);
	
	    Matrix x_updated = x1.plus( K.times(z.minus(z1)) );  //state update
	    Matrix P_updated =P1.minus( K.times(P12.transpose()) );  //covariance update
	    Matrix[] output = {x_updated, P_updated};
		//System.out.println("Debug: ukf, x_updated:");
		//printM(x_updated);
		//System.out.println("Debug: ukf, P_updated:");
		//printM(P_updated);		    
		return output;
		
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
		

	}
	
	
	/**
	 * Unscented Transformation
	 * @param f  nonlinear map
	 * @param X  sigma points
	 * @param m  weights for mean
	 * @param Wc  weights for covariance
	 * @param n  number of outputs of f
	 * @param R  additive covariance
	 * @return y: transformed mean, Y: transformed sampling points, P: transformed covariance, Y1: transformed deviations 
	 */
	private Matrix[] ut(IFunction f, Matrix X, Matrix Wm, Matrix Wc, int n, Matrix R)
	{
		int L = X.getColumnDimension();
		Matrix y = Matlab.zeros(n,1);
		Matrix Y = Matlab.zeros(n,L);
		
		//System.out.println("Debug: ut, X:");
		//printM(X);
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
		//System.out.println("Debug: ut, y:");
		//printM(y);
		//System.out.println("Debug: ut, Y:");
		//printM(Y);
		
		Matrix Y1 = Y.minus(  y.times( Matlab.ones(1,Y.getColumnDimension()) )  );
		//System.out.println("Debug: ut, Y1:");
		//printM(Y1);	
		
		Matrix P = Y1.times(Matlab.diag(Wc));
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
	}
	
	/**
	 * Sigma points around reference point
	 * @param x  reference point  
	 * @param P  covariance  
	 * @param c  coefficient
	 * @return Sigma points
	 */
	private Matrix sigmas(Matrix x, Matrix P, double c)
	{
		Matrix A = new Matrix( Cholesky.cholesky( P.getArray() ) );
		A = (A.times(c)).transpose();
		//System.out.println("Debug: sigmas, A:");
		//printM(A);
		
		int n = x.getRowDimension();
		//System.out.println("Debug: sigmas, x:");
		//printM(x);
		
		//Create Y
		Matrix Y = new Matrix(n, n, 1);
		for (int j=0; j<n; j++)  //columns
		{
			Y.setMatrix(0, n-1, j, j, x);
		}
		//System.out.println("Debug: sigmas, Y:");
		//printM(Y);

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
	}
	
		
	
		
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
	
	
	
	public static void main(String args[])
	{
		
	}

}