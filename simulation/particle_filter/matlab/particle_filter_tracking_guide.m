%% Clean up
close all
clear all

%% Set basic options
N = 120
stddegrees = 2;
T = 0.5;
cat(1) = 0.1 + i * 0.1;
cat(2) = 2.9 + i * 0.1;
cat(3) = 1.5 + i * 2.9;
catv = [0 0 0];
no_sensors = 3;
arena = [0 3 0 3];	% Size of arena (x0, x, y0, y) [m]
time = 0:T:60;	% Time span of simulation
mouse_f = mouse_init(1.5, 1.5, pi/2, 1, max(time), T);

%% Set settings for methods and plotting
do_plotting = 1;		% Flag for doing continous plotting
make_mov = 0;			% Record movie
make_images = 0;		% Write plots to images
survival_of_fittest = 1;	% Resampling method (alt. complete resampling)
dt = 1;				% Prediction interval

% Init movie
if (make_mov)
	mov = avifile('particle_filter_tracking.avi');
end


%% Init data
p = [rand(N, 1)*arena(2), rand(N, 1)*arena(4), rand(N, 2)*0.04-0.02, ...
	ones(N, 1)*(1/N), zeros(N, 1)];	% Init particles (x, y, vx, vy, w, age)
w = zeros(N, 1);
Nuc = floor(N*3/4);		% Set up cut-offs for survival of the fittest
Nlc = floor(N*1/4);
sensor_std = stddegrees*(pi/180);	% Standard deviation in radians
frame = 1;
lastmouse = 1.6 + i;
real_states = zeros(size(time, 2), 4);
est_states = zeros(size(time, 2), 4);
C1 = weightedCovariance(p(:, 1), p(:, 2), p(:, 5));
C2 = weightedCovariance(p(:, 3), p(:, 4), p(:, 5));
C3 = C1 + [dt^2 dt; dt dt^2].*C2;

if (do_plotting)
	figure
