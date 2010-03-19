clc
clear
close all
% n=2;
% cats=[0, 0.8; 0.8, 0];
n=3;
cats=[.6,.6; 0,0.8; 0.8,0];
mouse=[.8;1];
N=500;

for r=1:1
betaTotW=zeros(N,2);
betaTot=zeros(N,2);
beta2=zeros(N,2);

% figure
% hold on
% axis([0 1.5 0 1.5])
% raysX=zeros(3,2);
% raysY=zeros(3,2);
% faraway=10;
% slope=zeros(n,n-1);
% raysX=cell(n,n-1);
% raysY=cell(n,n-1);
tic
for p=1:N
    th=zeros(n,1);
    for i=1:n
        th(i,1)=atan((mouse(2,1)-cats(i,2))/(mouse(1,1)-cats(i,1)));
    end
    lambda=0.05;
    th=th+lambda*randn(n,1);
    dth=[lambda/1e10; 0];
    thAdj=zeros(n,1);
    beta=cell(n,n-1,length(dth));
    betaErr=zeros(n,n-1);
    for j=1:n %current cat
        for k=1:n %combined with other cats
            if j==k
                continue
            end
            for q=1:length(dth) %adjust with dth
                thAdj=th;
                thAdj(j,1)=th(j,1)+dth(q,1);
                
                A=zeros(n-1,2);
                C=zeros(n-1,1);
                A(1,:)=[tan(thAdj(j,1)), -1];
                A(2,:)=[tan(thAdj(k,1)), -1];
                
                C(1,1)=tan(thAdj(j,1))*cats(j,1)-cats(j,2);
                C(2,1)=tan(thAdj(k,1))*cats(k,1)-cats(k,2);
                
                beta{j,k,q}=A\C;
                %                 if q==2
                %                     raysX{j,k}(1,:)=[cats(j,1), beta{j,k,2}(1,1)+faraway*(beta{j,k,2}(1,1)-cats(j,1))];
                %                     raysY{j,k}(1,:)=[cats(j,2), beta{j,k,2}(2,1)+faraway*(beta{j,k,2}(2,1)-cats(j,2))];
                %                     plot(raysX{j,k}',raysY{j,k}','k')
                %                     plot(beta{j,k,q}(1,1),beta{j,k,q}(2,1),'.g')
                %                 else
                %                     plot(beta{j,k,q}(1,1),beta{j,k,q}(2,1),'.b')
                %                 end
            end
            betaErr(j,k)=norm(beta{j,k,1}-beta{j,k,2});
        end
    end
    %plot(mouse(1,1),mouse(2,1),'r*',cats(:,1),cats(:,2),'*k')
    betaErrSum=sum(betaErr,2);
    
    A=zeros(n,2);
    C=zeros(n,1);
    for i=1:n
        A(i,:)=[tan(th(i,1)), -1];
        C(i,1)=tan(th(i,1))*cats(i,1)-cats(i,2);
    end
    W=eye(n);
    betaErrSum=betaErrSum/max(betaErrSum);
    mouseDupe=zeros(n,2);
    mouseDupe(:,1)=mouse(1,1);
    mouseDupe(:,2)=mouse(2,1);
    catrange=cats-mouseDupe;
    for i=1:n
        W(i,i)=1/sqrt((catrange(i,1)^2+catrange(i,2)^2))^2;
    end
    
    betaTotW(p,:)=(A'*W*A)\(A'*W*C);
    betaTot(p,:)=A\C;
    
    %2 cats only
    A=zeros(2,2);
    C=zeros(2,1);
    for i=2:3
        A(i,:)=[tan(th(i,1)), -1];
        C(i,1)=tan(th(i,1))*cats(i,1)-cats(i,2);
    end
    beta2(p,:)=A\C;
    
end
toc
mouseBig=zeros(N,2);
mouseBig(:,1)=mouse(1,1);
mouseBig(:,2)=mouse(2,1);

betaTotWerror=betaTotW-mouseBig;
betaTotError=betaTot-mouseBig;
beta2TotError=beta2-mouseBig;
for i=1:N
    betaTotWerrorNorm=norm(betaTotWerror(i,:));
    betaTotErrorNorm=norm(betaTotError(i,:));
    beta2TotErrorNorm=norm(beta2TotError(i,:));
end
perfW(r,1)=sum(betaTotWerrorNorm)/N;
perf(r,1)=sum(betaTotErrorNorm)/N;
perf2(r,1)=sum(beta2TotErrorNorm)/N;
r
end
perfWavg=mean(perfW)
perfavg=mean(perf)
perf2avg=mean(perf2)

varPerfW=norm(var(betaTotWerror))
varPerf=norm(var(betaTotError))
varPerf2=norm(var(beta2TotError))

figure
hold on
axis([0 1.5 0 1.5])
scatter(betaTot(:,1),betaTot(:,2),1,'k')
plot(mouse(1,1),mouse(2,1),'r*',cats(:,1),cats(:,2),'*k')

figure
hold on
axis([0 1.5 0 1.5])
scatter(betaTotW(:,1),betaTotW(:,2),1,'k')
plot(mouse(1,1),mouse(2,1),'r*',cats(:,1),cats(:,2),'*k')

figure
hold on
axis([0 1.5 0 1.5])
scatter(beta2(:,1),beta2(:,2),1,'k')
plot(mouse(1,1),mouse(2,1),'r*',cats(:,1),cats(:,2),'*k')


