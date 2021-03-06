package GSim;

import static GSim.Matlab.printM;
import lejos.util.Matrix;

/**
 * Tests the measurement function for the (tracked) mouse's position.
 * @author Edvard
 */
public class HmeasMouseTest {
	
	/*Original matlab code fore HmeasMouse:
	function zm = measurem(xm)
	%calculates the bearings from the cats to the mouse
	%xm is where the cats think the mouse is
	global x; %where we think the cats are
	%global nm;
	nm=size(x,2);
	zm=zeros(nm,1);
	for i=1:nm
	    %use either acos or asin
	    if (xm(2,1)-x(2,i)>=0) %needed because of ambiguity in acos (and asin)
	                             %can be simplified(?)
	        zm(i,1)=acos((xm(1,1)-x(1,i)) /...
	           sqrt((xm(1,1)-x(1,i))^2 + (xm(2,1)-x(2,i))^2));
	    else
	        zm(i,1)=2*pi-acos((xm(1,1)-x(1,i)) /...
	            sqrt((xm(1,1)-x(1,i))^2 + (xm(2,1)-x(2,i))^2));
	    end

	    %Alternative: (somtimes less divergent, sometimes more)
	    %z(i,1)=atan2(x(2,1)-cats(i,2),x(1,1)-cats(i,1));
	end
	 */

	public static void main(String[] arg) {
		BillBoard bb = new BillBoard(3);
		IFunction h = new HmeasMouse(bb);

		bb.setAbsolutePosition(0, 0.381674224891206f, 1.512334856720926f, 4.058937708438013f, 0);
		bb.setAbsolutePosition(1, 0.489571459163319f, 0.381883712654769f, 5.629734035232906f, 0);
		bb.setAbsolutePosition(2, 1.800657229677585f, 1.076843261031238f, 1.702743218245687f, 0);	
		bb.setLatestSighting(0, 0.381674224891206f, 1.512334856720926f, 0f, 0);
		bb.setLatestSighting(1, 0.489571459163319f, 0.381883712654769f, 0f, 0);
		bb.setLatestSighting(2, 1.800657229677585f, 1.076843261031238f, 0f, 0);	
		float[] pos = bb.getAbsolutePositions();
		System.out.print("x1 = " + pos[(1-1)*4+0] + ", y1 = " + pos[(1-1)*4+1] + "; x2 = " + pos[(2-1)*4+0] + ", y2 = " + pos[(2-1)*4+1]);

		double[][] temp_xm = {{ -0.898673537632615   },
				{ 0.530788546001928  },
				{ -0.180865697487447   },
				{ -0.023680668354299   }};    
		Matrix xm= new Matrix(temp_xm);

		Matrix zm = h.eval(xm);
		System.out.println("debug, in HmeasCat: input (xm) = ");
		printM(xm);
		System.out.println("debug, in HmeasCat: output (zm) = ");
		printM(zm);	
		System.out.println("debug, in HmeasCat: expected output (zm) =  3.795648991349351, 3.034739976181827, 3.341191688477540");
	}

}
