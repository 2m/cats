package GSim;

import static GSim.Matlab.printM;
import lejos.util.Matrix;

/**
 * Test the state function for the cat's absolute position
 * @author Edvard
 */
public class FstateCatTest {
	
	/*Original matlab code for FstateCat:
	function x_update = update2(x)
	F=eye(4);
	%here dt=1 since speed is measured in m/samples, thus: FIXME dt shouldn't be =1 in FstateCat ...
	F(1,3)=1;
	F(2,4)=1;
	x_update=F*x(1:4,1);
	x_update(5,1)=x(5,1);%atan2(x(4,1),x(3,1));
	x_update(6,1)=x(6,1);
	*/
	
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
