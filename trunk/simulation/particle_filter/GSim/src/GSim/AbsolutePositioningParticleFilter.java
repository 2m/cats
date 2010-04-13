package GSim;

public class AbsolutePositioningParticleFilter extends AbsolutePositioningFilter {
	// TODO: Translate matlab to java
	// TODO: Write javadoc
	
	/* Particles */
	private int x[];
	private int y[];
	private int angle[];
	private int[] weights;
	
	private final int N;
	private final int Nuc;
	private final int Nlc;
	
	public AbsolutePositioningParticleFilter(int N, float T, Buffer sensorData, Buffer movementData, Arena arena, RealTimeClock rttime, LandmarkList landmarks) {
		super(T, sensorData, movementData, arena, rttime, landmarks);
		this.N = N;
		Nuc = (int) (N*(3/4));		// Set up cut-offs for survival of the fittest
		Nlc = (int) (N*(1/4));
	}

	/**
	 * Create and init all particles
	 */
	public void initParticles() {
		// State variables x, y, angle
		x = new int[N];
		y = new int[N];
		angle = new int[N];
		weights = new int[N];
		// TODO: Add init data
	}
	
/**
 * Move particles in the direction each particle is facing.
 * @param distance The distance driven
 */
	public void integrateParticles(int distance) {
		for(int i=0;i<N;i++) {
			x[i] = x[i] + Fixed.mul(Fixed.cos(angle[i]), distance);
			y[i] = y[i] + Fixed.mul(Fixed.sin(angle[i]), distance);
		}
	}

	public void turnParticles(int theta){
		for(int i=0;i<N;i++) {
			angle[i] = angle[i] + theta;
			// TODO: Mask high values
		}
	}
	
	public void compareParticles() {
		// Compare particles to sensors inputs
	/*
	
	for g1 = 1:no_sensors
		for g2 = 1:N
			v = [p(g2, 1)-real(cat(g1)) p(g2, 2)-imag(cat(g1))];
			v = v./norm(v);
			u = [real(sensor_v(g1)) imag(sensor_v(g1))];
			w(g2) = penalty(v*u');	% No constant
		end
		p(:, 5) = p(:, 5) .* w;	% Update weights
	end
*/}
	public void normaliseParticles() {
		
		int sum_w=0;
		for(int i=0;i<N;i++) {
			sum_w += weights[i];
		}
		int sum_inv = Fixed.div(Fixed.ONE, sum_w)
		for(int i=0;i<N;i++) {
			// TODO: Check for div by zero
			weights[i] = Fixed.mul(weights[i], sum_inv);
		}
	}
	
	public float getX() {
		return (float) 0.0;
	}

