package se.uu.it.cats.brick;

import se.uu.it.cats.brick.filter.BufferSorted;
import se.uu.it.cats.brick.filter.MovementData;
import lejos.nxt.Button;
import lejos.nxt.Motor;
import lejos.nxt.Sound;
import lejos.robotics.navigation.TachoPilot;

public class MovementPilot extends TachoPilot {
	//Instance variables
	private float startAng;
	private float distLast;
	private float dx;
	private float dy;
	private float ang;
	private boolean addedMovementSinceLastPush = false;
	private int lastTime;
	
	public static float lastR;
	public static float lastAngle;
	
	private static MovementPilot _instanceHolder = new MovementPilot();
	
	public static MovementPilot getInstance()
	{
		return _instanceHolder;
	} 
	
	private MovementPilot()
	{	
		super(Settings.WHEEL_DIAMATER - Settings.DRIFT_BALANCE,Settings.WHEEL_DIAMATER + Settings.DRIFT_BALANCE, Settings.TRACK_WIDTH, Motor.C, Motor.A, false);
		
		lastTime = Clock.timestamp();
	}	
	
	public void travel(float x, float y){
		
		float[] catPos = CatPosCalc.getCatPos();
		
		float deltaX = x-catPos[0];
		System.out.println("deltaX: " + deltaX);
		float deltaY = y-catPos[1];
		System.out.println("deltaY: " + deltaY);
		float r = (float) Math.sqrt(deltaX*deltaX + deltaY*deltaY);
		float newAngle = (float) Math.atan2(deltaY,deltaX);
		System.out.println("newAngle: " + newAngle*180f/Math.PI);
		
		float turnAngle = (float) ((newAngle - catPos[2]) % (Math.PI*2f));  //TODO: not 0 when it should!
		//System.out.println("turnAngle before: " + turnAngle*180f/Math.PI);
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
			_right.forward();
			_left.backward();
		}
		else if (turnAngle < 0) { //turn clockwise
			Motor.A.backward();
			Motor.C.forward();
		}
		angularAcceleration(turnAngle);
		update();	
		
