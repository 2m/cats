package GSim;

import lejos.util.Matrix;

/**
 * The interface for the Unscented Kalman Filter
 * @author Edvard
 *
 */
public interface IUnscentedKalmanFilter 
{
	/**
	 * Ukf, Unscented Kalman Filter for nonlinear dynamic systems
	 * [x, P] = ukf(f,x,P,h,z,Q,R) returns state estimate, x and state covariance, P 
	 * for nonlinear dynamic system (for simplicity, noises are assumed as additive):
	 *            x_k+1 = f(x_k) + w_k
	 *            z_k   = h(x_k) + v_k
	 * where w ~ N(0,Q) meaning w is gaussian noise with covariance Q
	 *       v ~ N(0,R) meaning v is gaussian noise with covariance R    
	 * @param f: function handle for f(x), nonlinear state equations
	 * @param x: "a priori" state estimate
	 * @param P: "a priori" estimated state covariance
	 * @param h: fanction handle for h(x), measurement equation
	 * @param z: current measurement
	 * @param Q: process noise covariance
	 * @param R: measurement noise covariance
	 * @param ???
	 * @return x: "a posteriori" state estimate and P: "a posteriori" state covariance
	 */
	public Matrix Ukf(float fstate, Matrix x, Matrix P, float hmeas, Matrix z, Matrix Q, float R, float Kzero);

}
