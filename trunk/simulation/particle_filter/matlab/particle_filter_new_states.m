close all
clear all

%mov = avifile('particle_filter_new_states.avi');

cat(1) = 0.1 + i * 0.1;
cat(2) = 1.9 + i * 0.1;
cat(3) = 1 + i * 1.9;

N = 400
stddegrees = 3;

[X, Y] = meshgrid(0:.02:2, 0:.02:2);
s = stddegrees*(pi/180);	% Standard deviation in radians
titletext = sprintf('Prototype of particle filter (\\sigma_{sensors}=%i degrees)', stddegrees);

p = [rand(N, 2)*2, rand(N, 1)*360, rand(N, 1)*0.04-0.02, ones(N, 1)*(1/N), zeros(N, 1)];	% x, y, theta, |v|, w, age
T = 0.5;	% Time step length
Nuc = floor(N*0.8);	% Set up cut-offs
Nlc = floor(N*0.2);
x_std = [];
x_std_approx = [];

figure
frame = 1;
lasttheta = 0;
lastmouse = 0;

tic
for theta = 0:0.05:2*pi
	% Update mouse
	%mouse = 1 + 0.6*cos(theta) + i*(1 + 0.6*sin(theta));
	mouse = 1 + i*1;
	if (theta<0.5*pi)
		mouse = 1.9 - 0.9*(theta/(pi*0.5)) + i*1;
	end
	if (theta>1.5*pi)
		mouse = 1 - 0.9*((theta-1.5*pi)/(pi*0.5)) + i*1;
	end
	movement = mouse - lastmouse;

	% Update sensors and add noise
	%v = (mouse - cat) .* exp(i*randn(1, 3)*s);
	%d = abs(v);
	%v = v ./ d;
	v1 = (mouse - cat(1)) * exp(i*randn(1)*s);
	v2 = (mouse - cat(2)) * exp(i*randn(1)*s);
	v3 = (mouse - cat(3)) * exp(i*randn(1)*s);
	d1 = abs(v1);
	d2 = abs(v2);
	d3 = abs(v3);
	v1 = v1/d1;
	v2 = v2/d2;
	v3 = v3/d3;

	% Update particles
	% Integrate
	p(:, 1) = p(:, 1) + cos(p(:, 3)*(pi/360)).*(T*p(:, 4));
	p(:, 2) = p(:, 2) + sin(p(:, 3)*(pi/360)).*(T*p(:, 4));

	% Compare particles to sensors
	A = p(:, 1) + i*p(:, 2);

	% Reinit since we have access to all three sensors
	for g = 1:3
		z = angle(A - cat(g));
		mu = angle(mouse - cat(g));
		% Penalty function
		w = exp(-((z - mu).^2)/(2*s^2));	% Is at most 1
		%w = (1/sqrt(2*pi*s^2))*exp(-((z - mu).^2)/(2*s^2));
		p(:, 5) = p(:, 5) .* w;	% Update weights
	end

	% Increase age
	p(:, 6) = p(:, 6) + 1;

	% Check for errors
	if (sum(isnan(p(:, 5)))>0)
		nan = sum(isnan(p(:, 5)))
		break
	end

	% Sort according to weights
	A = p;
	[OldRowNumber, NewRowNumber] = sort(A(:, 5));
	p = A(NewRowNumber, :);

	% Normalise
	p(:, 5) = p(:, 5)/sum(p(:, 5));
	p(:, 3) = mod(p(:, 3), 360);

	% Calculate mean
	m = [sum(p(:, 1).*p(:, 5)) sum(p(:, 2).*p(:, 5))];
	%m = [mean(p(:, 1)) mean(p(:, 2))];

	% Check std
	x_std(end+1) = std(p(:, 1));
	temp = zeros(1, 6);
	sumw = 0;
	for g = 1:N	% Extract particles in a propability mass
		sumw = sumw + p(g, 5);
		if (sumw <= 0.68)
			temp(g, :) = p(g, :);
		end
	end
	x_std_approx(end+1) = (max(temp(:, 1)) - min(temp(:, 1)))/4;

	%standard_deviation_in_degrees = sqrt(var(c))*(180/pi)

	% Resample
	A = [p(Nuc:N, 1:5), zeros(N-Nuc+1, 1)];
	B = [randn(N-Nuc+1, 2)*0.015, randn(N-Nuc+1, 1)*2, randn(N-Nuc+1, 1)*0.05, ones(N-Nuc+1, 1)*mean(A(:, 5)), zeros(N-Nuc+1, 1) ];
	p(1:Nlc+1,:) = A + B;
	p(:, 5) = ones(N, 1)*(1/N);	% Reset weights

	% Plot
	l1 = [cat(1) cat(1)+d1*1.2*v1];
	l2 = [cat(2) cat(2)+d2*1.2*v2];
	l3 = [cat(3) cat(3)+d3*1.2*v3];
	plot(real(l1), imag(l1), 'b-', 'LineWidth', 1)
	axis([0 2 0 2])
	hold on
	scatter(p(:,1), p(:,2), 'g.')
	plot(real(l2), imag(l2), 'b-', 'LineWidth', 1)
	plot(real(l3), imag(l3), 'b-', 'LineWidth', 1)
	plot(real(mouse), imag(mouse), 'rx', 'LineWidth', 2)
	plot(m(1), m(2), 'bx', 'LineWidth', 2)
	title(titletext)
	hold off

	% Update frame counter and save frame
	frame = frame + 1;
	f2 = getframe(gcf);
	%mov = addframe(mov, f2);

	% Save stuff for next iteration
	lasttheta = theta;
	lastmouse = mouse;
end

std_approx_error = (x_std_approx - x_std)./x_std;

% Sort according to weights
A = p;
[OldRowNumber, NewRowNumber] = sort(A(:, 5));
p = A(NewRowNumber, :);

for i = 1:N
	if (p(i, 4)<0)
		p(i, 3) = p(i, 3) + 180;
		p(i, 4) = -p(i, 4);
	end
end

p(:, 3) = mod(p(:, 3), 360);

p = p(Nuc:N, :);	% Only use the best
 
p_mean = mean(p)
p_std = std(p)
ph = phase(movement)*(180/pi);
if (ph<0)
	ph = ph + 360;
end
p_error = [real(mouse) imag(mouse) ph abs(movement)/T 0 max(p(:, 6))] - p_mean
fprintf('seconds/frame %i\n', toc/frame);
%mov = close(mov);
