clc
clear
close all
global cats;
global n;
global nx;
global dt;
cats=[0.1,0.1 ;...
      1.9,0.1 ;...
      1  ,1.9 ];
n=size(cats,1);
nx=4;
stddegrees = 1;
lambda = stddegrees*(pi/180);	% Standard deviation in radians
N=600;                           %Number of time steps
dt=0.1;
phi=0;                          %angle of the circular movement of the mouse
radius=0.2;
sm=[1+radius*cos(phi);1+radius*sin(phi)]; 
vm=[0; 0];
mouseinit=[sm; vm];


q=0.1;    %std of expected process noise
%qa=0.02;    %std of actual process noise
r=lambda;    %std of expected measurement noise
ra=lambda;       %std of actual measurement noise
Q=q^2*[(dt^4)/4 0      (dt^3)/2  0     ;...
        0       dt^4/4  0        dt^3/2;...
        dt^3/2  0       dt^2     0     ;...
        0       dt^3/2  0        dt^2];   % covariance of process
R = r^2*eye(n);                             % covariance of measurement
s = mouseinit;                              % initial state
initerror = [0; 0];
x = [sm + initerror; vm];                   % initial state with noise
f = @update;                                % nonlinear state equations
h = @measure;                               % measurement equation
P = 1e1*eye(nx);                       % initial state covraiance
xV = zeros(nx,N);         %estmate        % allocate memory
sV = zeros(nx,N);         %actual
zV = zeros(n,N);

raysX=cell(n,N);
raysY=cell(n,N);
faraway=4;

for k=1:N
    mouse =[1+radius*cos(phi);1+radius*sin(phi);0;0]; %mouse position
    phi=phi+0.05;
    radius=radius+0.002;
    posErrNorm(k)=norm(s(1:2)-x(1:2));
    velErrNorm(k)=norm(s(3:4)-x(3:4)*dt);
    xV(:,k) = x;
    z=h(s); + ra*randn(n,1);                % measurements
    for j=1:n
        raysX{j,k}(1,:)=[cats(j,1), cos(z(j,1))+cats(j,1) + faraway*cos(z(j,1))];
        raysY{j,k}(1,:)=[cats(j,2), sin(z(j,1))+cats(j,2) + faraway*sin(z(j,1))];
    end
    sV(:,k)= s;                             % save actual state
    zV(:,k)  = z;                           % save measurment
    [x, P] = ukf(f,x,P,h,z,Q,R);            % ekf
    %xV(:,k) = x;                           % save estimate
    s = mouse;% + qa*randn(nx,1);           % update process
end

%mov = avifile('circular_movementUKF7.avi');
%frame = 1;
%fig=figure;
  for k=2:N
    plot(sV(1,1:k),sV(2,1:k),'r',...
        xV(1,1:k),xV(2,1:k),'b',...
        cats(:,1),cats(:,2),'*k',...
        [raysX{1,k}],[raysY{1,k}],'y',...
        [raysX{2,k}],[raysY{2,k}],'m',...
        [raysX{3,k}],[raysY{3,k}],'c');
        axis([-2 4 -2 4])
    
    pause(.01)
%     frame = frame + 1
%     f2 = getframe(fig);
%     mov = addframe(mov, f2);
  end
    figure
    subplot(2,1,1)
    plot(1:N,posErrNorm(1:N))
    subplot(2,1,2)
    plot(1:N,velErrNorm(1:N))
    axis([0 300 0 0.05]);
% mov = close(mov);

