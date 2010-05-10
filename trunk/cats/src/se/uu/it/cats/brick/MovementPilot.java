package se.uu.it.cats.brick;

import lejos.nxt.Button;
import lejos.nxt.Motor;
import lejos.nxt.Sound;
import lejos.robotics.navigation.TachoPilot;

public class MovementPilot extends TachoPilot {	
	
	public MovementPilot()
	{
		//super(0.054725f,0.0544862f, 0.167f,Motor.C, Motor.A, true); //0.05475f //old design cat1, old controller
		super(0.05475f,0.05475f, 0.167f,Motor.C, Motor.A, false); //0.05475f //old design cat1
		//super(0.055f, 0.172f, Motor.C, Motor.A, false); //new design
		
		/*Motor.A.regulateSpeed(true);
		Motor.C.regulateSpeed(true);
		Motor.A.smoothAcceleration(true);
		Motor.C.smoothAcceleration(true);
		setMoveSpeed(2*getMoveMaxSpeed()/3);
		setTurnSpeed(getTurnMaxSpeed()/2);*/
	}	 
	
	public void travel(float x, float y){
		
		//for some strange reason I have to switch these to get the right coordiantes:
		float switchTemp = x;
		x = y;
		y = switchTemp;
		
		float[] catPos = CatPosCalc.getCatPos();
		
		float deltaX = x-catPos[0];
		float deltaY = y-catPos[1];
		System.out.println("deltaY: " + deltaY);
		float r = (float) Math.sqrt(deltaX*deltaX + deltaY*deltaY);
		float newAngle = (float) Math.atan2(deltaY,deltaX);
		System.out.println("newAngle: " + newAngle*180f/Math.PI);
		
		float turnAngle = (float) ((newAngle - catPos[2]) % (Math.PI*2f));  //TODO: not 0 when it should!
		System.out.println("turnAngle before: " + turnAngle*180f/Math.PI);
		if (turnAngle < -Math.PI)
			turnAngle += 2f * Math.PI;
		else if (turnAngle > Math.PI)
			turnAngle -= 2f * Math.PI;
		
		//drive backward or forward
		boolean forward;
		if (turnAngle > Math.PI/2f) { //2nd quadrant
			forward = false; //drive backward
			turnAngle -= Math.PI;
		}
		else if (turnAngle < -Math.PI/2f) { //3rd quadrant
			forward = false; //drive backward
			turnAngle += Math.PI;
		}
		else
			forward = true;
	
		
		turnAngle = (float) ((turnAngle)*180f/Math.PI); // to angles
		System.out.println("turnAngle: " + turnAngle);
		
		/*Logger.println("Before rotate");
		Logger.println("r:"+r+" deltaX"+deltaX+" deltaY"+deltaY+" catPos[0]"+catPos[0]+" catPos[1]"+catPos[1]);
		Logger.println("Angle:"+turnAngle+" newAngle:"+newAngle+" catPos[2]"+catPos[2]);*/
		
		//rotate(turnAngle); // blocks while rotating
		if (turnAngle > 0) { //turn counter clockwise
			Motor.A.forward();
			Motor.C.backward();
		}
		else if (turnAngle < 0) { //turn clockwise
			Motor.A.backward();
			Motor.C.forward();
		}
		angularAcceleration(turnAngle);
		
		CatPosCalc.update();
		
		//travel(r); // blocks while moving
		if (forward) { //turn counter clockwise
			Motor.A.forward();
			Motor.C.forward();
			travelAcceleration(r);
		}
		else { //turn clockwise
			Motor.A.backward();
			Motor.C.backward();
			travelAcceleration(-r);
		}
		
		CatPosCalc.update();

		
	}
	private void angularAcceleration(float turnAngle) {
		Sound.beep(); //for debugging
		float  startAngle = super.getAngle();
		float  targetAngle = startAngle + turnAngle;
		float currentAngle;
		float deltaAngle;
		float angleEpsilon = .75f;
		float normalizeAngle = 60f;
		int maxPower = 700;
		int minPower = 300;
		int currentPower = minPower;
		int angCount = 0;

		do {
			angCount += 1;
			currentAngle = super.getAngle();
			deltaAngle = Math.min(Math.abs(currentAngle-startAngle), Math.abs(currentAngle-targetAngle));
			//maximum power after "normalizeAngle" degrees rotation, 
			//also throttle down when less than "normalizeAngle" left to go
			//Gives a linear acceleration
			currentPower = (int) (maxPower*deltaAngle/normalizeAngle);
			//power clipping:
			if (currentPower>maxPower)
				currentPower = maxPower;
			else if (currentPower<minPower)
				currentPower = minPower;
			Motor.A.setSpeed(currentPower); //0-100 input argument interval
			Motor.C.setSpeed(currentPower);
			try{Thread.sleep(10);}catch(Exception ex){}
		}
		while (Math.abs(targetAngle - currentAngle) > angleEpsilon );
		Motor.A.stop();
		Motor.C.stop();
		/*
		System.out.println("Final angle error: " + (currentAngle - targetAngle));
		System.out.println("angCount: " + angCount);
		System.out.println("Turning finished, press button");
		*/
		//Button.waitForPress();
		//try{Thread.sleep(1000);}catch(Exception ex){}
	}
	private void travelAcceleration(float r) {
		float  startDist = super.getTravelDistance();
		float  targetDist = startDist + r;
		float currentDist;
		float deltaDist;
		float distEpsilon = 0.001f;
		float normalizeDist = 0.05f;
		int maxPower = 900;
		int minPower = 200;
		int currentPower = minPower;
		int distCount = 0;

		do {
			distCount += 1;
			currentDist = super.getTravelDistance();
			deltaDist = Math.min(Math.abs(currentDist-startDist), Math.abs(currentDist-targetDist));
			//maximum power after "normalizeDist" meters traveled, 
			//also throttle down when less than "normalizeDist" meters left to go
			//Gives a linear acceleration
			currentPower = (int) (maxPower*deltaDist/normalizeDist);
			//power clipping:
			if (currentPower>maxPower)
				currentPower = maxPower;
			else if (currentPower<minPower)
				currentPower = minPower;
			Motor.A.setSpeed(currentPower); //0-900 input argument interval
			Motor.C.setSpeed(currentPower);
			try{Thread.sleep(10);}catch(Exception ex){}
		}
		while (Math.abs(targetDist - currentDist) > distEpsilon );
		Motor.A.stop();
		Motor.C.stop();
		/*
		System.out.println("Final dist error: " + (currentDist - targetDist));
		System.out.println("distCount: " + distCount);
		System.out.println("Travel finished, press button");
		*/
		//Button.waitForPress();
	}
}
