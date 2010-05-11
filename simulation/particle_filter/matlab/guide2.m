% Draft of a simple AI built on minimising a criterion function
%
% Author: Fredrik Wahlberg
% Version: 1.0

close all
clear all

d1 = .8;	% Opitmal distance from mouse
d2 = .3;	% Min distance to cats
d3 = .2;	% Min distance from line of sight
d4 = .1;	% Distance from edges
d5 = .1;	% Interval of optimal distance from the mouse
m = [1.50 1.50];	% Mouse position
c = [.10 .10 ; .10 .80; .10 1.50];	% Cat positions
steepest_descent = 1;
make_mov = 0;

% Init movie
if (make_mov)
	mov = avifile('guide.avi');
end

% Make grid
x = 0:.05:3;
y = 0:.05:3;
[X, Y] = meshgrid(x, y);

% Set up variables
%Z = zeros(size(X, 1), size(X, 2), size(c, 1));
%Z2 = zeros(size(X, 1), size(X, 2), size(c, 1));
%Z3 = zeros(size(X, 1), size(X, 2), size(c, 1));
%T = zeros(size(X));

% Probability map for distance from the mouse
Z1 = abs(sqrt((m(1) - X).^2 + (m(2) - Y).^2) - d1);
Z1 = (Z1-d5).*(Z1>d5);
Z1 = 1 - Z1/sqrt(3*3*2);

% Probability map for distance from the edges
Z4 = ones(size(X));
Z4 = Z4.*(X/d4).*(X<=d4) + Z4.*(X>d4);
Z4 = Z4.*(Y/d4).*(Y<=d4) + Z4.*(Y>d4);
Z4 = Z4.*((3-X)/d4).*(X>=(3-d4)) + Z4.*(X<(3-d4));
Z4 = Z4.*((3-Y)/d4).*(Y>=(3-d4)) + Z4.*(Y<(3-d4));

frame = 1;
figure
tic
for t = 0:0.5:30

% Probability map of keeping the distance from cats and mouse positions
T = sqrt((m(1) - X).^2 + (m(2) - Y).^2);
Z5 = (T/d2).*(T<=d2) + ones(size(T)).*(T>d2);
for g = 1:size(c, 1)
	G = sqrt((c(g, 1) - X).^2 + (c(g, 2) - Y).^2);
	G = (G/d2).*(G<=d2) + ones(size(G)).*(G>d2);
	Z2(:, :, g) = G;
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
	Z(:, :, j) = Z1.*Z4.*Z5;
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
	% Determine movement according to the steepest descent method
	[t, ix] = min(abs(x - c(j, 1)));
	[t, iy] = min(abs(y - c(j, 2)));
	if (ix>1)
		h = x(3) - x(1);
		dx = (Z(iy, ix+1, j) - Z(iy, ix-1, j))/h;
	else 
		dx = 1;
	end
	if (iy>1)
		h = y(3) - y(1);
		dy = (Z(iy+1, ix, j) - Z(iy-1, ix, j))/h;
	else
		dy = 1;
	end
	v = [dx dy];
	if sum(v==0)<2
		% Check for max speed
		v = 0.04*v./norm(v);
		% Plot movement arrow
		d = [x(ix) y(iy) angle(v(1) + i*v(2))];
		arrow(d, norm(v)*5, 'b-', 1)
		% Move cat
		c(j, 1) = c(j, 1) + v(1);
		c(j, 2) = c(j, 2) + v(2);
	end
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
