function w = penalty(z)
	% Penalty function
	% Takes cosine of angle between lines
	% Cutoffs: 1.00000, 0.97815, 0.97723, 0.97630, 0.97437
	%w = exp(-(acos(z).^2)/(2*sensor_std^2));
	if (z>=0.97437)
		if (0.97732<z)
			if (0.97815<z)
				w = z*4.3212e+01 - 4.2577e+01;
			else
				w = z*1.1974e-04 + 1.2253e-04;
			end
		else
			if (0.97630<z)
				w = z*8.4904e-05 + 8.6965e-05;
			else
				w = z*5.9788e-05 + 6.1299e-05;
			end
		end
	else
		w = 0;
	end
end
