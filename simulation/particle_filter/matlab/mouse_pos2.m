function [x, y, ang, dxy, dang, f] = mouse_pos(t, f)
	dang = 0;
	dxy = 0.06;
	f(1) = f(1) + cos(f(3))*dxy;
	f(2) = f(2) + sin(f(3))*dxy;
	if (f(1)<.5)
		f(3) = rand*pi-pi/2;
	end
	if (f(2)<.5)
		f(3) = rand*pi;
	end
	if (f(1)>2.5)
		f(3) = rand*pi+pi/2;
	end
	if (f(2)>2.5)
		f(3) = -rand*pi;
	end
	x = f(1);
	y = f(2);
	ang = f(3);
end
