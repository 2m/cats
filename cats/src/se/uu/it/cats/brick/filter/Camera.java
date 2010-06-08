package se.uu.it.cats.brick.filter;

import java.awt.Rectangle;

import se.uu.it.cats.brick.Clock;
import se.uu.it.cats.brick.Identity;
import se.uu.it.cats.brick.Logger;
import se.uu.it.cats.brick.Main;
import se.uu.it.cats.brick.MovementPilot;
import se.uu.it.cats.brick.Settings;
import se.uu.it.cats.brick.network.ConnectionManager;
import se.uu.it.cats.brick.network.packet.SimpleMeasurement;

import lejos.nxt.Button;
import lejos.nxt.Motor;
import lejos.nxt.SensorPort;
import lejos.nxt.Sound;
import lejos.nxt.addon.NXTCam;

public class Camera implements Runnable {
	
	private static final int CLOCKWISE = -1;
	private static final int COUNTER_CLOCKWISE = 1;
	
	// iteration counter which will increase when camera is stopped when landmark is seen
	private int iterCounterSweep = 0;
	private final int maxIterCounter = 40;
	private final int maxIterToCapture = 20;
	
	private NXTCam NXTcamera = null;
	private Motor camMotor = Motor.B;
	
	private final int maxSpeed = 900;
	private final int maxSweepSpeed = 500;
	private final float motorCal = 0.978f;//1.19f; //linear calibration
	private final float gearRatio = -0.2f * motorCal; //1:5 gear down (smallest gear to biggest)
	
	private final float radPerPix = -1*(float) ((float) 43*Math.PI/180.0 / 176);
	
	private final int maxAngAbs = 180;
	private final int upperMaxAng = maxAngAbs;
	private final int lowerMaxAng = maxAngAbs * -1;
	
	private final int offset = Settings.CAMERA_OFFSET;
	
	final static int dt = 10; // milliseconds
	private Buffer unifiedBuffer;
	
	private int cyanCounter;
	private int dir = COUNTER_CLOCKWISE; //Specifies which direction to turn
	
	public static boolean doSweep = false;
	
	public Camera(Buffer unifiedBuffer) {
		this.unifiedBuffer = unifiedBuffer;
		
		NXTcamera = new NXTCam(SensorPort.S3);
		
		NXTcamera.setTrackingMode(NXTCam.OBJECT_TRACKING);
		NXTcamera.sortBy(NXTCam.SIZE);
		NXTcamera.enableTracking(true);
	}
	
	public void run() {		
		track();
	}
	