		//travel(r); // blocks while moving
		if (forward) { //turn counter clockwise
			_right.forward();
			_left.forward();
			travelAcceleration(r);
		}
		else { //turn clockwise
			_right.backward();
			_left.backward();
			travelAcceleration(-r);
		}
		update();
		
	}
	
	//old version
	private void update() {
		CatPosCalc.update();
	}
	
	//new version
	/*private void update() {
	 	if (Settings.POSITIONING_FILTER_UNSCENTED_KALMAN)
		    addMovementSinceLastPush();
		else if (Settings.POSITIONING_FILTER_PARTICLE)
			;
			//push to particle filter
		else if (Settings.POSITIONING_FILTER_BASIC)
			//push to basic filter	
			;
		else
			System.out.println("No positioning filter selected");
	}*/
	
	public void addMovementSinceLastPush() {

		float distNew = this.getTravelDistance();
		ang = (float) ((startAng + getAngle()*Math.PI/180f) % (2f*Math.PI));
		
		float deltaDist = distNew - distLast;
		
		dx += deltaDist * (float) Math.cos(ang);
		dy +=  deltaDist * (float) Math.sin(ang);

		distLast = distNew;
		addedMovementSinceLastPush = true;
	}
	
	/**
	 * Returns the velocity since the last call (to the UKF positioning filter, 
	 * this should be done before each filter iteration. Called by the filter.)
	 * @return MovementData currentTime,vx,vy
	 */
	public MovementData getVelocity() {
		
		//shouldn't be needed, we always need to update
		//if no new motor command was issued
		
		//if (!addedMovementSinceLastPush)
		addMovementSinceLastPush();	
			
		int currentTime = Clock.timestamp();
		double dt = (currentTime - lastTime)*1000; //elapsed time in seconds
		double vx=dx/dt;
		double vy=dy/dt;
		
		lastTime = currentTime;
		dx=0;
		dy=0;
		addedMovementSinceLastPush = false;
		
		return new MovementData(currentTime,vx,vy);
	}
	
	
	/**
	 * Pushes tachometer data (since last call) to the buffer, used by the absolute particle filter
	 */
	public void pushParticleMovementData() {

		float newR = getTravelDistance();
		float newAngle = (float) (getAngle() * Math.PI/180f);
		MovementData MD = new MovementData(Clock.timestamp(), newR - lastR, newAngle - lastAngle);
		Main.positioningFilter.getUnifiedBuffer().push(MD);
		lastR = newR;
		lastAngle = newAngle;
	}
	
	public float getdX() {
		return dx;
	}
	
	public float getdY() {
		return dy;
	}
	
	private void angularAcceleration(float turnAngle) {
		Sound.beep(); //for debugging
		float  startAngle = this.getAngle();
		float  targetAngle = startAngle + turnAngle;
		float currentAngle;
		float deltaAngle;
		float angleEpsilon = 5*.75f;
		float normalizeAngle = 22.5f;
		float maxPower = this.getTurnMaxSpeed()/3f;
		float minPower = maxPower/3f;
		float currentPower = minPower;
		int angCount = 0;

		do {
			angCount += 1;
			currentAngle = this.getAngle();
			deltaAngle = Math.min(Math.abs(currentAngle-startAngle), Math.abs(currentAngle-targetAngle));
			//maximum power after "normalizeAngle" degrees rotation, 
			//also throttle down when less than "normalizeAngle" left to go
			//Gives a linear acceleration
			currentPower = maxPower*deltaAngle/normalizeAngle;
			//power clipping:
			if (currentPower>maxPower)
				currentPower = maxPower;
			else if (currentPower<minPower)
				currentPower = minPower;
			this.setTurnSpeed(currentPower); //0-100 input argument interval
			try{Thread.sleep(50);}catch(Exception ex){}
		}
		while (Math.abs(targetAngle - currentAngle) > angleEpsilon );
		_right.stop();
		_left.stop();
		/*
		System.out.println("Final angle error: " + (currentAngle - targetAngle));
		System.out.println("angCount: " + angCount);
		System.out.println("Turning finished, press button");
		*/
		//Button.waitForPress();
		//try{Thread.sleep(1000);}catch(Exception ex){}
	}
	private void travelAcceleration(float r) {
		float  startDist = this.getTravelDistance();
		float  targetDist = startDist + r;
		float currentDist;
		float deltaDist;
		float distEpsilon = 5*0.001f;
		float normalizeDist = 0.05f;
		float maxPower = this.getMoveMaxSpeed();
		float minPower = maxPower*.2f;
		float currentPower = minPower;
		int distCount = 0;
		boolean atMaxPower = false;

		do {
			distCount += 1;
			currentDist = this.getTravelDistance();
			deltaDist = Math.min(Math.abs(currentDist-startDist), Math.abs(currentDist-targetDist));
			//maximum power after "normalizeDist" meters traveled, 
			//also throttle down when less than "normalizeDist" meters left to go
			//Gives a linear acceleration
			currentPower = maxPower*deltaDist/normalizeDist;
			//power clipping:
			if (currentPower > maxPower) {
				currentPower = maxPower;  
				if (!atMaxPower) {
					this.setMoveSpeed(currentPower); //0-900 input argument interval
					atMaxPower = true;
				}
			}
			else {
				if (currentPower < minPower)
					currentPower = minPower;
				
				this.setMoveSpeed(currentPower); //0-900 input argument interval
			}
			
			try{Thread.sleep(50);}catch(Exception ex){}
		}
		while (Math.abs(targetDist - currentDist) > distEpsilon );
		_right.stop();
		_left.stop();
		/*
		System.out.println("Final dist error: " + (currentDist - targetDist));
		System.out.println("distCount: " + distCount);
		System.out.println("Travel finished, press button");
		*/
		//Button.waitForPress();
	}
	public void setTurnSpeed(float speed) {
		_robotTurnSpeed = speed;
		setSpeed(Math.round(speed * _leftTurnRatio), Math.round(speed
				* _rightTurnRatio));
	}
	
	public void setMoveSpeed(float speed) {
		_robotMoveSpeed = speed;
		_motorSpeed = Math.round(0.5f * speed
				* (_leftDegPerDistance + _rightDegPerDistance));
		setSpeed(Math.round(speed * _leftDegPerDistance), Math.round(speed
				* _rightDegPerDistance));
	}
	
	private void setSpeed(final int leftSpeed, final int rightSpeed) {
		_left.setSpeed(leftSpeed);
		_right.setSpeed(rightSpeed);
	}
	
	
}
