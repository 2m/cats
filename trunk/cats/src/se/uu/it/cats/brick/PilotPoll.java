package se.uu.it.cats.brick;

import lejos.robotics.navigation.Pilot;
import se.uu.it.cats.brick.Logger;

public class PilotPoll implements Runnable{
	private Pilot pilot;
	float distNew;
	float ang;
	float startAng;
	float distLast;
	float deltaDist;
	
	public float x;
	public float y;

	public PilotPoll(Pilot pilot, float[] c_state)
	{
		this.pilot = pilot;
		this.x = c_state[0];
		this.y = c_state[1];
		this.startAng = c_state[2];
	}
	
	public void run() {
		while(true){
			distNew = pilot.getTravelDistance();
			ang = startAng + pilot.getAngle() * (float)Math.PI/180f;
			
			deltaDist = distNew - distLast;
			
			x = x + deltaDist * (float) Math.cos(ang);
			y = y + deltaDist * (float) Math.sin(ang);
			
			distLast = distNew;
			
			Logger.println("x: " + (int)(x*100));
			Logger.println("y: " + (int)(y*100));
			Logger.println("ang: " + (int)(ang*180/(float)Math.PI));
			Logger.println("----------");
			try{Thread.sleep(500);}catch(Exception ex){}
		}
	}
}