package GSim;

import java.awt.Graphics;

/*import se.uu.it.cats.brick.Clock;*/

/**
 * Base class for the absolute positioning filter (graphics code should be taken
 * out before use on the NXT)
 */
public abstract class TrackingFilter extends Thread {
	/** Id number */
	protected int id;
	/** Period of filter */
	protected final float T;
	/** Period of filter in milliseconds */
	protected final int Tint;
	/** Shared data object */
	protected BillBoard billboard;

	public TrackingFilter(int id, float T, BillBoard billboard) {
		/** Save id number */
		this.id = id;
		/** Period of filter */
		this.T = T;
		this.Tint = (int) (T * 1000);
		/** Shared data */
		this.billboard = billboard;
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

	/** Poll estimated x velocity value from filter */
	public float getXv() {
		return (float) 0.0;
	}

	/** Poll estimated y velocity value from filter */
	public float getYv() {
		return (float) 0.0;
	}

	/**
	 * Runs one update of the filter then exits
	 */
	public void update() {
	}

	/** Draw stuff, should not be included in final code on the NXT */
	public void draw(Graphics g) {
	}

	/** Reset filter with some initial data */
	public void initData(float x, float y, float xv, float yv) {
	}

	public void run() {
		while (true) {
			// update();
			pause((long) (Tint - (Clock.timestamp() % Tint)));
		}
	}

	/**
	 * Pause the excution this many milliseconds
	 * 
	 * @param millis
	 *            to pause as a long int
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
