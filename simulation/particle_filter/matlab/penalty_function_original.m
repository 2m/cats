mu = 0;
s = 1;
x1 = -4:0.05:4;
x2 = -1.3:0.05:1.3;
x3 = 1.3:0.05:4;
y1 = 1/sqrt(2*pi*s^2)*exp(-((x1 - mu).^2)/(2*s^2));	% Exact function
y2 = 0.4 - ((x2*2).^2)*(1/30);				% Approximation
y3 = 0.4 - ((x2*2).^2)*(1/30);
plot(x1, y1, 'b')
hold on
plot(x2, y2, 'r')
hold off
