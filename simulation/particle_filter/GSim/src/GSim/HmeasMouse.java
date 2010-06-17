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
		//IMPORTANT: atan2 is not working good if the bearing is close to 0 (fluctuates between 0 and 2 PI)
		//NB: All -1 in the indices are used to indicate the shift form the first array index in matlab = 1 to java's = 0.
		//float[] positions = billboard.getAbsolutePositions();  //Gives incorrect/not relevant positions
		float[] positions = billboard.getLatestSightings();
		Matrix zm = Matlab.zeros(nm, 1);
		double x_pos = xm.get(0,0);
		double y_pos = xm.get(1,0);
		
		for (int i = 0; i<nm; i++)
		{
			float measured_x_pos = positions[i*4+0];
			float measured_y_pos = positions[i*4+1];
			//use either acos or asin
			/*if (y_pos - positions[i*4+1] >=0) //needed because of ambiguity in acos (and asin)
			{
				zm.set(   i, 0, acos(  ( x_pos - measured_x_pos ) / sqrt( Math.pow(x_pos - measured_x_pos, 2) + Math.pow(y_pos - measured_y_pos, 2) )  )   );
			}
			else
			{
				zm.set(   i, 0, 2*PI-acos(  ( x_pos - measured_x_pos ) / sqrt( Math.pow(x_pos - measured_x_pos, 2) + Math.pow(y_pos - measured_y_pos, 2) )  )   );
			}*/
				zm.set(i, 0, Math.atan2(y_pos - measured_y_pos, x_pos - measured_x_pos));
				zm.set(i, 0, (zm.get(i,0)+2*Math.PI)%(2*Math.PI));
				if (Double.isNaN(zm.get(i,0))){
					Logger.println("Hmeas: z is NaN");
					throw new RuntimeException("Hmeas: z is NaN");
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

