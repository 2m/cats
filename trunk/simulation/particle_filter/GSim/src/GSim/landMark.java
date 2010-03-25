package GSim;

/**
 * This is a class for a landmark
 * 
 * @author Fredrik Wahlberg
 * @version 0.1
 */

public class landMark extends Actor {

	public landMark(Actor mouse, double x, double y) {
		super(mouse, x, y, 0.0, LANDMARK);
	}
}
