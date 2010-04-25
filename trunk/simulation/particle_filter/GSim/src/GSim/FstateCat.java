package GSim;

import lejos.util.Matrix;

/**
 * State function for the cat's position
 * @author Edvard
 */
public class FstateCat implements IFunction{
	
	public Matrix eval(Matrix x){
		/*Original matlab code:
		function x_update = update2(x)
		F=eye(4);
		%here dt=1 since speed is measured in m/samples, thus:
		F(1,3)=1;
		F(2,4)=1;
		x_update=F*x(1:4,1);
		x_update(5,1)=x(5,1);%atan2(x(4,1),x(3,1));
		x_update(6,1)=x(6,1);
		*/
		
		//TODO test this function		
		Matrix x_update = Matlab.zeros(x.getRowDimension(), x.getColumnDimension());
		Matrix F = Matlab.eye(4);
		//here dt=1 since speed is measured in m/samples, thus:
		F.set(1-1,3-1, 1);
		F.set(2-1,4-1, 1);
		x_update=F.times( x.getMatrix(1-1, 4-1, 1-1, 1-1) );
		x_update.set(5-1, 1-1, x.get(5-1, 1-1));
		x_update.set(6-1, 1-1, x.get(6-1, 1-1));

		return x_update; 
	}
}