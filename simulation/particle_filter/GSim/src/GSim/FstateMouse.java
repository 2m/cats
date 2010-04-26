package GSim;

import lejos.util.Matrix;

/**
 * State function for the (tracked) mouse's position
 * @author Edvard
 */
public class FstateMouse implements IFunction{
	
	public Matrix eval(Matrix xm){
		/*Original matlab code:
		function x_update = update(x)
		F=eye(4);
		%here sampling period is dt=1, since speed is measured in meters/samples, thus:
		F(1,3)=1;
		F(2,4)=1;
		x_update=F*x;
		*/
		
		//TODO test this function		
		Matrix xm_updated = Matlab.zeros(xm.getRowDimension(), xm.getColumnDimension());
		Matrix F = Matlab.eye(4);
		//here dt=1 since speed is measured in m/samples, thus:
		F.set(1-1,3-1, 1);
		F.set(2-1,4-1, 1);
		xm_updated=F.times(xm);
		return xm_updated; 
	}
	
}