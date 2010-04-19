package se.uu.it.cats.brick;

import lejos.nxt.Motor;
import lejos.robotics.navigation.Pilot;
import lejos.robotics.navigation.TachoPilot;

public class MovementPilot {	
	public MovementPilot(float x, float y)
	{
		float[] c_state = filter.getState();
		float r = (float) Math.sqrt(Math.pow(x-c_state[1],2)+Math.pow(y-c_state[2],2));
		float angle = (float) (c_state[3]-Math.acos(x/r));
		Pilot pilot = new TachoPilot(0.055f, 0.16f, Motor.B, Motor.C, true);
		pilot.setMoveSpeed(10);
		pilot.rotate(angle);
		pilot.travel(r);
	    
	    }	 
}
