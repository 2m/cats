% Inspired by http://www.embedded.com/98/9802fe2.htm
% and the article "Fixed point square root" by Ken Turkowski

function root = binfpsqrt(x)
	root = uint32(0);
	remHi = uint32(0);
	remLo = x;
	for count = 1:31
		remHi = bitor(bitshift(remHi, 2), bitshift(remLo, -30));
		remLo = bitshift(remLo, 2);
		root = bitshift(root, 1);
		testDiv = bitshift(root, 1) + 1;
		if (remHi >= testDiv)
			remHi = remHi - testDiv;
			root = root + 1;
		end
	end
end
