clc
clear
close all
global landm;
global n;
global nextr;
global nx;
global dt;
global R;
global r;
global landmFull;
global vc;
global k,

stream = RandStream.create('mt19937ar','seed',78693);
%s=RandStream('mt19937ar'); %
RandStream.setDefaultStream(stream);

bound=[0 2 0 2];
landmFull=[.1,0.1 ;
          0.1,1.9 ;...
          1.9,0.1 ;...
          1.9,1.9 ];
landm=landmFull;
n=size(landm,1);
nextr=n+1;
nx=4;
stddegrees = 3;
lambda = [stddegrees*(pi/180), 1e-3, stddegrees*(pi/180)] ;	% Standard deviation in radians
N=470;                           %Number of time steps
dt=0.1;
sc=[.15;.15];
vc=zeros(2,N);

turnInd = [90,120,240,300,370,N];
turnWait = 35; %value of m means m-1 steps wait
speed=0.013;

vc(:,1:turnInd(1))=+speed;

vc(1,turnInd(1)+turnWait:turnInd(2))=-speed;
vc(2,turnInd(1)+turnWait:turnInd(2))=speed;

vc(1,turnInd(2)+turnWait:turnInd(3))=0;
vc(2,turnInd(2)+turnWait:turnInd(3))=-speed;

vc(1,turnInd(3)+turnWait:turnInd(4))=speed;
vc(2,turnInd(3)+turnWait:turnInd(4))=0;

vc(1,turnInd(4)+turnWait:turnInd(5))=0;
vc(2,turnInd(4)+turnWait:turnInd(5))=speed;

vc(:,turnInd(5)+turnWait:turnInd(6))=-speed;


q=.1;    %std of expected process noise
%qa=0.02;    %std of actual process noise
r=lambda;    %std of expected measurement noise
ra=lambda;       %std of actual measurement noise
Q=q^2*[(dt^4)/4 0      (dt^3)/2  0     ;...
        0       dt^4/4  0        dt^3/2;...
        dt^3/2  0       dt^2     0     ;...
        0       dt^3/2  0        dt^2];     % covariance of process
R = r(1)^2*eye(n);                          % covariance of measurement
R(n+1,n+1) = r(2)^2;
%R(n+2,n+2) = r(3)^2;
f = @update;                                % nonlinear state equations
h = @measure2;                              % measurement equation
P = 1e-3*eye(nx);                           % initial state covraiance
xV = zeros(nx,N);         %estmate          % allocate memory

s = [sc; vc(:,1)];                              % initial state
trueInit=[sc; vc(:,1)];

mNoise=ra(1)*randn(n,1);
mNoise(n+1,1)=ra(2)*randn;
%mNoise(n+2,1)=ra(3)*randn;
ls_est2( h(trueInit)+ mNoise);  %+mNoise    % initial position with noise

x = [sc; 0; 0];
if x(1)<bound(1)
    x(1)=bound(1);
end
if x(1)>bound(2)
    x(1)=bound(2);
end
if x(2)<bound(3)
    x(2)=bound(3);
end
if x(2)>bound(4)
    x(2)=bound(4);
end

sV = zeros(nx,N);         %actual states
zV = zeros(nextr,N);

raysX=cell(n,N);
raysY=cell(n,N);
faraway=10;

posErrNorm=zeros(N,1);
velErrNorm=zeros(N,1);

%xstatic=zeros(2,N);

actLandm=[1:size(landmFull,1)];

for k=1:N
    
%     if k==round(N/7)
%         R(1,1)=1e30;
%         R(2,2)=1e30;
%         R(3,3)=1e30;
%         actLandm=[];
%     end
%     if k==round(4*N/7)
%         R=r^2*eye(n);
%         actLandm=[1 2 3];
%     end
%     if k==round(3*N/7)
%         R(1,1)=1e30;
%         actLandm=[2 3];
%     end
%     if k==round(4*N/7)
%         R=r^2*eye(n);
%         actLandm=[1 2 3];
%     end
%     if k==round(5*N/7)
%         R(1,1)=1e30;
%         R(2,2)=1e30;
%         R(3,3)=1e30;
%         actLandm=[];
%     end
%     if k==round(6*N/7)
%         R=r^2*eye(n);
%         actLandm=[1 2 3];
%     end    
    
    cats=[sc; vc(:,k)];                     %linear movement
    posErrNorm(k)=norm(s(1:2)-x(1:2));
    velErrNorm(k)=norm(s(3:4)-x(3:4)*dt);
    sc=sc+vc(:,k);
    xV(:,k) = x;
    
    mNoise=ra(1)*randn(n,1);
    mNoise(n+1,1)=ra(2)*randn;
    %mNoise(n+2,1)=ra(3)*randn;
    z=h(s) + mNoise;                   % measurements
    
    for j=actLandm
        raysX{j,k}(1,:)=[x(1), cos(z(j,1))+x(1) - faraway*cos(z(j,1))];
        raysY{j,k}(1,:)=[x(2), sin(z(j,1))+x(2) - faraway*sin(z(j,1))];
    end
    sV(:,k)= s;                             % save actual state
    zV(1:nextr,k) = z;                          % save measurment

    [x, P] = ukf(f,x,P,h,z,Q,R);
    
    if isempty(actLandm);
%         boundDist=min(abs([x(1)-bound(1),x(1)-bound(2),...
%                           x(2)-bound(3),x(2)-bound(4)]));
%         if first==1
%             normlz=1/boundDist;
%             first=0;
%         end
        x(3)=x(3)*.99;
        x(4)=x(3)*.99;
%     else
%         first=1;
    end
    
    if (x(1)<bound(1) || x(1)>bound(2) ||...
        x(2)<bound(3) || x(2)>bound(4))
        if k~=1;
            x=xV(:,k-1);
        end
    end
    
    s = cats; %+ qa*randn(nx,1);           % update process
    
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
    pause(.001)
end

    
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

