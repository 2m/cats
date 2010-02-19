close all
clear all

mov = avifile('particle_filter_moving_sensors.avi');
 
cat(1) = 0.1 + i * 0.1;
cat(2) = 1.9 + i * 0.1;
cat(3) = 1 + i * 1.9;

[X, Y] = meshgrid(0:.02:2, 0:.02:2);
stddegrees = 3;
s = stddegrees*(pi/180);	% Standard deviation in radians
titletext = sprintf('Prototype of particle filter (\\sigma_{sensors}=%i degrees)', stddegrees);

N = 80
p = zeros(N, 5);
p = [rand(N, 2)*2 , rand(N, 2)*0.04-0.02, ones(N, 1)];	% x, y, vx, vy, w
T = 0.5;

figure
frame = 1;
tic
lasttheta = 0;
for theta = 0:0.05:2*pi
	% Update mouse
	mouse = 1 + 0.2*cos(theta) + i*(1 + 0.05*sin(theta));

	% Update cat
	cat(1) = 1 + 0.7*cos(theta*2) + i*(1.8 + 0.05*sin(theta*2));
	cat(2) = 1 + 0.7*cos(theta*3) + i*(0.2 + 0.05*sin(theta*3));

	% Update sensors and add noise
	v(1) = (mouse - cat(1)) * exp(i*randn(1)*s);
	v(2) = (mouse - cat(2)) * exp(i*randn(1)*s);
	d = abs(v);
	v = v./d;
	mu = angle(v);

	% Update particles
	% Integrate
	p(:, 1) = p(:, 1) + T*p(:, 3);
	p(:, 2) = p(:, 2) + T*p(:, 4);

	% Compare particles to sensors
	A = p(:, 1) + i*p(:, 2);
	for g = 1:2
		z = angle(A - cat(g));
		% Penalty function
		w = (1/sqrt(2*pi*s^2))*exp(-((z - mu(g)).^2)/(2*s^2));
		p(:, 5) = p(:, 5) .* w;	% Update weights
	end

	% Normalise
	p(:, 5) = p(:, 5)/max(p(:, 5));

	% Set up cut-offs
	Nuc = floor(N*0.8);
	Nlc = floor(N*0.2);
	%standard_deviation_in_degrees = sqrt(var(c))*(180/pi)

	% Sort according to weights
	A = p;
	[OldRowNumber, NewRowNumber] = sort(A(:, 5));
	p = A(NewRowNumber, :);

	% Calculate mean
	m = [mean(p(Nuc:N, 1)) mean(p(Nuc:N, 2))];

	% Resample
	A = [p(Nuc:N, 1:4) zeros(size(p(Nuc:N, 5)))];
	B = [randn(N-Nuc+1, 2)*0.02-0.01, randn(N-Nuc+1, 2)*0.02-0.01, ones(N-Nuc+1, 1)];
	p(1:Nlc+1,:) = A + B;

	% Plot
	l1 = [cat(1) cat(1)+d(1)*1.2*v(1)];
	l2 = [cat(2) cat(2)+d(2)*1.2*v(2)];
	plot(real(l1), imag(l1), 'b-', 'LineWidth', 1)
	axis([0 2 0 2])
	hold on
	scatter(p(:,1), p(:,2), 'ro')
	plot(real(cat(1)), imag(cat(1)), 'bo', 'LineWidth', 1)
	plot(real(cat(2)), imag(cat(2)), 'bo', 'LineWidth', 1)
	plot(real(l2), imag(l2), 'b-', 'LineWidth', 1)
	plot(real(mouse), imag(mouse), 'rx', 'LineWidth', 2)
	plot(m(1), m(2), 'bx', 'LineWidth', 2)
	title(titletext)
	hold off

	% Update frame counter and save frame
	frame = frame + 1;
	f2 = getframe(gcf);
	mov = addframe(mov, f2);

	% Save stuff for next iteration
	lasttheta = theta;
end
fprintf('seconds/frame %i\n', toc/frame);
mov = close(mov);
