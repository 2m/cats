function [V, D] = myeig(C)
	a = C(1, 1);
	b = C(1, 2);
	c = C(2, 1);
	d = C(2, 2);
	p = -(a+d);
	q = a*d - b*c;
	lambda1 = -p/2 + sqrt((p/2)^2 - q);
	lambda2 = -p/2 - sqrt((p/2)^2 - q);
	D = [lambda1 0; 0 lambda2];
	v1 = [1; -(a-lambda1)/b];
	v1 = v1./norm(v1);
	% Eigen vectors are perpendicular => rot_p == [0 -1; 1 0]	
	v2 = [-v1(2); v1(1)];
	V = [v1 v2];
end
