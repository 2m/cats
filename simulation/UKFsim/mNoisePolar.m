function mN=mNoisePolar
global n;
global nz;
global ra;

mN=ra(1)*randn(n,1);

for i=1:nz-n
mN(n+i,1)=ra(1+i)*randn;
end