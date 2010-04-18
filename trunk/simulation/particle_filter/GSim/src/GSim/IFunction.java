package GSim;

import lejos.util.Matrix;

/**
 * A interface for matrix functions (sent to the Unscented Kalman Filter)
 * @author Edvard
 */
public interface IFunction {
	
	/** 
	 * @param x input matrix
	 * @return output matrix 
	*/
	public Matrix eval(Matrix x);

}
