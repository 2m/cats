package GSim;

public abstract class AbsolutePositioningFilter {
	/* Buffers with data no movment and laandmark sightings */
	protected final Buffer sensorData;
	protected final Buffer movementData;
	/* Period of filter (can be changed by filter for adaptive period) */
	protected float T;
	/* Data on arena size and stuff */
	protected final Arena arena;
	/* Pointer to common clock object */
	protected final RealTimeClock rttime;
	/* Data on landmarks (positions, colours etc.) */
	protected final LandmarkList landmarks;

	public AbsolutePositioningFilter(float T, Buffer sensorData, Buffer movementData, Arena arena, RealTimeClock rttime, LandmarkList landmarks) {
		this.T = T;		// Period, could be adaptive
		this.sensorData = sensorData;
		this.movementData = movementData;
		this.arena = arena;
		this.rttime = rttime;
		this.landmarks = landmarks;
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
}
