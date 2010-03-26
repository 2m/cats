function x = ls_est2(th)
%Uses weighted least squares to estimate the position of the mouse.
%Input: vector of cat bearings
global landm;
global n;
[A,b]=lls2(th);
xLS=A\b; %ordinary least square estimate
W=zeros(n);
for i=1:n
    landmRange=landm(i,:)-xLS';
    W(i,i)=1/norm(landmRange)^2;
end
x=(A'*W*A)\(A'*W*b); %weigted least square estimate