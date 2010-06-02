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
import se.uu.it.cats.brick.storage.BillBoard;
import se.uu.it.cats.brick.storage.StorageManager;

/**
 * Example leJOS Project with an ant build file
 * 
 */
public class Main {
	private static Buffer unifiedBuffer;
	private static MovementPilot movementPilot;
	private static AbsolutePositioningFilter positioningFilter;
	private static TrackingFilter trackingFilter;

	// beep every two seconds
	private static boolean doBeep = true;

	public static void main(String[] args) throws InterruptedException {
		Settings.init();
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

		// sync with cat0
		if (Identity.getId() != 0) {
			Clock.syncTime();
		} else {
			// if we are cat0 wait until some ammount of cats sync
			while (Clock.getReceivedPackets() < 1) {
				try {
					Thread.sleep(100);
				} catch (Exception ex) {
				}
			}
		}

		// wait for one second until sync packets get through
		try {
			Thread.sleep(1000);
		} catch (Exception ex) {
		}

		boolean startYourEngines = true;
		if (startYourEngines) {
			unifiedBuffer = new BufferSorted();

			// run camera thread
			Thread cameraThread = new Thread(new Camera(unifiedBuffer));
			cameraThread.start();

			movementPilot = new MovementPilot(unifiedBuffer);
			Thread movementThread = new Thread(movementPilot);
			movementThread.start();

			positioningFilter = new AbsolutePositioningNaiveFilter(Identity
					.getId(), .33f, unifiedBuffer, BillBoard.getInstance());
			// FIXME: ERROR init data not set!
			positioningFilter.initData(1.0f, 1.0f, 0.0f);
			Thread positioningFilterThread = new Thread(positioningFilter);
			positioningFilterThread.start();

			// trackingFilter = new
			// TrackingUnscentedKalmanFilter(Identity.getId(), 0.25f,
			// BillBoard.getInstance());

			/*
			 * trackingFilter = new TrackingParticleFilter(Identity.getId(), 50,
			 * 1f, BillBoard.getInstance()); Thread trackingFilterThread = new
			 * Thread(trackingFilter); trackingFilterThread.start();
			 */
		}

		/*
		 * while (!startYourEngines) {
		 * ConnectionManager.getInstance().sendPacketToAll(new
		 * LatestSightingUpdate(0f, 0f, 0f, Clock.timestamp()));
		 * //ConnectionManager.getInstance().sendPacketToAll(new
		 * AbsolutePositionUpdate(0f, 0f, 0f, Clock.timestamp())); try
		 * {Thread.sleep(100);} catch(Exception ex) {} }
		 */

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
				doBeep = false;
				Music m = new Music(Identity.getId(), 3);
				m.play();

				doBeep = true;
			}
		});

		Button.ESCAPE.addButtonListener(new ButtonListener() {
			public void buttonPressed(Button b) {
			}

			public void buttonReleased(Button b) {
				Clock.syncTime();
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
			/*
			 * Random r = new Random();
			 * ConnectionManager.getInstance().sendPacketToAll( new
			 * SimpleMeasurement((float)r.nextDouble()) );
			 */

			// LCD.drawInt((byte)StorageManager.getInstance().getData(), 2, 2);
			int milisUntilNextSec = 2000 - (Clock.timestamp() % 2000);
			Thread.sleep(milisUntilNextSec);

			int myid = Identity.getId();
			float[] s = BillBoard.getInstance().getLatestSightings();
			for (int i = 0; i < BillBoard.getInstance().getNoCats(); i++) {
				if (myid != i) {
					Logger.println("id: " + i + ", x:" + s[i * 4 + 0] + ", y:"
							+ s[i * 4 + 1] + ", th:" + s[i * 4 + 2]);
					/*
					 * sightings[id * 4 + 0] = x; sightings[id * 4 + 1] = y;
					 * sightings[id * 4 + 2] = theta; sightings[id * 4 + 3] =
					 * timestamp;
					 */
				}
			}
			// Thread.sleep(100);
			// Thread.yield();
			if (doBeep)
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
}
