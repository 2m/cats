clc
clear
close all

global cats;
global actCats;
global rm;
global nm;
global nxm;
global Rm;
global Rfullm;
global zVm;
global xVm;
global xm;

global s;
global landm;
global actLandm;
global R;
global Rfull;
global xV;
global zV;
global n;
global nz;
global nx;
global dt;
global R;
global r;
global ra;
global landmFull;
global vc;
global k;
global N;
global x;

stream = RandStream.create('mt19937ar','seed',604);
%s=RandStream('mt19937ar'); %
RandStream.setDefaultStream(stream);

%%
bound=[0 2 0 2];
landmFull=[ 1  ,1.9 ;
            0.1,1.9 ;...
            1.9,0.1 ;...
            1.9,1.9 ];
landm=landmFull;
n=size(landmFull,1);
sc=[.15 1;...
    .2  .5]; %[0.15; 0.2];
nm=size(sc,2); %number of cats
nz=n+3;
nx=6;
nxm=4;
stddegrees = 1;
fov=43*pi/180;
lambda = [1e-6,1e-6,1e-6,1e-6,1e-6];%[stddegrees*(pi/180),.5e-2,.5e-2,0,0] ;	% Standard deviation in radians
N=500;                           %Number of time steps
dt=.1;
sm=[.2; .5];
vc=cell(1,nm);
for i=1:nm
    vc{1,i}=zeros(2,N);
end
vm=initvelm;

q=.005;     %std of expected process noise
qm=.005;    %std of expected process noise
r=lambda;   %std of expected measurement noise
rm=lambda(1);
ra=lambda;       %std of actual measurement noise
ram=lambda(1);
k1=dt;          %how much the noise in the wheel tachometers is amplified
k2=dt;     %how much the noise in the camera motor tachometers is amplified
Q=q^2*[ dt^4/4  0       dt^3/2  0       0    0;...   % covariance of process
        0       dt^4/4  0       dt^3/2  0    0;...
        dt^3/2  0       dt^2    0       0    0;...
        0       dt^3/2  0       dt^2    0    0;...
        0       0       0       0       k1   0;...
        0       0       0       0       0    k2];
Qm=qm^2*[ dt^4/4  0        dt^3/2  0     ;...
          0       dt^4/4  0        dt^3/2;...
          dt^3/2  0       dt^2     0     ;...
          0       dt^3/2  0        dt^2] ;

R=cell(1,nm);
for i=1:nm
    R{1,i} = r(1)^2*eye(nz);            % covariance of measurement
    for j=1:nz-n
        R{1,i}(n+j,n+j) = r(j+1)^2;
    end
end
Rm = rm^2*eye(nm);
Rfull=R;
Rfullm=Rm;

f = @update2;                               % nonlinear state equations
h = @measure2WspeedMeasurements;            % measurement equation
P=cell(1,nm);
for i=1:nm
    P{1,i} = 1e-3*eye(nx);                  % initial state covraiance
end
fm = @update;                               % nonlinear state equations
hmT = @measuremT;                            % true measurement equation
hm = @measurem;                            % true measurement equation
Pm = 1e-3*eye(nxm);                         % initial state covraiance

s=zeros(nx,nm);
initpos=zeros(2,nm);
for i=1:nm
    s(:,i) = [sc(:,i); vc{1,i}(:,1);...
        atan2(vc{1,i}(2,1),vc{1,i}(1,1)); pi/4];          % true initial state
    initpos(:,i)=sc(:,i);%ls_est2(h(s)+ mNoise);             % initial position with noise
    x(:,i) = [initpos(:,i); 0; 0; pi; pi];                % estimated initial state
    x(:,i)=initboundcorr(x(:,i),bound);
end

s2 = [sm; vm(:,1)];                         % true initial state
initposm=ls_estm(hm(s2) + ram*randn(nm,1)); % initial position with noise
xm = [initposm; 0; 0];                % estimated initial state
xm=initboundcorr(xm,bound);

%%
%Allocate memory
xV=cell(1,nm);
sV=cell(1,nm);
zV=cell(1,nm);
for i=1:nm
    xV{1,i} = zeros(nx,N);         %estmate
    sV{1,i} = zeros(nx,N);         %actual states
    zV{1,i} = zeros(nz,N);
end
xVm = zeros(nxm,N);         %estmate
sVm = zeros(nxm,N);         %actual states
zVm = zeros(nm,N);

raysX=cell(n,nm);
raysY=cell(n,nm);
raysXm=cell(nm,1);
raysYm=cell(nm,1);

faraway=10;
posErrNorm=zeros(N,1);
velErrNorm=zeros(N,1);
posErrNormm=zeros(N,1);
velErrNormm=zeros(N,1);

actLandm=cell(1,nm);
for i=1:nm
    actLandm{1,i}=[];
end
actCats=[];
large=1e9;
arrowl=0.17;

%%
for k=1:N
    camA=camAng(k,5);
    for i=1:nm
        cats(:,i)=[sc(:,i); vc{1,i}(:,k);atan2(vc{1,i}(2,k),vc{1,i}(1,k));camA];    %linear movement
    end
    mouse=[sm; vm(:,k)];                 %linear movement
    