	public void track() {
		
		//one sum for each color and dimension
				
		float err = 0; //error in pixels to the mouse
		
		//PID-controller: tunable parameters
		//Tuned using manual tuning method:
		float Kp = 8.42f; //start osc. at 4
		float Ki = 0f;
		float Kd = 3f;
		float previous_error = 0;
		float integral = 0;
		float derivative;
		
		//int iterCounter = 0;		
		
		int cyanCounter = 0;
	
		while (true) {
			if (doSweep) {
				sweep();
			}
			else {
				
				// Motor angle relative to starting position
				int motorAng = getMotorAng();
				
				int numObjects = NXTcamera.getNumberOfObjects();
				
				/*LCD.clear();
				LCD.drawString(NXTcamera.getProductID(), 0, 0);
				LCD.drawString(NXTcamera.getSensorType(), 0, 1);
				LCD.drawString(NXTcamera.getVersion(), 9, 1);
				LCD.drawString(objects, 0, 2);
				LCD.drawInt(numObjects,1,9,2);*/
	
				//Logger.println("numObjects"+numObjects);
				
				boolean mouseFound = false;
	
				if (numObjects >= 1) {// && numObjects <= 8) {
					
					boolean[] foundColor = {false,false,false,false,false};
					float[] foundColorAng = new float[5];
					
					for (int i=0;i<numObjects;i++) {
						Rectangle r = NXTcamera.getRectangle(i);
						
						//calibrated color group, 0 up to 7
						int currentColor = NXTcamera.getObjectColor(i);
						
						try {					
							if (foundColor[currentColor])
								continue;
							
							//color has been found,
							//discard all forthcoming smaller objects
							foundColor[currentColor] = true;
						}
						catch (Exception ex) {
							// there was uncaught exception in this method once
							// the exception was ArrayIndexOutOfBoundsException
							// I believe it was thrown here
							Logger.println("Unknown color found:"+currentColor);
							
							continue;
						}
						
						// x-coordinate of the middle of the found color rectangle
						int xColor = r.x + r.width / 2;					
						
						float angToTarget = (xColor - 176f/2f + offset)*radPerPix;
						float angToTargetRelCat = angToTarget + degToRad(motorAng);
						
						if (currentColor == Settings.TYPE_MOUSE) {
							// set the error in pixels to the mouse
							err = xColor - 176f/2f + offset; //0-88-19=-107 worst case
							mouseFound = true;
						}
						//Logger.println("currentColor: "+currentColor+", i: " + i + ", err: "+ err);
						
						foundColorAng[currentColor] = angToTargetRelCat;
						
						/*if (r.height > 30 && r.width > 30) {
							LCD.drawInt(NXTcamera.getObjectColor(i), 2, 0, 3+i);
							LCD.drawInt(r.width, 3, 3, 3+i);
							LCD.drawInt(r.height, 3, 7, 3+i);
							LCD.drawInt(r.y, 3, 11, 3+i);
							LCD.drawInt(r.x, 3, 15, 3+i);
						}*/					
					}
					
					// if we found cyan
					if (foundColor[Settings.TYPE_CYAN]) {
						if (foundColor[Settings.TYPE_GREEN] ) {
							// found CYAN and GREEN true landmark is GREEN
							foundColor[Settings.TYPE_CYAN] = false;
							foundColorAng[Settings.TYPE_GREEN] = (foundColorAng[Settings.TYPE_GREEN] + foundColorAng[Settings.TYPE_CYAN]) / 2;
						}
						else if (foundColor[Settings.TYPE_BLUE]) {
							// found CYAN and BLUE true landmark is BLUE
							foundColor[Settings.TYPE_CYAN] = false;
							foundColorAng[Settings.TYPE_BLUE] = (foundColorAng[Settings.TYPE_BLUE] + foundColorAng[Settings.TYPE_CYAN]) / 2;
						}
					}
					else if (foundColor[Settings.TYPE_BLUE]) {
						foundColor[Settings.TYPE_PURPLE] = false;
					}
					
					int cyanWindow = 3;
					for (int i = 0; i < foundColor.length; i++) {					
						if (foundColor[i]) {
							
							boolean sendColor = true;
							
							if (i == Settings.TYPE_CYAN) {
								if (cyanCounter < cyanWindow) {
									// see cyan but not too many times to send,
									// increase counter and not send
									cyanCounter++;
									sendColor = false;
								}
								else {
									// cyan counter is large enough, send data
									cyanCounter = 0;
								}
							}
							else if (i != Settings.TYPE_MOUSE) {
								// color is not cyan neither mouse, reset cyan counter
								cyanCounter = 0;
							}
							
							if (sendColor) {
								unifiedBuffer.push(new SightingData(Clock.timestamp(), foundColorAng[i], i));
								MovementPilot.newSighting = true;
								
								// send some measurements to the GUI
								ConnectionManager.getInstance().sendPacketToAll(
										new SimpleMeasurement(i, foundColorAng[i], 0));
							}
						}
					}
					
					// send measurements to everyone
					/*try
					{
						Thread.sleep(1000);
					}
					catch (Exception e)
					{
						
					}*/
					/*ConnectionManager.getInstance().sendPacketToAll(
							new SimpleMeasurement(id, angToTargetRelCat, motorAngRad)
					);*/
					
					//Logger.println("Found at:" + (int) (angToTargetAbs*180/Math.PI));
					//Logger.println("Rel cat:" + (int) (angToTargetRelCat*180/Math.PI));
					//Logger.println("CamMotor:" + motorAng);
				}
				
				if (mouseFound)
				{
					if (Math.abs(err) < 10) //P: 10
					{
						camMotor.stop();
					}
					else
					{
						//PID-controller:
						integral=integral+err*dt;
						derivative = (err - previous_error)/dt;
						int newSpeed = (int) (Kp*err + Ki*integral + Kd*derivative);
						
						//newSpeed = (int)Math.exp(Math.abs(err) * 0.08);
						if (Math.abs(newSpeed) > maxSpeed)
							camMotor.setSpeed(maxSpeed);
						else
							camMotor.setSpeed(Math.abs(newSpeed));
						
						if (newSpeed < 0) {
							camMotor.backward();
							//dir=1;
						}
						else {
							camMotor.forward();
							//dir=-1;
						}
						if (err < 0)
							dir = COUNTER_CLOCKWISE;
						else
							dir = CLOCKWISE;
						//Stop if motor is at maximum turning angle
						if (motorAng>upperMaxAng) {
							camMotor.stop();
						}
						if (motorAng<lowerMaxAng) {
							camMotor.stop();
						}	
					}
					
					//Logger.println("x: "+xAvg+" err:"+err+" numObj:"+numObjects+" speed:"+camMotor.getSpeed());
					
					/*LCD.drawInt(xAvg, 3, 0, 5);
					LCD.drawInt(yAvg, 3, 4, 5);
					LCD.drawInt(err, 3, 8, 5);*/
				}
				else {
					//Logger.println("No found!");
					//Sound.beep(); //Beep if no target is found
					camMotor.setSpeed(maxSpeed); //Search for mouse with maximum speed
					
					//Reverse motor direction if at maximum turning angle
					if (motorAng > upperMaxAng) {
						dir = CLOCKWISE;
					}
					if (motorAng < lowerMaxAng) {
						dir = COUNTER_CLOCKWISE;
					}
					
					changeDirection(dir);
				}
	
				//LCD.refresh();
				try{Thread.sleep(dt);}catch(Exception ex){}
				
				//iterCounter = (iterCounter + 1) % 100;
			}
		}//end of while(true)
	}
	
