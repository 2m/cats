clc
clear
close all
warning off all;

global cats; %true position of the cats TODO and some other stuff...
global activeCats; %contains the indices of the cats that sees the mouse
global rm; %std of expected bearing measurement noise 
global nm; %number of cats
global nxm; %number of variables in the mouse's state vector
global Rm; % covariance of measurement of the mouse
global zVm; %contains the current and all past bearings measurements to the mouse 
global xVm; %contains the current and all past state estimates of the mouse 
global xm; %current state estimate of the mouse

global s; %true state of the cats
global landm; %true position of the landmaks
global activeLandm; %the indices of the landmarks that are seen
global R; % covariance of measurement of the cats
global xV; %contains the current and all past state estimates of the cats 
global zV; %contains the current and all past bearings measurements to the cats
global n;  %number of landmarks
global nz; %number of elements in the measurement vector of the cats
global nx; %number of variables in the cats' state vector
global dt; %sampling period, must be 1 for now
global r;  %std of expected measurement noise 
global ra; %std of actual measurement noise 
global vc; %the velocities of the cats
global k;  %current time step
global N;  %total number of time steps
global x;  %state vector of the cats
global phi; %Angle of circular motion of the cats

stream = RandStream.create('mt19937ar','seed',6094);
%s=RandStream('mt19937ar'); %
RandStream.setDefaultStream(stream);

%%
bound=[0 2 0 2];
%Different landmark scenarios:
landm=[1 , 1];
%landm=[]; %no landmarks
%landm=[ .6  ,1.4
%         1.4 ,1.4
%         1   ,.6];
%landm=[  0.1 .1 
%         0.1,1.9
%         1.9,0.1
%         1.9,1.9];

n=size(landm,1);  %number of landmarks
nm=3;  %number of cats
nz=n+4;  %?? number of elements in the measurement vector of the cats = number of landmarks + 4
nx=6;  %number of variables in the cats' state vector
nxm=4;  %number of variables in the mouse's state vector
fov=43*pi/180;  %field of view
stddegrees = 2;
lambda = [stddegrees*(pi/180),1e-2,1e-2,1e-20,1e-20];% Standard deviation of measurement noise
        %[bearing angle      ,x   ,y   ,orient., cam.ang]
N=500;  %total number of time steps
dt=1;

q=.005;  %std of expected process noise for the cat
qm=.005;  %std of expected process noise for the mouse
r=lambda;  %std of expected measurement noise for the cat
rm=lambda(1);  %std of expected measurement noise for the mouse
ra=lambda;  %std of actual measurement noise for the cat
ram=lambda(1);  %std of actual measurement noise for the mouse
k1=dt;          %how much the noise in the wheel tachometers is amplified
k2=dt;     %how much the noise in the camera motor tachometers is amplified
Q=q^2*[ dt^4/4  0       dt^3/2  0       0    0;...   % covariance of process for the cat
        0       dt^4/4  0       dt^3/2  0    0;...
        dt^3/2  0       dt^2    0       0    0;...
        0       dt^3/2  0       dt^2    0    0;...
        0       0       0       0       k1   0;...
        0       0       0       0       0    k2];
Qm=qm^2*[ dt^4/4  0        dt^3/2  0     ;...  % covariance of process for the mouse
          0       dt^4/4  0        dt^3/2;...
          dt^3/2  0       dt^2     0     ;...
          0       dt^3/2  0        dt^2] ;

R=cell(1,nm);  % covariance of measurement of the cats
for i=1:nm
    R{1,i} = r(1)^2*eye(nz);            % covariance of measurement
    for j=1:nz-n
        R{1,i}(n+j,n+j) = r(j+1)^2;
    end
end
Rm = rm^2*eye(nm);  % covariance of measurement of the mouse

f = @update2;                               % nonlinear state equations
h = @measure2WspeedMeasurements;            % measurement equation
P=cell(1,nm);
for i=1:nm
    P{1,i} = 1e-3*eye(nx);                  % initial state covariance
end
fm = @update;                               % nonlinear state equations
hmT = @measuremT;  % true measurement equation, used to generate measurements
hm = @measurem;  % measurement equation, used by the ukf
Pm = 1e-3*eye(nxm);                         % initial state covariance

pos=zeros(2,nm);  %true initial position of the cats
phi=[0;pi/2; 5*pi/4];   %angle of the circular movement of the mouse
radius=[.8;.8;.8];
%todo: for all cats, do ...?
for i=1:nm
    pos(:,i)=[1+radius(i)*cos(phi(i));1+radius(i)*sin(phi(i))];
end

vc=initvelCircle;  %the velocities of the cats

