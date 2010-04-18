package GSim;

import lejos.util.Matrix;

/**
 * The state function
 * @author Edvard
 */
public class Fstate implements IFunction{
	
	public Matrix eval(Matrix x){
		
		//TODO implement real state function
		
		//Basic state function to test the ukf implementation
		//x is a (n)x(1) Matrix
		Matrix output = new Matrix(x.getRowDimension(),x.getColumnDimension());
		output.set(0, 0, x.get(0,1) );
		output.set(1, 0, x.get(0,2) );
		output.set(2, 0, 0.05 * x.get(0,0) * x.get(0,1) + x.get(0,2) );
		return output; 
	}
}
