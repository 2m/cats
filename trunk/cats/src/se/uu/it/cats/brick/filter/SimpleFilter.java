package se.uu.it.cats.brick.filter;

import java.awt.Rectangle;

import se.uu.it.cats.brick.Logger;
import se.uu.it.cats.brick.network.ConnectionManager;
import se.uu.it.cats.brick.network.packet.SimpleMeasurement;

import lejos.nxt.Button;
import lejos.nxt.Motor;
import lejos.nxt.SensorPort;
import lejos.nxt.Sound;
import lejos.nxt.addon.NXTCam;
import lejos.nxt.comm.RConsole;

public class SimpleFilter implements Runnable {
	
	final static int INTERVAL = 1; // milliseconds
	
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
		
		//int iterCounter = 0;
		int maxSpeed = 200;
		int dir = 1; //Specifies which direction to turn, if the mouse is lost. Default is clockwise.
		int motorAng; //Motor angle relative to starting position
		float motorAngRad;
		float angToTargetRelCat;
		int maxAngAbs = 180;
		int upperMaxAng = maxAngAbs;
		int lowerMaxAng = maxAngAbs*-1;
		//int offset = -7; //for camera on the Karl-Gösta cat.
		int offset = -19; //for camera on cat2
		float radPerPix = -1*(float) ((float) 43*Math.PI/180.0 / 176);
	
		while(!Button.ESCAPE.isPressed()) {
			
			motorAng = Motor.A.getTachoCount();
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
			//int xAvg;
			//int err;
			//float angToTarget;
			
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
				//int yAvg = ySum / numObjects; //never used
				
				err = xAvg - 176 / 2 + offset;
				angToTarget = err*radPerPix;
				angToTargetRelCat = motorAngRad + angToTarget;
				
				// send measurements to everyone
				ConnectionManager.getInstance().sendPacketToAll(
						new SimpleMeasurement(angToTargetRelCat)
				);
				
				Logger.println("Found at:" + (int) (angToTargetRelCat*180/Math.PI));
				Logger.println("            CamMotor:" + motorAng);
				
				if (Math.abs(err) < 10)
					Motor.A.stop();
				else
				{
					int newSpeed = (int)Math.exp(Math.abs(err) * 0.08);
					if (newSpeed < maxSpeed)
						Motor.A.setSpeed(newSpeed);
					
					if (err > 0) {
						Motor.A.backward();
						dir=-1;
					}
					else {
						Motor.A.forward();
						dir=1;
					}
					//Stop if motor is at maximum turning angle
					if (motorAng>upperMaxAng) {
						Motor.A.stop();
					}
					if (motorAng<lowerMaxAng) {
						Motor.A.stop();
					}	
				}
				
				//Logger.println("x: "+xAvg+" err:"+err+" numObj:"+numObjects+" speed:"+Motor.A.getSpeed());
				
				/*LCD.drawInt(xAvg, 3, 0, 5);
				LCD.drawInt(yAvg, 3, 4, 5);
				LCD.drawInt(err, 3, 8, 5);*/
			}
			else {
				RConsole.println("No found!");
				Sound.beep(); //Beep if no target is found
				Motor.A.setSpeed(maxSpeed); //Search for mouse with maximum speed
				
				//Reverse motor direction if at maximum turning angle
				if (motorAng>upperMaxAng) {
					dir=-1;
				}
				if (motorAng<lowerMaxAng) {
					dir=1;
				}
				
				if (dir==1) {
					Motor.A.forward();
				}
				else {
					Motor.A.backward();
				}
			}

			//LCD.refresh();
			//Thread.sleep(INTERVAL);
			
			//iterCounter = (iterCounter + 1) % 100;
		}
	}
}
