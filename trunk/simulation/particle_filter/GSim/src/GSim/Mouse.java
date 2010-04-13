package GSim;

/**
 * The Mouse
 * 
 * @author Fredrik Wahlberg
 * @version $Rev$
 * 
 */
public class Mouse extends Actor {
	public Mouse(SensorHandler sensors, double x, double y, double angle) {
		super(sensors, x, y, angle, MOUSE);
	}

	
}
