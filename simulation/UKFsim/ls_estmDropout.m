function xm = ls_estmDropout(th)
%Uses weighted least squares to estimate the position of the mouse.
%Input: vector of cat bearings
global x;
global actCats;
        [A,b]=llsmDropout(th);
        xLS=A\b; %ordinary least square estimate
        W=zeros(length(actCats));
        k=1;
        for i=actCats
            catrange=x(1:2,i)-xLS;
            W(k,k)=1/norm(catrange)^2;
            k=k+1;
        end
        xm=(A'*W*A)\(A'*W*b); %weigted least square estimate