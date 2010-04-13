package GSim;

/**
 * This is a class for a landmark
 * 
 * @author Fredrik Wahlberg
 * @version $Rev$
 */

public class LandMark extends Actor {

	public LandMark(SensorHandler sensors, double x, double y) {
		super(sensors, x, y, 0.0, LANDMARK);
	}


}
