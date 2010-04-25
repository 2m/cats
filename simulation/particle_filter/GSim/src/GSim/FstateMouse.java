package GSim;

import lejos.util.Matrix;

/**
 * State function for the mouse's position
 * @author Edvard
 */
public class FstateMouse implements IFunction{
	
	public Matrix eval(Matrix x){
		/*Original matlab code:
		function x_update = update(x)
		F=eye(4);
		%here sampling period is dt=1, since speed is measured in meters/samples, thus:
		F(1,3)=1;
		F(2,4)=1;
		x_update=F*x;
		*/
		
		//TODO test this function		
		Matrix x_update = Matlab.zeros(x.getRowDimension(), x.getColumnDimension());
		Matrix F = Matlab.eye(4);
		//here dt=1 since speed is measured in m/samples, thus:
		F.set(1-1,3-1, 1);
		F.set(2-1,4-1, 1);
		x_update=F.times(x);
		return x_update; 
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