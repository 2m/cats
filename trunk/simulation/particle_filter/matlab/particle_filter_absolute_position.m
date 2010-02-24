%% Clean up
close all
clear all

%% Set basic options
N = 40
stddegrees = 1;
T = 0.5;
landmarks = [0.05 + i * 0.05 1.95 + i * 0.05 0.05 + i * 1.95 1.95 + i * 1.95 ...
0.05 + i 1.95 + i 1 + i * 1.95 1 + i * 0.05];
cat = 1.6 + i * 1;
no_landmarks = 8;
looking = i*1;
vision = 180;	% Field of vision in degrees

%% Set settings for methods and plotting
do_plotting = 1;		% Flag for doing continous plotting
make_mov = 0;			% Record movie
make_images = 0;		% Write plots to images
survival_of_fittest = 1;	% Resampling method (alt. complete resampling)

% Init movie
if (make_mov)
	mov = avifile('particle_filter_absolute_position.avi');
end


%% Init data
% Init particles (x, y, angle, w, age)
p = [ones(N, 1)*real(cat) + randn(N,1)*0.2 , ...
	ones(N, 1)*imag(cat) + randn(N, 1)*0.2, ...
	ones(N, 1)*angle(looking) + randn(N, 1)*10*(pi/180), ...
	ones(N, 1)*(1/N), zeros(N, 1)];
Nuc = floor(N*3/4);	% Set up cut-offs
Nlc = floor(N*1/4);
% Init misc data
%[X, Y] = meshgrid(0:.02:2, 0:.02:2);
sensor_std = stddegrees*(pi/180);	% Standard deviation in radians
frame = 1;
lastcat = cat-i*0.1;
lastlooking = looking;
time = -6:T:60;	% Time span of simulation
real_states = zeros(size(time, 2), 3);
est_states = zeros(size(time, 2), 3);
speed = [];

if (do_plotting)
	figure
