function f = mouse_init(cx, cy, direction, radius, laptime, T)
	f(1) = cx + radius;
	f(2) = cy;
	f(3) = direction;
	f(4) = radius;
	f(5) = laptime;
	f(6) = T;
	f(7) = radius*2*pi/laptime;	% speed
	f(8) = 2*pi/laptime;		% angular speed
end
