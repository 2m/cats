function xm = ls_estm(th)
%Uses weighted least squares to estimate the position of the mouse.
%Input: vector of cat bearings
global x;
global nm;
        [A,b]=llsm(th);
        xLS=A\b; %ordinary least square estimate
        W=zeros(nm);
        for i=1:nm
            catrange=x(1:2,i)-xLS;
            W(i,i)=1/norm(catrange)^2;
        end
        xm=(A'*W*A)\(A'*W*b); %weigted least square estimate