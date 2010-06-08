package se.uu.it.cats.brick;

import java.io.File;
import java.util.Random;

import lejos.nxt.Button;
import lejos.nxt.ButtonListener;
import lejos.nxt.LCD;
import lejos.nxt.Sound;
import lejos.nxt.comm.RConsole;
import lejos.util.Timer;
import lejos.util.TimerListener;
import se.uu.it.cats.brick.filter.*;
import se.uu.it.cats.brick.network.*;
import se.uu.it.cats.brick.network.packet.AbsolutePositionUpdate;
import se.uu.it.cats.brick.network.packet.LatestSightingUpdate;
import se.uu.it.cats.brick.network.packet.MeanAndCovarianceUpdate;
import se.uu.it.cats.brick.network.packet.MoveOrder;
import se.uu.it.cats.brick.storage.BillBoard;
import se.uu.it.cats.brick.storage.StorageManager;

/**
 * Main file to run the bearings only project
 * 
 */
public class Main {
	private static Buffer unifiedBuffer;
	private static MovementPilot movementPilot;
	private static AbsolutePositioningFilter positioningFilter;
	private static TrackingFilter trackingFilter;
	private static Guide guide;

	public static void main(String[] args) throws InterruptedException {
		Settings.init(Identity.getId());
		Logger.init();
		Clock.init();

		// connection listener thread
		Thread listenerThread = new Thread(new ConnectionListener());
		listenerThread.start();

		// wait until connection from the GUI is established
		while (!ConnectionManager.getInstance().isAlive(
				ConnectionManager.INBOUND_CONN_ID)) {
			try {
				Thread.sleep(100);
			} catch (Exception ex) {
			}
		}

		// wait until time is synced
		Clock.blockUntilSynced();

		boolean startYourEngines = true;
		if (startYourEngines) {
			unifiedBuffer = new BufferSorted();

			// run camera thread
			Thread cameraThread = new Thread(new Camera(unifiedBuffer));
			cameraThread.start();

			movementPilot = new MovementPilot(unifiedBuffer);
			Thread movementThread = new Thread(movementPilot);
			movementThread.start();
				
			if (Settings.USE_POSITIONING_PARTICLE_FILTER) {
				positioningFilter = new AbsolutePositioningParticleFilter(Identity.getId(), 
						Settings.N_POSITIONING, (float) Settings.PERIOD_POSITIONING_PARTICLE / 1000f,
						unifiedBuffer, BillBoard.getInstance());
			} else if (Settings.USE_POSITIONING_GEOMETRIC_FILTER) {
				positioningFilter = new AbsolutePositioningGeometricFilter(Identity.getId(),
						(float) Settings.PERIOD_POSITIONING_GEOMETRIC / 1000f, unifiedBuffer,
						BillBoard.getInstance());
			} else {
				positioningFilter = new AbsolutePositioningNaiveFilter(Identity.getId(),
						(float) Settings.PERIOD_POSITIONING_NAIVE / 1000f, unifiedBuffer,
						BillBoard.getInstance());
			}
			positioningFilter.initData(Settings.START_X, Settings.START_Y, Settings.START_ANGLE);
			Thread positioningFilterThread = new Thread(positioningFilter);
			positioningFilterThread.start();
			/*else if (Settings.USE_POSITIONING_UNSCENTED_KALMAN_FILTER) {
			positioningFilter = new AbsolutePositioningUKF(Identity.getId(),
					(float) Settings.PERIOD_POSITIONING_KALMAN / 1000f, unifiedBuffer,
					BillBoard.getInstance());
			}*/


			if (Settings.USE_TRACKING_PARTICLE_FILTER) {
				trackingFilter = new TrackingParticleFilter(Identity.getId(), 
						Settings.N_TRACKING, (float) Settings.PERIOD_TRACKING_PARTICLE / 1000f,
						BillBoard.getInstance());
			} else if (Settings.USE_TRACKING_UNSCENTED_KALMAN_FILTER) {
				trackingFilter = new TrackingUnscentedKalmanFilter(Identity.getId(),
						(float) Settings.PERIOD_TRACKING_KALMAN / 1000f, 
						BillBoard.getInstance());
			}
			Thread trackingFilterThread = new Thread(trackingFilter);
			trackingFilterThread.start();
			
			// init guide
			guide = new Guide(Identity.getId(), BillBoard.getInstance());
		}

		while (!startYourEngines) {
			ConnectionManager.getInstance().sendPacketToAll(
					new LatestSightingUpdate(0f, 0f, 0f, Clock.timestamp()));

			// ConnectionManager.getInstance().sendPacketToAll(new
			// AbsolutePositionUpdate(0f, 0f, 0f, Clock.timestamp()));

			try {
				Thread.sleep(20);
			} catch (Exception ex) {
			}
		}

		Button.LEFT.addButtonListener(new ButtonListener() {
			public void buttonPressed(Button b) {
			}

			public void buttonReleased(Button b) {
				Sound.playSample(new File("phaser.wav"), 100);
			}
		});

		Button.ENTER.addButtonListener(new ButtonListener() {
			public void buttonPressed(Button b) {
			}

			public void buttonReleased(Button b) {
				billboardTest();
				travelTest();
			}
		});

		Button.RIGHT.addButtonListener(new ButtonListener() {
			public void buttonPressed(Button b) {
			}

			public void buttonReleased(Button b) {

				// Music m = new Music(Identity.getId(), 3);
				// m.play();

			}
		});

		Button.ESCAPE.addButtonListener(new ButtonListener() {
			public void buttonPressed(Button b) {
			}

			public void buttonReleased(Button b) {
				// Clock.syncTime();
			}
		});

		System.out.println("Buttons:");
		System.out.println("L - phaser");
		System.out.println("C - travelTest");
		System.out.println("R - music.play()");
		System.out.println("D - sync time");
		// Button.waitForPress();
		// Thread.sleep(2000);

		int test = 0;
		while (test == 0) {

			int milisUntilNextSec = 2000 - (Clock.timestamp() % 2000);
			Thread.sleep(milisUntilNextSec);

			/*Logger.println("--- Latest sightings ---");
			float[] s = BillBoard.getInstance().getLatestSightings();
			for (int i = 0; i < BillBoard.getInstance().getNoCats(); i++) {
				Logger.println("id: " + i + ", x:" + s[i * 4 + 0] + ", y:"
						+ s[i * 4 + 1] + ", th:" + s[i * 4 + 2]);
			}

			Logger.println("--- Absolute Positions ---");
			float[] p = BillBoard.getInstance().getAbsolutePositions();
			for (int i = 0; i < BillBoard.getInstance().getNoCats(); i++) {
				Logger.println("id: " + i + ", x:" + p[i * 4 + 0] + ", y:"
						+ p[i * 4 + 1] + ", th:" + p[i * 4 + 2]);
			}*/
			
			if (Settings.USE_GUIDE)
			{
				float[] advice = guide.getAdvice();
				if (!movementPilot.isProcessing() && advice[0] != -1) {
					movementPilot.travel(advice[0], advice[1], positioningFilter.getX(), positioningFilter.getY(), positioningFilter.getAngle());
					ConnectionManager.getInstance().sendPacketToAll(new MoveOrder(advice[0], advice[1]));
				}
			}
			else if (!Settings.GUI_ORDER_PROCESSED)
			{
				movementPilot.travel(Settings.GUI_ORDER_X, Settings.GUI_ORDER_Y, positioningFilter.getX(), positioningFilter.getY(), positioningFilter.getAngle());
				Settings.GUI_ORDER_PROCESSED = true;
			}

			// Logger.println("Buffer size:"+unifiedBuffer.getLength());
			// Thread.sleep(100);
			// Thread.yield();
			Sound.beep();
		}

		if (RConsole.isOpen())
			RConsole.close();
	}

