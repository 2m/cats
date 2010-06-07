clear all
close all

function t= test(d)
	t = d
end

for i=1:100
	x=rand*3;
	y=rand*3;

	% North part
	phi = atan2(3-y,-x) - atan2(3-y, 3-x);
	sidex = 3;
	sidey = 3;
	x1 = sidex/2;
	r0 = x1/sin(phi);
	y1 = sidey - x1/tan(phi);
	P0 = [x1 y1];

	% West part
	theta = atan2(-y,-x) + 2*pi - atan2(3-y,-x);
	sidex = 3;
	sidey = 3;
	y2 = sidey/2;
	r1 = y2/sin(theta);
	x2 = y2/tan(theta);
	P1 = [x2 y2];

	d = sqrt((x1-x2)^2 + (y1-y2)^2);

	if (d > (r0 + r1))
		disp('circles are separate');
		end
	if (d < abs(r0 - r1))
		disp('one circle is inside the other');
	end
	if ((d==0) && (r0=r1))
		disp('circles coincide');
	end

	a = (r0^2 - r1^2 + d^2)/(2*d);
	h = sqrt(r0^2 - a^2);

	v=(P1-P0)/d;
	u=[v(2) -v(1)];
	P2 = P0 + a*v + h*u;
	P3 = P0 + a*v - h*u;

	e1 = norm(P2-[x y]);
	e2 = norm(P3-[x y]);
	if (e1<e2)
		error(i) = e1;
	else
		error(i) = e2;
	end
end

break
scatter(x, y, 'g.')
hold on
axis([-5 8 -5 8])
scatter([P0(1) P1(1)], [P0(2) P1(2)], 'rx')
plot(P0(1)+cos([0:0.05:2*pi])*r0, P0(2)+sin([0:0.05:2*pi])*r0)
plot(P1(1)+cos([0:0.05:2*pi])*r1, P1(2)+sin([0:0.05:2*pi])*r1)
scatter([P2(1) P3(1)], [P2(2) P3(2)], 'r+')
hold off
