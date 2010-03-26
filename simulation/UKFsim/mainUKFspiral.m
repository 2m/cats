clc
clear
close all
global catsClean;
global n;
global nx;
global dt;

s = RandStream.create('mt19937ar','seed',5486);%546589
%s=RandStream('mt19937ar'); %
RandStream.setDefaultStream(s);

ax=[0 3 0 3];
catsClean=[0.1,0.1 ;...
      2.9,0.1 ;...
      1.5  ,2.9 ];
n=size(catsClean,1);
nx=4;
stddegrees = 1;
lambda = stddegrees*(pi/180);	% Standard deviation in radians
N=120;                          %Number of time steps
dt=.1;                         %samping period, affects process noise through Q

phi=0;                          %angle of the circular movement of the mouse
radius=1;
sm=[1.5+radius*cos(phi);1.5+radius*sin(phi)]; %true position of mouse
vm=[0; 0];
mouseinit=[sm; vm];



q=.05;    %std of expected process noise
%qa=0.02;    %std of actual process noise
r=lambda;    %std of expected measurement noise
ra=lambda;       %std of actual measurement noise
Q=q^2*[(dt^4)/4 0      (dt^3)/2  0     ;...
        0       dt^4/4  0        dt^3/2;...
        dt^3/2  0       dt^2     0     ;...
        0       dt^3/2  0        dt^2];     % covariance of process
R = r^2*eye(n);                             % covariance of measurement
f = @update;                                % nonlinear state equations
h = @measure;                               % measurement equation
P =1e-3*eye(nx);  %1e-3*eye(nx);                            % initial state covraiance
xV = zeros(nx,N);         %estmate          % allocate memory

s = [sm; vm(:,1)];                              % initial state
initerror = [0; 0];
trueInit=[sm; vm(:,1)];
initpos=ls_est(h(trueInit)+ra*randn(n,1));      % initial position with noise
x = [initpos + initerror; 0; 0];

sV = zeros(nx,N);         %actual states
zV = zeros(n,N);

raysX=cell(n,N);
raysY=cell(n,N);
faraway=10;

posErrNorm=zeros(N,1);
velErrNorm=zeros(N,1);

xstatic=zeros(2,N);

for k=1:N
    mouse =[1.5+radius*cos(phi);1.5+radius*sin(phi);0;0]; %mouse position
    phi=phi+2*pi/N;
    %radius=radius+0.002;
    
    posErrNorm(k)=norm(s(1:2)-x(1:2));
    velErrNorm(k)=norm(s(3:4)-x(3:4)*dt);
    %sm=sm+vm(:,k);
    xV(:,k) = x;
    z=h(s)+ra*randn(n,1);                   % measurements
    for j=1:n
        raysX{j,k}(1,:)=[catsClean(j,1), cos(z(j,1))+catsClean(j,1) + faraway*cos(z(j,1))];
        raysY{j,k}(1,:)=[catsClean(j,2), sin(z(j,1))+catsClean(j,2) + faraway*sin(z(j,1))];
    end
    sV(:,k)= s;                             % save actual state
    zV(:,k)  = z;                           % save measurment
    [x, P] = ukf(f,x,P,h,z,Q,R);            % ekf
    %xV(:,k) = x;                            % save estimate
    s = mouse; %+ qa*randn(nx,1);            % update process
    
    xstatic(:,k)=ls_est(z);
end

%mov = avifile('circular_movementUKF7.avi');
%frame = 1;
%fig=figure;
tic
  for k=1:N
%     plot([sV(1,k-1) sV(1,k)],[sV(2,k-1) sV(2,k)],'r',...
%         [xV(1,k-1) xV(1,k)],[xV(2,k-1) xV(2,k)],'b',catsClean(:,1),catsClean(:,2),'*k');
    %hold on
    plot(xstatic(1,1:k),xstatic(2,1:k),'g.',...
        sV(1,1:k),sV(2,1:k),'r',...
        xV(1,1:k),xV(2,1:k),'b',...
        catsClean(:,1),catsClean(:,2),'*k');
    hold on
    for j=1:n
        plot([raysX{j,k}],[raysY{j,k}],'k');
    end
    hold off
    axis(ax)
    
    pause(.02)
%     frame = frame + 1
%     f2 = getframe(fig);
%     mov = addframe(mov, f2);
  end
    figure
    subplot(2,1,1)
    plot(1:N,posErrNorm,[15,N],[mean(posErrNorm(15:N)), mean(posErrNorm(15:N))],'r');
    title('Position error');
    ylabel('Position error norm');
    xlabel('Time [samples]');
    subplot(2,1,2)
    plot(1:N,velErrNorm(1:N))
    title('Velocity error');
    ylabel('Velocity error norm');
    xlabel('Time [samples]');
toc
% fprintf('seconds/frame %i\n', toc/frame);
% mov = close(mov);

