package GSim;

import java.awt.Graphics;

public abstract class AbsolutePositioningFilter {
	/* Buffers with data no movement and landmark sightings */
	protected final Buffer sensorData;
	protected final Buffer movementData;
	/* Period of filter (can be changed by filter for adaptive period) */
	protected float T;
	/* Pointer to common clock object */
	protected final RealTimeClock rttime;

	public AbsolutePositioningFilter(float T, Buffer sensorData,
			Buffer movementData, RealTimeClock rttime) {
		this.T = T; // Period, could be adaptive
		this.sensorData = sensorData;
		this.movementData = movementData;
		this.rttime = rttime;
	}

	public float getX() {
		return (float) 0.0;
	}

	public float getY() {
		return (float) 0.0;
	}

	public float getAngle() {
		return (float) 0.0;
	}

	public void update() {
		// Update stuff
	}

	public void draw(Graphics g) {
		// Draw stuff
	}

	public void initData(float x, float y, float angle) {
		// Init data
	}
}
