function x = ls_est(th)
%Uses weighted least squares to estimate the position of the mouse.
%Input: vector of cat bearings
global catsClean;
global n;
        [A,b]=lls(th);
        xLS=A\b; %ordinary least square estimate
        W=zeros(n);
        for i=1:n
            catrange=catsClean(i,:)-xLS';
            W(i,i)=1/norm(catrange)^2;
        end
        x=(A'*W*A)\(A'*W*b); %weigted least square estimate