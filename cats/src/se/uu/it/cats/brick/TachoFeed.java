package se.uu.it.cats.brick;

import se.uu.it.cats.brick.filter.MovementData;
import lejos.robotics.navigation.Pilot;

/* @Nils Tornblom
 * For the real implementation of the absolute positioning particle filter.
 */
public class TachoFeed {
	
	
	public Buffer MovementBuffer;
	private static Pilot pilot;
	public static float lastR;
	public static float newR;
	public static float newAngle;
	public static float lastAngle;
	
	public static void setPilot(Pilot p)
	{
		pilot = p;
	}
	
	public static void pushMovement() {
		newR = pilot.getTravelDistance();
		newAngle = (float) (pilot.getAngle() * Math.PI/180f);
		MovementData MD = new MovementData(Clock.timestamp(), newR - lastR, newAngle - lastAngle);
		MovementBuffer.push(MD);
		lastR = newR;
		lastAngle = newAngle;
		
	}
	
		
}
