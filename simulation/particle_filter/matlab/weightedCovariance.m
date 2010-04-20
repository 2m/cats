function C = weightedCovariance(x, y, w)
	C = zeros(2, 2);
	d = [x y];
	for t=1:2
		for g=1:2
			C(t, g)=sum(w.*(d(:,t)-mean(d(:,t))).*(d(:,g)-mean(d(:,g))));
		end
	end
end
