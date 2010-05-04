package se.uu.it.cats.brick;

import lejos.robotics.navigation.Pilot;
import se.uu.it.cats.brick.Logger;

public class CatPosCalc {
	private static Pilot pilot;
	
	private static float startAng;
	private static float distLast;
	
	public static float x;
	public static float y;
	public static float ang;
	
	public static void setPilot(Pilot p)
	{
		pilot = p;
	}
	
	public static void setCatState(float[] sCat)
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
	
	public static float getCatDist()
	{
		return pilot.getTravelDistance();
	}
	
	public static void update() {
		float distNew = pilot.getTravelDistance();
		ang = (startAng + pilot.getAngle()) % 360 * (float)Math.PI/180f;
		
		float deltaDist = distNew - distLast;
		
		x = x + deltaDist * (float) Math.cos(ang);
		y = y + deltaDist * (float) Math.sin(ang);
		
		distLast = distNew;
		
		/*Logger.println("x: " + (int)(x*100));
		Logger.println("y: " + (int)(y*100));
		Logger.println("ang: " + (int)(ang*180/(float)Math.PI));
		Logger.println("----------");*/
	}
}