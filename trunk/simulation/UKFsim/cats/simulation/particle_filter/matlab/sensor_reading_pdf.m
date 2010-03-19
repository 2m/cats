close all
clear all

%mov = avifile('sensor_reading_distribution.avi');
 
c1 = 0.1 + i * 0.1;
c2 = 1.9 + i * 0.1;
c3 = 1 + i * 1.9;

[X, Y] = meshgrid(0:.02:2, 0:.02:2);
stddegrees = 3;
s = stddegrees*(pi/180);	% Standard deviation in radians
titletext = sprintf('Sensor lines with gaussian error (std=%i degrees)', stddegrees);
%V = 0.5:0.5:4;
%V = V * s;

frame = 1;
tic
for theta = 0:0.05:2*pi
	m1 = 1 + 0.8*cos(theta) + i*(1 + 0.8*sin(theta));
	v1 = m1 - c1;
	v2 = m1 - c2;
	v3 = m1 - c3;

	Z1 = sensor_pdf(X, Y, c1, v1, s);
	Z2 = sensor_pdf(X, Y, c2, v2, s);
	Z3 = sensor_pdf(X, Y, c3, v3, s);
	Z4 = Z1 .* Z2 .* Z3;

	subplot(2, 2, 1)
	contour(X, Y, Z1)
	hold on
	plot(real(m1), imag(m1), 'rx', 'LineWidth',2)
	hold off

	subplot(2, 2, 2)
	contour(X, Y, Z2)
	hold on
	plot(real(m1), imag(m1), 'rx', 'LineWidth',2)
	hold off

	subplot(2, 2, 3)
	contour(X, Y, Z3)
	hold on
	plot(real(m1), imag(m1), 'rx', 'LineWidth',2)
	hold off

	subplot(2, 2, 4)
	contour(X, Y, Z4)
	hold on
	plot(real(m1), imag(m1), 'rx', 'LineWidth',2)
	hold off

	frame = frame + 1
	%f2 = getframe(gcf);
	%mov = addframe(mov, f2);
end
fprintf('seconds/frame %i\n', toc/frame);
%mov = close(mov);
