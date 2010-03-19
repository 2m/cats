function Z = sensor_pdf(X, Y, r, v, s)
	mu = 0;
	v = v/abs(v);
	Z = abs(angle(((X + i * Y) - r)*v'));
	Z = 1/sqrt(2*pi*s^2)*exp(-((Z - mu).^2)/(2*s^2));
end
