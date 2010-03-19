clc
clear
close all
global n;
global cats;
global N;
global mouse;
n=3;
ax=[0 2 0 2];
cats=[0.1,0.1 ; 1.9,0.1 ; 1,1.9];
n=size(cats,1);

% figure(1)
% hold on
% axis(ax);
% [mousex,mousey]=ginput(1);
% mouse=[mousex; mousey];
% plot(mousex,mousey,'r*');
% axis(ax);
% for i=1:n
%     [catsx(i),catsy(i)]=ginput(1);
%     cats(i,:)=[catsx(i),catsy(i)];
%     plot(cats(i,1),cats(i,2),'*k')
% end
% close(1)

N=600;
stddegrees = 3;
lambda = stddegrees*(pi/180);	% Standard deviation in radians

betaLS=zeros(N,2);
beta2=zeros(N,2);
betaWLS=zeros(N,2);
betaIV=zeros(N,2);
betaWIV=zeros(N,2);

mov = avifile('circular_movementAll.avi');
frame = 1;
fig=figure;
tic
for phi = 0:0.05:2*pi
    mouse =[1+0.8*cos(phi);1+0.8*sin(phi)];

    for p=1:N
        th=theta(mouse);
        th=th+lambda*randn(n,1);
        
        [A,b]=lls(th);
        betaLS(p,:)=A\b;
        
        W=zeros(n);
        betaLSDupe=zeros(n,2);
        betaLSDupe(:,1)=betaLS(p,1);
        betaLSDupe(:,2)=betaLS(p,2);
        catrange=cats-betaLSDupe;
        for i=1:n
            W(i,i)=1/sqrt((catrange(i,1)^2+catrange(i,2)^2))^2;
        end
        
        betaWLS(p,:)=(A'*W*A)\(A'*W*b);
        
        thLS=theta(betaLS(p,:)');
        [G,h]=lls(thLS);
        betaIV(p,:)=(G'*A)\(G'*b);
        
        thIV=theta(betaIV(p,:)');
        betaWIV(p,:)=(G'*W*A)\(G'*W*b);
    end
    
%     [betaLSperf,betaLSvar]=perf(betaLS);
%     [beta2perf,beta2var]=perf(beta2);
%     [betaWLSperf,betaWLSvar]=perf(betaWLS);
%     [betaIVperf,betaIVvar]=perf(betaIV);
%     [betaWIVperf,betaWIVvar]=perf(betaWIV);
%     
%     disp('Perf:')
%     disp(betaLSperf);
%     disp(betaWLSperf);
%     disp(betaIVperf);
%     disp(betaWIVperf);
%     
%     disp('Var:')
%     disp(betaLSvar);
%     disp(betaWLSvar);
%     disp(betaIVvar);
%     disp(betaWIVvar);
    
    subplot(2,2,1)
    axis(ax)
    scatter(betaLS(:,1),betaLS(:,2),1,'k')
    hold on
    plot(mouse(1,1),mouse(2,1),'r*',cats(:,1),cats(:,2),'*k')
    axis(ax)
    hold off
    
    subplot(2,2,2)
    scatter(betaWLS(:,1),betaWLS(:,2),1,'k')
    hold on
    plot(mouse(1,1),mouse(2,1),'r*',cats(:,1),cats(:,2),'*k')
    axis(ax)
    hold off
    
    subplot(2,2,3)
    scatter(betaIV(:,1),betaIV(:,2),1,'k')
    hold on
    plot(mouse(1,1),mouse(2,1),'r*',cats(:,1),cats(:,2),'*k')
    axis(ax)
    hold off
    
    subplot(2,2,4)
    scatter(betaWIV(:,1),betaWIV(:,2),1,'k')
    hold on
    plot(mouse(1,1),mouse(2,1),'r*',cats(:,1),cats(:,2),'*k')
    axis(ax)
    hold off
    
    frame = frame + 1
    f2 = getframe(fig);
    mov = addframe(mov, f2);
end
toc
fprintf('seconds/frame %i\n', toc/frame);
mov = close(mov);
