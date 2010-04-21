package GSim;

/** 
 * The Cat
 * 
 * @author Fredrik Wahlberg
 * @version $Rev$
 *
 */
public class Cat extends Actor {
	public Cat(SensorHandler sensors, LandmarkList list, double x, double y, double angle) {
		super(sensors, list, x, y, angle, CAT);
	}
}
