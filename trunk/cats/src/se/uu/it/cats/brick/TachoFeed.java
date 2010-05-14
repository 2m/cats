package se.uu.it.cats.brick;

import lejos.robotics.navigation.Pilot;

/* @Nils Törnblom
 * For the real implementation of the absolute positioning particle filter.
 */
public class TachoFeed {
	private RealTimeClock realtime;
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
		MovementData MD = new MovementData(realtime.getTime(), newR - lastR, newAngle - lastAngle);
		MovementBuffer.push(MD);
		lastR = newR;
		lastAngle = newAngle;
		
	}
		
}
