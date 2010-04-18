package GSim;

import lejos.util.Matrix;

/**
 * The measurement function
 * @author Edvard
 */
public class Hmeas implements IFunction{
	
	public Matrix eval(Matrix x){
		//TODO implement real measurement function
		
		//Basic measurement function to test the ukf implementation
		//x is a (1)x(1) Matrix
		Matrix output = new Matrix(1,1,x.get(0,0));
		return output; 
	}
}

