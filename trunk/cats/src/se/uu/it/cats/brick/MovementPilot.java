package se.uu.it.cats.brick;

import lejos.nxt.Button;
import lejos.nxt.Motor;
import lejos.robotics.navigation.Pilot;
import lejos.robotics.navigation.TachoPilot;

public class MovementPilot {	
	
	//Instance variables
	public Pilot pilot;
	private float[] c_state;
	
	public MovementPilot(float[] c_state)
	{
		this.c_state = c_state;
		
		Motor.B.regulateSpeed(true);
		Motor.C.regulateSpeed(true);
		Motor.B.smoothAcceleration(true);
		Motor.C.smoothAcceleration(true);
			
		pilot = new TachoPilot(0.054725f,0.0544862f, 0.167f,Motor.C, Motor.B, false); //0.05475f //old design cat1
		//pilot = new TachoPilot(0.055f, 0.172f, Motor.B, Motor.C, true); //new design
	    }	 
	
	public void travel(float x, float y){
		float deltaX = x-c_state[0];
		float deltaY = y-c_state[1];
		float r = (float) Math.sqrt(Math.pow(deltaX,2)+Math.pow(deltaY,2));
		float newAngle = (float) Math.acos(x/r);
		float angle = (float) ((newAngle-c_state[2])*180/Math.PI);
		pilot.setMoveSpeed(pilot.getMoveMaxSpeed()/2);
		pilot.setTurnSpeed(pilot.getTurnMaxSpeed()/2);
		pilot.rotate(angle);
		pilot.travel(r);
	}
}
