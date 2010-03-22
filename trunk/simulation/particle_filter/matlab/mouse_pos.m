function [x, y, ang, dxy, dang, f] = mouse_pos(t, f)
	if (t<0)
		dxy = 0.005*f(6);
		dang = 0;
	else
		dxy = f(7)*f(6);
		dang = f(8)*f(6);
	end
	f(1) = f(1) + cos(f(3))*dxy;
	f(2) = f(2) + sin(f(3))*dxy;
	f(3) = f(3) + dang;
	x = f(1);
	y = f(2);
	ang = f(3);
end
