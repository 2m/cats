package GSim;

import lejos.util.Matrix;
import static GSim.Matlab.*; //Only for testing
import static java.lang.Math.*;


/**
 * Measurement function for the (tracked) mouse's position.
 * Calculates the bearings from the cats to the mouse
 * @author Edvard
 */
public class HmeasMouse implements IFunction{
	
	//Instance variables
	/** (a reference to the) estimated state (position) of the cats and latest sightings.*/
	private BillBoard billboard;
	/** number of cats, ie number of columns in x.*/
	private int nm;
	
	/**
	 * Constructor needed to be used for this class to work properly
	 * @param x  a reference to the object containing the estimated state (position) of the cats
	 */
	public HmeasMouse(BillBoard billboard){
		this.billboard = billboard;
		nm = billboard.NUMBER_OF_CATS;
	}

	
	/**
	 * 
	 */
	public Matrix eval(Matrix xm){
		//Calculates the bearing from the cats to the mouse
		//xm is where the cats think the mouse is
		//positions is where the cats thinks they are
		//NB: All -1 in the indices are used to indicate the shift form the first array index in matlab = 1 to java's = 0.
		float[] positions = billboard.getAbsolutePositions();
		Matrix zm = Matlab.zeros(nm, 1);
		for (int i = 1; i<=nm; i++)
		{
			//use either acos or asin
			if (xm.get(2-1,1-1) - positions[(i-1)*3+1] >=0) //needed because of ambiguity in acos (and asin)
			{
				zm.set(   i-1,1-1, acos(  ( xm.get(1-1,1-1) - positions[(i-1)*3+0] ) / sqrt( Math.pow(xm.get(1-1,1-1) - positions[(i-1)*3+0], 2) + Math.pow(xm.get(2-1,1-1) - positions[(i-1)*3+1], 2) )  )   );
			}
			else
			{
				zm.set(   i-1,1-1, 2*PI-acos(  ( xm.get(1-1,1-1) - positions[(i-1)*3+0] ) / sqrt( Math.pow(xm.get(1-1,1-1) - positions[(i-1)*3+0], 2) + Math.pow(xm.get(2-1,1-1) - positions[(i-1)*3+1], 2) )  )   );
			}
		}	
		return zm;
		
		
		/*Original matlab code::
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
	}
	
	
	//Only for testing
	public static void main(String[] arg) {
		BillBoard bb = new BillBoard(3);
		IFunction h = new HmeasMouse(bb);
		
		bb.setAbsolutePosition(1, 0.381674224891206f, 1.512334856720926f, 4.058937708438013f);
		bb.setAbsolutePosition(2, 0.489571459163319f, 0.381883712654769f, 5.629734035232906f);
		bb.setAbsolutePosition(3, 1.800657229677585f, 1.076843261031238f, 1.702743218245687f);	
		float[] pos = bb.getAbsolutePositions();
		System.out.print("x1 = " + pos[(1-1)*3+0] + ", y1 = " + pos[(1-1)*3+1] + "; x2 = " + pos[(2-1)*3+0] + ", y2 = " + pos[(2-1)*3+1]);
		
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

