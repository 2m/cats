package GSim;
/**
 * Data of a sighting of the mouse or landmark
 * 
 * @author Fredrik Wahlberg
 * @version $Rev$
 * 
 */
public class SightingData extends BufferData {
	/** Position in global reference system (used for mouse sightings) */
	public double x;
	public double y;
	/** Angle to target in radians in global reference system */
	public double angle;
	/** Type (used on landmarks) */
	public int type;

	public SightingData(int timestamp, float x, float y, float angle, int type) {
		super(timestamp);
		this.x = x;
		this.y = y;
		this.angle = angle;
		this.type = type;
	}

	public String toString() {
		return "[" + timestamp + ", " + x + ", " + y + ", " + angle + "]";
	}
}
