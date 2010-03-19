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
cats=[0,0 ; 3,2 ; 2,3];
mouse=[3;3];
N=2000;

betaLS=zeros(N,2);
beta2=zeros(N,2);
betaWLS=zeros(N,2);
% betaIV=zeros(N,2);
% betaWIV=zeros(N,2);

for p=1:N
    th=theta(mouse);
    lambda=0.08;
    th=th+lambda*randn(n,1);
    
    [A,b]=lls(th);
    betaLS(p,:)=A\b;
    
%     betaPert=cell(n,1);
%     betaErr=zeros(n,1);
%     for j=1:n %current cat
%         [A,b]=lls(th,j);
%         betaPert{j}=A\b;
%         betaErr(j,1)=norm((betaPert{j,1}'-betaLS(p,:)));
%     end
    
    [A,b]=lls(th);
    
    W1=eye(n);
    betaLSDupe=zeros(n,2);
    betaLSDupe(:,1)=betaLS(p,1);
    betaLSDupe(:,2)=betaLS(p,2);
    catrange=cats-betaLSDupe;
    for i=1:n
        W(i,i)=1/sqrt((catrange(i,1)^2+catrange(i,2)^2))^2;
    end

    betaWLS(p,:)=(A'*W*A)\(A'*W*b);
    %betaW2LS(p,:)=(A'*W1*A)\(A'*W1*b);
    
%     thWLS=theta(betaWLS(p,:)');
%     [G,h]=lls(thWLS);
%     betaIV(p,:)=(G'*A)\(G'*b);
%     thIV=theta(betaIV(p,:)');
%     
%     betaIVpert=cell(n);
%     betaErrIV=zeros(n,1);
%     for j=1:n %current cat
%         [A,b]=lls(thIV,j);
%         betaIVpert{j}=A\b;
%         betaErrIV(j,1)=norm((betaIVpert{j,1}'-betaIV(p,:)));
%     end
%     
%     WIV=eye(n);
%     for i=1:n
%         WIV(i,i)=1/betaErrIV(i,1)^2;
%     end
%     betaWIV(p,:)=(G'*WIV*A)\(G'*WIV*b);
    
    %2 cats only
    A=zeros(2,2);
    b=zeros(2,1);
    for i=2:3
        A(i,:)=[sin(th(i,1)), -cos(th(i,1))];
        b(i,1)=sin(th(i,1))*cats(i,1)-cos(th(i,1))*cats(i,2);
    end
    beta2(p,:)=A\b;
    
end

[betaLSperf,betaLSvar]=perf(betaLS);
[beta2perf,beta2var]=perf(beta2);
[betaWLSperf,betaWLSvar]=perf(betaWLS);
%[betaW2LSperf,betaW2LSvar]=perf(betaW2LS);
% [betaIVperf,betaIVvar]=perf(betaIV);
% [betaWIVperf,betaWIVvar]=perf(betaWIV);
disp(betaLSperf);
disp(beta2perf);
disp(betaWLSperf);
%disp(betaW2LSperf);
% disp(betaIVperf);
% disp(betaWIVperf);

ax=[0 5 0 5];

figure
hold on
axis(ax)
scatter(beta2(:,1),beta2(:,2),1,'k')
plot(mouse(1,1),mouse(2,1),'r*',cats(:,1),cats(:,2),'*k')

figure
hold on
axis(ax)
scatter(betaLS(:,1),betaLS(:,2),1,'k')
plot(mouse(1,1),mouse(2,1),'r*',cats(:,1),cats(:,2),'*k')

figure
hold on
axis(ax)
scatter(betaWLS(:,1),betaWLS(:,2),1,'k')
plot(mouse(1,1),mouse(2,1),'r*',cats(1:3,1),cats(1:3,2),'*k')
% 
% figure
% hold on
% axis(ax)
% scatter(betaW2LS(:,1),betaW2LS(:,2),1,'k')
% plot(mouse(1,1),mouse(2,1),'r*',cats(1:3,1),cats(1:3,2),'*k')

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
