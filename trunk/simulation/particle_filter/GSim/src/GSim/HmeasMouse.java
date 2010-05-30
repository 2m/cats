package GSim;

import lejos.util.Matrix;
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
		nm = billboard.getNoCats();
	}

	public Matrix eval(Matrix xm){
		//Calculates the bearing from the cats to the mouse
		//xm is where the cats think the mouse is
		//positions are where the cats thinks they are
		//NB: All -1 in the indices are used to indicate the shift form the first array index in matlab = 1 to java's = 0.
		//float[] positions = billboard.getAbsolutePositions();  //Gives incorrect/not relevant positions
		float[] positions = billboard.getLatestSightings();
		Matrix zm = Matlab.zeros(nm, 1);
		for (int i = 0; i<nm; i++)
		{
			//use either acos or asin
			if (xm.get(1,0) - positions[i*4+1] >=0) //needed because of ambiguity in acos (and asin)
			{
				zm.set(   i, 0, acos(  ( xm.get(0,0) - positions[i*4+0] ) / sqrt( Math.pow(xm.get(0,0) - positions[i*4+0], 2) + Math.pow(xm.get(1,0) - positions[i*4+1], 2) )  )   );
			}
			else
			{
				zm.set(   i, 0, 2*PI-acos(  ( xm.get(0,0) - positions[i*4+0] ) / sqrt( Math.pow(xm.get(0,0) - positions[i*4+0], 2) + Math.pow(xm.get(1,0) - positions[i*4+1], 2) )  )   );
			}
		}	

		/*System.out.println("Debug, in HmeasCat: input (xm) = ");
		printM(xm);	
		System.out.println("Debug, in HmeasCat: x1 = " + positions[(1-1)*4+0] + ", y1 = " + positions[(1-1)*4+1] + "; x2 = " + positions[(2-1)*4+0] + ", y2 = " + positions[(2-1)*4+1]);
		System.out.println("Debug, in HmeasCat: output (zm) = ");
		printM(zm);	
		*/
		
		return zm;	
	}
}

