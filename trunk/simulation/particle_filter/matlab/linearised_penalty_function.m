close all
clear all

tic
d = 0:0.25:180;					% Interval in degrees
r = d*(pi/180);					% Convert degrees to radians
mu = 0;						% Mean of normal distribution
s = 3;						% Standard deviation in degrees
y = 1/sqrt(2*pi*s^2)*exp(-((d - mu).^2)/(2*s^2));	% Gaussian pdf
y = y*(1/max(y));					% Normalise
x = cos(r);					% Cosine of interval
e = 1e-3;

% Find interesting interval
i = 1;
while (y(i)<e)
	i = i + 1;
end
ilower = i;
i = size(y, 2);
while (y(i)<e)
	i = i - 1;
end
iupper = i;

%% Brute force optimal solution
xs = x(ilower:iupper);
ys = y(ilower:iupper);
g = zeros(size(ys));	% Allocate memory
ilen = iupper - ilower + 1;
i = [0 0 0];
e = 1e10;
c = zeros(4, 2);
for i1 = 1:ilen-3
	c(1, :) = polyfit(xs(1:i1),ys(1:i1), 1);
	g(1:i1) = c(1, 1)*xs(1:i1) + c(1, 2);
	for i2 = i1+1:ilen-2
		c(2, :) = polyfit(xs(i1+1:i2),ys(i1+1:i2), 1);
		g(i1+1:i2) = c(2, 1)*xs(i1+1:i2) + c(2, 2);
		for i3 = i2+1:ilen-1
			c(3, :) = polyfit(xs(i2+1:i3),ys(i2+1:i3), 1);
			g(i2+1:i3) = c(3, 1)*xs(i2+1:i3) + c(3, 2);
			c(4, :) = polyfit(xs(i3+1:ilen),ys(i3+1:ilen), 1);
			g(i3+1:ilen) = c(4, 1)*xs(i3+1:ilen) + c(4, 2);
			t = sum((g - ys).^2);
			if (t<e)
				yc = g;
				e = t;
				i = [i1 i2 i3];
			end
		end
	end
end
toc
cosine_cutoff = x(iupper + 1);
cosine_cutoff_12_20 = int32(cosine_cutoff * 2^20)
c_12_20 = int32(c * 2^20)

figure
subplot(2, 2, 1)
hold on
title('Cosine(\theta)')
plot(x)
hold off

subplot(2, 2, 2)
hold on
title('Normal distribution pdf')
plot(y)
hold off

subplot(2, 2, 3)
hold on
title('Cosine against pdf')
plot(x, y)
hold off

subplot(2, 2, 4)
hold on
title('Subset of cosine against linearised pdf')
plot(xs, ys, 'b-')
plot(xs, yc, 'r-')
hold off

%print('linearised_penalty_function.png', '-dpng')
