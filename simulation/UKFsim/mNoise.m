function mN=mNoise(catidx)
global n;
global ra;
global cats;
mN=ra(1)*randn(n,1);

vnoise=randn(2,1);

mNoiseVec=vnoise/norm(vnoise)... %normalize
    *norm(cats(3:4,catidx)); %make same length as cats(3,catidx)

mN(n+1,1)=ra(1+1)*mNoiseVec(1);
mN(n+2,1)=ra(1+2)*mNoiseVec(2);

mN(n+3,1)=ra(1+3)*randn;
mN(n+4,1)=ra(1+4)*randn;