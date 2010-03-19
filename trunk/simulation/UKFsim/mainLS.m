clc
clear
close all
global n;
global cats;
global N;
global mouse;
% n=2;
% cats=[0, 0.8; 0.8, 0];
n=3;
cats=[0,0 ; 0,0.8 ; 0.8,0];
mouse=[.8;1];
N=2000;

betaLS=zeros(N,2);
beta2=zeros(N,2);
betaWLS=zeros(N,2);
betaIV=zeros(N,2);
betaWIV=zeros(N,2);

for p=1:N
    th=theta(mouse);
    lambda=0.08;
    th=th+lambda*randn(n,1);
    
    [A,b]=lls(th);
    betaLS(p,:)=A\b;
    
    betaPert=cell(n);
    betaErr=zeros(n,1);
    for j=1:n %current cat
        [A,b]=lls(th,j);
        betaPert{j}=A\b;
        betaErr(j,1)=norm((betaPert{j}-betaLS));
    end
    
    [A,b]=lls(th);
    W=eye(n);
    for i=1:n
        W(i,i)=1/betaErr(i,1)^2;
    end
    betaWLS(p,:)=(A'*W*A)\(A'*W*b);
    
    thWLS=(betaTotW(p,:));
    [G,h]=lls(thWLS);
    betaIV(p,:)=(G'*A)\(G'*b);
    thIV=(betaIV);
    
    betaIVpert=cell(n);
    betaErrIV=zeros(n,1);
    for j=1:n %current cat
        [A,b]=lls(thIV,j);
        betaIVpert{j}=A\b;
        betaErrIV(j,1)=norm((betaIVpert{j}-betaIV));
    end
    
    WIV=eye(n);
    for i=1:n
        WIV(i,i)=1/betaErrIV(i,1)^2;
    end
    betaWIV(p,:)=(G'*WIV*A)\(G'*WIV*b);
    
    %2 cats only
    A=zeros(2,2);
    b=zeros(2,1);
    for i=2:3
        A(i,:)=[tan(th(i,1)), -1];
        b(i,1)=tan(th(i,1))*cats(i,1)-cats(i,2);
    end
    beta2(p,:)=A\b;
    
end

% [betaLSperf,betaLSvar]=perf(betaLS)
% [beta2perf,beta2var]=perf(beta2)
% [betaWLSperf,betaWLSvar]=perf(betaWLS)
% [betaIVperf,betaIVvar]=perf(betaIV)
% [betaWIVperf,betaWIVvar]=perf(betaWIV)
% 
figure
hold on
axis([0 1.5 0 1.5])
scatter(betaLS(:,1),betaLS(:,2),1,'k')
plot(mouse(1,1),mouse(2,1),'r*',cats(:,1),cats(:,2),'*k')

figure
hold on
axis([0 1.5 0 1.5])
scatter(beta2(:,1),beta2(:,2),1,'k')
plot(mouse(1,1),mouse(2,1),'r*',cats(:,1),cats(:,2),'*k')

figure
hold on
axis([0 1.5 0 1.5])
scatter(betaWLS(:,1),betaWLS(:,2),1,'k')
plot(mouse(1,1),mouse(2,1),'r*',cats(2:3,1),cats(2:3,2),'*k')
% 
% figure
% hold on
% axis([0 1.5 0 1.5])
% scatter(betaIV(:,1),betaIV(:,2),1,'k')
% plot(mouse(1,1),mouse(2,1),'r*',cats(2:3,1),cats(2:3,2),'*k')
% 
% figure
% hold on
% axis([0 1.5 0 1.5])
% scatter(betaWIV(:,1),betaWIV(:,2),1,'k')
% plot(mouse(1,1),mouse(2,1),'r*',cats(2:3,1),cats(2:3,2),'*k')
