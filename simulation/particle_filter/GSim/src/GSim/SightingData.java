package GSim;


public class SightingData {
	// Global time stamp
	public int timestamp;
	// Position in global reference system (used for mouse sightings)
	public double x;
	public double y;
	// Angle to target in radians in global reference system
	public double angle;
	// Type (used on landmarks)
	public int type;

	public SightingData(int timestamp, float x, float y, float angle, int type) {
		this.timestamp = timestamp;
		this.x = x;
		this.y = y;
		this.angle = angle;
		this.type = type;
	}

	public boolean compareTo(SightingData other) {
		return (this.timestamp>other.timestamp);
	}

	public String toString() {
		return "[" + timestamp + ", " + x + ", " + y ", " + dangle + "]";
	}
}