s=zeros(nx,nm);  %true state of the cats, each cats state is a column
initpos=zeros(2,nm);
camAngle=zeros(1,nm); %estimated initial relative camera angles of the cats
absCamAngle=zeros(1,nm); %estimated initial absolute camera angles of the cats
for i=1:nm
    s(:,i) = [pos(:,i); vc{1,i}(:,1);...
        atan2( vc{1,i}(2,1),vc{1,i}(1,1) ); pi/4];  % true initial state
    initpos(:,i)=pos(:,i);%ls_est2(h(s)+ mNoise(i));  % initial position with noise
    x(:,i) = [initpos(:,i); 0; 0; 0; absCamAngle(i)];  % estimated initial state
    x(:,i)=initboundcorr(x(:,i),bound);
end

posm=[1.4; 1];  %true initial position of the mouse
vm=initvelCirclem;  %the velocity of the mouse
mouse = [posm; vm(:,1)];                         % true initial state
initposm=ls_estm(hm(mouse) + ram*randn(nm,1)); % initial position with noise
xm = [initposm; 0; 0];                      % estimated initial state
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

activeLandm=cell(1,nm);
for i=1:nm
    activeLandm{1,i}=[];
end
activeCats=[];
large=1e9;
arrowl=0.17;
tail=5;

cats=zeros(nx,nm);
dir=ones(1,nm);
maxCamAngularVel=0.03;

noiseShift=zeros(nz,1);
noiseShift(n+1:n+2)=.001;

for k=1:N
    %%
    absCamAngle=mod(camAngle+s(5,:),2*pi);
    
    for i=1:nm
        catOrientation=atan2(vc{1,i}(2,k),vc{1,i}(1,k));
        if catOrientation<0
            catOrientation=2*pi+catOrientation;
        end
        cats(:,i)=[pos(:,i); vc{1,i}(:,k);catOrientation;absCamAngle(i)];    %update true state
    end
    mouse=[posm; vm(:,k)];                 %update position
    
%    posErrNorm(k)=norm(s(1:2,:)-x(1:2,:));
%    velErrNorm(k)=norm(s(3:4,:)-x(3:4,:)*dt);
    posErrNormm(k)=norm(mouse(1:2)-xm(1:2));
    velErrNormm(k)=norm(mouse(3:4)-xm(3:4)*dt);
    
    for i=1:nm
        pos(:,i)=pos(:,i)+vc{1,i}(:,k);
    end
    posm=posm+vm(:,k);
    
    
    for i=1:nm
        xV{1,i}(:,k) = x(:,i);               % store state estimate
        z(:,i)=h(s(:,i)) + mNoise(i)+noiseShift;        % measurements
%         z(1:3,i)=z(1:3,i)+x(5,i)
%         z(1:3,i)=mod(z(i,1),2*pi);
    end
    
    xVm(:,k) = xm;                       % store state estimate
    zm=hmT(mouse) + ram*randn(nm,1);        % measurements
    [Rm,activeLandm,activeCats]=fovCheckMerge(z,zm,fov,large,absCamAngle);  %updates covariance of measurement of the cats and covariance of measurement of the mouse according to what the cats sees
    %Use previous measurement if target is out of view
    z=noCheat(z);
    zm=noCheatm(zm);
    xstatic=ls_estmDropout(zm);
    
    for i=1:nm
        sV{1,i}(:,k)= s(:,i);                % save actual state
        zV{1,i}(1:nz,k) = z(:,i);                      % save measurment
        [x(:,i), P{1,i}] = ukf(f,x(:,i),P{1,i},h,z(:,i),Q,R{1,i});
        x(:,i)=outOfBoundsCorr(x(:,i),xV{1,i},bound);
        s(:,i) = cats(:,i);                            % update process
    end
    
    sVm(:,k)= mouse;                        % save actual state
    zVm(1:nm,k) = zm;                    % save measurment
    [xm, Pm] = ukf(fm,xm,Pm,hm,zm,Qm,Rm);
    xm=outOfBoundsCorrm(xm,xVm,bound);
    %s2 = mouse = mouse; ???                         % update process
    
    %Camera control
    inactCats=1:nm; %all by default
    inactCats(activeCats)=[]; %remove active cats
    %If the mouse is out of view, search for it
    for i=inactCats
        camAngle(i)=searchm(maxCamAngularVel,camAngle(i),dir(i));
    end
    %If the mouse is in view, track it using small adjustment
    for i=activeCats
%         disp('adjusting cam ');
%         disp(i);
        [camAngle(i) dir(i)]=trackm(zm(i,1),maxCamAngularVel,camAngle(i),absCamAngle(i));
    end
