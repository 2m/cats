package GSim;

/**
 * This is a class for a landmark
 * 
 * @author Fredrik Wahlberg
 * @version 0.1
 */

public class landMark extends Actor {

	public landMark() {
		super(30.0, 30.0, 0.0, LANDMARK);
	}

	public landMark(double x, double y) {
		super(x, y, 0, LANDMARK);
	}
}
