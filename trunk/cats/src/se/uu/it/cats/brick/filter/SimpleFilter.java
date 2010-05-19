package se.uu.it.cats.brick.filter;

import java.awt.Rectangle;

import se.uu.it.cats.brick.CatPosCalc;
import se.uu.it.cats.brick.Identity;
import se.uu.it.cats.brick.Logger;
import se.uu.it.cats.brick.Settings;
import se.uu.it.cats.brick.network.ConnectionManager;
import se.uu.it.cats.brick.network.packet.SimpleMeasurement;

import lejos.nxt.Button;
import lejos.nxt.Motor;
import lejos.nxt.SensorPort;
import lejos.nxt.Sound;
import lejos.nxt.addon.NXTCam;

public class SimpleFilter implements Runnable {
	
	final static int dt = 10; // milliseconds
	
	public void run() {
		
		//Motor.A.setSpeed(100);
		
		NXTCam camera = new NXTCam(SensorPort.S3);
		//String objects = "Objects: ";
		int numObjects;
		
		//camera.sendCommand('A'); // sort objects by size
		//camera.sendCommand('E'); // start tracking
		
		camera.setTrackingMode(NXTCam.OBJECT_TRACKING);
		camera.sortBy(NXTCam.SIZE);
		camera.enableTracking(true);
		
		int xSum;
		int ySum ;
		int xAvg;
		int err;
		float angToTarget;
		//PID-controller: tunable parameters
		//Tuned using manual tuning method:
		float Kp = 8.42f; //start osc. at 4
		float Ki = 0f;
		float Kd = 3f;
		int previous_error = 0;
		int integral = 0;
		float derivative;
		
		//int iterCounter = 0;
		int maxSpeed = 900;
		int newSpeed;
		int dir = 1; //Specifies which direction to turn, if the mouse is lost. Default is clockwise.
		int motorAng; //Motor angle relative to starting position
		float motorAngRad;
		float angToTargetRelCat;
		float angToTargetAbs;
		float motorCal = 0.978f;//1.19f; //linear calibration
		float gearRatio = -0.2f*motorCal; //1:5 gear down (smallest gear to biggest)
		int maxAngAbs = 180;
		int upperMaxAng = maxAngAbs;
		int lowerMaxAng = maxAngAbs*-1;
		
		int offset = Settings.CAMERA_OFFSET;
		
		float radPerPix = -1*(float) ((float) 43*Math.PI/180.0 / 176);
	
		while(!Button.ESCAPE.isPressed()) {
			
			motorAng = (int) (Motor.B.getTachoCount()*gearRatio);
			motorAngRad = (float) (motorAng * Math.PI/180.0);
			numObjects = camera.getNumberOfObjects();
			
			/*LCD.clear();
			LCD.drawString(camera.getProductID(), 0, 0);
			LCD.drawString(camera.getSensorType(), 0, 1);
			LCD.drawString(camera.getVersion(), 9, 1);
			LCD.drawString(objects, 0, 2);
			LCD.drawInt(numObjects,1,9,2);*/
			
			xSum = 0;
			ySum = 0;
			
			if (numObjects >= 1) {// && numObjects <= 8) {
				for (int i=0;i<numObjects;i++) {
					Rectangle r = camera.getRectangle(i);
					
					xSum += r.x + r.width / 2;
					ySum += r.y - r.height / 2;
					
					/*if (r.height > 30 && r.width > 30) {
						LCD.drawInt(camera.getObjectColor(i), 2, 0, 3+i);
						LCD.drawInt(r.width, 3, 3, 3+i);
						LCD.drawInt(r.height, 3, 7, 3+i);
						LCD.drawInt(r.y, 3, 11, 3+i);
						LCD.drawInt(r.x, 3, 15, 3+i);
					}*/
					
				}
				
				xAvg = xSum / numObjects;
				err = xAvg - 176/2 + offset; //0-88-19=-107 worst case
				angToTarget = err*radPerPix;
				angToTargetRelCat = motorAngRad + angToTarget;
				angToTargetAbs = angToTargetRelCat + CatPosCalc.getCatAng();
				System.out.println("CamMotor:" + motorAng);
				
				// send measurements to everyone
				/*try
				{
					Thread.sleep(1000);
				}
				catch (Exception e)
				{
					
				}*/
				/*ConnectionManager.getInstance().sendPacketToAll(
						new SimpleMeasurement(angToTargetRelCat)
				);*/
				
				//Logger.println("Found at:" + (int) (angToTargetAbs*180/Math.PI));
				//Logger.println("Rel cat:" + (int) (angToTargetRelCat*180/Math.PI));
				Logger.println("CamMotor:" + motorAng);
				
				if (Math.abs(err) < 0) //P: 10
				{
					Motor.B.stop();
				}
				else
				{
					//PID-controller:
					integral=integral+err*dt;
					derivative = (err - previous_error)/dt;
					newSpeed = (int) (Kp*err + Ki*integral + Kd*derivative);
					
					//newSpeed = (int)Math.exp(Math.abs(err) * 0.08);
					if (Math.abs(newSpeed) > maxSpeed)
						Motor.B.setSpeed(maxSpeed);
					else
						Motor.B.setSpeed(Math.abs(newSpeed));
					
					if (newSpeed < 0) {
						Motor.B.backward();
						//dir=1;
					}
					else {
						Motor.B.forward();
						//dir=-1;
					}
					if (err < 0)
						dir=1;
					else
						dir=-1;
					//Stop if motor is at maximum turning angle
					if (motorAng>upperMaxAng) {
						Motor.B.stop();
					}
					if (motorAng<lowerMaxAng) {
						Motor.B.stop();
					}	
				}
				
				//Logger.println("x: "+xAvg+" err:"+err+" numObj:"+numObjects+" speed:"+Motor.B.getSpeed());
				
				/*LCD.drawInt(xAvg, 3, 0, 5);
				LCD.drawInt(yAvg, 3, 4, 5);
				LCD.drawInt(err, 3, 8, 5);*/
			}
			else {
				//Logger.println("No found!");
				Sound.beep(); //Beep if no target is found
				Motor.B.setSpeed(maxSpeed); //Search for mouse with maximum speed
				
				//Reverse motor direction if at maximum turning angle
				if (motorAng>upperMaxAng) {
					dir=-1;
				}
				if (motorAng<lowerMaxAng) {
					dir=1;
				}
				
				if (dir==1) {
					Motor.B.backward();
				}
				else {
					Motor.B.forward();                                                                                                                                                                                                                                             
				}
			}

			//LCD.refresh();
			try{Thread.sleep(dt);}catch(Exception ex){}
			
			//iterCounter = (iterCounter + 1) % 100;
		}
	}
}