end
tic
x = 1;
y = 1;
ang = 1;
for t = time
	% Update mouse
	%[x, y, ang, dxy, dang, mouse_f] = mouse_pos(time(1), mouse_f);
	[x, y, ang, dxy, dang, mouse_f] = mouse_pos2(time(1), mouse_f);

	% Update cat position
	cat = cat + catv;

	% Update sensor data and add noise
	sensor_v = ((x + i*y) - cat) .* exp(i*randn(1, 3)*(sensor_std));
	sensor_d = abs(sensor_v);
	sensor_v = sensor_v ./ sensor_d;

	% Integrate particles
	p(:, 1) = p(:, 1) + T*p(:, 3);
	p(:, 2) = p(:, 2) + T*p(:, 4);

	% Compare particles to sensors inputs
	for g1 = 1:no_sensors
		for g2 = 1:N
			v = [p(g2, 1)-real(cat(g1)) p(g2, 2)-imag(cat(g1))];
			v = v./norm(v);
			u = [real(sensor_v(g1)) imag(sensor_v(g1))];
			w(g2) = penalty(v*u');	% No constant
		end
		p(:, 5) = p(:, 5) .* w;	% Update weights
	end

	% Normalise
	sum_w = sum(p(:, 5));
	if (sum_w > 0)	% Check for div by zero
		p(:, 5) = p(:, 5)/sum_w;
	end

	% Inc age
	p(:, 6) = p(:, 6) + 1;

	% Sort according to weights
	A = p;
	[OldRowNumber, NewRowNumber] = sort(A(:, 5));
	p = A(NewRowNumber, :);

	% Calculate mean
	if (sum_w == 0)
		% Ordinary mean
		m = [mean(p(Nuc:N, 1)) mean(p(Nuc:N, 2))];
		m_v = [mean(p(Nuc:N, 3)) mean(p(Nuc:N, 4))];
		m_p = m + dt*m_v;
	else
		% Weighted mean
		m = [sum(p(:, 1).*p(:, 5)) sum(p(:, 2).*p(:, 5))];
		m = m.*(m<=3) + 3*(m>3);
		m = m.*(m>=0) + 3*(m<0);
		m_v = [sum(p(:, 3).*p(:, 5)) sum(p(:, 4).*p(:, 5))];
		m_v = m_v.*(m<=.2) + .2*(m_v>.2);
		m_v = m_v.*(m>=-.2) - .2*(m_v<-.2);
		C1 = weightedCovariance(p(:, 1), p(:, 2), p(:, 5));
		C2 = weightedCovariance(p(:, 3), p(:, 4), p(:, 5));
		
		%SYS=SS(A,[B G],C,[D H])
		%SYS=SS(A, [B G], C, [D H])
		%[KEST,L,P,M,Z] = KALMAN(SYS,QN,RN,NN,'delayed');
		C3 = C1 + [dt^2 dt; dt dt^2].*C2;
		m_p = m + dt*m_v;
	end

	% Resample
	if (survival_of_fittest)
		%A = [p(Nuc:N, 1:5), zeros(N-Nuc+1, 1)];
		%B = [rand(N-Nuc+1, 2)*0.02-0.01, rand(N-Nuc+1, 2)*0.1-0.05, ones(N-Nuc+1, 1), zeros(N-Nuc+1, 1)];
		%p(1:Nlc+1,:) = A + B;
		C1 = C1 + (C1<[.001 0 ;0 .001]).*[.001 0;0 .001];
		C2 = C2 + (C2<[.001 0 ;0 .001]).*[.001 0;0 .001];
		[EigV1, EigD1] = myeig(C1);
		[EigV2, EigD2] = myeig(C2);
		EigV1(:, 1) = EigV1(:, 1) * sqrt(EigD1(1, 1));
		EigV1(:, 2) = EigV1(:, 2) * sqrt(EigD1(2, 2));
		EigV2(:, 1) = EigV2(:, 1) * sqrt(EigD2(1, 1));
		EigV2(:, 2) = EigV2(:, 2) * sqrt(EigD2(2, 2));
		for g=1:Nlc
			e1 = (EigV1*randn(2, 1))' + m;
			e2 = (EigV2*randn(2, 1))' + m_v;
			p(g, :) = [e1(1), e1(2), e2(1), e2(2), 1/N, 1];
		end
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
	if (do_plotting)
		subplot(2, 2, 1)
		scatter(p(:,1), p(:,2), 'g.')
		axis(arena)
		hold on
		scatter(real(cat), imag(cat), 'bo', 'LineWidth', 2)
		for g = 1:no_sensors
			l = [cat(g) cat(g)+sensor_d(g)*1.2*sensor_v(g)];
			plot(real(l), imag(l), 'b--', 'LineWidth', 1)
		end
		plot(x, y, 'r+', 'LineWidth', 2)
		plot(m(1), m(2), 'bx', 'LineWidth', 2)
		title(['Particle filter data (\sigma_{sensors}=' num2str(stddegrees) ' degrees)']);
		xlabel('X [m]')
		ylabel('Y [m]')
		hold off
		subplot(2, 2, 2)
		plotCovar2D(m(1), m(2), C1, arena)
		axis(arena)
		hold on
		title(['Estimated pdf at time ' num2str(t, '%1.1f')])
		xlabel('X [m]')
		ylabel('Y [m]')
		[V, D] = eig(C1);
		if (D(1,1)>D(2,2))
			l1 = D(1,1);
			l2 = D(2,2);
			v1 = V(:, 1);
			v2 = V(:, 2);
		else
			l1 = D(2,2);
			l2 = D(1,1);
			v1 = V(:, 2);
			v2 = V(:, 1);
		end
		arrow([m(1) m(2) atan2(v1(1),v1(2))], l1*30, 'b', 1);
		arrow([m(1) m(2) atan2(v2(1),v2(2))], l2*30, 'r', 1);
		hold off
		subplot(2, 2, 3)
		plotCovar2D(m_v(1), m_v(2), C2, [-1 1 -1 1])
		axis([-1 1 -1 1])
		hold on
		title(['Estimated pdf of speed at time ' num2str(t, '%1.1f')])
		xlabel('X [m]')
		ylabel('Y [m]')
		[V, D] = eig(C2);
		if (D(1,1)>D(2,2))
			l1 = D(1,1);
			l2 = D(2,2);
			v1 = V(:, 1);
			v2 = V(:, 2);
		else
			l1 = D(2,2);
			l2 = D(1,1);
			v1 = V(:, 2);
			v2 = V(:, 1);
		end
		arrow([m_v(1) m_v(2) atan2(v1(1),v1(2))], l1*30, 'b', 1);
		arrow([m_v(1) m_v(2) atan2(v2(1),v2(2))], l2*30, 'r', 1);
		hold off
		subplot(2, 2, 4)
		plotCovar2D(m_p(1), m_p(2), C3, arena)
		hold on
		title(['Predicted PDF in ' num2str(dt) ' seconds']);
		xlabel('X [m]')
		ylabel('Y [m]')
		[V, D] = eig(C3);
		if (D(1,1)>D(2,2))
			l1 = D(1,1);
			l2 = D(2,2);
			v1 = V(:, 1);
			v2 = V(:, 2);
		else
			l1 = D(2,2);
			l2 = D(1,1);
			v1 = V(:, 2);
			v2 = V(:, 1);
		end
		arrow([m_p(1) m_p(2) atan2(v1(1),v1(2))], l1*10, 'b', 1);
		arrow([m_p(1) m_p(2) atan2(v2(1),v2(2))], l2*10, 'r', 1);
		hold off
	end

	% Save data from this iteration
	real_states(frame, :) = [x y (cos(ang)*dxy)/T (sin(ang)*dxy)/T];
	est_states(frame, :) = [m(1) m(2) m_v(1) m_v(2)];

	% Update frame counter and save frame
	frame = frame + 1;
	if (do_plotting)
		f2 = getframe(gcf);
		if (make_mov)
			mov = addframe(mov, f2);
		end
	end
end

frametime = toc/(frame - 1);
fprintf('seconds/frame %i\n', frametime);
fprintf('seconds/frame/T %i\n', frametime/T);

% Plot position
figure
plot(real_states(:, 1), real_states(:, 2), 'r-')
axis([0 3 0 3])
hold on
plot(est_states(:, 1), est_states(:, 2), 'bd')
scatter(real(cat), imag(cat), 'ro', 'LineWidth', 2)
hold off
if (make_images)
	print('particle_filter_tracking_position.png', '-dpng')
end

% Plot errors
figure
conv_gap = 8;	% Gap for convergence (used in calculation of mean)
% Remove first element for nicer plots
est_error = real_states(2:end, :) - est_states(2:end, :);
pos_error = abs(est_error(:, 1) + i*est_error(:, 2));
pos_error_mean = mean(pos_error(conv_gap:end));
vel_error = abs(est_error(:, 3) + i*est_error(:, 4));
vel_error_mean = mean(vel_error(conv_gap:end));

subplot(2, 1, 1)
plot(time(2:end), pos_error, 'b-')
hold on
title(['Error in position estimation and average (mean=' num2str(pos_error_mean, '%1.3f') ')'])
ylabel('Estimation error [m]')
xlabel(['Time [s] (T=' num2str(T) ')'])
plot(time(conv_gap:end), pos_error_mean*ones(size(time(conv_gap:end))), 'r--')
hold off

subplot(2, 1, 2)
plot(time(2:end), vel_error, 'b-')
hold on
title(['Error in velocity estimation and average (mean=' num2str(vel_error_mean, '%1.3f') ')'])
ylabel('Estimation error [m]')
xlabel(['Time [s] (T=' num2str(T) ')'])
plot(time(conv_gap:end), vel_error_mean*ones(size(time(conv_gap:end))), 'r--')
hold off

if (make_images)
	print('particle_filter_tracking_error.png', '-dpng')
end

% Write mov to disk
if (make_mov)
	mov = close(mov);
end
