function [A,b] = llsmDropout(th)
global x;
global actCats;
A=zeros(length(actCats),2);
b=zeros(length(actCats),1);
k=1;
for i=actCats
    A(k,:)=[sin(th(i,1)), -cos(th(i,1))];
    b(k,1)=sin(th(i,1))*x(1,i)-cos(th(i,1))*x(2,i);
    k=k+1;
end