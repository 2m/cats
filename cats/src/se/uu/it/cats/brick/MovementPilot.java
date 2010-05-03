package se.uu.it.cats.brick;

import lejos.nxt.Button;
import lejos.nxt.Motor;
import lejos.robotics.navigation.TachoPilot;

public class MovementPilot extends TachoPilot {	
	
	public MovementPilot()
	{
		//super(0.054725f,0.0544862f, 0.167f,Motor.C, Motor.B, false); //0.05475f //old design cat1
		super(0.055f, 0.172f, Motor.A, Motor.C, false); //new design
		
		Motor.A.regulateSpeed(true);
		Motor.C.regulateSpeed(true);
		Motor.A.smoothAcceleration(true);
		Motor.C.smoothAcceleration(true);
		setMoveSpeed(2*getMoveMaxSpeed()/3);
		setTurnSpeed(getTurnMaxSpeed()/2);
	}	 
	
	public void travel(float x, float y){
		
		float[] catPos = CatPosCalc.getCatPos();
		
		float deltaX = x-catPos[0];
		float deltaY = y-catPos[1];
		float r = (float) Math.sqrt(deltaX*deltaX + deltaY*deltaY);
		float newAngle = (float) Math.atan2(deltaY,deltaX);
		
		float angle = (float) ((newAngle - catPos[2]) % (Math.PI*2));
		if (angle < -Math.PI)
			angle += 2 * Math.PI;
		else if (angle > Math.PI)
			angle -= 2 * Math.PI;
			
		angle = (float) ((angle)*180/Math.PI); // to angles
		
		Logger.println("Before rotate");
		Logger.println("r:"+r+" deltaX"+deltaX+" deltaY"+deltaY+" catPos[0]"+catPos[0]+" catPos[1]"+catPos[1]);
		Logger.println("Angle:"+angle+" newAngle:"+newAngle+" catPos[2]"+catPos[2]);
		
		rotate(angle); // blocks while rotating
		CatPosCalc.update();
		
		travel(r); // blocks while moving
		CatPosCalc.update();

	}
}
