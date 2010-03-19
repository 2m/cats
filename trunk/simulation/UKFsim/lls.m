function [A,b] = lls(th)
global cats;
global n;
A=zeros(n-1,2);
b=zeros(n-1,1);
for k=1:n
    A(k,:)=[sin(th(k,1)), -cos(th(k,1))];
    b(k,1)=sin(th(k,1))*cats(k,1)-cos(th(k,1))*cats(k,2);
end
return