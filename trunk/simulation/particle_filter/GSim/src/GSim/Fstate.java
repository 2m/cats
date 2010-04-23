package GSim;

import lejos.util.Matrix;

/**
 * The state function
 * @author Edvard
 */
public class Fstate implements IFunction{
	
	public Matrix eval(Matrix x){
		
		//TODO implement real state function
		
		return null; 
	}
	
	/*
	public Matrix eval(Matrix x){
		//Basic state function to test the ukf implementation
		//x is a (n)x(1) Matrix	
		Matrix output = new Matrix(x.getRowDimension(),x.getColumnDimension());
		//System.out.println("Debug: In Fstate.eval(), x:");
		//UnscentedKalmanFilter.printM(x);
		
		output.set(0, 0, x.get(1,0) );
		output.set(1, 0, x.get(2,0) );
		output.set(2, 0, 0.05 * x.get(0,0) * (x.get(1,0) + x.get(2,0)) );	
		//System.out.println("Debug: In Fstate.eval(), output:");
		//UnscentedKalmanFilter.printM(output);
		return output; 
	}
	*/
}