end
tic
for t = time
	disp(['Frame: ', num2str(frame)]);

	% Update cat position and angle
	if (t>=0)
		theta = t*(2*pi/max(time));
		cat = 1 + 0.6*cos(theta) + i*(1 + 0.6*sin(theta));
	else
		cat = 1.6 + i*(1 + t*0.005);
		theta = 0;
	end

	% Update the direction the cat is facing
	looking = cat - lastcat;
	looking = looking/abs(looking);

	% Calc deltas
	movement = abs(cat-lastcat);
	rotation = angle(looking) - angle(lastlooking);
	speed(end + 1) = movement/T;	% Save speed

	% Move and rotate particles while adding some gaussian noise
	p(:, 1) = p(:, 1) + (1 + randn(N, 1)*0.01)*movement.*cos(p(:, 3));
	p(:, 2) = p(:, 2) + (1 + randn(N, 1)*0.01)*movement.*sin(p(:, 3));
	p(:, 3) = p(:, 3) + (1 + randn(N, 1)*0.01)*rotation;

	% Compare particles to sensors inputs
	landmark_seen = [];
	re_sample = 0;
	vision_angle = abs(angle((landmarks - cat)*looking'))*(180/pi);
	% Input from sensor (angle in radians from forward direction)
	mu = (angle(landmarks - cat)-angle(looking))+randn(1, 8)*sensor_std;
	% Go through landmarks
	for g = 1:no_landmarks
		landmark = landmarks(g) - cat;
		% Check if landmark is seen
		if (vision_angle(g) < (vision/2))
			re_sample = 1;
			landmark_seen(end + 1) = g;

			% --- Filter implementation on robot ---

			% Guess sensor
			% TODO: rewrite with vector product
			z = zeros(N, 1);
			for g1 = 1:N
				%ph = angle((landmarks-(p(g1,1)+i*p(g1,2)))*...
				%	exp(i*p(g1, 3))');
				%[a, b] = min(abs(ph - mu(g)));
				%z(g1, 1) = ph(b);
				z(g1) = pi;
				l = 1;
				for g2 = 1:no_landmarks
					% Find which sensor is seen
					c = landmarks(g2) - (p(g1,1)+i*p(g1,2));
					v = [real(c) imag(c)];
					v = v./norm(v);
					u = [cos(mu(g)) sin(mu(g))];
					a = acos(u*v');
					if (a<b)
						b = a;
						l = g2;
					end
				end
			end

			% Penalty function
			% TODO: linearise this:
			w = exp(-((z - mu(g)).^2)/(2*sensor_std^2));

			% --- end ---

			% Update weights
			p(:, 4) = p(:, 4) .* w;
		end
	end

	% Check for errors (NaN probably from sum(w)=0 => div by zero)
	if (sum(isnan(p(:, 4)))>0)
		nan = sum(isnan(p(:, 4)))
		break
	end

	%% Normalise weights
	sum_w = sum(p(:, 4));
	if (sum_w > 0)	% Check for div by zero
		p(:, 4) = p(:, 4)/sum_w;
	end

	% Inc age
	p(:, 5) = p(:, 5) + 1;

	%% Sort according to weights
	% Decending from N to 1
	A = p;
	[OldRowNumber, NewRowNumber] = sort(A(:, 4));
	p = A(NewRowNumber, :);

	%% Calculate mean
	if (sum_w == 0)
		% Ordinary mean
		m = [mean(p(:, 1)) mean(p(:, 2)) mean(p(:, 3))];
	else
		% Weighted mean
		m = [sum(p(:, 1).*p(:, 4)) sum(p(:, 2).*p(:, 4)) ...
			sum(p(:, 3).*p(:, 4))];
	end

	%p_std = std(p)

	%% Re-sample
	if (re_sample)
		if (survival_of_fittest)
			A = [p(Nuc:N, 1:3), zeros(N-Nuc+1, 2)];
			B = [randn(N-Nuc+1, 2)*0.015, ...
				randn(N-Nuc+1, 1)*1*(pi/180), ...
				zeros(N-Nuc+1, 2)];
			p(1:Nlc+1,:) = A + B;
		else
			p = [m(1) + randn(N, 1)*p_std(1), ...
				m(2) + randn(N, 1)*p_std(2), ...
				m_v(1) + randn(N, 1)*p_std(3), ...
				m_v(2) + randn(N, 1)*p_std(4), ...
				ones(N, 1)*(1/N), zeros(N, 1)];
		end
	end

	%% Reset weights (all particles equaly posible)
	% This gives a significant performance increase! (and is also cheap)
	p(:, 4) = ones(N, 1)*(1/N);

	%% Plot playing field
	if (do_plotting)
	scatter(p(:,1), p(:,2), 'g.')
	axis([0 2 0 2])
	hold on
	arrow(p, 0.1, 'g-', 1)
	for g = 1:no_landmarks
		plot(real(landmarks(g)),imag(landmarks(g)),'ro','LineWidth', 2)
	end
	plot(real(cat), imag(cat), 'r+', 'LineWidth', 2)
	arrow([real(cat) imag(cat) angle(looking)], 0.2, 'r-', 2)
	%arrow([real(cat) imag(cat) angle(looking)+(vision/2)*(pi/180)], ...
	%	0.2, 'r--', 1)
	%arrow([real(cat) imag(cat) angle(looking)-(vision/2)*(pi/180)], ...
	%	0.2, 'r--', 1)
	for g = 1:no_landmarks
		arrow([real(cat) imag(cat) mu(g)+angle(looking)], 0.15, 'r--', 1)
	end
	% Plot estimate
	plot(m(1), m(2), 'bx', 'LineWidth', 2)
	for g = 1:size(landmark_seen, 2)
		arrow([m(1) m(2) mu(landmark_seen(g))+m(3)], 0.15, 'b--', 1)
	end
	arrow(m, 0.2, 'b-', 2)
	% Set title text
	titletext = sprintf('Prototype of particle filter for absolute positioning at time %3.1f (\\sigma_{sensors}=%i degrees)', t, stddegrees);
	title(titletext)
	hold off
	end

	%% Save stuff for next iteration and plotting
	lastcat = cat;
	lastlooking = looking;
	real_states(frame, :) = [real(cat) imag(cat) angle(looking)];
	est_states(frame, :) = m;

	%% Update frame counter and save frame
	frame = frame + 1;
	if (do_plotting)
		f2 = getframe(gcf);
		if (make_mov)
			mov = addframe(mov, f2);
		end
	end
end
fprintf('seconds/frame %i\n', toc/(frame-1));

if (make_mov) && (do_plotting)
	mov = close(mov);
end

% Plot position
figure
plot(real_states(:, 1), real_states(:, 2), 'r+')
axis([0 2 0 2])
hold on
plot(est_states(:, 1), est_states(:, 2), 'bx')
for g = 1:no_landmarks
	plot(real(landmarks(g)),imag(landmarks(g)),'ro','LineWidth', 2)
end
hold off
if (make_images)
	print('particle_filter_absolute_position_xy.png', '-dpng')
end

break

% Plot angle
figure
plot(time(2:end), real_states(2:end, 3), 'r+')
hold on
plot(time(2:end), est_states(2:end, 3), 'bx')
hold off
if (make_images)
	print('particle_filter_absolute_position_angle.png', '-dpng')
end
