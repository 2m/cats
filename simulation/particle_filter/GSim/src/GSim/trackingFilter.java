package GSim;

public class trackingFilter {
	private buffer sensorData;
	private buffer movementData;
	private int[] weights;
	protected int N;
	protected double T;
	
	public trackingFilter(int N, double T, buffer sensorData, buffer movementData) {
		this.N = N;
		this.T = T;
		this.sensorData = sensorData;
		this.movementData = movementData;
	}
	
/*	
 function w = penalty(z)
	% Penalty function
	% Takes cosine of angle between lines
	c = [	292.8173 -291.8267
		139.2145 -138.4522
		42.0960  -41.7202
		2.0903   -2.0474];
	cut = [1.0000 0.9986 0.9962 0.9920 0.9744];
	if (z>=cut(5))
		if (cut(3)<z)
			if (cut(2)<z)
				% cut(1) - cut(2)
				w = z*c(1, 1) + c(1, 2);
			else
				% cut(2) - cut(3)
				w = z*c(2, 1) + c(2, 2);
			end
		else
			if (cut(4)<z)
				% cut(3) - cut(4)
				w = z*c(3, 1) + c(3, 2);
			else
				% cut(4) - cut(5)
				w = z*c(4, 1) + c(4, 2);
			end
		end
	else
		w = 0;
	end
end

 */
}