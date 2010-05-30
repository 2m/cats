package GSim;

import static GSim.Matlab.printM;
import lejos.util.Matrix;

/**
 * Test the state function for the (tracked) mouse's position
 * @author Edvard
 */
public class FstateMouseTest {
	
	/*Original matlab code for FstateMouse:
	function x_update = update(x)
	F=eye(4);
	%here sampling period is dt=1, since speed is measured in meters/samples, thus:
	F(1,3)=1;
	F(2,4)=1;
	x_update=F*x;
	*/
	
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
