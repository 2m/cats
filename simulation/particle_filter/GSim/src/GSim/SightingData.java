package GSim;

/**
 * Data of a sighting of the mouse or landmark
 * 
 * @author Fredrik Wahlberg
 * @version $Rev$
 * 
 */
public class SightingData extends ComparableData {
	/** Position in global reference system (used for mouse sightings) */
	public float x;
	public float y;
	/** Angle to target in radians in global reference system */
	public float angle;
	/** Type (used on landmarks) */
	public int type;

	//For particle filter
	public SightingData(int timestamp, float x, float y, float angle, int type) {
		super(timestamp);
		this.x = x;
		this.y = y;
		this.angle = angle;
		this.type = type;
	}
	
	//For UKF
	public SightingData(int timestamp, float angle, int type) {
		super(timestamp);
		this.angle = angle;
		this.type = type;
	}

	public boolean isSightingData() {
		return true;
	}

	public String toString() {
		return "[" + comparable + ", " + x + ", " + y + ", " + angle
				* (180 / Math.PI) + ", " + type + "]";
	}
}
