package GSim;

public class trackingFilter {
	private sortedBuffer sensorData;
	private sortedBuffer movementData;

	public trackingFilter(sortedBuffer sensordata, sortedBuffer movementData) {
		self.movementData = movementData;
		self.sensorData = sensorData;
	}
}
