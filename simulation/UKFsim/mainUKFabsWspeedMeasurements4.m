clc
clear
close all
global landm;
global actLandm;
global R;
global Rfull;
global xV;
global zV;
global n;
global nextr;
global nx;
global dt;
global R;
global r;
global ra;
global landmFull;
global vc;
global k;
global N;

stream = RandStream.create('mt19937ar','seed',1643);
%s=RandStream('mt19937ar'); %
RandStream.setDefaultStream(stream);

%%
bound=[0 2 0 2];
landmFull=[.1,0.1 ;
          0.1,1.9 ;...
          1.9,0.1 ;...
          1.9,1.9 ];
landm=landmFull;
n=size(landm,1);
nextr=n+2;
nx=4;
stddegrees = 1;
lambda = [stddegrees*(pi/180),.5e-2,.5e-2] ;	% Standard deviation in radians
N=500;                           %Number of time steps
dt=.1;
sc=[.15;.5];
vc=initvel;

q=.005;    %std of expected process noise
%qa=0.02;    %std of actual process noise
r=lambda;    %std of expected measurement noise
ra=lambda;       %std of actual measurement noise
Q=q^2*[ dt^4/4 0      dt^3/2  0     ;...
        0       dt^4/4  0        dt^3/2;...
        dt^3/2  0       dt^2     0     ;...
        0       dt^3/2  0        dt^2];     % covariance of process
R = r(1)^2*eye(n);            % covariance of measurement
R(n+1,n+1) = r(2)^2;
R(n+2,n+2) = r(3)^2;
Rfull=R;
f = @update;                                % nonlinear state equations
h = @measure2WspeedMeasurements;            % measurement equation
P = 1e-3*eye(nx);                           % initial state covraiance
xV = zeros(nx,N);         %estmate          % allocate memory
s = [sc; vc(:,1)];                              % initial state
trueInit=[sc; vc(:,1)];
ls_est2( h(trueInit)+ mNoise);              % initial position with noise

x = [sc; 0; 0];
x=initboundcorr(x,bound);

%%
%Allocate memory
sV = zeros(nx,N);         %actual states
zV = zeros(nextr,N);
raysX=cell(n,N);
raysY=cell(n,N);
faraway=10;
posErrNorm=zeros(N,1);
velErrNorm=zeros(N,1);
%xstatic=zeros(2,N);

actLandm=[1:size(landmFull,1)];
per=N/7;
large=1e9;

%%
for k=1:N
    periodic(per,large);
    
    cats=[sc; vc(:,k)];                     %linear movement
    posErrNorm(k)=norm(s(1:2)-x(1:2));
    velErrNorm(k)=norm(s(3:4)-x(3:4)*dt);
    sc=sc+vc(:,k);
    xV(:,k) = x;
    
    z=h(s) + mNoise;                   % measurements
    z=noCheat(z);
    
    for j=actLandm
        raysX{j,k}(1,:)=[x(1), cos(z(j,1))+x(1) - faraway*cos(z(j,1))];
        raysY{j,k}(1,:)=[x(2), sin(z(j,1))+x(2) - faraway*sin(z(j,1))];
    end
    sV(:,k)= s;                             % save actual state
    zV(1:nextr,k) = z;                      % save measurment
    [x, P] = ukf(f,x,P,h,z,Q,R);
    x=outOfBoundsCorr(x,bound);
    s = cats; %+ qa*randn(nx,1);            % update process
    
    %%Least squares estimates, for comparison
    %xstatic(:,k)=ls_est(z);
    %xstatic(1,1:k),xstatic(2,1:k),'g.',...
    
    %real time plotting
    plot(xV(1,1:k),xV(2,1:k),'b',...
        sV(1,1:k),sV(2,1:k),'r',...
        landm(:,1),landm(:,2),'*k');
    hold on
    for j=actLandm
        plot([raysX{j,k}],[raysY{j,k}],'k');
    end
    axis(bound)
    hold off
    pause(.02)
end

%%
% %Movie recording
%mov = avifile('circular_movementUKF7.avi');
%frame = 1;
%fig=figure;
% tic
%   for k=1:N
% %     plot([sV(1,k-1) sV(1,k)],[sV(2,k-1) sV(2,k)],'r',...
% %         [xV(1,k-1) xV(1,k)],[xV(2,k-1) xV(2,k)],'b',landm(:,1),landm(:,2),'*k');
%     %hold on
%     plot(sV(1,1:k),sV(2,1:k),'r',...
%         xV(1,1:k),xV(2,1:k),'b',...
%         landm(:,1),landm(:,2),'*k',...
%         [raysX{1,k}],[raysY{1,k}],'y',...
%         [raysX{2,k}],[raysY{2,k}],'m',...
%         [raysX{3,k}],[raysY{3,k}],'c');
%         axis(bound)
%     
%     pause(.001)
% %     frame = frame + 1
% %     f2 = getframe(fig);
% %     mov = addframe(mov, f2);
%   end

%Plot error norms
    figure
    subplot(2,1,1)
    plot(1:N,posErrNorm(1:N))
    title('Position error');
    ylabel('Position error norm');
    xlabel('Time [samples]');
    subplot(2,1,2)
    plot(1:N,velErrNorm(1:N))
    title('Velocity error');
    ylabel('Velocity error norm');
    xlabel('Time [samples]');
% toc
% fprintf('seconds/frame %i\n', toc/frame);
% mov = close(mov);

