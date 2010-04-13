package GSim;

public class TrackingFilter {
	private Buffer sensorData;
	private Buffer movementData;
	protected double T;

	public TrackingFilter(double T, Buffer sensorData, Buffer movementData) {
		this.T = T;
		this.sensorData = sensorData;
		this.movementData = movementData;
	}
}
