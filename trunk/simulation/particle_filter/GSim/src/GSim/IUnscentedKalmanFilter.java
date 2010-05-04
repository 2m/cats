package GSim;

import lejos.util.Matrix;

/**
 * The interface for the Unscented Kalman Filter
 * @author Edvard
 */
public interface IUnscentedKalmanFilter 
{

	/**
	 * UKF, Unscented Kalman Filter, for nonlinear dynamic systems. 
	 * [x, P] = ukf(f,x,P,h,z,Q,R) returns state estimate x and state covariance P 
	 * for nonlinear dynamic system (for simplicity, noises are assumed as additive):
	 *            x_k+1 = f(x_k) + w_k
	 *            z_k   = h(x_k) + v_k
	 * where w ~ N(0,Q) meaning w is gaussian noise with covariance Q and 
	 *       v ~ N(0,R) meaning v is gaussian noise with covariance R.         
	 * @param f  function handle for f(x), nonlinear state equations
	 * @param x  "a priori" state estimate
	 * @param P  "a priori" estimated state covariance
	 * @param h  fanction handle for h(x), measurement equation
	 * @param z  current measurement
	 * @param Q  process noise covariance
	 * @param R  measurement noise covariance
	 * @return  "a posteriori" state estimate and P: "a posteriori" state covariance
	 */
	public Matrix[] ukf(IFunction f, Matrix x, Matrix P, IFunction h, Matrix z, Matrix Q, Matrix R);
	

	/*
	/**
	 * Not implemnted
	 * @param f
	 * @param x
	 * @param P
	 * @param h
	 * @param z
	 * @param Q
	 * @param R
	 * @param Kzero
	 * @return
	public Matrix[] ukf(IFunction f, Matrix x, Matrix P, IFunction h, Matrix z, Matrix Q, float R, float Kzero);
	 */
		

}
