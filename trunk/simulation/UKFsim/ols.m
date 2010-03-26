function beta = ols(theta, cats, j)
    if nargin > 2
        theta(j,1)=theta(j,1)+1e-10;
    end
        A=zeros(n-1,2);
        b=zeros(n-1,1);
        for k=1:3
            A(k,:)=[tan(theta(k,1)), -1];
            b(k,1)=tan(theta(k,1))*cats(k,1)-cats(k,2);
        end
        beta=A\b;
end