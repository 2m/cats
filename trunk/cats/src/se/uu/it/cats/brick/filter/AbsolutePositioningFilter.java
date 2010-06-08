package se.uu.it.cats.brick.filter;

import se.uu.it.cats.brick.Clock;
import se.uu.it.cats.brick.storage.BillBoard;

/**
 * Base class for the absolute positioning filter (graphics code should be taken
 * out before use on the NXT)
 */
public abstract class AbsolutePositioningFilter extends Thread {
	/** Buffer with data on movement and landmark sightings */
	protected final Buffer unifiedBuffer;
	/** Period of filter in seconds */
	protected final float T;
	/** Period of filter in milliseconds */
	protected final int Tint;
	protected BillBoard billboard;
	protected int id;

	public AbsolutePositioningFilter(int id, float T, Buffer unifiedBuffer,
			BillBoard billboard) {
		/** Period of filter */
		this.T = T;
		this.Tint = (int) (T * 1000);
		/** Sorted buffer with readings */
		this.unifiedBuffer = unifiedBuffer;
		this.billboard = billboard;
		this.id = id;
		// Set priority for thread
		setPriority(Thread.MIN_PRIORITY);
	}

	/** Poll estimated x position value from filter */
	public float getX() {
		return (float) 0.0;
	}

	/** Poll estimated y position value from filter */
	public float getY() {
		return (float) 0.0;
	}

	/** Poll estimated direction angle value from filter */
	public float getAngle() {
		return (float) 0.0;
	}

	/**
	 * Runs one update of the filter then exits
	 */
	public void update() {
	}

	/** Reset filter with some initial data */
	public void initData(float x, float y, float angle) {
	}

	public void run() {
		while (true) {
			update();
			pause((long) (Tint - (Clock.timestamp() % Tint)));
		}
	}

	/**
	 * Pause the execution this many milliseconds
	 * 
	 * @param millis
	 *            to pause as a long
	 * @return Result as boolean
	 */
	public boolean pause(long millis) {
		if (Thread.interrupted())
			return false;
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			return false;
		}
		return true;
	}
}
