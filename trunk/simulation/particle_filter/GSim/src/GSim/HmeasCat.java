package GSim;

import lejos.util.Matrix;

/**
 * Measurement function for the cats's absolute position
 * @author Edvard
 */
public class HmeasCat implements IFunction{
	
	public Matrix eval(Matrix xc){	
		/*Original matlab code:
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
		
		//TODO test measurement function
		//NB: All -1 in the indices are used to indicate the shift form the first array index in matlab = 1 to java's = 0.
		Matrix zc = Matlab.zeros(xc.getRowDimension(), xc.getColumnDimension());
		int n = LandmarkList.landmarkX.length;
		for (int i = 1; i<n; i++)
		{
			zc.set(i-1,1-1, Math.atan2(LandmarkList.landmarkY[i-1] - xc.get(2-1,1-1), LandmarkList.landmarkX[i-1] - xc.get(1-1,1-1)));
			zc.set(i-1,1-1, zc.get(i-1,1-1)%2*Math.PI);
		}
		zc.set(n+1-1,1-1, xc.get(1-1,1-1));
		zc.set(n+2-1,1-1, xc.get(2-1,1-1));
		zc.set(n+3-1,1-1, xc.get(5-1,1-1));
		zc.set(n+4-1,1-1, xc.get(6-1,1-1));
	
		return zc;
	}
}

