clear all

N = 100

hb = 12;
lb = 20;

x1 = rand(N, 1)*2^(hb-1);
x2 = uint32(x1*2^lb);
tic
for i = 1:N
	y1(i) = sqrt(x1(i));
end
t1 = toc;
tic
for i = 1:N
	y2(i) = binfpsqrt(x2(i));
end
t2 = toc;
%t1/N
%t2/N
avg_diff = sum(y1 - double(y2)*2^-lb)/N
