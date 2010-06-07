package GSim;

import lejos.util.Matrix;

/**
 * Measurement function for the cats's absolute position
 * @author Edvard
 */
public class HmeasCat implements IFunction{
	
	public Matrix eval(Matrix xc){		
		//NB: All -1 in the indices are used to indicate the shift form the first array index in matlab = 1 to java's = 0.
		
		int n = Settings.NO_LANDMARKS;
		Matrix zc = Matlab.zeros(n+3, n+3);
		//System.out.println("Debug, HmeasCat: n = " + n);
		for (int i = 1; i<=n; i++)
		{
			zc.set(i-1,1-1, Math.atan2(Settings.LANDMARK_POSITION[i-1][1] - xc.get(1,0), Settings.LANDMARK_POSITION[i-1][0] - xc.get(0,0)));
			zc.set(i-1,1-1, (zc.get(i-1,1-1)+2*Math.PI)%(2*Math.PI));
		}
		zc.set(n+1-1,1-1, xc.get(3-1,1-1));  //measure x velcoity
		zc.set(n+2-1,1-1, xc.get(4-1,1-1));  //measure y velcoity
		zc.set(n+3-1,1-1, xc.get(5-1,1-1));  //measure cat orientation
		
		return zc;
	}
}

