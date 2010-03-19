%% Clean up
close all
clear all

%% Set basic options
N = 80
stddegrees = 3;
T = 0.5;
cat(1) = 0.1 + i * 0.1;
cat(2) = 1.9 + i * 0.1;
cat(3) = 1 + i * 1.9;
no_sensors = 3;

%% Set settings for methods and plotting
make_mov = 0;			% Record movie
make_images = 0;		% Write plots to images
survival_of_fittest = 1;	% Resampling method (alt. complete resampling)

% Init movie
if (make_mov)
	mov = avifile('particle_filter.avi');
end


%% Init data
% Init particles (x, y, vx, vy, w, age)
p = [rand(N, 2)*2 , rand(N, 2)*0.04-0.02, ones(N, 1)*(1/N), zeros(N, 1)];
Nuc = floor(N*3/4);	% Set up cut-offs
Nlc = floor(N*1/4);
% Init misc data
%[X, Y] = meshgrid(0:.02:2, 0:.02:2);
sensor_std = stddegrees*(pi/180);	% Standard deviation in radians
titletext = sprintf('Prototype of particle filter (\\sigma_{sensors}=%i degrees)', stddegrees);
frame = 1;
lastmouse = 1.6 + i;
time = 0:T:60;	% Time span of simulation
real_states = zeros(size(time, 2), 4);
est_states = zeros(size(time, 2), 4);

figure
tic
for t = time
	% Update mouse
	theta = t*(2*pi/max(time));
	mouse = 1 + 0.6*cos(theta) + i*(1 + 0.6*sin(theta));
	movement = mouse - lastmouse;

	% Update sensor data and add noise
	sensor_v = (mouse - cat) .* exp(i*randn(1, 3)*sensor_std);
	sensor_d = abs(sensor_v);
	sensor_v = sensor_v ./ sensor_d;

	% Integrate particles
	p(:, 1) = p(:, 1) + T*p(:, 3);
	p(:, 2) = p(:, 2) + T*p(:, 4);

	% Compare particles to sensors inputs
	for g = 1:no_sensors
		z = angle((p(:, 1) + i*p(:, 2)) - cat(g));
		mu = angle(mouse - cat(g));
		% Penalty function
		%w = (1/sqrt(2*pi*s^2))*exp(-((z - mu).^2)/(2*sensor_std^2));
		w = exp(-((z - mu).^2)/(2*sensor_std^2));	% No constant
		p(:, 5) = p(:, 5) .* w;	% Update weights
	end

	% Normalise
	p(:, 5) = p(:, 5)/sum(p(:, 5));

	% Inc age
	p(:, 6) = p(:, 6) + 1;

	% Sort according to weights
	A = p;
	[OldRowNumber, NewRowNumber] = sort(A(:, 5));
	p = A(NewRowNumber, :);

	% Calculate mean
	%m = [mean(p(Nuc:N, 1)) mean(p(Nuc:N, 2))];
	m = [sum(p(:, 1).*p(:, 5)) sum(p(:, 2).*p(:, 5))];
	m_v = [sum(p(:, 3).*p(:, 5)) sum(p(:, 4).*p(:, 5))];
	%p_std = std(p)

	% Resample
	if (survival_of_fittest)
		A = [p(Nuc:N, 1:5), zeros(N-Nuc+1, 1)];
		B = [rand(N-Nuc+1, 2)*0.02-0.01, rand(N-Nuc+1, 2)*0.1-0.05, ones(N-Nuc+1, 1), zeros(N-Nuc+1, 1)];
		p(1:Nlc+1,:) = A + B;
	else
		p = [m(1) + randn(N, 1)*p_std(1), ...
			m(2) + randn(N, 1)*p_std(2), ...
			m_v(1) + randn(N, 1)*p_std(3), ...
			m_v(2) + randn(N, 1)*p_std(4), ...
			ones(N, 1)*(1/N), zeros(N, 1)];
	end

	% Reset weights (all particles equaly posible)
	% This gives a significant performance increase! (and is also cheap)
	p(:, 5) = ones(N, 1)*(1/N);

	% Plot
	scatter(p(:,1), p(:,2), 'g.')
	axis([0 2 0 2])
	hold on
	for g = 1:no_sensors
		l = [cat(g) cat(g)+sensor_d(g)*1.2*sensor_v(g)];
		plot(real(l), imag(l), 'b--', 'LineWidth', 1)
	end
	plot(real(mouse), imag(mouse), 'r+', 'LineWidth', 2)
	plot(m(1), m(2), 'bx', 'LineWidth', 2)
	title(titletext)
	hold off

	% Save stuff for next iteration
	%lasttheta = theta;
	lastmouse = mouse;
	real_states(frame, :) = [real(mouse) imag(mouse) ...
				real(movement)/T imag(movement)/T];
	est_states(frame, :) = [m(1) m(2) m_v(1) m_v(2)];

	% Update frame counter and save frame
	frame = frame + 1;
	f2 = getframe(gcf);
	if (make_mov)
		mov = addframe(mov, f2);
	end
end
fprintf('seconds/frame %i\n', toc/(frame-1));

% Plot position
figure
plot(real_states(:, 1), real_states(:, 2), 'r+')
axis([0 2 0 2])
hold on
plot(est_states(:, 1), est_states(:, 2), 'bx')
hold off
if (make_images)
	print('particle_filter_position.png', '-dpng')
end

% Plot speed
figure
plot(time(2:end), abs(real_states(2:end, 3) + i*real_states(2:end, 4)), 'r+')
hold on
plot(time(2:end), abs(est_states(2:end, 3) + i*est_states(2:end, 4)), 'bx')
hold off
if (make_images)
	print('particle_filter_speed.png', '-dpng')
end

if (make_mov)
	mov = close(mov);
end
