package GSim;

import lejos.util.Matrix;

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
		Wm.set(0,0,lambda/c);  //TODO Check if correctly converted from matlab 
		Wc=Wm.copy();
		Wc.set(0,0, Wc.get(0,0) + 1 - Math.pow(alpha, 2) + beta);  //TODO Check if correctly converted from matlab             
		c=(float)Math.sqrt(c);


		
		/*int test_uygsudf = 3;
		private Matrix test = new Matrix(1,1,(double)2.0);
		double[] test2 =  {1,2,3};
		Matrix test3 = new Matrix(test2,test2.length);
		
		Matrix P = new Matrix(L,L); //initial state covraiance
		for (int i =0; i<=L; i++){
			P.set(i, i, 1);
		}
		*/		
		
	}
	
	/*
	public static void main(String args[])
	{
		//For testing
		UnscentedKalmanFilter filter = new UnscentedKalmanFilter(3,1);
		System.out.println(filter);
		
	}*/

    /* (non-Javadoc)
     * @see IUnscentedKalmanFilter#ukf()
     */
	public Matrix ukf(IFunction f, Matrix x, Matrix P, IFunction h, Matrix z, Matrix Q, float R, float Kzero) 
	{
		//L=x.getColumnDimension();  //Currently not needed //TODO check that its the right dimension (column/row)
		//m=z.getColumnDimension(); 
		
		
		

		Matrix X = new Matrix(1,1); //TODO matrix size 
		X=sigmas(x,P,c);  //sigma points around x
		/*[x1,X1,P1,X2]=ut(fstate,X,Wm,Wc,L,Q);          %unscented transformation of process
		% X1=sigmas(x1,P1,c);                         %sigma points around x1
		% X2=X1-x1(:,ones(1,size(X1,2)));             %deviation of X1
		[z1,Z1,P2,Z2]=ut(hmeas,X1,Wm,Wc,m,R);       %unscented transformation of measurments
		P12=X2*diag(Wc)*Z2';                        %transformed cross-covariance
		K=P12/P2;           %old: P12*inv(P2);
		*/
		
/*

		%
		% Example:
		%{
		n=3;      %number of state
		q=0.1;    %std of process 
		r=0.1;    %std of measurement
		Q=q^2*eye(n); % covariance of process
		R=r^2;        % covariance of measurement  
		f=@(x)[x(2);x(3);0.05*x(1)*(x(2)+x(3))];  % nonlinear state equations
		h=@(x)x(1);                               % measurement equation
		s=[0;0;1];                                % initial state
		x=s+q*randn(3,1); %initial state          % initial state with noise
		P = eye(n);                               % initial state covariance
		N=20;                                     % total dynamic steps
		xV = zeros(n,N);          %estmate        % allocate memory
		sV = zeros(n,N);          %actual
		zV = zeros(1,N);
		for k=1:N
		  z = h(s) + r*randn;                     % measurments
		  sV(:,k)= s;                             % save actual state
		  zV(k)  = z;                             % save measurment
		  [x, P] = ukf(f,x,P,h,z,Q,R);            % ekf 
		  xV(:,k) = x;                            % save estimate
		  s = f(s) + q*randn(3,1);                % update process 
		end
		for k=1:3                                 % plot results
		  subplot(3,1,k)
		  plot(1:N, sV(k,:), '-', 1:N, xV(k,:), '--')
		end
		%}
		%
		% By Yi Cao at Cranfield University, 04/01/2008
		%
		*/
		
		/*
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
	private Object ut(IFunction f, Object X, Object Wm, Object Wc, Object n, Object R)
	{
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
	
	/**TODO implement
	 * Sigma points around reference point
	 * @param x  reference point  
	 * @param P  covariance  
	 * @param c  coefficient
	 * @return Sigma points
	 */
	private Matrix sigmas(Object x, Object P, Object c)
	{/*
		A = c*chol(P)';
		Y = x(:,ones(1,numel(x)));
		X = [x Y+A Y-A]; 
		*/
		return null;
	}

	public String toString()
	{
		//TODO print L and m, but only if the have values
		String s = " alpha = " + alpha + "\n ki = " + ki + "\n beta = " + beta +
		"\n lambda = " + lambda + "\n  c = " + c;// + "\n Wm = " + Wm.getArray() + "\n Wc = " + Wc.getArray();
		return s;	
	}

}


