clc
clear
figure
close all
n=3;
cats=[0, 0; 0, 0.8; 0.8, 0];
%beta=zeros(N,2);
mouse=[.8;1];
N=500;
th=zeros(n,1);
for i=1:n
    th(i,1)=atan((mouse(2,1)-cats(i,2))/(mouse(1,1)-cats(i,1)));
end
lambda=0.08;
th=th+lambda*randn(n,1);
dth=[-lambda/5; 0; lambda/5];
thAdj=zeros(n,1);
beta=cell(n,n-1,length(dth));
betaErr=zeros(n,n-1);

figure
hold on
axis([0 1.5 0 1.5])
raysX=zeros(3,2);
raysY=zeros(3,2);
faraway=10;
slope=zeros(n,n-1);
raysX=cell(n,n-1);
raysY=cell(n,n-1);
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
            if q==2
                raysX{j,k}(1,:)=[cats(j,1), beta{j,k,2}(1,1)+faraway*(beta{j,k,2}(1,1)-cats(j,1))];
                raysY{j,k}(1,:)=[cats(j,2), beta{j,k,2}(2,1)+faraway*(beta{j,k,2}(2,1)-cats(j,2))];
                plot(raysX{j,k}',raysY{j,k}','k')
                plot(beta{j,k,q}(1,1),beta{j,k,q}(2,1),'.g')
            else
                plot(beta{j,k,q}(1,1),beta{j,k,q}(2,1),'.b')
            end
        end
        betaErr(j,k)=norm(beta{j,k,1}-beta{j,k,2})+norm(beta{j,k,3}-beta{j,k,2});
    end
end
plot(mouse(1,1),mouse(2,1),'r*',cats(:,1),cats(:,2),'*k')
betaErrSum=sum(betaErr,2)

A=zeros(n,2);
C=zeros(n,1);
for i=1:n
    A(i,:)=[tan(th(i,1)), -1];
    C(i,1)=tan(th(i,1))*cats(i,1)-cats(i,2);
end
W=eye(n);
for i=1:n
   W(i,i)=1/betaErrSum(i,1)^2;
end

betaTotW(:,1)=(A'*W*A)\(A'*W*C);
betaTot(:,1)=A\C;
plot(betaTot(1,1),betaTot(2,1),'mx')
plot(betaTotW(1,1),betaTotW(2,1),'m*')




