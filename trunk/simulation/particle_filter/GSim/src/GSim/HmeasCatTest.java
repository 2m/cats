package GSim;

import static GSim.Matlab.printM;
import lejos.util.Matrix;

/**
 * Test the measurement function for the cats's absolute position
 * @author Edvard
 */
public class HmeasCatTest {
	
	/*Original matlab code for HmeasCat:
	function z = measure2WspeedMeasurements(x)
	%calculates the bearings from the landm to the cats
	global landm;
	%global n;
	n = size(landm,1);
		for i=1:n
	    %use either acos or asin
	%     if (landm(i,2)-x(2,1)>=0) %needed because of ambiguity in acos (and asin)
	%         %can be simplified(?)
	%         z(i,1)=acos((landm(i,1)-x(1,1)) /...
	%             sqrt((landm(i,1)-x(1,1))^2 + (landm(i,2)-x(2,1))^2));
	%     else
	%         z(i,1)=2*pi-acos((x(1,1)-landm(i,1)) /...
	%             sqrt((landm(i,1)-x(1,1))^2 + (landm(i,2)-x(2,1))^2));
	%     end
	%something wrong with the above, use instead:
		    %Alternative: (somtimes less divergent, sometimes more)
	     z(i,1)=atan2(landm(i,2)-x(2,1),landm(i,1)-x(1,1));
	     z(i,1)=mod(z(i,1),2*pi);
	end
	z(n+1,1)=x(1,1); %measure x position
	z(n+2,1)=x(2,1); %measure y position
	z(n+3,1)=x(5,1); %measure cat orientation
	z(n+4,1)=x(6,1); %measure camera orientation	
	*/
	
	public static void main(String[] arg) {
		IFunction f = new HmeasCat();
		double[][] temp_xc = {{ 1.477369839207423   },
							  { 0.5028351758705135  },
							  { 0.04999179014188175   },
							  { 0.07713072965540284   },
							  { 0.989083374173641   }};
		Matrix xc = new Matrix(temp_xc);
		Matrix zc = f.eval(xc);
		System.out.println("debug, in HmeasCat: input (xc) = ");
		printM(xc);
		System.out.println("debug, in HmeasCat: output (zc) = ");
		printM(zc);	
	}

}
