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

	/**
	 * Penalty function for Gaussian particle filter
	 * 
	 * @param z
	 *            cosine of angle error as 12.20 fixed point integer
	 * @return weight as 12.20 fixed point integer
	 */
	public static int penalty(int z) {
		int w;
		if (z >= CUT[4]) {
			if (CUT[2] < z) {
				if (CUT[1] < z) {
					// cut(1) - cut(2)
					w = Fixed.mul(z, COEFF[0]) + COEFF[1];
				} else {
					// cut(2) - cut(3)
					w = Fixed.mul(z, COEFF[2]) + COEFF[3];
				}
			} else {
				if (CUT[3] < z) {
					// cut(3) - cut(4)
					w = Fixed.mul(z, COEFF[4]) + COEFF[5];
				} else {
					// cut(4) - cut(5)
					w = Fixed.mul(z, COEFF[6]) + COEFF[7];
				}
			}
		} else {
			w = 0;
		}
		return w;
	}

	/**
	 * Creates a transform matrix for the re-sampling from a co-variance matrix.
	 * 
	 * @param C
	 *            The co-variance matrix
	 * @return V The transform matrix
	 */
	public static int[][] getTransformFromCovariance(int[][] C) {
		int[][] V = new int[2][2];
		int a = C[0][0];
		int b = C[0][1];
		if (b == 0) {
			// Matrix is diagonal
			V[0][0] = Fixed.sqrt(C[0][0]);
			V[0][1] = 0;
			V[1][0] = 0;
			V[1][1] = Fixed.sqrt(C[1][1]);
		} else {
			int c = C[1][0];
			int d = C[1][1];
			// Calculate eigenvalues
			int p = (a + d);
			int q = Fixed.mul(a, d) - Fixed.mul(b, c);
			int phalf = (p >> 1);
			int phalfsquare = Fixed.mul(phalf, phalf);
			int sqrt = Fixed.sqrt(phalfsquare - q);
			int lambda1 = phalf + sqrt;
			int lambda2 = phalf - sqrt;
			// Calculate standard deviations
			int lambda1sqrt = Fixed.sqrt(lambda1);
			int lambda2sqrt = Fixed.sqrt(lambda2);
			// D = [lambda1 0; 0 lambda2];
			// Assume one element in the eigenvector is 1 then solve first
			// equation.
			int v12 = Fixed.div(-(a - lambda1), b);
			// Get eigenvector norm
			int norm = Fixed.sqrt(Fixed.mul(v12, v12) + Fixed.ONE);
			int norminv = 0;
			if (norm == 0) {
				// TODO: Div by zero
				System.out.println("Division by zero");
				norminv = 1 << 30;
			} else {
				norminv = Fixed.div(Fixed.ONE, norm);
			}
			int v12_norm = Fixed.mul(v12, norminv);
			// v1 = [1; -(a-lambda1)/b];
			// v1 = v1./norm(v1);
			// Eigen vectors are perpendicular => rot_p == [0 -1; 1 0]
			// v2 = [-v1(2); v1(1)];
			// V = [v1 v2];
			// Eigenvector corresponding to lambda1
			V[0][0] = Fixed.mul(norminv, lambda1sqrt); // a
			V[1][0] = Fixed.mul(v12_norm, lambda1sqrt); // c
			// Eigenvector corresponding to lambda2
			V[0][1] = Fixed.mul(-v12_norm, lambda2sqrt); // b
			V[1][1] = Fixed.mul(norminv, lambda2sqrt); // d
		}
		return V;
	}
}