package GSim;

import java.awt.Graphics;

/**
 * Base class for the absolute positioning filter (graphics code should be
 * talken out befor use on the NXT)
 */
public abstract class AbsolutePositioningFilter {
	/* Buffers with data on movement and landmark sightings */
	protected final Buffer sensorData;
	protected final Buffer movementData;
	/* Period of filter */
	protected final float T;
	/* Period of filter in milliseconds*/
	protected final int Tint;
	/* Pointer to common clock object */
	protected final RealTimeClock rttime;

	public AbsolutePositioningFilter(float T, Buffer sensorData,
			Buffer movementData, RealTimeClock rttime) {
		/** Period of filter */
		this.T = T;
		this.Tint = (int)(T*1000);
		/** Sorded buffer with sensor readings */
		this.sensorData = sensorData;
		/** Sorted buffer wi th data on movement */
		this.movementData = movementData;
		/** Real time clock */
		this.rttime = rttime;
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

	/** Draw stuff, should not be included in final code on the NXT */
	public void draw(Graphics g) {
	}

	/** Reset filter with some initial data */
	public void initData(float x, float y, float angle) {
	}
}
