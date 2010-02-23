function arrow(data, length, opt, width)
	for g=1:size(data, 1)
		a = data(g, 1) + i*data(g, 2);
		b = a + exp(i*data(g, 3)) * length;
		c = [a b];
		plot(real(c), imag(c), opt, 'Linewidth', width)
	end
end