	public void sweep() {
		
		boolean[] seenLandmarks = new boolean[] {false, false, false, false, false};
		
		camMotor.stop();
		
		int startAngle = getMotorAng();
		camMotor.setSpeed(maxSweepSpeed); //Search for landmarks with maximum speed
		
		// first go to the nearest border
		if (startAngle > 0) {
			dir = COUNTER_CLOCKWISE;
			changeDirection(dir);
		}
		else {
			dir = CLOCKWISE;
			changeDirection(dir);
		}
		
		boolean firstTurnMade = false;
		boolean secondTurnMade = false;
		int motorAng = startAngle;
		// do full sweep and return to the starting position
		while (!firstTurnMade || !secondTurnMade || Math.abs(motorAng - startAngle) > 5) {
				
			motorAng = getMotorAng();
			
			//Reverse motor direction if at maximum turning angle
			if (motorAng > upperMaxAng) {
				dir = CLOCKWISE;
				changeDirection(dir);
				firstTurnMade = true;
			}
			if (motorAng < lowerMaxAng) {
				dir = COUNTER_CLOCKWISE;
				changeDirection(dir);
				secondTurnMade = true;
			}
			
			int landmark = checkForLandmark(motorAng, seenLandmarks);
			if (landmark != -1)
				seenLandmarks[landmark] = true;
		}
		
		camMotor.stop();
		
		doSweep = false;
	}
	
	public int getMotorAng() {
		return (int)(camMotor.getTachoCount() * gearRatio);
	}
	
	public float degToRad(int deg) {
		return (float)(deg * Math.PI/180.0);
	}
	
	// change direction to:	
	public void changeDirection(int dir) {
		if (dir == COUNTER_CLOCKWISE) {
			camMotor.backward();
		}
		else {
			camMotor.forward();                                                                                                                                                                                                                                             
		}
	}
	