%%    
    %Real time plotting
    for i=1:nm
        for j=activeLandm{1,i}
            raysX{j,i}(1,:)=[xV{1,i}(1,k), cos(zV{1,i}(j,k))+xV{1,i}(1,k) + faraway*cos(zV{1,i}(j,k))];
            raysY{j,i}(1,:)=[xV{1,i}(2,k), sin(zV{1,i}(j,k))+xV{1,i}(2,k) + faraway*sin(zV{1,i}(j,k))];
        end
        for j=activeCats
            raysXm{j,i}(1,:)=[xV{1,j}(1,k), cos(zVm(j,k))+xV{1,j}(1,k) + faraway*cos(zVm(j,k))];
            raysYm{j,i}(1,:)=[xV{1,j}(2,k), sin(zVm(j,k))+xV{1,j}(2,k) + faraway*sin(zVm(j,k))];
        end
    end
   
    for i=1:nm
        for j=activeLandm{1,i}
            landmLineHandle=plot([raysX{j,i}],[raysY{j,i}],':');
            set(landmLineHandle,'Color',[.8 .8 .8])
            hold on
        end
        for j=activeCats
            plot([raysXm{j,i}],[raysYm{j,i}],'k');
            hold on
        end
    end
    
    for i=1:nm
        dirx(i,:)=[xV{1,i}(1,k) , xV{1,i}(1,k)+arrowl*cos(xV{1,i}(5,k))];
        diry(i,:)=[xV{1,i}(2,k) , xV{1,i}(2,k)+arrowl*sin(xV{1,i}(5,k))];
        fov1x(i,:)=[xV{1,i}(1,k), xV{1,i}(1,k)+arrowl*cos(xV{1,i}(6,k)+fov/2)];
        fov2x(i,:)=[xV{1,i}(1,k), xV{1,i}(1,k)+arrowl*cos(xV{1,i}(6,k)-fov/2)];
        fov1y(i,:)=[xV{1,i}(2,k), xV{1,i}(2,k)+arrowl*sin(xV{1,i}(6,k)+fov/2)];
        fov2y(i,:)=[xV{1,i}(2,k), xV{1,i}(2,k)+arrowl*sin(xV{1,i}(6,k)-fov/2)];
    end
    
    if k<=tail
        startplotidx=1;
    else
        startplotidx=k-tail;
    end
    colInc1=1/tail;
    colInc2=0.5/tail;
    j=0;
    for j=1:k-startplotidx
        plotColor1=[0       .8          0          ;
                1-j*colInc1 1-j*colInc1 1          ;
                0           1           0          ;
                1           0           1          ;
                1           0           1          ;
                0           0           1          ;
                1           1-j*colInc1 1-j*colInc1;
                1           0           0         ];
        idxVec=startplotidx+j-1:startplotidx+j;
        for i=1:nm
            lineHandle1=plot(xstatic(1),xstatic(2),'g.',...
                xV{1,i}(1,idxVec),xV{1,i}(2,idxVec),'b',...
                dirx(i,:),diry(i,:),'g',...   %direction arrow base
                fov1x(i,:),fov1y(i,:),'m',...
                fov2x(i,:),fov2y(i,:),'m',...
                xV{1,i}(1,k),xV{1,i}(2,k),'b.',...
                sV{1,i}(1,idxVec),sV{1,i}(2,idxVec),'r',...
                sV{1,i}(1,k),sV{1,i}(2,k),'r.');
                hold on
            for u=1:size(plotColor1,1)
                set(lineHandle1(u),'Color',plotColor1(u,:))
            end
        end
        

        idxVec=startplotidx+j-1:startplotidx+j;
        if (length(landm) ~= 0) %landmarks exist
            plotColor2=[0        0           0         ;
                 1-j*colInc2 1-j*colInc2 1         ;
                 .5          .5          1         ;
                 1           1-j*colInc2 1-j*colInc2;
                 1           .5          .5       ];
            lineHandle2=plot(landm(:,1),landm(:,2),'*',...
            xVm(1,idxVec),xVm(2,idxVec),'-',...
            xVm(1,k),xVm(2,k),'.',...
            sVm(1,idxVec),sVm(2,idxVec),'-',...
            sVm(1,k),sVm(2,k),'.');
        else %no landmarks to plot
            plotColor2=[1-j*colInc2 1-j*colInc2 1         ;
                 .5          .5          1         ;
                 1           1-j*colInc2 1-j*colInc2;
                 1           .5          .5       ];
            lineHandle2=plot(xVm(1,idxVec),xVm(2,idxVec),'-',...
            xVm(1,k),xVm(2,k),'.',...
            sVm(1,idxVec),sVm(2,idxVec),'-',...
            sVm(1,k),sVm(2,k),'.');
        end
        
        for u=1:size(plotColor2,1)
            set(lineHandle2(u),'Color',plotColor2(u,:))
        end
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

figure
subplot(2,1,1)
plot(1:N,posErrNormm(1:N))
title('Position error');
ylabel('Position error norm');
xlabel('Time [samples]');
subplot(2,1,2)
plot(1:N,velErrNormm(1:N))
title('Velocity error');
ylabel('Velocity error norm');
xlabel('Time [samples]');

% toc
% fprintf('seconds/frame %i\n', toc/frame);
% mov = close(mov);

