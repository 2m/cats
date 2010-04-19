package se.uu.it.cats.brick;

import lejos.nxt.Motor;
import lejos.robotics.navigation.Pilot;
import lejos.robotics.navigation.TachoPilot;

public class MovementPilot {	
	public MovementPilot(float x, float y)
	{
		float[] c_state = filter.getState();
		float deltaX = x-c_state[1];
		float deltaY = y-c_state[2];
		float r = (float) Math.sqrt(Math.pow(deltaX,2)+Math.pow(deltaY,2));
		float newAngle = (float) Math.acos(x/r);
		float angle = (c_state[3]-newAngle);
		Pilot pilot = new TachoPilot(0.055f, 0.16f, Motor.B, Motor.C, true);
		pilot.setMoveSpeed(10);
		pilot.rotate(angle);
		pilot.travel(r);
	   
	    }	 
}
