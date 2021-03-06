clc
clear
figure
close all
global cats;
global catsClean;
global n;
global nx;
global dt;
global R;
global r;

s = RandStream.create('mt19937ar','seed',30);
%s=RandStream('mt19937ar'); %
RandStream.setDefaultStream(s);

marg=0.3;
bound=[0-marg 2+marg 0-marg 2+marg];
catsFullClean=[0.1,1.9 ;...
               1.9, .1 ;...
               1.9,1.9 ];
catsClean=catsFullClean;
n=size(catsFullClean,1);
nx=4;
stddegrees = 1;
lambda = stddegrees*(pi/180);	% Standard deviation in radians
catPosNoise = .05;%std of the noise of the cat positions
                   %0.04 was measured as an average error of the moving mouse
                   %position when no cat position error was applied, only a
                   %measurement noise with a std of 3 degrees.
                   %Cats (3 of them) were located in the corners of the
                   %arena
N=450;                           %Number of time steps
dt=0.1;
sm=[.2;.5];
vm=zeros(2,N);
turnInd = [90,120,230,320,370,N];
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

q=.05;    %std of expected process noise
%qa=0.02;    %std of actual process noise
r=7*(.3*catPosNoise+lambda);    %std of expected measurement noise
ra=lambda;       %std of actual measurement noise
Q=q^2*[ dt^4/4  0        dt^3/2  0     ;...
        0       dt^4/4  0        dt^3/2;...
        dt^3/2  0       dt^2     0     ;...
        0       dt^3/2  0        dt^2] ;    % covariance of process
R = r^2*eye(n);                             % covariance of measurement
f = @update;                                % nonlinear state equations
hT = @measureTrue;                          % measurement equation (actual readings)
h = @measure;                               % measurement equation (for UKF)
P = 1e-3*eye(nx);                           % initial state covraiance
xV = zeros(nx,N);         %estmate          % allocate memory

cats=catsClean + catPosNoise*randn(n,2)+catPosNoise;            %cat positions with noise
s = [sm; vm(:,1)];                              % initial state
trueInit=[sm; vm(:,1)];
initpos=ls_est(hT(trueInit)+ra*randn(n,1));      % initial position with noise
x = [initpos; 0; 0];

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
zV = zeros(n,N);

raysX=cell(n,N);
raysY=cell(n,N);
faraway=10;

posErrNorm=zeros(N,1);
velErrNorm=zeros(N,1);

xstatic=zeros(2,N);

actCats=[1:n];

for k=1:N
    cats=catsClean + catPosNoise*randn(n,2)+catPosNoise;
        
%     if k==round(N/7)
%         R(1,1)=1e30;
%         R(2,2)=1e30;
%         R(3,3)=1e30;
%         actCats=[];
%     end
%     if k==round(4*N/7)
%         R=r^2*eye(n);
%         actCats=[1 2 3];
%     end
%     if k==round(5*N/7)
%         R(1,1)=1e30;
%         R(2,2)=1e30;
%         R(3,3)=1e30;
%         actCats=[];
%     end
%     if k==round(6*N/7)
%         R=r^2*eye(n);
%         actCats=[1 2 3];
%     end    
    
    mouse=[sm; vm(:,k)];                 %linear movement
    posErrNorm(k)=norm(s(1:2)-x(1:2));
    velErrNorm(k)=norm(s(3:4)-x(3:4)*dt);
    sm=sm+vm(:,k);
    xV(:,k) = x;
    z=hT(s)+ra*randn(n,1);                   % measurements
    
    for j=actCats
        raysX{j,k}(1,:)=[catsClean(j,1), cos(z(j,1))+catsClean(j,1) + faraway*cos(z(j,1))];
        raysY{j,k}(1,:)=[catsClean(j,2), sin(z(j,1))+catsClean(j,2) + faraway*sin(z(j,1))];
    end
    
    sV(:,k)= s;                             % save actual state
    zV(1:n,k) = z;                         % save measurment

    [x, P] = ukf(f,x,P,h,z,Q,R);
    
    if isempty(actCats);
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
    
    s = mouse; %+ qa*randn(nx,1);           % update process
    
    %%Least squares estimates, for comparison
     %xstatic(:,k)=ls_est(z);
    
    %real time plotting
    %xstatic(1,1:k),xstatic(2,1:k),'g.',...
    plot(cats(:,1),cats(:,2),'*b',...
        sV(1,1:k),sV(2,1:k),'r',...
        xV(1,1:k),xV(2,1:k),'b',...
        catsClean(:,1),catsClean(:,2),'*k');
    hold on
    
    for j=actCats
        plot([raysX{j,k}],[raysY{j,k}],'k');
    end
    
    axis(bound)
    hold off
    pause(.002)
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

