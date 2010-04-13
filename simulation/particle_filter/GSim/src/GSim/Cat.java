package GSim;

/** 
 * The Cat
 * 
 * @author Fredrik Wahlberg
 * @version $Rev$
 *
 */
public class Cat extends Actor {
	public Cat(SensorHandler sensors, double x, double y, double angle) {
		super(sensors, x, y, angle, CAT);
	}
}
