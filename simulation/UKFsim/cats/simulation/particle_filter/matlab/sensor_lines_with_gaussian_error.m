close all
clear all

%mov = avifile('sensor_lines_with_gaussian_error.avi');
 
c1 = 0.1 + i * 0.1;
c2 = 1.9 + i * 0.1;
c3 = 1 + i * 1.9;

[X, Y] = meshgrid(0:.02:2, 0:.02:2);
stddegrees = 3;
s = stddegrees*(pi/180);	% Standard deviation in radians
titletext = sprintf('Sensor lines with gaussian error (\\sigma=%i degrees)', stddegrees);

figure
frame = 1;
tic
for theta = 0:0.05:2*pi
	m1 = 1 + 0.8*cos(theta) + i*(1 + 0.8*sin(theta));
	v1 = (m1 - c1) * exp(i*randn(1)*s);
	v2 = (m1 - c2) * exp(i*randn(1)*s);
	v3 = (m1 - c3) * exp(i*randn(1)*s);
	d1 = abs(v1);
	d2 = abs(v2);
	d3 = abs(v3);
	v1 = v1/d1;
	v2 = v2/d2;
	v3 = v3/d3;
	theta1 = angle(v1);
	theta2 = angle(v2);
	theta3 = angle(v3);

	l1 = [c1 c1+d1*1.2*v1];
	l2 = [c2 c2+d2*1.2*v2];
	l3 = [c3 c3+d3*1.2*v3];
	plot(real(l1), imag(l1), 'b-', 'LineWidth', 1)
	axis([0 2 0 2])
	hold on
	plot(real(l2), imag(l2), 'b-', 'LineWidth', 1)
	plot(real(l3), imag(l3), 'b-', 'LineWidth', 1)
	plot(real(m1), imag(m1), 'rx', 'LineWidth', 2)
	title(titletext)
	hold off

	frame = frame + 1
	%f2 = getframe(gcf);
	%mov = addframe(mov, f2);
end
fprintf('seconds/frame %i\n', toc/frame);
%mov = close(mov);
