package GSim;

public class UnscentedKalmanFilter implements IUnscentedKalmanFilter
{
	
	public float[] Ukf(float fstate, float x, float P, float hmeas, float z,
			float Q, float R, float Kzero) 
	{
		
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
		P = eye(n);                               % initial state covraiance
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
		L=numel(x);                                 %numer of states
		m=numel(z);                                 %numer of measurements
		alpha=3.5;%1e-3;                           %default, tunable
		ki=0;                                       %default, tunable
		beta=alpha^2-.9;%lower bound% -2;%10 5 10000 -2* -1 0 1 def:2; %default, tunable
		lambda=alpha^2*(L+ki)-L;                    %scaling factor
		c=L+lambda;                                 %scaling factor
		Wm=[lambda/c 0.5/c+zeros(1,2*L)];           %weights for means
		Wc=Wm;
		Wc(1)=Wc(1)+(1-alpha^2+beta);               %weights for covariance
		c=sqrt(c);
		X=sigmas(x,P,c);                            %sigma points around x
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

		function [y,Y,P,Y1]=ut(f,X,Wm,Wc,n,R)
		%Unscented Transformation
		%Input:
		%        f: nonlinear map
		%        X: sigma points
		%       Wm: weights for mean
		%       Wc: weights for covraiance
		%        n: numer of outputs of f
		%        R: additive covariance
		%Output:
		%        y: transformed mean
		%        Y: transformed sampling points
		%        P: transformed covariance
		%       Y1: transformed deviations

		L=size(X,2);
		y=zeros(n,1);
		Y=zeros(n,L);
		for k=1:L                   
		    Y(:,k)=f(X(:,k));       
		    y=y+Wm(k)*Y(:,k);       
		end
		Y1=Y-y(:,ones(1,L));
		P=Y1*diag(Wc)*Y1'+R;          

		function X=sigmas(x,P,c)
		%Sigma points around reference point
		%Inputs:
		%       x: reference point
		%       P: covariance
		%       c: coefficient
		%Output:
		%       X: Sigma points

		A = c*chol(P)';
		Y = x(:,ones(1,numel(x)));
		X = [x Y+A Y-A]; 
		
		
		*/
		
		
		return null;
	}

}
