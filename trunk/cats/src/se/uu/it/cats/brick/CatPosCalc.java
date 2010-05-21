package se.uu.it.cats.brick;

import se.uu.it.cats.brick.Logger;
import se.uu.it.cats.brick.filter.Buffer;
import se.uu.it.cats.brick.filter.BufferSorted;
import se.uu.it.cats.brick.filter.MovementData;

public class CatPosCalc {

	private static float startAng;
	private static float distLast;
	
	public static float x;
	public static float y;
	public static float ang;
	
	public static Buffer movementData = new BufferSorted();
	
	/*public static void setCatState(float[] sCat)
	{
		x = sCat[0];
		y = sCat[1];
		startAng = sCat[2];
	}
	
	public static float[] getCatPos()
	{
		update();
		return new float[] {x, y, ang};
	}
	
	public static float getCatAng()
	{
		update();
		return ang;
	}
	
	public static float getTravelDistance()
	{
		return MovementPilot.getInstance().getTravelDistance();
	}
	
	public static float getCurrentAngle()
	{
		return MovementPilot.getInstance().getAngle();
	}
	
	public static Buffer getCatMovementBuffer()
	{
		return movementData;
	}
	
	public static void update() {
		float distNew = getTravelDistance();
		ang = (float) ((startAng + getCurrentAngle()*Math.PI/180f) % (2f*Math.PI));
		
		float deltaDist = distNew - distLast;
		
		x = x + deltaDist * (float) Math.cos(ang);
		y = y + deltaDist * (float) Math.sin(ang);
		
		distLast = distNew;

		//Logger.println("x: " + (int)(x*100));
		//Logger.println("y: " + (int)(y*100));
		//Logger.println("ang: " + (int)(ang*180/(float)Math.PI));
		//Logger.println("----------");
	}*/
}