package GSim;

import java.awt.Graphics;

/**
 * Base class for the absolute positioning filter (graphics code should be
 * talken out befor use on the NXT)
 */
public abstract class TrackingFilter extends Thread{
	/** Id number */
	protected int id;
	/** Buffers with data on mouse sightings */
	protected final Buffer sensorData;
	/** Period of filter */
	protected final float T;
	/** Period of filter in milliseconds */
	protected final int Tint;
	/** Pointer to common clock object */
	protected final RealTimeClock rttime;
	/** Shared data object */
	protected BillBoard billboard;

	public TrackingFilter(int id, float T, Buffer sensorData,
			RealTimeClock rttime, BillBoard billboard) {
		/** Save id number */
		this.id = id;
		/** Period of filter */
		this.T = T;
		this.Tint = (int) (T * 1000);
		/** Sorded buffer with sensor readings */
		this.sensorData = sensorData;
		/** Real time clock */
		this.rttime = rttime;
		/** Shared data */
		this.billboard = billboard;
		// Set priority for thread
		// TODO: Decide priority for absolute positioning filter
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
