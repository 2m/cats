clc
clear
close all
global cats;
global n;
global nx;
global dt;
global R;
global r;
global catsFull;

s = RandStream.create('mt19937ar','seed',546589);
%s=RandStream('mt19937ar'); %
RandStream.setDefaultStream(s);

ax=[0 2 0 2];
catsFull=[0.1,1.9 ;...
          1.9, .1 ;...
          1.9,1.9 ];
cats=catsFull;
n=size(catsFull,1);
nx=4;
stddegrees = 6;
lambda = stddegrees*(pi/180);	% Standard deviation in radians
N=420;                           %Number of time steps
dt=0.1;
sm=[0;.5];
vm=zeros(2,N);
turnInd = [90,120,190,250,310,N];
turnWait = 10; %value of m means m-1 steps wait
speed=0.01;

vm(:,1:turnInd(1))=+speed;

vm(1,turnInd(1)+turnWait:turnInd(2))=-speed;
vm(2,turnInd(1)+turnWait:turnInd(2))=speed;

vm(1,turnInd(2)+turnWait:turnInd(3))=0;
vm(2,turnInd(2)+turnWait:turnInd(3))=-speed;

vm(1,turnInd(3)+turnWait:turnInd(4))=speed;
vm(2,turnInd(3)+turnWait:turnInd(4))=0;

vm(1,turnInd(4)+turnWait:turnInd(5))=0;
vm(2,turnInd(4)+turnWait:turnInd(5))=speed;

vm(:,turnInd(5)+turnWait:turnInd(6))=-speed;


q=.09;    %std of expected process noise
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
P = 1e-3*eye(nx);                           % initial state covraiance
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
    %cats dropout:
%     if k==round(N/4)
%         reint([0 1 1])
%     end
%     if k==round(2*N/4)
%         reint([0 0 1])
%     end
%     if k==round(3*N/4)
%         reint([1 0 0])
%     end
    
    mouse=[sm; vm(:,k)];                 %linear movement
    posErrNorm(k)=norm(s(1:2)-x(1:2));
    velErrNorm(k)=norm(s(3:4)-x(3:4)*dt);
    sm=sm+vm(:,k);
    xV(:,k) = x;
    z=h(s)+ra*randn(n,1);                   % measurements
    for j=1:n
        raysX{j,k}(1,:)=[cats(j,1), cos(z(j,1))+cats(j,1) + faraway*cos(z(j,1))];
        raysY{j,k}(1,:)=[cats(j,2), sin(z(j,1))+cats(j,2) + faraway*sin(z(j,1))];
    end
    sV(:,k)= s;                             % save actual state
    zV(1:n,k)  = z;                           % save measurment
    [x, P] = ukf(f,x,P,h,z,Q,R);            % ekf
    s = mouse; %+ qa*randn(nx,1);           % update process
    
    xstatic(:,k)=ls_est(z);
    
    %real time plotting
    plot(xstatic(1,1:k),xstatic(2,1:k),'g.',...
        sV(1,1:k),sV(2,1:k),'r',...
        xV(1,1:k),xV(2,1:k),'b',...
        cats(:,1),cats(:,2),'*k');%,...
    hold on
    for j=1:n
        plot([raysX{j,k}],[raysY{j,k}],'k');
    end
    axis(ax)
    hold off
    pause(.001)
end

%mov = avifile('circular_movementUKF7.avi');
%frame = 1;
%fig=figure;
% tic
%   for k=1:N
% %     plot([sV(1,k-1) sV(1,k)],[sV(2,k-1) sV(2,k)],'r',...
% %         [xV(1,k-1) xV(1,k)],[xV(2,k-1) xV(2,k)],'b',cats(:,1),cats(:,2),'*k');
%     %hold on
%     plot(sV(1,1:k),sV(2,1:k),'r',...
%         xV(1,1:k),xV(2,1:k),'b',...
%         cats(:,1),cats(:,2),'*k',...
%         [raysX{1,k}],[raysY{1,k}],'y',...
%         [raysX{2,k}],[raysY{2,k}],'m',...
%         [raysX{3,k}],[raysY{3,k}],'c');
%         axis(ax)
%     
%     pause(.001)
% %     frame = frame + 1
% %     f2 = getframe(fig);
% %     mov = addframe(mov, f2);
%   end
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
