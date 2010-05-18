package GSim;

import lejos.util.Matrix;
import static GSim.Matlab.*; //Only for testing


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

	//FIXME: change to billborad form matrix
	public Matrix eval(Matrix xm){
		/*
		
		//TODO test measurement function
		//NB: All -1 in the indices are used to indicate the shift form the first array index in matlab = 1 to java's = 0.
		Matrix zm = Matlab.zeros(nm, 1);
		for (int i = 1; i<nm; i++)
		{
			//use either acos or asin
			if (xm.get(2-1,1-1)-x.get(2-1,i-1)>=0) //needed because of ambiguity in acos (and asin)
			{
				zm.set(i-1,1-1,  Math.acos( xm.get(1-1,1-1)-x.get(1-1,i-1) ) / Math.sqrt( Math.pow(xm.get(1-1,1-1)-x.get(1-1,i-1),2) ) + Math.pow(xm.get(2-1,1-1)-x.get(2-1,i-1),2)  );

			}
			else
			{
				zm.set(i-1,1-1,  2*Math.PI-Math.acos( xm.get(1-1,1-1)-x.get(1-1,i-1) ) / Math.sqrt( Math.pow(xm.get(1-1,1-1)-x.get(1-1,i-1),2) ) + Math.pow(xm.get(2-1,1-1)-x.get(2-1,i-1),2)  );

			}
		}	
		return zm;
		
		*/
		return null;
		
		
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
		IFunction f = new FstateCat(1);
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

