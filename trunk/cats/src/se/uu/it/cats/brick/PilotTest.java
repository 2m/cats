package se.uu.it.cats.brick;

import lejos.nxt.Motor;
import lejos.robotics.navigation.Pilot;
import lejos.robotics.navigation.TachoPilot;

public class PilotTest
{
	public void run()
	{
		 Pilot pilot = new TachoPilot(56.0f, 56.0f, 112.0f, Motor.A, Motor.C, true);  // parameters in millimeters
		 
		 pilot.setMoveSpeed(100);
		 pilot.travel(-12, true);
		 
		 while(pilot.isMoving())Thread.yield();
		 
		 for (int i = 0; i < 9; i++)
			 pilot.rotate(-90);		 
		
		 pilot.steer(-50, 180, true);
		 
		 while(pilot.isMoving())Thread.yield();
		 
		 pilot.steer(100);
		 try{Thread.sleep(1000);}
		 catch(InterruptedException e){}
		 pilot.stop();
	}
}
