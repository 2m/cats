package GSim;

import lejos.util.Matrix;

/**
 * Measurement function for the (tracked) mouse's position
 * @author Edvard
 */
public class HmeasMouse implements IFunction{
	
	//TODO implement real measurement function
	
	
	
	public Matrix eval(Matrix xm){
		return null;
		/*
		function zm = measurem(xm)
		%calculates the bearings from the cats to the mouse and adds measurement noise
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
	
	/*
	public Matrix eval(Matrix x){
		//Basic measurement function to test the ukf implementation
		//x is a (1)x(1) Matrix
		Matrix output = new Matrix(1,1,x.get(0,0));
		return output; 
	}*/
}