	public int checkForLandmark(int motorAng, boolean[] seenLandmarks) {
		int landmarkToReturn = -1;
		
		iterCounterSweep++;
		// start moving is not moving longer than max iterations
		if (!camMotor.isMoving() && iterCounterSweep > maxIterCounter)
			changeDirection(dir);
		
		int numObjects = NXTcamera.getNumberOfObjects();
		
		if (numObjects >= 1) {// && numObjects <= 8) {
			
			boolean[] foundColor = {false,false,false,false,false};
			float[] foundColorAng = new float[5];
			
			for (int i=0;i<numObjects;i++) {
				Rectangle r = NXTcamera.getRectangle(i);
				
				//calibrated color group, 0 up to 7
				int currentColor = NXTcamera.getObjectColor(i);
				
				if (currentColor == Settings.TYPE_MOUSE) {
					// skip the mouse
					continue;
				}
				
				try {					
					if (foundColor[currentColor])
						continue;
					
					//color has been found,
					//discard all forthcoming smaller objects
					foundColor[currentColor] = true;
				}
				catch (Exception ex) {
					// there was uncaught exception in this method once
					// the exception was ArrayIndexOutOfBoundsException
					// I believe it was thrown here
					Logger.println("Unknown color found:"+currentColor);
					
					continue;
				}
				
				// x-coordinate of the middle of the found color rectangle
				int xColor = r.x + r.width / 2;					
				
				float angToTarget = (xColor - 176f/2f + offset)*radPerPix;
				float angToTargetRelCat = angToTarget + degToRad(motorAng);				
				
				foundColorAng[currentColor] = angToTargetRelCat;
			}
			
			// if we found cyan
			if (foundColor[Settings.TYPE_CYAN]) {
				if (foundColor[Settings.TYPE_GREEN] ) {
					// found CYAN and GREEN true landmark is GREEN
					foundColor[Settings.TYPE_CYAN] = false;
					foundColorAng[Settings.TYPE_GREEN] = (foundColorAng[Settings.TYPE_GREEN] + foundColorAng[Settings.TYPE_CYAN]) / 2;
				}
				else if (foundColor[Settings.TYPE_BLUE]) {
					// found CYAN and BLUE true landmark is BLUE
					foundColor[Settings.TYPE_CYAN] = false;
					foundColorAng[Settings.TYPE_BLUE] = (foundColorAng[Settings.TYPE_BLUE] + foundColorAng[Settings.TYPE_CYAN]) / 2;
				}
			}
			else if (foundColor[Settings.TYPE_BLUE]) {
				foundColor[Settings.TYPE_PURPLE] = false;
			}
			
			int cyanWindow = 3;
			for (int i = 0; i < foundColor.length; i++) {					
				if (foundColor[i]) {
					
					boolean sendColor = true;
					
					if (i == Settings.TYPE_CYAN) {
						if (cyanCounter < cyanWindow) {
							// see cyan but not too many times to send,
							// increase counter and not send
							cyanCounter++;
							sendColor = false;
						}
						else {
							// cyan counter is large enough, send data
							cyanCounter = 0;
						}
					}
					else if (i != Settings.TYPE_MOUSE) {
						// color is not cyan neither mouse, reset cyan counter
						cyanCounter = 0;
					}
					
					if (seenLandmarks[i]) {
						// skip all seen landmarks
						continue;
					}
					
					if (sendColor) {
						if (camMotor.isMoving()) {
							camMotor.stop();
							iterCounterSweep = 0;
						}
						else {
							if (iterCounterSweep > maxIterToCapture) {
								unifiedBuffer.push(new SightingData(Clock.timestamp(), foundColorAng[i], i));
								
								// send some measurements to the GUI
								ConnectionManager.getInstance().sendPacketToAll(
										new SimpleMeasurement(i, foundColorAng[i], 0));
								
								changeDirection(dir);
								
								landmarkToReturn = i;
							}
						}
					}
				}
			}
		}
		
		return landmarkToReturn;
	}
}
