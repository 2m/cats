package GSim;

public abstract class AbsolutePositioningFilter {
	protected final Buffer sensorData;
	protected final Buffer movementData;
	protected final float T;
	protected final Arena arena;
	protected final RealTimeClock rttime;
	protected final LandmarkList landmarks;

	public AbsolutePositioningFilter(float T, Buffer sensorData, Buffer movementData, Arena arena, RealTimeClock rttime, LandmarkList landmarks) {
		this.T = T;	// Period
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
