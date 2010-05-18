package GSim;

import lejos.util.Matrix;
import static GSim.Matlab.*; //Only for testing

/**
 * State function for the cat's absolute position
 * @author Edvard
 */
public class FstateCat implements IFunction{
	
	/* Period of filter */
	protected final float dt;
	
	public FstateCat(float dt)
	{
		this.dt = dt;
	}
	
	public Matrix eval(Matrix xc){
		/*Original matlab code:
		function x_update = update2(x)
		F=eye(4);
		%here dt=1 since speed is measured in m/samples, thus: FIXME dt shouldn't be =1 in FstateCat ...
		F(1,3)=1;
		F(2,4)=1;
		x_update=F*x(1:4,1);
		x_update(5,1)=x(5,1);%atan2(x(4,1),x(3,1));
		x_update(6,1)=x(6,1);
		*/
			
		Matrix xc_updated = Matlab.zeros(xc.getRowDimension(), xc.getColumnDimension());
		//Matlab.printM(xc);
		if (xc.getRowDimension() != 5|| xc.getColumnDimension() != 1)
			{
			System.out.println("WARNING, update FstateCat to match the state vector");
			}
		Matrix F = Matlab.eye(4);
		//Matlab.printM(F);
		//here dt=1 since speed is measured in m/samples, thus:
		F.set(1-1,3-1, dt);
		F.set(2-1,4-1, dt);
		Matrix xc_updated_part = new Matrix(4,1,0);
		xc_updated_part=F.times( xc.getMatrix(1-1, 4-1, 1-1, 1-1) );
		//Matlab.printM(xc_updated_part);
		xc_updated.setMatrix(1-1, 4-1, 1-1, 1-1, xc_updated_part);
		//System.out.println("xc_updated: ");
		//Matlab.printM(xc_updated);
		xc_updated.set(5-1, 1-1, xc.get(5-1, 1-1));

		return xc_updated; 
	}
	
	//Only for testing
	public static void main(String[] arg) {
		IFunction f = new FstateCat(1);
		double[][] temp_xc = {{ 2 },
							  { 3 },
							  { 0.5 },
							  { 0.0 },
							  { 3.14 }};
		Matrix xc = new Matrix(temp_xc);
		System.out.println("xc before update:");
		printM(xc);
		Matrix xm_updated = f.eval(xc);
		System.out.println("xc after updated:");
		printM(xm_updated);	
	}
}
