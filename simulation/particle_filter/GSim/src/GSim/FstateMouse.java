package se.uu.it.cats.brick.filter;

import lejos.util.Matrix;

/**
 * State function for the (tracked) mouse's position
 * @author Edvard
 */
public class FstateMouse implements IFunction{
	
	/** Period of filter */
	protected final float dt;
	
	public FstateMouse(float dt)
	{
		this.dt = dt;
	}
	
	public Matrix eval(Matrix xm){
			
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
}

