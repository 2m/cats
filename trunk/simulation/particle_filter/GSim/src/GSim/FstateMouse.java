package GSim;

import static java.lang.Math.pow;
import lejos.util.Matrix;
import static GSim.Matlab.*; //Only for testing

/**
 * State function for the (tracked) mouse's position
 * @author Edvard
 */
public class FstateMouse implements IFunction{
	
	/* Period of filter */
	protected final float dt;
	
	public FstateMouse(float dt)
	{
		this.dt = dt;
	}
	
	public Matrix eval(Matrix xm){
		/*Original matlab code:
		function x_update = update(x)
		F=eye(4);
		%here sampling period is dt=1, since speed is measured in meters/samples, thus:
		F(1,3)=1;
		F(2,4)=1;
		x_update=F*x;
		*/
			
		Matrix xm_updated = Matlab.zeros(xm.getRowDimension(), xm.getColumnDimension());
		if (xm.getRowDimension() != 4|| xm.getColumnDimension() != 1)
		{
		System.out.println("WARNING, update FstateMouse to match the state vector");
		}
		Matrix F = Matlab.eye(4);
		//here dt=1 since speed is measured in m/samples, thus:
		F.set(1-1,3-1, dt);
		F.set(2-1,4-1, dt);	
		xm_updated=F.times(xm);
		return xm_updated; 
	}
	
	//Only for testing
	public static void main(String[] arg) {
		IFunction f = new FstateMouse(1);
		double[][] temp_xm = {{ 2 },
							  { 3 },
							  { 0.5 },
							  {0.0 }};
		Matrix xm = new Matrix(temp_xm);
		System.out.println("xm before update:");
		printM(xm);
		Matrix xm_updated = f.eval(xm);
		System.out.println("xm after updated:");
		printM(xm_updated);	
	}
	
}

