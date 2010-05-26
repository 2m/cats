package se.uu.it.cats.brick.filter;

import lejos.util.Matrix;

/**
 * A interface for vector functions (sent to the Unscented Kalman Filter)
 * @author Edvard
 */
public interface IFunction {
	
	/** Evaluates the function for the given vector 
	 * @param x input vector, in our cases a (n)x(1) Matrix
	 * @return output vector, in our cases a (n)x(1) Matrix 
	*/
	public Matrix eval(Matrix x);

}