	public float getY() {
		return (float) 0.0;
	}
	public void run(){
		/*
		 * Init
		 * Read time
		 * Read buffers for integration (angles, distance) 
		 */
	}
}
/*
%% Clean up
close all
clear all

%% Set basic options
N = 40
stddegrees = 2;
T = 0.5;
landmarks = [0.05+i*0.05 2.95+i*0.05 0.05+i*2.95 2.95+i*2.95 ...
		0.05+i*1.5 2.95+i*1.5 1.5+i*2.95 1.5+i*0.05];
no_landmarks = 4;
looking = i*1;
vision = 42;		% Field of vision in degrees
time = -6:T:60;		% Time span of simulation
cat_f = mouse_init(1.5, 1.5, pi/2, 1, max(time), T);

%% Set settings for methods and plotting
do_plotting = 0;		% Flag for doing continous plotting
make_mov = 0;			% Record movie
make_images = 1;		% Write plots to images
survival_of_fittest = 1;	% Resampling method (alt. complete resampling)

% Init movie
if (make_mov)
	mov = avifile('particle_filter_absolute_position.avi');
end


%% Init data
[x, y, ang, dxy, dang, cat_f] = mouse_pos(time(1), cat_f);
cat = x + i*y;
% Init particles (x, y, angle, w, age)
p = [ones(N, 1)*x + randn(N,1)*0.2 , ...
	ones(N, 1)*y + randn(N, 1)*0.2, ...
	ones(N, 1)*ang + randn(N, 1)*5*(pi/180), ...
	ones(N, 1)*(1/N), zeros(N, 1)];
Nuc = floor(N*3/4);	% Set up cut-offs
Nlc = floor(N*1/4);
w = zeros(N, 1);	% Make room for weights

% Init misc data
sensor_std = stddegrees*(pi/180);	% Standard deviation in radians
init_particles = 1;
frame = 1;
lastcat = cat-i*0.1;
lastlooking = looking;
real_states = zeros(size(time, 2), 3);
est_states = zeros(size(time, 2), 3);

if (do_plotting)
	figure
end
tic
for t = time
	% Update cat position and angle
	[x, y, ang, dxy, dang, cat_f] = mouse_pos(t, cat_f);
	cat = x + i*y;
	if (t>=0)
		init_particles = 0;
	end

	% Update the direction the cat is facing
	looking = cos(ang) + i*sin(ang);

	% Calc deltas
	movement = dxy;
	rotation = dang;

	% Move and rotate particles while adding some gaussian noise
	% Move
	p(:, 1) = p(:, 1) + (0.98 + rand(N, 1)*0.04)*movement.*cos(p(:, 3));
	p(:, 2) = p(:, 2) + (0.98 + rand(N, 1)*0.04)*movement.*sin(p(:, 3));
	% Rotate
	p(:, 3) = p(:, 3) + (0.98 + rand(N, 1)*0.04)*rotation;

	% Compare particles to sensors inputs
	landmark_seen = [];
	mu = [];
	re_sample = 0;
	vision_angle = abs(angle((landmarks - cat)*looking'))*(180/pi);
	mu = angle((landmarks - cat)*looking') + randn(size(landmarks))*sensor_std;	% Sensor readings
	% Input from sensor (angle in radians from forward direction)
	% Go through landmarks
	for g = 1:no_landmarks
		% Check if landmark is seen
		if (vision_angle(g) < (vision/2)) || (init_particles)
			re_sample = 1;			% Flag re-sampling
			landmark_seen(end + 1) = g;	% Add landmark to list
			z = ones(N, 1)*(-1);		% Make room
			% --- Filter implementation on robot ---

			u = [cos(0); sin(0)];	% Sensor vector
			for g1 = 1:N
				theta = -p(g1, 3) - mu(g);
				rot_p = [cos(theta) -sin(theta); ...
					sin(theta) cos(theta)];
				% Vector towards landmark in ??
				for g2 = 1:no_landmarks
					v=rot_p*[real(landmarks(g))-p(g1,1) ;...
						imag(landmarks(g)) - p(g1, 2)];
					v = v./norm(v);
					%a = u'*v;
					a = v(1);	% Assumes u=[1; 0]
					if (a>z(g1))
						z(g1) = a;
					end
				end
			end

			% Penalty function
			for g1 = 1:N
				w(g1) = penalty(z(g1));
			end

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
	axis([0 3 0 3])
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
plot(real_states(:, 1), real_states(:, 2), 'r-')
axis([0 3 0 3])
hold on
title('Real and estimated position')
ylabel('Y [m]')
xlabel('X [m]')
plot(est_states(:, 1), est_states(:, 2), 'bd')
for g = 1:no_landmarks
	plot(real(landmarks(g)),imag(landmarks(g)),'ro','LineWidth', 2)
end
hold off
if (make_images)
	print('particle_filter_absolute_position_xy.png', '-dpng')
end

figure
conv_gap = find(time==0);	% Gap for convergence (used in calculation of mean)
% Remove first element for nicer plots
est_error = real_states(2:end, :) - est_states(2:end, :);
pos_error = abs(est_error(:, 1) + i*est_error(:, 2));
pos_error_mean = mean(pos_error(conv_gap:end));
angle_error = mod(abs(est_error(:, 3)*(180/pi)), 360);
angle_error = abs((angle_error<=180).*angle_error+(angle_error>180).*(angle_error-360));
angle_error_mean = mean(angle_error(conv_gap:end));

subplot(2, 1, 1)
plot(time(2:end), pos_error, 'b-')
hold on
title(['Error in position estimation and average (mean=' num2str(pos_error_mean, '%1.3f') ')'])
ylabel('Estimation error [m]')
xlabel(['Time [s] (T=' num2str(T) ')'])
plot(time(conv_gap:end), pos_error_mean*ones(size(time(conv_gap:end))), 'r--')
hold off

subplot(2, 1, 2)
plot(time(2:end), angle_error, 'b-')
hold on
title(['Error in angle estimation and average (mean=' num2str(angle_error_mean, '%1.3f') ')'])
ylabel('Estimation error [degrees]')
xlabel(['Time [s] (T=' num2str(T) ')'])
plot(time(conv_gap:end), angle_error_mean*ones(size(time(conv_gap:end))), 'r--')
hold off

if (make_images)
	print('particle_filter_absolute_position_error.png', '-dpng')
end
*/