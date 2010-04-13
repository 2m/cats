package GSim;

public class TrackingParticleFilter extends TrackingFilter {
	// TODO: Translate matlab to java
	// TODO: Write javadoc
	private int particles[][];
	private int N;
	private int[] weights;
	
	public TrackingParticleFilter(int N, double T, Buffer sensorData, Buffer movementData) {
		super(T, sensorData, movementData);
		this.N = N;
	}

	public void initParticles() {
		// State variables x, y, x', y'
		particles = new int[N][4];
		weights = new int[N];
		/*
		 * p = [rand(N, 1)*arenasize(1), rand(N, 1)*arenasize(2), rand(N, 2)*0.04-0.02, ...
	ones(N, 1)*(1/N), zeros(N, 1)];	% Init particles (x, y, vx, vy, w, age)
w = zeros(N, 1);
Nuc = floor(N*3/4);		% Set up cut-offs for survival of the fittest
Nlc = floor(N*1/4);
*/
	}
	

	public void integrateParticles(){
	/*p(:, 1) = p(:, 1) + T*p(:, 3);
	p(:, 2) = p(:, 2) + T*p(:, 4);*/
	}

	public void compareParticles() {
	/*
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
*/}
	public void normaliseParticles() {
	/*sum_w = sum(p(:, 5));
	if (sum_w > 0)	% Check for div by zero
		p(:, 5) = p(:, 5)/sum_w;
	end*/
	}
	/*
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
	else
		% Weighted mean
		m = [sum(p(:, 1).*p(:, 5)) sum(p(:, 2).*p(:, 5))];
		m_v = [sum(p(:, 3).*p(:, 5)) sum(p(:, 4).*p(:, 5))];
	end

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

	% Update frame counter and save frame
	frame = frame + 1;
end
*/
}
