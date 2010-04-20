function plotCovar2D(xmu, ymu, C, arena)
	A = inv(C);			% the inverse covariance matrix

	% plot between +-2SDs along each dimension
	x = arena(1):(arena(2)-arena(1))/100:arena(2);
	y = arena(3):(arena(4)-arena(3))/100:arena(4);
	[X, Y] = meshgrid(x,y); % matrices used for plotting

	% Compute value of Gaussian pdf at each point in the grid
	z = 1/(2*pi*sqrt(det(C))) * exp(-0.5 * (A(1,1)*(X-xmu).^2 + 2*A(1,2)*(X-xmu).*(Y-ymu) + A(2,2)*(Y-ymu).^2));

	contour(x,y,z);
end
