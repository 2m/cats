function [A,b] = llsm(th)
global x;
global nm;
A=zeros(nm,2);
b=zeros(nm,1);
for k=1:nm
    A(k,:)=[sin(th(k,1)), -cos(th(k,1))];
    b(k,1)=sin(th(k,1))*x(1,k)-cos(th(k,1))*x(2,k);
end
return