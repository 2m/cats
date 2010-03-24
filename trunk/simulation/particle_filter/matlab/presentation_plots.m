close all
clear all

d1 = -15:0.1:15;
d2 = 0:0.1:15;
stddegrees = 3;

r1 = d1*(pi/180);
r2 = d2*(pi/180);
c1 = cos(r1);
c2 = cos(r2);
yn = 1/sqrt(2*pi*stddegrees^2)*exp(-(d1.^2)/(2*stddegrees^2));	% Gaussian pdf
y = exp(-(d1.^2)/(2*stddegrees^2));	% Gaussian pdf (not normalised)

for i = 1:size(c2, 2)
	w(i) = penalty(c2(i));
end

figure
plot(d1, yn)
title('Gaussian pdf')
ylabel('Probability')
xlabel('Deviation [degrees]')
%print('fig1.png', '-dpng')

figure
plot(c1, yn)
title('Gaussian pdf')
ylabel('Probability')
xlabel('Deviation (cos on angle)')
%print('fig2.png', '-dpng')

figure
plot(c1, y, 'b--')
hold on
plot(c2, w, 'r')
title('Gaussian pdf (not normalised)')
ylabel('Probability')
xlabel('Deviation (cos on angle)')
legend('Exact function', 'Approximation', 'Location', 'NorthWest')
hold off
%print('fig3.png', '-dpng')