	public static void travelTest() {

		// movementPilot.travel( 0, 3f, 0, 0f, (float)Math.PI/2f);
		// while (movementPilot.isProcessing()) { Thread.yield(); }

		for (int i = 0; i < 4; i++) { // turn in square
			movementPilot.travel(0.05f, 0f, 0, 0f, 0);
			while (movementPilot.isProcessing()) {
				Thread.yield();
			}
			// Button.waitForPress();

			movementPilot.travel(0.05f, 0.05f, 0.05f, 0, 0);
			while (movementPilot.isProcessing()) {
				Thread.yield();
			}
			// Button.waitForPress();

			movementPilot.travel(-0.05f, 0.05f, 0.05f, 0.05f,
					(float) Math.PI / 2f);
			while (movementPilot.isProcessing()) {
				Thread.yield();
			}
			// Button.waitForPress();

			movementPilot.travel(-0.05f, 0f, -0.05f, 0.05f, (float) Math.PI);
			while (movementPilot.isProcessing()) {
				Thread.yield();
			}
			// Button.waitForPress();

			movementPilot.travel(0, 0f, -0.05f, 0f, (float) Math.PI / 2f * 3);
			while (movementPilot.isProcessing()) {
				Thread.yield();
			}
			// Button.waitForPress();

			System.out.println("BIG SQUARE FINISHED!");
		}

		// the current hardcoded positions have to be changed with the
		// approximated
		// positions from the positioning filter
		for (int i = 0; i < 4; i++) { // turn in square
			movementPilot.travel(0.75f, 0f, 0, 0f, (float) Math.PI / 2f * 3);
			while (movementPilot.isProcessing()) {
				Thread.yield();
			}

			movementPilot.travel(0.75f, 0.75f, 0.75f, 0, 0);
			while (movementPilot.isProcessing()) {
				Thread.yield();
			}

			movementPilot.travel(0f, 0.75f, 0.75f, 0.75f, (float) Math.PI / 2f);
			while (movementPilot.isProcessing()) {
				Thread.yield();
			}

			movementPilot.travel(0f, 0f, 0, 0.75f, (float) Math.PI);
			while (movementPilot.isProcessing()) {
				Thread.yield();
			}

			System.out.println("SQUARE FINISHED!");
		}

		/*
		 * mPilot.travel(0f,.2f); mPilot.travel(0f,.4f); mPilot.travel(0f,.6f);
		 * mPilot.travel(0f,.8f); mPilot.travel(0f,1f); mPilot.travel(0f,1.2f);
		 * mPilot.travel(0f,1.4f); mPilot.travel(0f,1.6f);
		 * mPilot.travel(0f,1.8f); mPilot.travel(0f,2f); mPilot.travel(0f,2.2f);
		 * mPilot.travel(0f,2.4f); mPilot.travel(0f,2.6f);
		 * mPilot.travel(0f,2.8f); mPilot.travel(0f,3f);
		 */

		// drive from (0,0) to (0,3) to (-1,4) to (-1,5) i steps
		/*
		 * mPilot.travel(0f,.5f); mPilot.travel(0f,1f); mPilot.travel(0f,1.5f);
		 * mPilot.travel(0f,2.5f); mPilot.travel(0f,3f);
		 * mPilot.travel(-.5f,3.5f); mPilot.travel(-1f,4f);
		 * mPilot.travel(-1f,4.5f); mPilot.travel(-1f,5f);
		 */
	}

