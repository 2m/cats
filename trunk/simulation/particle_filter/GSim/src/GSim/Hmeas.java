package GSim;

import lejos.util.Matrix;

/**
 * The measurement function
 * @author Edvard
 */
public class Hmeas implements IFunction{
	
	//TODO implement real measurement function
	
	public Matrix eval(Matrix x){
		return null;
	}
	
	/*
	public Matrix eval(Matrix x){
		//Basic measurement function to test the ukf implementation
		//x is a (1)x(1) Matrix
		Matrix output = new Matrix(1,1,x.get(0,0));
		return output; 
	}*/
}

