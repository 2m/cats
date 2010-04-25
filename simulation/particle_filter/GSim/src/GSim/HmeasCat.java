package GSim;

import lejos.util.Matrix;

/**
 * Measurement function for the cats's position
 * @author Edvard
 */
public class HmeasCat implements IFunction{

	
	public Matrix eval(Matrix x){	
		/*Original matlab code:
		function z = measure2WspeedMeasurements(x)
		%calculates the bearings from the landm to the mouse and adds measurement
		%noise
		global landm;
		global n;

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
		z(n+1,1)=x(3,1);
		z(n+2,1)=x(4,1);
		z(n+3,1)=x(5,1);
		z(n+4,1)=x(6,1);
		*/
		
		//TODO test measurement function
		//NB: All -1 in the indices are used to indicate the shift form the first array index in matlab = 1 to java's = 0.
		Matrix z = Matlab.zeros(x.getRowDimension(), x.getColumnDimension());
		int n = LandmarkList.landmarkX.length;
		for (int i = 1; i<n; i++)
		{
			z.set(i-1,1-1, Math.atan2(LandmarkList.landmarkY[i-1] - x.get(2-1,1-1), LandmarkList.landmarkX[i-1] - x.get(1-1,1-1)));
			z.set(i-1,1-1, z.get(i-1,1-1)%2*Math.PI);
		}
		z.set(n+1-1,1-1, x.get(3-1,1-1));
		z.set(n+2-1,1-1, x.get(4-1,1-1));
		z.set(n+3-1,1-1, x.get(5-1,1-1));
		z.set(n+4-1,1-1, x.get(6-1,1-1));
	
		return z;
	}
}

