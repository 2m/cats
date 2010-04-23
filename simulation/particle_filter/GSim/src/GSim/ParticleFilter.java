package GSim;

/**
 * Common functions for particle filters.
 * 
 */
public class ParticleFilter {

	public final static int[] COEFF = { Fixed.floatToFixed(292.8173),
			Fixed.floatToFixed(-291.8267), Fixed.floatToFixed(139.2145),
			Fixed.floatToFixed(-138.4522), Fixed.floatToFixed(42.0960),
			Fixed.floatToFixed(-41.7202), Fixed.floatToFixed(2.0903),
			Fixed.floatToFixed(-2.0474) };
	public final static int[] CUT = { Fixed.floatToFixed(1.0000),
			Fixed.floatToFixed(0.9986), Fixed.floatToFixed(0.9962),
			Fixed.floatToFixed(0.9920), Fixed.floatToFixed(0.9796) };

	public ParticleFilter() {
	}

	/**
	 * Penalty function for Gaussian particle filter
	 * 
	 * @param z
	 *            cosine of angle error as 12.20 fixed point integer
	 * @return weight as 12.20 fixed point integer
	 */
	public static int penalty(int z) {
		int w;
		if (z >= CUT[5]) {
			if (CUT[3] < z) {
				if (CUT[2] < z) {
					// cut(1) - cut(2)
					w = Fixed.mul(z, COEFF[1]) + COEFF[2];
				} else {
					// cut(2) - cut(3)
					w = Fixed.mul(z, COEFF[3]) + COEFF[4];
				}
			} else {
				if (CUT[4] < z) {
					// cut(3) - cut(4)
					w = Fixed.mul(z, COEFF[5]) + COEFF[6];
				} else {
					// cut(4) - cut(5)
					w = Fixed.mul(z, COEFF[7]) + COEFF[8];
				}
			}
		} else {
			w = 0;
		}
		return w;
	}
}
/*
 * function w = penalty(z) % Penalty function % Takes cosine of angle between
 * lines c = [ 292.8173 -291.8267 139.2145 -138.4522 42.0960 -41.7202 2.0903
 * -2.0474]; cut = [1.0000 0.9986 0.9962 0.9920 0.9744]; if (z>=cut(5)) if
 * (cut(3)<z) if (cut(2)<z) % cut(1) - cut(2) w = z*c(1, 1) + c(1, 2); else %
 * cut(2) - cut(3) w = z*c(2, 1) + c(2, 2); end else if (cut(4)<z) % cut(3) -
 * cut(4) w = z*c(3, 1) + c(3, 2); else % cut(4) - cut(5) w = z*c(4, 1) + c(4,
 * 2); end end else w = 0; end end
 */
