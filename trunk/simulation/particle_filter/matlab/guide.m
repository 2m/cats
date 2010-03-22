% Draft of a simple AI built on minimising a criterion function
%
% Author: Fredrik Wahlberg
% Version: 1.0

close all
clear all

d1 = 80;	% Opitmal distance from mouse
d2 = 30;	% Min distance to cats
d3 = 20;	% Min distance from line of sight
m = [150 150];	% Mouse position
c = [10 10 ; 10 80; 10 150];	% Cat positions
steepest_descent = 1;
make_mov = 0;

% Init movie
if (make_mov)
	mov = avifile('guide.avi');
end

% Make grid
x = 0:5:300;
y = 0:5:300;
[X, Y] = meshgrid(x, y);

% Set up variables
Z = zeros(size(X, 1), size(X, 2), size(c, 1));
Z2 = zeros(size(X, 1), size(X, 2), size(c, 1));
Z3 = zeros(size(X, 1), size(X, 2), size(c, 1));
T = zeros(size(X, 1), size(X, 2));

% Probability map of mouse
Z1 = abs(sqrt((m(1) - X).^2 + (m(2) - Y).^2) - d1);
Z1 = 1 - Z1/max(max(Z1));

frame = 1;
figure
tic
for t = 0:0.5:30

% Probability map of cat position
for g = 1:size(c, 1)
	T = sqrt((c(g, 1) - X).^2 + (c(g, 2) - Y).^2);
	T = T.*(T<d2) + d2*(1 + 0.5*T./max(max(T))).*(T>=d2);
	%T = T.*(T<d2) + (d2 + d2*0.1*T./max(max(T)))*(T>=d2);
	Z2(:, :, g) = T/max(max(T));
end

% Probability map of cat camera direction
for g = 1:size(c, 1)
	v = m' - c(g, :)';
	v = v/norm(v);		% Direction to mouse
	u = [v(2); -v(1)];	% Perpendicular to v
	T = abs((u(1)*(X - c(g, 1))) + (u(2)*(Y - c(g, 2))));
	T = T.*(T<d3) + d3*(T>=d3);
	Z3(:, :, g) = T/max(max(T));
end

% Multiply probability distrubutions
for j=1:size(c,1)
	Z(:, :, j) = Z1;
	for g = 1:size(c, 1)
		if not (g==j)
			Z(:, :, j) = Z(:, :, j) .* Z2(:, :, g) .* Z3(:, :, g);
		end
	end
end

% Plot
for j=1:size(c,1)
	% Make subplot
	%surfc(X, Y, Z)
	subplot(2, 2, j)
	contour(X, Y, Z(:, :, j), 0:0.1:1)
	hold on
	title(['Criterion function in cat #' num2str(j)])
	ylabel('Y [cm]')
	xlabel('X [cm]')
	plot(m(1), m(2), 'rx', 'LineWidth', 2)
	for g = 1:size(c, 1)
		if not (g==j)
			plot(c(g, 1), c(g, 2), 'bx', 'LineWidth', 2)
		else
			plot(c(g, 1), c(g, 2), 'gx', 'LineWidth', 2)
		end
	end
	hold off
end

% Plot and move cats
subplot(2, 2, 4)
plot(m(1), m(2), 'rx', 'LineWidth', 2)
axis([min(min(X)) max(max(X)) min(min(Y)) max(max(Y))])
hold on
title(['Decisions at time ' num2str(t, '%2.1f')])
ylabel('Y [cm]')
xlabel('X [cm]')
for j = 1:size(c, 1)	% Go through cats
	plot(c(j, 1), c(j, 2), 'bx', 'LineWidth', 2)
	if (steepest_descent)
		% Determine movement according to the steepest descent method
		[dx, dy] = gradient(Z(:, :, j));
		[t, ix] = min(abs(x - c(j, 1)));
		[t, iy] = min(abs(y - c(j, 2)));
		v = [dx(iy, ix) dy(iy, ix)]*300;
	else
		% Look for local minima
		[t, ix] = min(abs(x - c(j, 1)));
		[t, iy] = min(abs(y - c(j, 2)));
		sq = 1;
		i1 = max([1 ix-sq]);
		i2 = min([size(x, 2) ix+sq]);
		i3 = max([1 iy-sq]);
		i4 = min([size(y, 2) iy+sq]);
		localmax = Z(ix, iy, j);
		v = [0 0];
		for g1=i1:i2
			for g2=i3:i4
				w = Z(g1, g2, j);
				if (localmax<w)
					localmax = w;
					v = [x(g1)-x(ix) y(g2)-y(iy)];
				end
			end
		end
	end
	% Check for max speed
	if (norm(v)>4)
		v = 4*v./norm(v);
	end
	% Plot movement arrow
	d = [x(ix) y(iy) angle(v(1) + i*v(2))];
	arrow(d, norm(v)*5, 'b-', 1)
	% Move cat
	c(j, 1) = c(j, 1) + v(1);
	c(j, 2) = c(j, 2) + v(2);
end
hold off

	frame = frame + 1;
	f2 = getframe(gcf);
	if (make_mov)
		mov = addframe(mov, f2);
	end
end
%print('guide.png', '-dpng')
fprintf('seconds/frame %i2.2\n', toc/(frame-1));

if (make_mov)
	mov = close(mov);
end