	public static void billboardTest() {

		// if (Identity.getId() == 2) {
		// BillBoard.getInstance().setMeanAndCovariance(Identity.getId(),
		// Clock.timestamp(), Clock.timestamp(), Clock.timestamp(),
		// Clock.timestamp(), Clock.timestamp(), Clock.timestamp(),
		// Clock.timestamp(), Clock.timestamp(), Clock.timestamp(),
		// Clock.timestamp(), Clock.timestamp());
		// MeanAndCovarianceUpdate macu = new
		// MeanAndCovarianceUpdate(Clock.timestamp(), Clock.timestamp(),
		// Clock.timestamp(), Clock.timestamp(), Clock.timestamp(),
		// Clock.timestamp(), Clock.timestamp(), Clock.timestamp(),
		// Clock.timestamp(), Clock.timestamp(), Clock.timestamp(),
		// Clock.timestamp());
		// ConnectionManager.getInstance().sendPacketToAll(macu);

		BillBoard.getInstance().setLatestSighting(Identity.getId(),
				Clock.timestamp(), Clock.timestamp(), Clock.timestamp(),
				Clock.timestamp());
		// }

		float[] meanAndCo = BillBoard.getInstance().getLatestSightings();
		for (int i = 0; i < meanAndCo.length; i++) {
			Logger.print(meanAndCo[i] + ", ");
		}

		Logger.println("");
	}

	/** Returns which global time slot is the current one */
	public static int getTimeSlot() {
		int time = Clock.timestamp()
				% (Settings.CAMERA_TIME_SLOT_LENGTH * Settings.CAMERA_TIME_SLOTS);
		return (int) Math.floor(time / Settings.CAMERA_TIME_SLOT_LENGTH);
	}
}
