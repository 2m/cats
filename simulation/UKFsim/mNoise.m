function mN=mNoise
global n;
global ra;
global nz;
mN=ra(1)*randn(n,1);
for i=1:nz-n
    mN(n+i,1)=ra(i+1)*randn;
end