%     posErrNorm(k)=norm(s(1:2)-x(1:2));
%     velErrNorm(k)=norm(s(3:4)-x(3:4)*dt);
%     posErrNormm(k)=norm(s2(1:2)-xm(1:2));
%     velErrNormm(k)=norm(s2(3:4)-xm(3:4)*dt);
    for i=1:nm
        sc(:,i)=sc(:,i)+vc{1,i}(:,k);
    end
    sm=sm+vm(:,k);
    
    for i=1:nm
        xV{1,i}(:,k) = x(:,i);               % store state estimate
        z(:,i)=h(s(:,i)) + mNoise;           % measurements
    end
    
    xVm(:,k) = xm;                       % store state estimate
    zm=hmT(mouse) + ram*randn(nm,1);        % measurements
    [Rm,actLandm,actCats]=fovCheckMerge(z,zm,fov,large,camA);
    
    z=noCheat(z);
    zm=noCheatm(zm);
    for i=1:nm
        sV{1,i}(:,k)= s(:,i);                % save actual state
        zV{1,i}(1:nz,k) = z(:,i);                      % save measurment
        [x(:,i), P{1,i}] = ukf(f,x(:,i),P{1,i},h,z(:,i),Q,R{1,i});
        x(:,i)=outOfBoundsCorr(x(:,i),xV{1,i},bound);
        s(:,i) = cats(:,i);                            % update process
    end
    
    sVm(:,k)= s2;                        % save actual state
    zVm(1:nm,k) = zm;                    % save measurment
    [xm, Pm] = ukf(fm,xm,Pm,hm,zm,Qm,Rm);
    xm=outOfBoundsCorrm(xm,xVm,bound);
    s2 = mouse;                          % update process
    
    %Real time plotting
    for i=1:nm
        for j=actLandm{1,i}
            raysX{j,i}(1,:)=[x(1,i), cos(z(j,i))+x(1,i) + faraway*cos(z(j,i))];
            raysY{j,i}(1,:)=[x(2,i), sin(z(j,i))+x(2,i) + faraway*sin(z(j,i))];
        end
        for j=actCats
            raysXm{j,i}(1,:)=[x(1,j), cos(zm(j,1))+x(1,j) + faraway*cos(zm(j,1))];
            raysYm{j,i}(1,:)=[x(2,j), sin(zm(j,1))+x(2,j) + faraway*sin(zm(j,1))];
        end
    end
    
    for i=1:nm
        dirx(i,:)=[x(1,i), x(1,i)+arrowl*cos(x(5,i))];
        diry(i,:)=[x(2,i), x(2,i)+arrowl*sin(x(5,i))];
        fov1x(i,:)=[x(1,i), x(1,i)+arrowl*cos(x(6,i)+fov/2)];
        fov2x(i,:)=[x(1,i), x(1,i)+arrowl*cos(x(6,i)-fov/2)];
        fov1y(i,:)=[x(2,i), x(2,i)+arrowl*sin(x(6,i)+fov/2)];
        fov2y(i,:)=[x(2,i), x(2,i)+arrowl*sin(x(6,i)-fov/2)];
    end
    for i=1:nm
        plot(xV{1,i}(1,1:k),xV{1,i}(2,1:k),'b',...
            dirx(i,:),diry(i,:),'g',...   %direction arrow base
            fov1x(i,:),fov1y(i,:),'m',...
            fov2x(i,:),fov2y(i,:),'m',...
            xV{1,i}(1,k),xV{1,i}(2,k),'b.',...
            sV{1,i}(1,1:k),sV{1,i}(2,1:k),'r',...
            sV{1,i}(1,k),sV{1,i}(2,k),'r.');
            hold on
    end
    plot(landm(:,1),landm(:,2),'*k',...
        xVm(1,1:k),xVm(2,1:k),'b',...
        xVm(1,k),xVm(2,k),'b.',...
        sVm(1,1:k),sVm(2,1:k),'r',...
        sVm(1,k),sVm(2,k),'r.');
    for i=1:nm
        for j=actLandm{1,i}
            plot([raysX{j,i}],[raysY{j,i}],'k:');
        end
        for j=actCats
            plot([raysXm{j,i}],[raysYm{j,i}],'k');
        end
    end
    axis(bound)
    hold off
    pause(.002)
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
% figure
% subplot(2,1,1)
% plot(1:N,posErrNorm(1:N))
% title('Position error');
% ylabel('Position error norm');
% xlabel('Time [samples]');
% subplot(2,1,2)
% plot(1:N,velErrNorm(1:N))
% title('Velocity error');
% ylabel('Velocity error norm');
% xlabel('Time [samples]');
% 
% figure
% subplot(2,1,1)
% plot(1:N,posErrNormm(1:N))
% title('Position error');
% ylabel('Position error norm');
% xlabel('Time [samples]');
% subplot(2,1,2)
% plot(1:N,velErrNormm(1:N))
% title('Velocity error');
% ylabel('Velocity error norm');
% xlabel('Time [samples]');

% toc
% fprintf('seconds/frame %i\n', toc/frame);
% mov = close(mov);
